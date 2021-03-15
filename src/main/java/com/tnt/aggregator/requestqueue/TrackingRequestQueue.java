package com.tnt.aggregator.requestqueue;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

@Component
public class TrackingRequestQueue {

    private final BlockingQueue<RequestQueueElement<String>> requestQueue = new LinkedBlockingQueue<RequestQueueElement<String>>();

    public void push(String key, BiConsumer<Map<String, String>, Object> responseConsumer, Object lock, LocalDateTime entryDate) throws InterruptedException {
        //insert into queue or wait if full
        requestQueue.put( new RequestQueueElement<>( key, responseConsumer, lock, entryDate ) );
    }

    public RequestQueueElement<String> poll( ) {
        return requestQueue.poll();
    }

}
