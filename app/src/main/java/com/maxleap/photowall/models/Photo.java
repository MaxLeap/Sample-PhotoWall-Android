package com.maxleap.photowall.models;

import com.maxleap.*;

@MLClassName(value = "Photos")
public class Photo extends MLObject {

    public static final String TITLE = "title";

    public static final String LOCATION = "location";

    public static final String ATTACHMENT = "attachment";

    public void setTitle(String title) {
        put(TITLE, title);
    }

    public String getTitle() {
        return getString(TITLE);
    }

    public void setLocation(MLGeoPoint geoPoint) {
        put(LOCATION, geoPoint);
    }

    public MLGeoPoint getLocation() {
        return getMLGeoPoint(LOCATION);
    }

    public void setAttachment(MLFile file) {
        put(ATTACHMENT, file);
    }

    public MLFile getAttachment() {
        return getMLFile(ATTACHMENT);
    }

    public static MLQuery<Photo> getQuery() {
        return MLQuery.getQuery(Photo.class);
    }
}
