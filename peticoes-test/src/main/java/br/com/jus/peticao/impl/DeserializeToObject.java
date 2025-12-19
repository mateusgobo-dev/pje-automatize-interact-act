package br.com.jus.peticao.impl;

import br.com.pje.model.TokenPattern;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Objects;

@FunctionalInterface
public interface DeserializeToObject {
    TokenPattern deserializeJson(String json) throws JsonProcessingException;

    DeserializeToObject apply = (json) -> {
        TokenPattern tokenPattern = null;
        if(Objects.nonNull(json)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
            mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
            tokenPattern = mapper.readValue(json, TokenPattern.class);
            System.out.println(tokenPattern);
        }
        return tokenPattern;
    };
}
