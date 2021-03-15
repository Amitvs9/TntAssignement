package com.tnt.aggregator.requestqueue;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

@Component
public class PricingRequestQueue {

    private final BlockingQueue<RequestQueueElement<Double>> requestQueue = new LinkedBlockingQueue<>();

    public void push(String key, BiConsumer<Map<String, Double>, Object> responseConsumer, Object lock, LocalDateTime entryDate) throws InterruptedException {
        //insert into queue or wait if full
        requestQueue.put( new RequestQueueElement<>( key, responseConsumer, lock, entryDate ) );
    }

    public RequestQueueElement<Double> poll( ) {
        return requestQueue.poll();
    }

}
