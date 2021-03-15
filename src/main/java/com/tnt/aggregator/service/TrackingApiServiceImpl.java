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
public class TrackingApiServiceImpl{

    private static final Logger log = LoggerFactory.getLogger(TrackingApiServiceImpl.class);

    private final TntAggregatorRestAPIDataImpl restClient;
    private final TntAggregatorConfig tntAggregatorConfig;

    public TrackingApiServiceImpl(TntAggregatorRestAPIDataImpl restClient, TntAggregatorConfig tntAggregatorConfig) {
        this.restClient = restClient;
        this.tntAggregatorConfig = tntAggregatorConfig;
    }


    public Map<String, String> getTrackingResponse(List<String> parameters) throws IOException {

        log.info( "calling Tracking endpoint.." );
        ObjectMapper mapper = new ObjectMapper();
        try {
            String response = restClient.getResponseFromApi( parameters, tntAggregatorConfig.getTrackUrl() );
            return mapper.readValue( response, new TypeReference<HashMap<String, String>>() {
            } );
        } catch (JsonParseException | JsonMappingException ex) {
            throw new TntCustomException( "Exception in Json Parsing", "Json Parsing", ex );
        } catch (IOException iox) {
            throw new TntCustomException( "IO Exception in Json Parsing", "IO", iox );
        }
    }
}
