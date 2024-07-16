package com.email.send.grid.service;

import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.management.openmbean.InvalidKeyException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AzureStorageService {
    @Value("${azure.storage.account-name}")
    private String azureStorageAccountName;

    @Value("${azure.storage.account-key}")
    private String azureStorageAccountKey;

    @Value("${azure.storage.container-name}")
    private String azureStorageContainerName;

    @Value("${azure.storage.temp-container-name}")
    private String destinationContainer;

    public String uploadFileToStorage(MultipartFile file, Integer separationCount) {
        try {
            // Parse the connection string and create a blob client to interact with Blob storage
            String storageConnectionString =
                    String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                    azureStorageAccountName, azureStorageAccountKey);
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Get reference to the container
            CloudBlobContainer container = blobClient.getContainerReference(azureStorageContainerName);

            // Create a blob in the container
            String fileName = file.getOriginalFilename();
            CloudBlockBlob blob = container.getBlockBlobReference(fileName);

            // Upload file contents from InputStream
            try (InputStream inputStream = file.getInputStream()) {
                blob.upload(inputStream, file.getSize());
            }

            // Return the URL of the uploaded file
//            return blob.getUri().toString();

            List<File> smallerFiles = splitFile(file,separationCount);

            // Upload each smaller file to Azure Blob Storage
            for (File smallerFile : smallerFiles) {
                uploadSingleFile(smallerFile, container);
            }

            // Return a message indicating success
            return "Files uploaded successfully";

        } catch (URISyntaxException | StorageException | InvalidKeyException | IOException |
                 java.security.InvalidKeyException e) {
            throw new RuntimeException("Failed to upload file to Azure Storage: " + e.getMessage(), e);
        }
    }

    private List<File> splitFile(MultipartFile file, Integer separationCount) throws IOException {
        List<File> smallerFiles = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             BufferedReader br = new BufferedReader(reader)) {

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(br);
            Set<CSVRecord> recordList = new HashSet<>();
            for (CSVRecord record : records) {
                recordList.add(record);
            }

            int rowCount = 0;
            int fileIndex = 1;
            List<CSVRecord> currentBatch = new ArrayList<>();
            for (CSVRecord record : recordList) {
                currentBatch.add(record);
                rowCount++;

                if (rowCount == separationCount) {
                    File smallerFile = createTempFile(currentBatch, fileIndex);
                    smallerFiles.add(smallerFile);
                    fileIndex++;
                    rowCount = 0;
                    currentBatch.clear();
                }
            }

            if (!currentBatch.isEmpty()) {
                File smallerFile = createTempFile(currentBatch, fileIndex);
                smallerFiles.add(smallerFile);
            }
        }

        return smallerFiles;
    }

    private File createTempFile(List<CSVRecord> records, int index) throws IOException {
        File tempFile = File.createTempFile("part_" + index + "_", ".csv");

        try (Writer writer = new FileWriter(tempFile);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            for (CSVRecord record : records) {
                csvPrinter.printRecord(record);
            }
        }

        return tempFile;
    }

    private void uploadSingleFile(File file,
                                  CloudBlobContainer container)
            throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blob = container.getBlockBlobReference(file.getName());

        try (InputStream inputStream = new FileInputStream(file)) {
            blob.upload(inputStream, file.length());
        }

        file.delete();
    }

    public List<String> listBlobsInContainer() {
        try {
            // Parse the connection string and create a blob client to interact with Blob storage
            String storageConnectionString =
                    String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                    azureStorageAccountName, azureStorageAccountKey);
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Get reference to the container
            CloudBlobContainer container = blobClient.getContainerReference(azureStorageContainerName);

            List<String> blobUrls = new ArrayList<>();
            for (ListBlobItem blobItem : container.listBlobs()) {
                if (blobItem instanceof CloudBlob) {
                    CloudBlob blob = (CloudBlob) blobItem;
                    blobUrls.add(blob.getUri().toString());
                } else if (blobItem instanceof CloudBlobDirectory) {
                    CloudBlobDirectory directory = (CloudBlobDirectory) blobItem;
                    for (ListBlobItem subBlobItem : directory.listBlobs()) {
                        if (subBlobItem instanceof CloudBlob) {
                            CloudBlob subBlob = (CloudBlob) subBlobItem;
                            blobUrls.add(subBlob.getUri().toString());
                        }
                    }
                }
            }
            return blobUrls;

        } catch (URISyntaxException | StorageException | InvalidKeyException | java.security.InvalidKeyException e) {
            throw new RuntimeException("Failed to list blobs in Azure Storage: " + e.getMessage(), e);
        }
    }

    public String copyBlob(String blobUrl) throws IOException {
        String storageConnectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                azureStorageAccountName, azureStorageAccountKey);
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().
                connectionString(storageConnectionString).buildClient();
        BlobClient sourceBlobClient = new BlobClientBuilder().endpoint(blobUrl).buildClient();

        // Extract the original file name from the blob URL
        String fileName = Paths.get(new URL(blobUrl).getPath()).getFileName().toString();
        String tempFileName = "temp_" + fileName;

        // Get a reference to the destination blob
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(destinationContainer);
        BlobClient destinationBlobClient = containerClient.getBlobClient(tempFileName);

        // Start the copy operation
        SyncPoller<BlobCopyInfo, Void> poller = destinationBlobClient.
                beginCopy(blobUrl, null, null, null,
                        null, null, null);
        BlobCopyInfo blobCopyInfo = poller.waitForCompletion().getValue();

        // Return the URL of the uploaded blob
        return destinationBlobClient.getBlobUrl();
    }
}
