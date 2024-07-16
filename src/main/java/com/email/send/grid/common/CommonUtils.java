package com.email.send.grid.common;

import com.azure.storage.blob.*;

import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommonUtils {

    private BlobContainerClient getBlobContainerClient(String azureStorageAccountName, String azureStorageAccountKey,
    String destinationContainer) {
        String storageConnectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                azureStorageAccountName, azureStorageAccountKey);
        return new BlobContainerClientBuilder().connectionString(storageConnectionString).
                containerName(destinationContainer).buildClient();
    }

    private String getContainerName(String blobUrl) throws URISyntaxException {
        URI uri = new URI(blobUrl);
        String[] pathSegments = uri.getPath().split("/");
        return pathSegments[1];
    }

    private String getBlobName(String blobUrl) throws URISyntaxException {
        URI uri = new URI(blobUrl);
        String path = uri.getPath();
        return path.substring(path.indexOf("/", 1) + 1);
    }

    public List<String> updateCsvFile(String blobUrl, Integer count,String azureStorageAccountName, String azureStorageAccountKey,
                                        String destinationContainer) throws IOException, CsvValidationException, URISyntaxException {
        String storageConnectionString =
                String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                        azureStorageAccountName, azureStorageAccountKey);
        CommonUtils utils = new CommonUtils();
        String blobName = utils.getBlobName(blobUrl);
        String containerName = utils.getContainerName(blobUrl);
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(storageConnectionString).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        // Step 1: Download the CSV file
        InputStream inputStream = containerClient.getBlobClient(blobName).openInputStream();

        // Step 2: Read the CSV file
        List<String[]> allRows = new ArrayList<>();
        try (CSVParser csvParser = new CSVParser(new InputStreamReader(inputStream, StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
            for (CSVRecord record : csvParser) {
                allRows.add(record.stream().toArray(String[]::new));
            }
        }

        // Step 3: Get specified rows
        List<String[]> rowsToReturn = new ArrayList<>();
        for (int i = 0; i < count && i < allRows.size(); i++) {
            rowsToReturn.add(allRows.get(i));
        }

        // Step 4: Remove those rows
        allRows.subList(0, Math.min(count, allRows.size())).clear();

        if(allRows.isEmpty()){
            blobClient.delete();
        }else {

            // Step 5: Prepare to save the updated CSV
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                for (String[] row : allRows) {
                    writer.write(String.join(",", row) + "\n");
                }
            }

            // Step 6: Upload the updated CSV
            InputStream updatedInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            BlobClientBuilder blobClientBuilder = new BlobClientBuilder()
                    .connectionString(storageConnectionString)
                    .containerName(containerName)
                    .blobName(blobName);
            blobClientBuilder.buildClient().upload(updatedInputStream, outputStream.size(), true);
        }
        return convertToListOfStrings(rowsToReturn);
    }

    public static List<String> convertToListOfStrings(List<String[]> listOfArrays) {
        List<String> listOfStrings = new ArrayList<>();
        for (String[] array : listOfArrays) {
            if (array.length > 0) {
                listOfStrings.add(array[0]);
            }
        }
        return listOfStrings;
    }
}
