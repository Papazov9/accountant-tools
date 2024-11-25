package com.wildrep.accountantapp.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;


@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private Drive drive;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google.application.name}")
    private String applicationName;

    @Value("${google.credentials.path}")
    private String credentialsPath;

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        HttpCredentialsAdapter credentialsAdapter  = new HttpCredentialsAdapter(credentials);

        this.drive = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credentialsAdapter)
                .setApplicationName(applicationName)
                .build();
    }

    public String uploadFile(String filePath, String fileName, String parentFolderId) throws IOException {
        File fileMetadata = new File();
       fileMetadata.setName(fileName);
        if (parentFolderId != null) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        java.io.File file = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/pdf", file);

        File uploadFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink")
                .execute();

        return uploadFile.getWebViewLink();
    }
}
