package com.example.drive;

import com.example.auth.GoogleAuth;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class DriveAccessor {

    public interface SearchCallback{
        void onSearchCompleted(List<SearchResult> results);
    }

    private final DriveRepository    repository;
    private final DriveAccessor.SearchCallback callback;
    private GoogleAuth auth;


    public DriveAccessor(DriveAccessBuilder builder) {

        if(builder.getDrive() == null)
            throw new RuntimeException("This builder is not valid status.");

        callback    = builder.getCallback();
        repository  = new DriveRepository(builder.getDrive());

        try {

            this.auth = new GoogleAuth(GoogleNetHttpTransport.newTrustedTransport());

        } catch (GeneralSecurityException | IOException e) {

            throw new RuntimeException(e);

        }

    }


    public GoogleAuth getAuth() {
        return auth;
    }
    public void setAuth(GoogleAuth auth){
        this.auth = auth;
    }

    public boolean  isAuthorized() {

        return auth.isAuthorized();

    }

    public String   redirectURI(){

        return auth.redirectURI();

    }

    public TokenResponse requestNewToken(final String code){

        try {

            return auth.requestNewToken(code);

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

    }

    public void authorize(final TokenResponse tokenResponse){

        Credential credential;

        try {

            credential = auth.authorize(tokenResponse);

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

        System.out.println("Obtained Access Token: " + credential.getAccessToken());

    }


    public List<String> moveFileToFolder(final String fileId, final String folderId) throws IOException {

        return repository.moveFileToFolder(fileId, folderId, auth.getAuthorizedCredential());

    }
}

class DriveRepository{

    private final Drive driveService;

    public DriveRepository(Drive driveService) {

        this.driveService = driveService;

    }

    public List<String> moveFileToFolder(
            final String fileId,
            final String folderId,
            final Credential credential
    ) throws IOException {

        // Retrieve the existing parents to remove
        File file = driveService.files().get(fileId)
                .setOauthToken(credential.getAccessToken())
                .setFields("parents")
                .execute();

        StringBuilder previousParents = new StringBuilder();

        file.getParents().forEach(parent -> {
            previousParents.append(parent);
            previousParents.append(',');
        });


        // Move the file to the new folder
        file = driveService.files().update(fileId, null)
                .setOauthToken(credential.getAccessToken())
                .setAddParents(folderId)
                .setRemoveParents(previousParents.toString())
                .setFields("id, parents")
                .execute();

        return file.getParents();

    }
}
