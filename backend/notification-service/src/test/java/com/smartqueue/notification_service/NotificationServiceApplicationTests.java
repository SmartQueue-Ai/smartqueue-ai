package com.smartqueue.notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "management.health.mail.enabled=false",
        "management.health.redis.enabled=false",
        "management.health.rabbit.enabled=false",
        "management.health.defaults.enabled=false"
})
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

    @MockBean
    private ConnectionFactory connectionFactory;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private JavaMailSenderImpl javaMailSenderImpl;

    @Test
    void contextLoads() {
    }

}
