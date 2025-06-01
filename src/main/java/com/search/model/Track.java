package com.search.model;

public class Track {
    private String track_id;
    private String track;
    private String release;
    private String artist;

    public String getTrack_id() {
        return track_id;
    }

    public String getTrack() {
        return track;
    }

    public String getRelease() {
        return release;
    }

    public String getArtist() {
        return artist;
    }

    public void setTrack_id(String track_id) {
        this.track_id = track_id;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}