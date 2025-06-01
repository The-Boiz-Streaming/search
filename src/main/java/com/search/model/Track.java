package com.search.model;

public class Track {
    private String track;
    private Integer track_id;
    private String release;
    private Integer release_id;
    private String artist;
    private Integer artist_id;

    // Getters and Setters
    public Integer getTrackId() { return track_id; }
    public String getTrack() { return track; }
    public void setTrack(String track, Integer track_id) { this.track = track; this.track_id = track_id; }

    public Integer getReleaseId() { return release_id; }
    public void setDescription(String release, Integer release_id) { this.release = release; this.release_id = release_id; }

    public Integer getArtistId() { return artist_id; }
    public void setArtist(String artist, Integer artist_id) { this.artist = artist; this.artist_id = artist_id; }
}