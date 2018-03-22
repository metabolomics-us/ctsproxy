package edu.ucdavis.fiehnlab.config;

import com.google.common.cache.CacheBuilder;
import feign.Logger;
import feign.Request;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by diego on 2/16/2017.
 */
@Configuration
@EnableCaching
@EnableFeignClients
public class CtsClientConfiguration {
    @Bean
    Logger.Level logger() {
        return Logger.Level.FULL;
    }

    @Bean
    Request.Options options() {
        return new Request.Options(1000, 120000);
    }

    @Bean
    GuavaCache quarterCache() {
        return new GuavaCache("QuarterCache", CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.DAYS).expireAfterWrite(30, TimeUnit.DAYS).build());
    }

    @Bean
    SimpleCacheManager cacheManager(Collection<GuavaCache> caches) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }
}
