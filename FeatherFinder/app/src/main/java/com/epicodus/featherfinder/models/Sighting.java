package com.epicodus.featherfinder.models;

import org.parceler.Parcel;

/**
 * Created by DroAlvarez on 12/19/16.
 */

@Parcel
public class Sighting {
    String order;
    String family;
    String genus;
    String species;
    String description;
    String details;
    String image;
    String latitude;
    String longitude;
    String timestamp;
    String soundByte;
    String pushId;

    public Sighting() {};

    public Sighting (String species, String description, String image, String latitude, String longitude, String timestamp) {
        this.species = species;
        this.description = description;
        this.details = details;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getOrder() {
        return order;
    }

    public String getFamily() {
        return family;
    }

    public String getGenus() {
        return genus;
    }

    public String getSpecies() {
        return species;
    }

    public String getDescription() {
        return description;
    }

    public String getDetails() {
        return details;
    }

    public String getImage() {
        return image;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getSoundByte() {
        return soundByte;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setSoundByte(String soundByte) {
        this.soundByte = soundByte;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
}
