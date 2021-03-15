package com.tnt.aggregator.consumer;

import com.tnt.aggregator.requestqueue.RequestQueueElement;
import com.tnt.aggregator.requestqueue.TrackingRequestQueue;
import com.tnt.aggregator.service.TrackingApiServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

@Component("track")
@Slf4j
public class TrackingQueueConsumer implements Runnable {

    private final TrackingApiServiceImpl api;
    private final TrackingRequestQueue requestQueue;

    public TrackingQueueConsumer(TrackingApiServiceImpl api, TrackingRequestQueue requestQueue){
        this.api = api;
        this.requestQueue = requestQueue;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info( "inside run of TrackingQueueConsumer" );

        List<RequestQueueElement<String>> retrievedElements = new ArrayList<>();

        //Keep track of minimum date in the queue. Can either track a variable or use a MinHeap/PriorityQueue based on datetime
        LocalDateTime minimumDateTime = LocalDateTime.now();

        while(true){
            //Fetch an element from queue as soon as it is available or wait if empty. For part 3, make it a non-blocking call and return null if queue is empty
            RequestQueueElement<String> queueElement = requestQueue.poll();

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

    private void callApiAndNotifyConsumer(List<RequestQueueElement<String>> retrievedElements) throws IOException {
        Map<String, RequestQueueElement<String>> keyToQueueElementMap = retrievedElements.stream().collect( Collectors.toMap( RequestQueueElement::getKey, Function.identity()));
        //call the api
        Map<String, String> apiResponse = api.getTrackingResponse(retrievedElements.stream().map( RequestQueueElement::getKey).collect(Collectors.toList()));

        for(Map.Entry<String, String> entry : apiResponse.entrySet()){
            String key = entry.getKey();
            String response = entry.getValue();
            RequestQueueElement<String> element = keyToQueueElementMap.get(key);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put(key, response);

            //call the individual threads to notify them of the response received
            element.getAggregateConsumer().accept(responseMap, element.getLock());
        }

    }
}
