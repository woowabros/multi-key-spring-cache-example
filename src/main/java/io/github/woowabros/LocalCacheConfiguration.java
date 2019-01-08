package io.github.woowabros;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.woowabros.model.LocalCacheType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@ComponentScan
@EnableCaching
@Configuration
public class LocalCacheConfiguration {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = Arrays.stream(LocalCacheType.values())
                .map(cache -> new CaffeineCache(cache.getCacheName(), Caffeine.newBuilder().recordStats()
                                .expireAfterWrite(cache.getExpireTime(), cache.getExpireTimeUnit())
                                .maximumSize(cache.getMaxSize())
                                .build()
                        )
                    )
                .collect(Collectors.toList());

        cacheManager.setCaches(caches);

        return cacheManager;
    }
}
