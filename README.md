# Kafka Testing

Project tests interlok-kafka features

## What it does

This project is very simple and contains two channels with one workflow each.

The first workflow has a polling trigger that produces a message every 10 seconds and publish it to a Kfka topic.

The second workflow is listening on the topic and copy the message on the file system.

```mermaid
graph LR
  subgraph Kafka To FS
    KB2[Kafka Broker] --> K_C(Kafka Consumer)
    K_C --> SC2(Service Collection)
    SC2 --> FS_P(FS Producer)
    FS_P --> FS[File System]
  end
  subgraph To Kafka
    PT_C[Polling Trigger] --> SC1(Service Collection)
    SC1 --> K_P(Kafka Producer)
	K_P --> KB1(Kafka Broker)
  end

  style PT_C fill:#FF6C6C
  style K_C fill:#FF6C6C
  style K_P fill:#6C79FF
  style FS_P fill:#6C79FF
  style SC1 fill: #F89347
  style SC2 fill: #F89347
```


## Getting started

Before starting Interlok you need to create a Kafka docker container with

* `docker-compose up`

Then start Interlok

* `./gradlew clean build`
* `(cd ./build/distribution && java -jar lib/interlok-boot.jar)`

The config is using a variables.properties to configure the Kafka host, topics and the file system directory.

```
kafkaHost=localhost
kafkaPort=9092
kafkaServer=${kafkaHost}:${kafkaPort}
kafkaGroup=interlok_kafka
kafkaTopic=destTopic
fsDir=file://localhost/./messages/in
```
