package io.github.woowabros.service;

import io.github.woowabros.model.CacheKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
@Service
public class MultiKeyCacheService {
    private final CacheManager cacheManager;

    @Autowired
    public MultiKeyCacheService(CacheManager cacheManager) {
        Assert.notNull(cacheManager, "cacheManager 는 null 일 수 없습니다.");
        this.cacheManager = cacheManager;
    }

    public <K, V> Set<V> multiCacheGet(String cacheKeyName,
                                       Class<K> keyClass,
                                       Class<V> valueClass,
                                       Collection<K> keys,
                                       Function<Set<K>, Set<V>> notCachedValueFinder) {

        return multiCacheGet(cacheKeyName, keyClass, valueClass, keys, notCachedValueFinder, this::cacheKeyAnnotationExtractor);
    }

    /**
     * key 목록을 받아서 {@code cacheKeyName} Spring Cache에 이미 캐싱돼 있는 항목은 캐시에서,
     * 캐시가 안 된 항목은 {@code notCachedValueFinder} 함수로 로딩해서 다시 캐시에 넣고, 이 두 값을
     * 합쳐서 순서없이 반환한다.
     * <p>
     * {@code notCachedValueFinder} 는 key 를 목록으로 받아서 SQL의 경우 {@code in} 절 처럼 최대한 좋은 성능을 내는
     * 방식으로 구현해야 한다.
     *
     * @param <K>                  Key 의 타입
     * @param <V>                  값 객체의 타입
     * @param cacheKeyName         캐시 키
     * @param valueClass           값 객체 타입
     * @param keys                 키 목록
     * @param notCachedValueFinder 캐시에 없는 key들을 가지고 실제 캐싱할 데이터를 로딩하는 함수
     * @param keyExtractor         valueFinder의 결과로 나온 값 객체들에서 key를 뽑아내는 함수
     * @return 이미 캐시돼 있는 데이터와 캐시 안된 데이터들을 valueFinder로 찾아서 함께 합쳐서 반환한다. 순서는 무시된다.
     */
    public <K, V> Set<V> multiCacheGet(String cacheKeyName,
                                       Class<K> keyClass,
                                       Class<V> valueClass,
                                       Collection<K> keys,
                                       Function<Set<K>, Set<V>> notCachedValueFinder,
                                       BiFunction<V, Class<K>, K> keyExtractor) {

        Cache cache = getCache(cacheKeyName);

        final Set<K> notCachedKeys = new HashSet<>();
        final Set<V> cachedValues = new HashSet<>();

        for (K key : keys) {
            V cachedValue = cache.get(key, valueClass);
            if (cachedValue != null) {
                cachedValues.add(cachedValue);
            } else {
                notCachedKeys.add(key);
            }
        }

        log.info("캐싱되지 않은 키값: {}", notCachedKeys);

        Set<V> loadedValues = loadNotCachedValues(cache, keyClass, notCachedKeys, notCachedValueFinder, keyExtractor);

        cachedValues.addAll(loadedValues);
        return cachedValues;
    }

    private Cache getCache(String cacheKeyName) {
        return Optional.ofNullable(cacheManager.getCache(cacheKeyName))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("선언되지 않은 cache 이름입니다. cacheName: %s", cacheKeyName)));
    }

    private <K, V> Set<V> loadNotCachedValues(Cache cache,
                                              Class<K> keyClass,
                                              Set<K> notCachedKeys,
                                              Function<Set<K>, Set<V>> notCachedValueFinder,
                                              BiFunction<V, Class<K>, K> keyExtractor) {
        if (notCachedKeys.isEmpty()) {
            return Collections.unmodifiableSet(new HashSet<>());
        }

        Set<V> loadedValues = notCachedValueFinder.apply(notCachedKeys);

        for (V value : loadedValues) {
            cache.put(keyExtractor.apply(value, keyClass), value);
        }
        return loadedValues;
    }

    private <K, V> K cacheKeyAnnotationExtractor(V v, Class<K> keyClass) {
        return Optional.ofNullable(cacheKeyAnnotationOfFieldExtractor(v, keyClass))
                .orElseGet(() -> Optional.ofNullable(cacheKeyAnnotationOfMethodExtractor(v, keyClass))
                        .orElseThrow(() -> new IllegalStateException(String.format("@CacheKey 애노테이션이 어느 필드/메소드 에도 존재하지 않음 - %s", v.getClass().getName()))));
    }

    private <K, V> K cacheKeyAnnotationOfFieldExtractor(V v, Class<K> keyClass) {
        for (Field field : v.getClass().getDeclaredFields()) {
            if (field.getAnnotation(CacheKey.class) != null) {
                try {
                    if (!field.getType().isAssignableFrom(keyClass)) {
                        throw new IllegalStateException(String.format("@CacheKey 애노테이션 Key의 타입 변환 실패 - %s - field: %s", v.getClass().getName(), field.getName()));
                    }

                    field.setAccessible(true);
                    return keyClass.cast(field.get(v));
                } catch (IllegalAccessException | RuntimeException e) {
                    // accessible 을 true 로 변경하므로 발생할 수 없다.
                    throw new IllegalStateException(String.format("@CacheKey 애노테이션 값 읽기 실패 - %s - field: %s", v.getClass().getName(), field.getName()), e);
                }
            }
        }

        return null;
    }

    private <K, V> K cacheKeyAnnotationOfMethodExtractor(V v, Class<K> keyClass) {
        for (Method method : v.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(CacheKey.class) != null) {
                try {
                    if (!method.getReturnType().isAssignableFrom(keyClass)) {
                        throw new IllegalStateException(String.format("@CacheKey 애노테이션 Key의 타입 변환 실패 - %s - field: %s", v.getClass().getName(), method.getName()));
                    }

                    return keyClass.cast(method.invoke(v));
                } catch (IllegalAccessException | RuntimeException | InvocationTargetException e) {
                    throw new IllegalStateException(String.format("@CacheKey 애노테이션 값 읽기 실패 - %s - method: %s", v.getClass().getName(), method.getName()), e);
                }
            }
        }

        return null;
    }
}
