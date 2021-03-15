package com.tnt.aggregator.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.aggregator.config.TntAggregatorConfig;
import com.tnt.aggregator.error.TntCustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PricingApiServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(PricingApiServiceImpl.class);

    private final TntAggregatorRestAPIDataImpl restClient;
    private final TntAggregatorConfig tntAggregatorConfig;

    public PricingApiServiceImpl(TntAggregatorRestAPIDataImpl restClient, TntAggregatorConfig tntAggregatorConfig) {
        this.restClient = restClient;
        this.tntAggregatorConfig = tntAggregatorConfig;
    }

    public Map<String, Double> getPricingResponse(List<String> parameters) throws IOException {
        //actual api call and return response
        ObjectMapper mapper = new ObjectMapper();
        log.info( "calling Pricing endpoint.." );
        try {
            String response = restClient.getResponseFromApi( parameters, tntAggregatorConfig.getPricingUrl() );
            return mapper.readValue( response, new TypeReference<HashMap<String, Double>>() {
            } );
        } catch (JsonParseException | JsonMappingException ex) {
            throw new TntCustomException( "Exception in Json Parsing", "Json Parsing", ex );
        } catch (IOException iox) {
            throw new TntCustomException( "IO Exception in Json Parsing", "IO", iox );
        }
    }
}
