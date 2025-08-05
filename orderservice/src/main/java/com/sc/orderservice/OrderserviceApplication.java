package com.sc.orderservice;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.sc.orderservice.external.intercept.RestTemplateInterceptor;

@SpringBootApplication
@EnableFeignClients
public class OrderserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderserviceApplication.class, args);

	}

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateInterceptor interceptor) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(List.of(interceptor));
		return restTemplate;
	}
}
