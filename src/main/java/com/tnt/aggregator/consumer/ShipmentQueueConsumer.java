package com.tnt.aggregator.consumer;

import com.tnt.aggregator.requestqueue.RequestQueueElement;
import com.tnt.aggregator.requestqueue.ShipmentRequestQueue;
import com.tnt.aggregator.service.ShipmentApiServiceImpl;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("shipment")
public class ShipmentQueueConsumer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger( ShipmentQueueConsumer.class );

    private final ShipmentApiServiceImpl api;
    private final ShipmentRequestQueue requestQueue;

    public ShipmentQueueConsumer(ShipmentApiServiceImpl api, ShipmentRequestQueue requestQueue){
        this.api = api;
        this.requestQueue = requestQueue;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info( "inside run of ShipmentQueueConsumer" );
        List<RequestQueueElement<List<String>>> retrievedElements = new ArrayList<>();
        //Keep track of minimum date in the queue. Can either track a variable or use a MinHeap/PriorityQueue based on datetime
        LocalDateTime minimumDateTime = LocalDateTime.now();

        while(true){
            //Fetch an element from queue as soon as it is available or wait if empty. For part 3, make it a non-blocking call and return null if queue is empty
            RequestQueueElement<List<String>> queueElement = requestQueue.poll();

            if(queueElement != null){
                retrievedElements.add(queueElement);

                //Compare the current mininumDateTime against the new element fetched from the queue
                LocalDateTime newElementDateTime = queueElement.getEntryDate();
                if(retrievedElements.size() == 1 || newElementDateTime.isBefore(minimumDateTime)){
                    minimumDateTime = newElementDateTime;
                }

            }

            //Check if the queue cap of 5 is reached or minimum element has been waiting for more than 5 seconds. If true, call the api
            if(retrievedElements.size() == 5 || (ChronoUnit.SECONDS.between(minimumDateTime, LocalDateTime.now()) > 10 && !retrievedElements.isEmpty())){

                callApiAndNotifyConsumer(retrievedElements);

                //clear the retrieved list and start afresh
                retrievedElements = new ArrayList<>();

            }
        }
    }

    private void callApiAndNotifyConsumer(List<RequestQueueElement<List<String>>> retrievedElements) throws IOException {
        Map<String, RequestQueueElement<List<String>>> keyToQueueElementMap = retrievedElements.stream().collect( Collectors.toMap( RequestQueueElement::getKey, Function.identity()));
        //call the api
        Map<String, List<String>> apiResponse = api.getShipmentResponse(retrievedElements.stream().map( RequestQueueElement::getKey).collect(Collectors.toList()));

        for(Map.Entry<String, List<String>> entry : apiResponse.entrySet()){
            String key = entry.getKey();
            List<String> response = entry.getValue();
            RequestQueueElement<List<String>> element = keyToQueueElementMap.get(key);
            Map<String, List<String>> responseMap = new HashMap<>();
            responseMap.put(key, response);

            //call the individual threads to notify them of the response received
            element.getAggregateConsumer().accept(responseMap, element.getLock());
        }

    }
}
