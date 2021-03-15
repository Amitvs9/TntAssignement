package com.tnt.aggregator.consumer;

import com.tnt.aggregator.requestqueue.PricingRequestQueue;
import com.tnt.aggregator.requestqueue.ShipmentRequestQueue;
import com.tnt.aggregator.requestqueue.TrackingRequestQueue;
import com.tnt.aggregator.service.PricingApiServiceImpl;
import com.tnt.aggregator.service.ShipmentApiServiceImpl;
import com.tnt.aggregator.service.TrackingApiServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RequestQueueConsumerRunnable implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger( RequestQueueConsumerRunnable.class );

    private final PricingApiServiceImpl pricingApiService;
    private final ShipmentApiServiceImpl shipmentApiService;
    private final TrackingApiServiceImpl trackingApiService;
    private final PricingRequestQueue pricingRequestQueue;
    private final ShipmentRequestQueue shipmentRequestQueue;
    private final TrackingRequestQueue trackingRequestQueue;


    public RequestQueueConsumerRunnable(PricingApiServiceImpl pricingApiService, ShipmentApiServiceImpl shipmentApiService, TrackingApiServiceImpl trackingApiService, PricingRequestQueue requestQueue, ShipmentRequestQueue shipmentRequestQueue, TrackingRequestQueue trackingRequestQueue) {
        this.pricingApiService = pricingApiService;
        this.shipmentApiService = shipmentApiService;
        this.trackingApiService = trackingApiService;
        this.pricingRequestQueue = requestQueue;
        this.shipmentRequestQueue = shipmentRequestQueue;
        this.trackingRequestQueue = trackingRequestQueue;
    }

    @Override
    public void run(String... args) {
        log.info( "inside run of APIQueueConsumerRunnable" );
        new Thread( new PricingQueueConsumer( pricingApiService, pricingRequestQueue ) ).start();
        new Thread( new TrackingQueueConsumer( trackingApiService, trackingRequestQueue ) ).start();
        new Thread( new ShipmentQueueConsumer( shipmentApiService, shipmentRequestQueue ) ).start();
    }

}
