package com.adobe.aem.studio.core.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import java.io.OutputStream;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ChatGPTApiModel {

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String apiKey;

    // @ValueMapValue
    // private String prompt;

    @ValueMapValue
    private String bannerPrompt;

    @ValueMapValue
    private Boolean lockGeneration;

    @Inject
    private Resource resource;

    // private List<String> contentData = new ArrayList<>();
    // private final List<Map<String, Object>> contentData = new ArrayList<>();

    // private String responseText;
    // private String responseImage;

    @PostConstruct
    protected void init() {
      if (lockGeneration == false) {
        try {
          // responseImage = callChatGPTImagesGenerations();
          Resource multifieldParent = resource.getChild("contentMultifield");
          // String contextText = "contextText something diff here"; 
          
          // Resource resourceTest = request.getResource();
          
          // Generate Banner Image 
          ModifiableValueMap mvn = resource.adaptTo(ModifiableValueMap.class);
          String bannerImage = "noimage"; //callChatGPTImagesGenerations();
          
          if(mvn != null && bannerImage != null && !bannerImage.equals("noimage")) {
              mvn.put("bannerImage", bannerImage);
              resource.getResourceResolver().commit();
          }
          
          // Generate Content 
          if (multifieldParent != null) {
              for (Resource item : multifieldParent.getChildren()) {
                ModifiableValueMap mvnMultifield = item.adaptTo(ModifiableValueMap.class);
                
                String value = item.getValueMap().get("promptValue", String.class);

                  if (value != null) {
                      String responseString = callChatGPTCompletions(value);
                      
                      if (responseString == null || responseString.isEmpty()) {
                          responseString = "noresponse";
                      }

                      if(responseString != null && mvnMultifield != null && !responseString.equals("noresponse")) {
                          mvnMultifield.put("promptResponse", responseString);
                          resource.getResourceResolver().commit();
                      }
                  }
              }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    private String callChatGPTCompletions(String prompt) throws Exception {
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
      catch (Exception e) {
          e.printStackTrace();
          return "noresponse";
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
          rootNode.put("prompt", bannerPrompt);
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
      } 
      catch (Exception e) {
          e.printStackTrace();
          return "noimage";
      }
      finally {
          if (connection != null) {
              connection.disconnect();
          }
      }
    }

    // public List<Map<String, Object>> getContentValue() {
    //     return contentData;
    // }

    // public List<String> getContentData() {
    //     return contentData;
    // }

    // public String getResponseText() {
    //     return responseText;
    // }

    // public String getResponseImage() {
    //     return responseImage;
    // }
}
