package com.tnt.aggregator.requestqueue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiConsumer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestQueueElement<T> {

    private String key;
    private BiConsumer<Map<String, T>, Object> aggregateConsumer;
    private Object lock;
    private LocalDateTime entryDate;

}
