package com.search.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.config.ElasticsearchJavaClient;
import com.search.model.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID; // ✅ Добавлен импорт

@Component
public class KafkaTrackConsumer {

    private final ElasticsearchJavaClient esClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public KafkaTrackConsumer(ElasticsearchJavaClient client) {
        this.esClient = client;
    }

    @KafkaListener(topics = "${kafka.topic}", groupId = "elasticsearch-group")
    public void consume(String message) {
        try {
            Track track = objectMapper.readValue(message, Track.class);
            String documentId = track.getTrack() != null ? track.getTrack() : UUID.randomUUID().toString();
            esClient.indexDocument("tracks", documentId, track);
            System.out.println("Indexed track: " + track.getTrack());
        } catch (Exception e) {
            System.err.println("Error indexing message: " + message);
            e.printStackTrace();
        }
    }
}