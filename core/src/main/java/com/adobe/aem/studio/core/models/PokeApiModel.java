package com.adobe.aem.studio.core.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.util.Map;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PokeApiModel {

    @Self
    private SlingHttpServletRequest request;
    
    @ValueMapValue
    private String pokesearch;
    
    private String name;
    private String spriteUrl;
    private List<String> types = new ArrayList<>();

    @PostConstruct
    protected void init() {
        if (pokesearch == null || pokesearch.isEmpty()) {
            return;
        }

        try {
            String baseUrl = "https://pokeapi.co/api/v2/pokemon/";
            URL url = new URL(baseUrl + pokesearch.toLowerCase());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream input = connection.getInputStream();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(input);

            this.name = root.path("name").asText();
            this.spriteUrl = root.path("sprites").path("front_default").asText();

            JsonNode typesNode = root.path("types");
            if (typesNode.isArray()) {
                for (JsonNode typeEntry : typesNode) {
                    String typeName = typeEntry.path("type").path("name").asText();
                    if (!typeName.isEmpty()) {
                        types.add(typeName);
                    }
                }
            }
            connection.disconnect();

        } catch (IOException e) {
            this.name = "Error";
            this.spriteUrl = "";
        }
    }

    public String getName() {
        return name;
    }

    public String getSpriteUrl() {
        return spriteUrl;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getPokesearch() {
        return pokesearch;
    }
}
