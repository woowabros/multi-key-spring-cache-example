package io.github.woowabros.example;

import io.github.woowabros.model.CacheExampleResponse;
import io.github.woowabros.service.MultiKeyCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Finder {

    private final MultiKeyCacheService multiKeyCacheService;

    @Autowired
    public Finder(MultiKeyCacheService multiKeyCacheService) {
        this.multiKeyCacheService = multiKeyCacheService;
    }

    @Cacheable(cacheNames = "test")
    public CacheExampleResponse find(Long key) {
        log.info("단건 캐싱 시작 - key: {}", key);

        return CacheExampleResponse.of(key, key + 1000L, key + "content");
    }

    public Set<CacheExampleResponse> find(Set<Long> keys) {
        log.info("다건 캐싱 시작 - keys: {}", keys);

        return multiKeyCacheService.multiCacheGet(
                "test",
                Long.class,
                CacheExampleResponse.class,
                keys,
                this::findValue);
    }

    private Set<CacheExampleResponse> findValue(Set<Long> keys) {
        return keys.stream()
                .map(key -> CacheExampleResponse.of(key, key + 1000L, key + "content"))
                .collect(Collectors.toSet());
    }
}
