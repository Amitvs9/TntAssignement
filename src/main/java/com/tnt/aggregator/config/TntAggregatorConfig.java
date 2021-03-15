package com.tnt.aggregator.config;

import com.tnt.aggregator.requestqueue.TrackingRequestQueue;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;

@Configuration
@Getter
public class TntAggregatorConfig {

    @Autowired
    private TrackingRequestQueue shipmentQueue;

    @Value("${tnt.shipmentsUrl}")
    private String shipmentsUrl;

    @Value("${tnt.trackUrl}")
    private String trackUrl;

    @Value("${tnt.pricingUrl}")
    private String pricingUrl;

    @Value("${tnt.connectTimeout}")
    private Integer connectTimeOut;

    @Value("${tnt.readTimeout}")
    private Integer readTimeOut;

    @Bean
    public CountDownLatch latch( ) {
        return new CountDownLatch( 6 );
    }

    @Bean(name = "restTemplate")
    public RestTemplate createRestTemplate( ) {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout( connectTimeOut );
        httpRequestFactory.setReadTimeout( readTimeOut );
        return new RestTemplate( httpRequestFactory );
    }


}