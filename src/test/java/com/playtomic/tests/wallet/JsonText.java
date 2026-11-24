package com.playtomic.tests.wallet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;

import java.io.IOException;

public final class JsonText {

    private static final ObjectReader READER = new ObjectMapper().reader()
            .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .without(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);

    private JsonText() {
    }

    public static JsonNode read(String json) throws IOException {
        return READER.readTree(json);
    }

    public static String compact(String json) throws IOException {
        return read(json).toString();
    }
}
