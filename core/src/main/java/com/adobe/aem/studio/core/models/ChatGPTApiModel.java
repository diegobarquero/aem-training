package com.adobe.aem.studio.core.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ChatGPTApiModel {

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String apiKey;

    @ValueMapValue
    private String prompt;

    // private final List<Map<String, Object>> gptData = new ArrayList<>();

    private String responseText;
    private String responseImage;

    @PostConstruct
    protected void init() {
        try {
            responseText = callChatGPTCompletions();
            responseImage = callChatGPTImagesGenerations();
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    private String callChatGPTCompletions() throws Exception {
      HttpURLConnection connection = null;

      try {
          URL url = new URL("https://api.openai.com/v1/chat/completions");
          connection = (HttpURLConnection) url.openConnection();

          connection.setRequestMethod("POST");
          connection.setDoOutput(true);
          connection.setRequestProperty("Authorization", "Bearer " + apiKey);
          connection.setRequestProperty("Content-Type", "application/json");

          ObjectMapper mapper = new ObjectMapper();
          ObjectNode messageNode = mapper.createObjectNode();
          messageNode.put("role", "user");
          messageNode.put("content", prompt);

          ObjectNode rootNode = mapper.createObjectNode();
          rootNode.put("model", "gpt-4");
          rootNode.putArray("messages").add(messageNode);

          try (OutputStream os = connection.getOutputStream()) {
              byte[] input = mapper.writeValueAsBytes(rootNode);
              os.write(input, 0, input.length);
          }

          try (Scanner scanner = new Scanner(connection.getInputStream())) {
            scanner.useDelimiter("\\A");
            String responseJson = scanner.hasNext() ? scanner.next() : "";

            JsonNode root = mapper.readTree(responseJson);
            return root.path("choices").get(0).path("message").path("content").asText();
          }
      } finally {
          if (connection != null) {
              connection.disconnect();
          }
      }
    }

    private String callChatGPTImagesGenerations() throws Exception {
      HttpURLConnection connection = null;

      try {
          URL url = new URL("https://api.openai.com/v1/images/generations");
          connection = (HttpURLConnection) url.openConnection();

          connection.setRequestMethod("POST");
          connection.setDoOutput(true);
          connection.setRequestProperty("Authorization", "Bearer " + apiKey);
          connection.setRequestProperty("Content-Type", "application/json");

          ObjectMapper mapper = new ObjectMapper();

          ObjectNode rootNode = mapper.createObjectNode();
          rootNode.put("model", "dall-e-3");
          rootNode.put("prompt", "A giant chicken wearing a tuxedo, standing on a forest");
          rootNode.put("n", 1);
          rootNode.put("size", "1024x1024");

          try (OutputStream os = connection.getOutputStream()) {
              byte[] input = mapper.writeValueAsBytes(rootNode);
              os.write(input, 0, input.length);
          }

          try (Scanner scanner = new Scanner(connection.getInputStream())) {
            scanner.useDelimiter("\\A");
            String responseJson = scanner.hasNext() ? scanner.next() : "";

            JsonNode root = mapper.readTree(responseJson);
            return root.path("data").get(0).path("url").asText();
          }
      } finally {
          if (connection != null) {
              connection.disconnect();
          }
      }
    }

    // public List<Map<String, Object>> getGPTData() {
    //     return gptData;
    // }

    public String getResponseText() {
        return responseText;
    }
    public String getResponseImage() {
        return responseImage;
    }
}
