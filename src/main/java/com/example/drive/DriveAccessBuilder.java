package com.example.drive;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

public class DriveAccessBuilder {

    private final Drive drive;
    private DriveAccessor.SearchCallback callback;
    private static final HttpTransport  HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory    JSON_FACTORY   = new GsonFactory();


    private DriveAccessBuilder(Drive drive) {
        this.drive = drive;
    }


    /*==================Builder Patterns===================*/
    public static DriveAccessBuilder init(final String applicationName){

        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {})
                .setApplicationName(applicationName != null ? applicationName : "drive-search-app")
                .build();

        return new DriveAccessBuilder(drive);
    }

    public DriveAccessBuilder setCallback(DriveAccessor.SearchCallback callback){

        if(drive == null)
            throw new RuntimeException("You should call DriveBuilder.init() first.");

        this.callback = callback;
        return this;

    }

    public DriveAccessor build(){

        if(this.callback == null)
            throw new RuntimeException("You should call DriveBuilder.setCallback() after called init() or setProperties().");

        return new DriveAccessor(this);
    }


    /*=================Getter and Others===================*/
    public Drive         getDrive(){ return drive; }

    public DriveAccessor.SearchCallback getCallback(){ return callback; }
}
