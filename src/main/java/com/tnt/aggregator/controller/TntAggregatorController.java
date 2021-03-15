package com.tnt.aggregator.controller;

import com.tnt.aggregator.model.TntAggregatorResponse;
import com.tnt.aggregator.service.AggregationApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
public class TntAggregatorController {

    private final AggregationApi aggregationApi ;


    public TntAggregatorController(final AggregationApi aggregationApi ){
        this.aggregationApi= aggregationApi;

    }

    @GetMapping("/health")
    public String checkHealth() {
        return "Hello TNT App";
    }


    @GetMapping("/aggregation")
    public TntAggregatorResponse apiAggregation(@RequestParam(value = "pricing") List<String> pricing,
                                                @RequestParam(value = "track") List<String> track,
                                                @RequestParam(value = "shipments") List<String> shipments) throws  ExecutionException, InterruptedException, IOException {

       return aggregationApi.getAggregatedResponse(pricing, track, shipments);
    }



}
