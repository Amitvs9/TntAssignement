package com.tnt.aggregator.requestqueue;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

@Component
public class ShipmentRequestQueue {

    private final BlockingQueue<RequestQueueElement<List<String>>> requestQueue = new LinkedBlockingQueue<RequestQueueElement<List<String>>>();

    public void push(String key, BiConsumer<Map<String, List<String>>, Object> responseConsumer, Object lock, LocalDateTime entryDate) throws InterruptedException {
        //insert into queue or wait if full
        requestQueue.put( new RequestQueueElement<>( key, responseConsumer, lock, entryDate ) );
    }

    public RequestQueueElement<List<String>> poll( ) {
        return requestQueue.poll();
    }

}
