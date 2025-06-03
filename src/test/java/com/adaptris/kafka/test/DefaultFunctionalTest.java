package com.adaptris.kafka.test;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ComposeContainer;
import com.adaptris.testing.DockerComposeFunctionalTest;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class DefaultFunctionalTest extends DockerComposeFunctionalTest {

    protected static String INTERLOK_SERVICE_NAME = "interlok-1";
    protected static int INTERLOK_PORT = 8080;
    protected static String KAFKA_SERVICE_NAME = "kafka-1";
    protected static int KAFKA_PORT = 9092;
    protected static String ZOOKEEPER_SERVICE_NAME = "zookeeper-1";
    protected static int ZOOKEEPER_PORT = 2181;

    private static final String TEST_TOPIC = "test-topic";
    private static final String TEST_KEY = "test-key";
    private static final String TEST_VALUE = "test-value";

    protected static WaitStrategy defaultWaitStrategy = Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30));
    @Override
    protected ComposeContainer setupContainers() throws Exception {
        return new ComposeContainer(new File("docker-compose.yml"))
                .withExposedService(INTERLOK_SERVICE_NAME, INTERLOK_PORT, defaultWaitStrategy)
                .withExposedService(KAFKA_SERVICE_NAME, KAFKA_PORT, defaultWaitStrategy)
                .withExposedService(ZOOKEEPER_SERVICE_NAME, ZOOKEEPER_PORT, defaultWaitStrategy);
    }

    @Test
    public void test() throws Exception {
        Thread.sleep(10000);

        InetSocketAddress address = getHostAddressForService(INTERLOK_SERVICE_NAME, INTERLOK_PORT);
        String bootstrapServers = address.getHostString() + ":" + KAFKA_PORT; // Or the external port

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<>(producerProps);
        //prepare the record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TEST_TOPIC, TEST_KEY, TEST_VALUE);
        //Sending message to Kafka Broker
        producer.send(producerRecord);
        producer.flush();

        Thread.sleep(5000);

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        //subscribe to topic
        consumer.subscribe(Collections.singleton(TEST_TOPIC));
        //poll the record from the topic
        ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
        for (ConsumerRecord<String, String> record : consumerRecords) {
            Assert.assertEquals(TEST_KEY, record.key());
            Assert.assertEquals(TEST_VALUE, record.value());
        }

    }
}
