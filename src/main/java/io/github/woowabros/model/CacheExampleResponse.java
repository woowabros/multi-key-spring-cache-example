package io.github.woowabros.model;

import lombok.ToString;

@ToString
public class CacheExampleResponse {
    @CacheKey
    private Long key;
    private Long value;
    private String content;

    public static CacheExampleResponse of(Long key,
                                          Long value,
                                          String content) {
        CacheExampleResponse response = new CacheExampleResponse();

        response.key = key;
        response.value = value;
        response.content = content;

        return response;
    }
}
