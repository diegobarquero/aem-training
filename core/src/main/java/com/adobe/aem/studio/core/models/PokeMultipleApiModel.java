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
public class PokeMultipleApiModel {

    @Self
    private SlingHttpServletRequest request;
    
    // Single Poke search
    @ValueMapValue
    private String pokeAmount;

    @ValueMapValue
    private String pokeOffset;
    
    private List<Pokemon> pokemons = new ArrayList<>();

    @PostConstruct
    protected void init() {


        try {

            String baseUrl = "https://pokeapi.co/api/v2/pokemon/";

            List<String> params = new ArrayList<>();

            if (pokeAmount != null && !pokeAmount.isEmpty()) {
                params.add("limit=" + pokeAmount);
            }

            if (pokeOffset != null && !pokeOffset.isEmpty()) {
                params.add("offset=" + pokeOffset);
            }

            String queryParamsString = String.join("&", params);


            URL url = new URL(baseUrl + '?' + queryParamsString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream input = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(input);

            JsonNode results = root.path("results");

            for (JsonNode result : results) {

                String name = result.path("name").asText();
                String detailUrl = result.path("url").asText();

                try {
                    URL urlSpecificPokemon = new URL(detailUrl);
                    HttpURLConnection connection2 = (HttpURLConnection) urlSpecificPokemon.openConnection();
                    connection2.setRequestMethod("GET");

                    InputStream input2 = connection2.getInputStream();
                    ObjectMapper mapper2 = new ObjectMapper();
                    JsonNode detailRoot = mapper2.readTree(input2);

                    String sprite = detailRoot.path("sprites").path("front_default").asText();

                    // Collect ALL types
                    List<String> types = new ArrayList<>();
                    JsonNode typesNode = detailRoot.path("types");
                    if (typesNode.isArray()) {
                        for (JsonNode typeEntry : typesNode) {
                            String typeName = typeEntry.path("type").path("name").asText();
                            if (!typeName.isEmpty()) {
                                types.add(typeName);
                            }
                        }
                    }

                    pokemons.add(new Pokemon(name, sprite, types));
                    connection2.disconnect();

                } catch (IOException e) {

                }

            }

            connection.disconnect();

        } catch (IOException e) {
            
         
        }
    }

    public List<Pokemon> getPokemons() {
        return pokemons;
    }

    public static class Pokemon {
        private final String name;
        private final String spriteUrl;
        private final List<String> types;

        public Pokemon(String name, String spriteUrl, List<String> types) {
            this.name = name;
            this.spriteUrl = spriteUrl;
            this.types = types;
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
        
    }

    
}
