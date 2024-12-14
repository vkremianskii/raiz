package net.kremianskii.roots.jooq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class Json {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Json() {
    }

    public static <T> String jsonStringFromValue(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Error converting value to JSON string", ex);
        }
    }

    public static <T> T valueFromJsonString(String s, Class<T> cls) {
        try {
            return objectMapper.readValue(s, cls);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Error converting JSON string to value", ex);
        }
    }

    public static JsonNode treeFromJsonString(String s) {
        try {
            return objectMapper.readTree(s);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Error converting JSON string to tree", ex);
        }
    }

    public static final class ObjectBuilder {
        private final ObjectNode root;

        private ObjectBuilder() {
            root = JsonNodeFactory.instance.objectNode();
        }

        public ObjectBuilder put(String fieldName, boolean value) {
            root.put(fieldName, value);
            return this;
        }

        public ObjectBuilder put(String fieldName, long value) {
            root.put(fieldName, value);
            return this;
        }

        public ObjectBuilder put(String fieldName, double value) {
            root.put(fieldName, value);
            return this;
        }

        public ObjectBuilder put(String fieldName, String value) {
            root.put(fieldName, value);
            return this;
        }

        public ObjectBuilder put(String fieldName, ObjectBuilder object) {
            root.set(fieldName, object.root);
            return this;
        }

        public ObjectBuilder put(String fieldName, ArrayBuilder array) {
            root.set(fieldName, array.array);
            return this;
        }

        public ObjectNode build() {
            return root;
        }

        public static ObjectBuilder jsonObject() {
            return new ObjectBuilder();
        }
    }

    public static final class ArrayBuilder {
        private final ArrayNode array;

        private ArrayBuilder() {
            array = JsonNodeFactory.instance.arrayNode();
        }

        public ArrayBuilder add(boolean value) {
            array.add(value);
            return this;
        }

        public ArrayBuilder add(long value) {
            array.add(value);
            return this;
        }

        public ArrayBuilder add(double value) {
            array.add(value);
            return this;
        }

        public ArrayBuilder add(String value) {
            array.add(value);
            return this;
        }

        public ArrayBuilder add(ObjectBuilder object) {
            array.add(object.root);
            return this;
        }

        public ArrayNode build() {
            return array;
        }

        public static ArrayBuilder jsonArray() {
            return new ArrayBuilder();
        }
    }
}
