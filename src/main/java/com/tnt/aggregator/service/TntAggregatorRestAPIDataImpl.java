package com.tnt.aggregator.service;

import com.tnt.aggregator.error.TntCustomException;
import com.tnt.aggregator.util.TntConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class TntAggregatorRestAPIDataImpl {

    private static final Logger log = LoggerFactory.getLogger(TntAggregatorRestAPIDataImpl.class);
    private static final String COMMA_SEPARATOR = ",";

    @Qualifier("restTemplate")
    private final RestTemplate restTemplate;

    public TntAggregatorRestAPIDataImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getResponseFromApi(List<String> ids, String url) throws TntCustomException {
        String response = null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                    .queryParam( TntConstant.SEARCH_QUERY_PARAM, String.join(COMMA_SEPARATOR, ids));
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                    builder.buildAndExpand().toUri().toString(), String.class);

            if (responseEntity.getStatusCode().value() == HttpStatus.OK.value()) {
                log.info(String.valueOf(responseEntity.getBody()));
                response = responseEntity.getBody();
            }

        } catch (HttpClientErrorException he) {
            log.error("HttpClientErrorException in getResponseFromApis ::" + he.getLocalizedMessage());
            if(he.getStatusCode() == HttpStatus.NOT_FOUND || he.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || he.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR){
                throw new TntCustomException(he.getStatusCode().toString(), he.getResponseBodyAsString(), he);
            }
        }
        catch (ResourceAccessException re) {
            log.error("TimeOutError in getResponseFromApis ::" + re.getLocalizedMessage());
            throw new TntCustomException(HttpStatus.REQUEST_TIMEOUT.toString(), re.getMessage(), re);
        }
        catch (Exception ex) {
            log.error("Exception occurred in getResponseFromApis ::" + ex.getLocalizedMessage());
            throw new TntCustomException(HttpStatus.SERVICE_UNAVAILABLE.toString(), ex.getMessage(), ex);
        }
        log.info("End getResponseFromApis");
        return response;
    }

}
