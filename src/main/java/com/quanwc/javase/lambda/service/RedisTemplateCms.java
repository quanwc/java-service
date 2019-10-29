package com.quanwc.javase.lambda.service;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

public class RedisTemplateCms<K,V> extends ReactiveRedisTemplate<K,V> {

    public RedisTemplateCms(ReactiveRedisConnectionFactory connectionFactory, RedisSerializationContext<K,V> serializationContext) {
        super(connectionFactory, serializationContext);
    }
}
