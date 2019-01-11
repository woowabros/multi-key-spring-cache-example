package io.github.woowabros;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@ComponentScan
@EnableCaching
@Configuration
public class LocalCacheConfiguration {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = new ArrayList<>();

        caches.add(new CaffeineCache("test", getTestCacheProperty()));

        cacheManager.setCaches(caches);

        return cacheManager;
    }

    private Cache<Object, Object> getTestCacheProperty() {
        return Caffeine.newBuilder().recordStats()
                .expireAfterWrite(10L, TimeUnit.MINUTES).maximumSize(100L)
                .build();
    }
}
