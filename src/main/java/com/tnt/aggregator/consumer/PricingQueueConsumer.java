package com.tnt.aggregator.consumer;

import com.tnt.aggregator.requestqueue.RequestQueueElement;
import com.tnt.aggregator.requestqueue.PricingRequestQueue;
import com.tnt.aggregator.service.PricingApiServiceImpl;
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

@Component("pricing")
public class PricingQueueConsumer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger( PricingQueueConsumer.class );

    private final PricingApiServiceImpl api;
    private final PricingRequestQueue requestQueue;

    public PricingQueueConsumer(PricingApiServiceImpl api, PricingRequestQueue requestQueue) {
        this.api = api;
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        log.info( "inside run of PricingQueueConsumer" );

        List<RequestQueueElement<Double>> retrievedElements = new ArrayList<>();

        //Keep track of minimum date in the queue. Can either track a variable or use a MinHeap/PriorityQueue based on datetime
        LocalDateTime minimumDateTime = LocalDateTime.now();
        try {
            while (true) {
                //Fetch an element from queue as soon as it is available or wait if empty. For part 3, make it a non-blocking call and return null if queue is empty
                RequestQueueElement<Double> queueElement = requestQueue.poll();

                if (queueElement != null) {
                    retrievedElements.add( queueElement );

                    //Compare the current minimumDateTime against the new element fetched from the queue
                    LocalDateTime newElementDateTime = queueElement.getEntryDate();
                    if (retrievedElements.size() == 1 || newElementDateTime.isBefore( minimumDateTime )) {
                        minimumDateTime = newElementDateTime;
                    }

                }

                //Check if the queue cap of 5 is reached or minimum element has been waiting for more than 5 seconds. If true, call the api
                if (retrievedElements.size() == 5 || (ChronoUnit.SECONDS.between( minimumDateTime, LocalDateTime.now() ) > 10 && !retrievedElements.isEmpty())) {

                    callApiAndNotifyConsumer( retrievedElements );

                    //clear the retrieved list and start afresh
                    retrievedElements = new ArrayList<>();

                }
            }
        } catch (Exception exception) {
            log.error( "exception inside run method of PricingConsumer" + exception );
        }
    }


    /**
     * method to call api and notify Consumer
     *
     * @param retrievedElements @PricingRequestQueueElement
     * @throws IOException exception..
     */
    private void callApiAndNotifyConsumer(List<RequestQueueElement<Double>> retrievedElements) throws IOException {
        Map<String, RequestQueueElement<Double>> keyToQueueElementMap = retrievedElements.stream().collect( Collectors.toMap( RequestQueueElement::getKey, Function.identity() ) );
        Map<String, Double> apiResponse = api.getPricingResponse( retrievedElements.stream().map( RequestQueueElement::getKey ).collect( Collectors.toList() ) );

        for (Map.Entry<String, Double> entry : apiResponse.entrySet()) {
            String key = entry.getKey();
            Double response = entry.getValue();
            RequestQueueElement<Double> element = keyToQueueElementMap.get( key );
            Map<String, Double> responseMap = new HashMap<>();
            responseMap.put( key, response );

            //call the individual threads to notify them of the response received
            element.getAggregateConsumer().accept( responseMap, element.getLock() );
        }

    }
}
