package com.smartqueue.realtime_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI realtimeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartQueue AI - Realtime Gateway Service API")
                        .version("1.0.0")
                        .description("WebSocket STOMP & Realtime Queue Updates Broadcasting Gateway Service")
                        .contact(new Contact()
                                .name("SmartQueue AI Engineering Team")
                                .email("engineering@smartqueue.ai"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
