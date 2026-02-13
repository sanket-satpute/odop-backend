package com.odop.root.extra;

// Unused imports commented out
// import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
// import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// import com.google.api.client.http.*;
// import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.File;
//import com.google.api.services.drive.model.Permission;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
// import java.util.Collections;

@Component
public class GoogleDriveUploader {

//    private Drive driveService;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
//        InputStream in = getClass().getClassLoader().getResourceAsStream("credentials.json");
//
//        GoogleCredential credential = GoogleCredential.fromStream(in)
//                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));
//
//        driveService = new Drive.Builder(
//                GoogleNetHttpTransport.newTrustedTransport(),
//                JacksonFactory.getDefaultInstance(),
//                credential
//        ).setApplicationName("ODOP").build();
    }

    // ✅ Upload file and return public view link
    public String uploadFile(MultipartFile multipartFile) throws IOException {
//        File fileMetadata = new File();
//        fileMetadata.setName(multipartFile.getOriginalFilename());
//
//        FileContent mediaContent = new FileContent(multipartFile.getContentType(), convert(multipartFile));
//
//        File uploadedFile = driveService.files()
//                .create(fileMetadata, mediaContent)
//                .setFields("id")
//                .execute();
//
//        // ✅ Make file publicly viewable
//        Permission permission = new Permission();
//        permission.setType("anyone");
//        permission.setRole("reader");
//
//        driveService.permissions()
//                .create(uploadedFile.getId(), permission)
//                .setSupportsAllDrives(true)
//                .execute();
//
//        // ✅ Return public link
//        return "https://drive.google.com/uc?id=" + uploadedFile.getId();
        return null;
    }

    // ✅ Convert MultipartFile to java.io.File
    private java.io.File convert(MultipartFile multipartFile) throws IOException {
        java.io.File file = java.io.File.createTempFile("temp", multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return file;
    }

    // ✅ Download file as byte[] using fileId
    public byte[] downloadFile(String fileId) throws IOException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
//        return outputStream.toByteArray();
        return null;
    }

    // ✅ Download and save to a local path
    public java.io.File downloadFileToLocal(String fileId, String localPath) throws IOException {
//        OutputStream outputStream = new FileOutputStream(localPath);
//        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
//        outputStream.close();
//        return new java.io.File(localPath);
        return null;
    }
}
