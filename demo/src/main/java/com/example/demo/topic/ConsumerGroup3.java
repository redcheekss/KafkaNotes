package com.example.demo.topic;


import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerGroup3 {
    private static final String brokerList="ha01:9092";
    private static final String topic="topic-demo";
    private static final String groupId="group.demo";
    private static final AtomicBoolean isRunning=new AtomicBoolean(true);

    public static Properties initProperties(){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,brokerList);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        return properties;
    }

    public static void main(String[] args) {
        Properties properties = initProperties();

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);

        kafkaConsumer.subscribe(Arrays.asList(topic), new ConsumerRebalanceListener() {

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                System.out.print("再均衡之前该消费者分配的分区");
                for (TopicPartition partition : partitions) {
                    System.out.printf("  %s",partition.partition());
                }
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                System.out.print("再均衡之后该消费者分配的分区");
                if (partitions != null) {
                    for (TopicPartition partition : partitions) {
                        System.out.printf("  %s",partition.partition());
                    }
                }
            }
        });


        Set<TopicPartition> assignment = new HashSet<>();
        do {
            kafkaConsumer.poll(1000);
            assignment = kafkaConsumer.assignment();
        }while (assignment.size()==0);

        System.out.print("再均衡之前该消费者分配的分区");
        for (TopicPartition topicPartition : assignment) {
            System.out.printf("  %s",topicPartition.partition());
        }

        while (isRunning.get()){
            ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);

            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record.value());
            }

        }

    }
}
