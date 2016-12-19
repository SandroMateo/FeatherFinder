package com.epicodus.featherfinder.models;

/**
 * Created by DroAlvarez on 12/19/16.
 */

public class Sighting {
    String order;
    String family;
    String genus;
    String species;
    String description;
    String details;
    String image;
    String location;
    String soundByte;

    public Sighting() {}

    public Sighting(String order, String family, String genus, String species, String description, String details, String image, String location, String soundByte) {
        this.order = order;
        this.family = family;
        this.genus = genus;
        this.species = species;
        this.description = description;
        this.details = details;
        this.image = image;
        this.location = location;
        this.soundByte = soundByte;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSoundByte() {
        return soundByte;
    }

    public void setSoundByte(String soundByte) {
        this.soundByte = soundByte;
    }
}
