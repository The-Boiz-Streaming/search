package com.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.search.model.Track;
import java.io.IOException;
import java.util.*;
import org.apache.http.Header;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

@Component
public class ElasticsearchJavaClient {

    private RestClient restClient;
    private ElasticsearchTransport transport;
    private ElasticsearchClient client;

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Header[] headers = new BasicHeader[]{
                new BasicHeader("Authorization", "ApiKey " + apiKey)
        };

        String cleanHost = host.replace("https://", "").replace("http://", "");

        restClient = RestClient.builder(HttpHost.create(cleanHost))
                .setDefaultHeaders(headers)
                .build();

        transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        client = new ElasticsearchClient(transport);
    }

    public List<Map<String, Object>> searchDocuments(String indexName, String query) throws IOException {
        SearchResponse<Track> response = client.search(s -> s
                        .index(indexName)
                        .query(q -> q.multiMatch(m -> m
                                .query(query)
                                .fields("track", "release", "artist")
                        )),
                Track.class
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Hit<Track> hit : response.hits().hits()) {
            Map<String, Object> sourceMap = new HashMap<>();
            sourceMap.put("id", hit.id());
            sourceMap.put("source", hit.source());
            result.add(sourceMap);
        }

        return result;
    }

    public void indexDocument(String indexName, String documentId, Track track) throws IOException {
        client.index(i -> i
                .index(indexName)
                .id(documentId)
                .document(track)
        );
    }

    public void close() throws IOException {
        if (transport != null) {
            transport.close();
        }
        if (restClient != null) {
            restClient.close();
        }
    }
}