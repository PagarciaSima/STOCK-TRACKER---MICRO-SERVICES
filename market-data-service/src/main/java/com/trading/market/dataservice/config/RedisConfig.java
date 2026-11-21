package com.trading.market.dataservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration for the Redis template used to cache stock prices.
 */
@Configuration
public class RedisConfig {

    /**
     * Creates a {@link RedisTemplate} with String keys and JSON values.
     *
     * @param connectionFactory the Redis connection factory. Cannot be null.
     * @return a configured {@link RedisTemplate}. Never null.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory){

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        // Use the builder to create the serializer
        template.setValueSerializer(
                GenericJacksonJsonRedisSerializer.builder().build());

        return template;
    }
}
