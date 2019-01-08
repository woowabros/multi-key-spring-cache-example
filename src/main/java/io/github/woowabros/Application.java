package io.github.woowabros;

import io.github.woowabros.example.Finder;
import io.github.woowabros.model.CacheExampleResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Application {

    public static void main(String... args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args)) {
            Finder finder = ctx.getBean(Finder.class);

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
}
