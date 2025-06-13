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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.nio.charset.StandardCharsets;

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

    private Integer offset = 0;

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

    private final List<Map<String, Object>> pages = new ArrayList<>();

    private String hashedId;

    private int currentPage = 1;

    @PostConstruct
    protected void init() {
        try {
            String baseUrl = "https://api.giphy.com/v1";

            String route = "/" + resourceType + "/search";

            params.add("api_key=" + URLEncoder.encode(apiKey, "UTF-8"));
            params.add("q=" + URLEncoder.encode(queryString, "UTF-8"));

            hashedId = generateHash(queryString);
            String offsetParamName = "offset" + hashedId;
            String offsetParamValue = request.getParameter(offsetParamName);

            if (offsetParamValue != null) {
                try {
                    offset = Integer.parseInt(offsetParamValue);
                } catch (NumberFormatException e) {
                // Keep the old value from dialog
                }
            }

            if (limit != null) {
                params.add("limit=" + limit);
            }
            if (offset != null) {
                if (limit != null && limit > 0) {
                    currentPage = (offset / limit) + 1;
                }
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
            int totalCount = root.get("pagination").path("total_count").asInt();
            int totalPages = (limit != null && limit > 0) ? (int) Math.ceil((double) totalCount / limit) : 1;
            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    Map<String, Object> gifInfo = mapper.convertValue(item, Map.class);
                    giphyData.add(gifInfo);
                }
            }
            for (int page = 0; page < totalPages; page++) {
                Map<String, String> overrideParams = new LinkedHashMap<>();
                overrideParams.put(offsetParamName, String.valueOf(page * limit));
                String fullUrl = buildUrlWithParams(overrideParams);
                Map<String, Object> pageEntry = new LinkedHashMap<>();
                pageEntry.put("key", String.valueOf(page + 1));
                pageEntry.put("value", fullUrl);
                pages.add(pageEntry);
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

    public List<Map<String, Object>> getPages() {
        return pages;
    }

    public String getCurrentPage() {
        return String.valueOf(currentPage);
    }

    private String buildUrlWithParams(Map<String, String> overrides) {
        String base = request.getRequestURI();
        Map<String, String[]> currentParams = request.getParameterMap();
        List<String> finalParams = new ArrayList<>();

        // Copy current params except the one that are going to be updated
        for (Map.Entry<String, String[]> entry : currentParams.entrySet()) {
            String key = entry.getKey();
            if (!overrides.containsKey(key) && entry.getValue().length > 0) {
                finalParams.add(key + "=" + URLEncoder.encode(entry.getValue()[0], StandardCharsets.UTF_8));
            }
        }

        // Add or replace with overrides
        for (Map.Entry<String, String> entry : overrides.entrySet()) {
            finalParams.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return base + "?" + String.join("&", finalParams);
    }

    private String generateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode()); // Fallback
        }
    }
}
