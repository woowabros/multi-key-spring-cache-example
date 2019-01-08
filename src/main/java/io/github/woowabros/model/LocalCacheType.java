package io.github.woowabros.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@Getter
public enum LocalCacheType {
    TEST(CacheName.TEST, 10L, 100L, TimeUnit.MINUTES);

    LocalCacheType(String cacheName,
                   Long maxSize,
                   Long expireTime,
                   TimeUnit expireTimeUnit) {

        this.cacheName = cacheName;
        this.maxSize = maxSize;
        this.expireTime = expireTime;
        this.expireTimeUnit = expireTimeUnit;
    }

    private String cacheName;
    private Long maxSize;
    private Long expireTime;
    private TimeUnit expireTimeUnit;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class CacheName {
        public static final String TEST = "test";
    }
}
