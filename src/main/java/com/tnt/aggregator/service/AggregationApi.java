package com.tnt.aggregator.service;

import com.tnt.aggregator.model.TntAggregatorResponse;
import com.tnt.aggregator.requestqueue.PricingRequestQueue;
import com.tnt.aggregator.requestqueue.ShipmentRequestQueue;
import com.tnt.aggregator.requestqueue.TrackingRequestQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Component
public class AggregationApi {

    private static final Logger log = LoggerFactory.getLogger( AggregationApi.class );

    //each call to this call will be a thread call or it shall create a thread
    //Each thread will maintain it's own logic for handling request, response object, sending to queue and consuming from the queue, waiting for the response from queue processor

    //input request parameter for each for easier testing
    List<String> trackingRequest;
    List<String> shipmentRequest;
    List<String> pricingRequest;

    //To be able to test
    CountDownLatch countDownLatch;

    Map<String, String> trackingResponse = new HashMap<>();
    Map<String, List<String>> shipmentResponse = new HashMap<>();
    Map<String, Double> pricingResponse = new HashMap<>();

    //Lock for each aggregate request thread i.e individual aggregateApi call
    final Object lock = new Object();

    //Queue for individual api
    PricingRequestQueue pricingQueue;
    ShipmentRequestQueue shipmentQueue;
    TrackingRequestQueue trackingQueue;


    public AggregationApi(List<String> trackingRequest,
                          List<String> shipmentRequest,
                          List<String> pricingRequest,
                          CountDownLatch countDownLatch,
                          TrackingRequestQueue trackingQueue,
                          ShipmentRequestQueue shipmentQueue,
                          PricingRequestQueue pricingQueue
    ) {

        this.trackingRequest = trackingRequest;
        this.shipmentRequest = shipmentRequest;
        this.pricingRequest = pricingRequest;
        this.countDownLatch = countDownLatch;
        this.pricingQueue = pricingQueue;
        this.shipmentQueue = shipmentQueue;
        this.trackingQueue = trackingQueue;
    }

    public TntAggregatorResponse getAggregatedResponse(List<String> trackingRequest, List<String> shipmentRequest, List<String> pricingRequest) {

        //Send all to queue and make this thread wait. Once all responses have returned to this thread or timer expires notify this thread and return the response.
        //Make a new lock per thread for it to wait on

        LocalDateTime currentDateTime = LocalDateTime.now();

        synchronized (lock) {
            try {
                //send a consumer function alongside key to be processed
                for (String track : trackingRequest) {
                    trackingQueue.push( track, this::fillTrackingResponse, lock, currentDateTime );
                }

                for (String shipment : shipmentRequest) {
                    shipmentQueue.push( shipment, this::fillShipmentResponse, lock, currentDateTime );
                }

                for (String price : pricingRequest) {
                    pricingQueue.push( price, this::fillPricingResponse, lock, currentDateTime );
                }

                print( "Waiting for response" );
                lock.wait();
            } catch (InterruptedException exception) {
                log.error( "Something failed in aggregation api wait " + exception );
            }
        }

        //Assume by this point either something failed or the notify has been called

        //TO_DO:print the data once all responses are gathered otherwise the thread will stay open
        TntAggregatorResponse response = new TntAggregatorResponse();
        response.setPricing( pricingResponse );
        response.setShipments( shipmentResponse );
        response.setTrack( trackingResponse );
        print( "Full response ::: trackingResponse: " + trackingResponse + "  shipmentResponse: " + shipmentResponse + "  pricingResponse: " + pricingResponse );


        //inform the main thread once all responses have been gathered from individual api's
        countDownLatch.countDown();
        return response;
    }

    //a method to fill the appropriate response

    public void fillTrackingResponse(Map<String, String> response, Object thisLock) {
        fillResponse( trackingResponse, response, thisLock );
    }

    public void fillShipmentResponse(Map<String, List<String>> response, Object thisLock) {
        fillResponseShipment( shipmentResponse, response, thisLock );
    }

    public void fillPricingResponse(Map<String, Double> response, Object thisLock) {
        fillResponsePricing( pricingResponse, response, thisLock );
    }

    private void fillResponse(Map<String, String> producerResponseMap, Map<String, String> consumerResponseMap, Object thisLock) {
        synchronized (thisLock) {
            print( "received response: " + consumerResponseMap );
            //fill the keys along with their values
            producerResponseMap.putAll( consumerResponseMap );
            if (isDone()) {
                print( "notifying waiting threads" );
                //TO_DO:Order it to ensure wait happens before all queue processors can complete
                thisLock.notify();
            }
        }
    }

    private void fillResponseShipment(Map<String, List<String>> producerResponseMap, Map<String, List<String>> consumerResponseMap, Object thisLock) {
        synchronized (thisLock) {
            print( "received response: " + consumerResponseMap );
            //fill the keys along with their values
            producerResponseMap.putAll( consumerResponseMap );
            if (isDone()) {
                print( "notifying waiting threads" );
                //TO_DO:Order it to ensure wait happens before all queue processors can complete
                thisLock.notify();
            }
        }
    }

    private void fillResponsePricing(Map<String, Double> producerResponseMap, Map<String, Double> consumerResponseMap, Object thisLock) {
        synchronized (thisLock) {
            print( "received response: " + consumerResponseMap );
            //fill the keys along with their values
            producerResponseMap.putAll( consumerResponseMap );
            if (isDone()) {
                print( "notifying waiting threads" );
                //TO_DO:Order it to ensure wait happens before all queue processors can complete
                thisLock.notify();
            }
        }
    }

    //Check if all input parameters are in response object
    //TODO:There may be duplicate keys
    boolean isDone( ) {
        for (String trackingKey : trackingRequest) {
            if (!trackingResponse.containsKey( trackingKey )) {
                return false;
            }
        }

        for (String pricingKey : pricingRequest) {
            if (!pricingResponse.containsKey( pricingKey )) {
                return false;
            }
        }

        for (String shipmentKey : shipmentRequest) {
            if (!shipmentResponse.containsKey( shipmentKey )) {
                return false;
            }
        }
        return true;
    }

    public void print(String str) {
        log.info( Thread.currentThread().getName() + " " + str );
    }
}
