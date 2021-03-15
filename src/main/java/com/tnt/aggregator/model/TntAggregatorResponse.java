package com.tnt.aggregator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor


@JsonPropertyOrder({
        "pricing",
        "track",
        "shipments"
})
public class TntAggregatorResponse implements Serializable {

    @JsonProperty("pricing")
    private Map<String, Double> pricing;

    @JsonProperty("track")
    private Map<String, String> track;

    @JsonProperty("shipments")
    private Map<String, List<String>> shipments;

}