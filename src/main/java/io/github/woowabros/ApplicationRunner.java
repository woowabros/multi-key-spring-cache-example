package io.github.woowabros;

import io.github.woowabros.example.Finder;
import io.github.woowabros.model.CacheExampleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ApplicationRunner implements CommandLineRunner {
    private final Finder finder;

    @Autowired
    public ApplicationRunner(Finder finder) {
        this.finder = finder;
    }

    @Override
    public void run(String... args) {
        CacheExampleResponse cacheExampleResponse = finder.find(1L);
        System.out.println(cacheExampleResponse);

        Set<Long> keys = new HashSet<>();
        keys.add(1L);
        keys.add(2L);
        keys.add(3L);
        Set<CacheExampleResponse> cacheExampleResponses = finder.find(keys);
        cacheExampleResponses.forEach(System.out::println);
    }
}
