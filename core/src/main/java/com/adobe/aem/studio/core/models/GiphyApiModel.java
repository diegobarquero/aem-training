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
import java.net.URL;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GiphyApiModel {

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String resourceType;

    @ValueMapValue
    private String queryString;

    @ValueMapValue
    private Integer limit;

    @ValueMapValue
    private Integer offset;

    @ValueMapValue
    private String language;

    @ValueMapValue
    private String rating;

    @ValueMapValue
    private String bundle;

    @ValueMapValue
    private String apiKey;

    private final List<Map<String, Object>> giphyData = new ArrayList<>();
    private final List<String> params = new ArrayList<>();

    @PostConstruct
    protected void init() {
        try {
            String baseUrl = "https://api.giphy.com/v1";

            String route = "/" + resourceType + "/search";

            params.add("api_key=" + URLEncoder.encode(apiKey, "UTF-8"));
            params.add("q=" + URLEncoder.encode(queryString, "UTF-8"));

            if (limit != null) {
                params.add("limit=" + limit);
            }
            if (offset != null) {
                params.add("offset=" + offset);
            }
            if (language != null && !language.isEmpty()) {
                params.add("lang=" + URLEncoder.encode(language, "UTF-8"));
            }
            if (rating != null && !rating.isEmpty()) {
                params.add("rating=" + URLEncoder.encode(rating, "UTF-8"));
            }
            if (bundle != null && !bundle.isEmpty()) {
                params.add("bundle=" + URLEncoder.encode(bundle, "UTF-8"));
            }

            String queryParamsString = String.join("&", params);

            URL url = new URL(baseUrl + route + "?" + queryParamsString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream input = connection.getInputStream();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(input);

            JsonNode dataArray = root.get("data");
            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    Map<String, Object> gifInfo = mapper.convertValue(item, Map.class);
                    giphyData.add(gifInfo);
                }
            }
            connection.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getGiphyData() {
        return giphyData;
    }

    public List<String> getParams() {
        return params;
    }
}
