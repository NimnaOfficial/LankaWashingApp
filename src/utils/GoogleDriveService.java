package utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Inventory System Receipt Uploader";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "reciept.json"; // Your JSON file name
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String TARGET_FOLDER_ID = "1KSvEl1s2NlGofEZYQsuEi0Ml9W1E9w0K"; // Your Folder ID

    private Drive driveService;

    public GoogleDriveService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            // Load client secrets
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

            // Build flow and trigger user authorization request
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new com.google.api.client.util.store.FileDataStoreFactory(new java.io.File("tokens")))
                    .setAccessType("offline")
                    .build();

            Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

            driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            System.out.println("✅ Drive Service Authenticated Successfully.");

        } catch (Exception e) {
            // Print error but allow app to continue (controller will handle null service)
            System.err.println("❌ Error initializing Google Drive Service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String uploadFile(java.io.File uploadFile) {
        if (driveService == null) {
            System.err.println("❌ Drive Service is not initialized. Cannot upload.");
            return null;
        }

        try {
            // 1. Create File Metadata (v2 style)
            File fileMetadata = new File();
            fileMetadata.setTitle(uploadFile.getName()); // v2 uses setTitle, not setName

            // 2. Set Parent Folder (v2 requires ParentReference objects)
            ParentReference parent = new ParentReference();
            parent.setId(TARGET_FOLDER_ID);
            fileMetadata.setParents(Collections.singletonList(parent));

            // 3. Define Content (Generic stream to support PDF, Images, etc.)
            FileContent mediaContent = new FileContent("application/octet-stream", uploadFile);

            // 4. Upload & Request SPECIFIC fields (alternateLink is crucial for v2)
            System.out.println("📤 Uploading " + uploadFile.getName() + "...");
            File file = driveService.files().insert(fileMetadata, mediaContent)
                    .setFields("id, webViewLink, alternateLink")
                    .execute();

            System.out.println("✅ File Uploaded! ID: " + file.getId());

            // 5. Get the Link (Try alternateLink first, then webViewLink)
            String link = file.getAlternateLink();
            if (link == null || link.isEmpty()) {
                link = file.getWebViewLink();
            }

            return link;

        } catch (Exception e) {
            System.err.println("❌ Upload Failed Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}