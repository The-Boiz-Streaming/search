package com.search.config;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class ElasticsearchJavaClient {

    private RestClient restClient;
    private ElasticsearchTransport transport;

    @Value("${elasticsearch.host}")
    private String host; // например, "https://localhost:9200"

    @Value("${elasticsearch.api-key}")
    private String apiKey; // base64 строка

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        Header[] headers = new Header[]{
                new BasicHeader("Authorization", "ApiKey " + apiKey)
        };

        String cleanHost = host.replace("https://",  "").replace("http://", "");

        restClient = RestClient.builder(HttpHost.create(cleanHost))
                .setDefaultHeaders(headers)
                .build();

        transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    /**
     * Индексация документа в Elasticsearch
     */
    public void indexDocument(String indexName, String documentId, Object document) throws IOException {
        IndexRequest.Builder<Object> requestBuilder = new IndexRequest.Builder<>();
        requestBuilder.index(indexName);
        requestBuilder.id(documentId);
        requestBuilder.document(document);

        var request = requestBuilder.build();
        transport.performRequest(request, IndexRequest._ENDPOINT, null);
    }

    /**
     * Поиск по нескольким полям
     */
    public List<Map<String, Object>> searchDocuments(String indexName, String query) throws IOException {
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
        requestBuilder.index(indexName);

        requestBuilder.query(q -> q.multiMatch(m -> m
                .query(query)
                .fields("track", "release", "artist")
        ));

        var searchRequest = requestBuilder.build();

        var responseBytes = transport.performRequest(searchRequest, SearchRequest._ENDPOINT, null);

        String jsonResponse = responseBytes.toString();

        // Удаляем префикс, если есть
        if (jsonResponse.startsWith("SearchResponse: ")) {
            jsonResponse = jsonResponse.substring("SearchResponse: ".length());
        }

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return parseHits(rootNode.get("hits").get("hits"));
        } catch (JsonProcessingException e) {
            throw new IOException("Ошибка десериализации JSON", e);
        }
    }

    /**
     * Парсинг хитов из JSON
     */
    private List<Map<String, Object>> parseHits(JsonNode hitsNode) throws JsonProcessingException {
        List<Map<String, Object>> result = new ArrayList<>();

        for (JsonNode hit : hitsNode) {
            String id = hit.get("id").asText();
            JsonNode sourceNode = hit.get("_source");

            Map<String, Object> sourceMap = objectMapper.convertValue(sourceNode, Map.class);
            sourceMap.put("id", id);
            result.add(sourceMap);
        }

        return result;
    }

    /**
     * Закрытие клиента
     */
    public void close() throws IOException {
        if (restClient != null) {
            restClient.close();
        }
    }
}