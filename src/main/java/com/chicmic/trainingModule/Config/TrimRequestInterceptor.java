package com.chicmic.trainingModule.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TrimRequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("application/json".equalsIgnoreCase(request.getContentType())) {
            System.out.println("Interceptor: Request intercepted before reaching the controller.");

            // Read the request body
            String requestBody = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));

            // Log the original request body
            System.out.println("Original Request Body:");
            System.out.println(requestBody);

            // Create a trimmed request body
            JsonNode jsonNode = new ObjectMapper().readTree(requestBody);
            String modifiedBody = traverseAndTrimJson(jsonNode).toString();

            // Log the modified request body
            System.out.println("Modified Request Body:");
            System.out.println(modifiedBody);

            // Wrap the original request with the modified content
            request = new TrimmedRequestWrapper(request, modifiedBody);
        }
        return true;
    }

    private String traverseAndTrimJson(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                if (entry.getValue().isTextual()) {
                    String key = entry.getKey().trim();
                    String value = entry.getValue().asText().trim();
                    objectNode.put(key, value);
                    System.out.println("Trimmed key: " + key);
                } else if (entry.getValue().isObject() || entry.getValue().isArray()) {
                    traverseAndTrimJson(entry.getValue());
                }
            });
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            arrayNode.elements().forEachRemaining(this::traverseAndTrimJson);
        }
        return node.toString();
    }

    // Other overridden methods (postHandle, afterCompletion) as needed

    private static class TrimmedRequestWrapper extends HttpServletRequestWrapper {
        private final String body;

        public TrimmedRequestWrapper(HttpServletRequest request, String modifiedBody) throws IOException {
            super(request);
            this.body = modifiedBody;
        }


        public TrimmedRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            // Read the request body
            String requestBody = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines().reduce("", String::concat);

            // Parse JSON and trim string fields
            JsonNode jsonNode = new ObjectMapper().readTree(requestBody);
            String modifiedBody = traverseAndTrimJson(jsonNode).toString();

            this.body = modifiedBody;
        }

        private JsonNode traverseAndTrimJson(JsonNode node) {
            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                objectNode.fields().forEachRemaining(entry -> {
                    if (entry.getValue().isTextual()) {
                        String key = entry.getKey().trim();
                        String value = entry.getValue().asText().trim();
                        objectNode.put(key, value);
                        System.out.println("Trimmed key: " + key);
                    } else if (entry.getValue().isObject() || entry.getValue().isArray()) {
                        traverseAndTrimJson(entry.getValue());
                    }
                });
            } else if (node.isArray()) {
                ArrayNode arrayNode = (ArrayNode) node;
                arrayNode.elements().forEachRemaining(this::traverseAndTrimJson);
            }
            return node;
        }

//        @Override
//        public BufferedReader getReader() throws IOException {
//            return new BufferedReader(new InputStreamReader(getInputStream()));
//        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    // Not implemented
                }

                @Override
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(this.getInputStream()));
        }
    }
}
