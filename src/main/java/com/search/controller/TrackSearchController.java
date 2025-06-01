package com.search.controller;

import com.search.config.ElasticsearchJavaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracks")
public class TrackSearchController {

    private final ElasticsearchJavaClient esClient;

    @Autowired
    public TrackSearchController(ElasticsearchJavaClient client) {
        this.esClient = client;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchTracks(@RequestParam String q) throws IOException {
        return esClient.searchDocuments("tracks", q);
    }
}