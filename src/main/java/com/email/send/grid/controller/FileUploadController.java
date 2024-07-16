package com.email.send.grid.controller;

import com.email.send.grid.dto.ResponseObject;
import com.email.send.grid.service.AzureStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/azure")
public class FileUploadController {

    @Autowired
    private AzureStorageService azureStorageService;

    @PostMapping(value = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("separationCount") Integer separationCount) {
        String fileUrl = azureStorageService.uploadFileToStorage(file, separationCount);
        return "File uploaded successfully to: " + fileUrl;
    }

    @GetMapping("/list-blobs")
    public List<String> listBlobs() {
        return azureStorageService.listBlobsInContainer();
    }

    @GetMapping(value = "/create-temp-file", produces = MediaType.TEXT_PLAIN_VALUE)
    public String copyBlob(@RequestParam("blobUrl") String blobUrl) {
        try {
            return azureStorageService.copyBlob(blobUrl);
        } catch (Exception e) {
            return "Error sending response";
        }
    }
}
