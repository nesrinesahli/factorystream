package com.factorystream.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class SensorProducer implements AutoCloseable {
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final ObjectMapper mapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final AtomicLong sent = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();

    public SensorProducer(String bootstrapServers, String topic) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Wait for the broker to persist each record, and let it drop duplicates caused by retries
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // Batch records for up to 20 ms instead of sending them one by one
        props.put(ProducerConfig.LINGER_MS_CONFIG, 20);

        this.producer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    public void send(SensorEvent event) throws JsonProcessingException {
        String json = mapper.writeValueAsString(event);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.machineId(), json);

        // send() is asynchronous: the callback runs later, once the broker answers
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                sent.incrementAndGet();
            } else {
                failed.incrementAndGet();
                System.err.println("Failed to send event for " + event.machineId() + ": " + exception.getMessage());
            }
        });
    }

    public long sentCount() {
        return sent.get();
    }

    public long failedCount() {
        return failed.get();
    }

    @Override
    public void close() {
        producer.close();   // flushes pending records before closing
    }
}
