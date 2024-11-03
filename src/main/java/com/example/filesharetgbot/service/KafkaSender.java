package com.example.filesharetgbot.service;

import com.example.filesharetgbot.dto.QuestionResponseDto;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaSender {

    private final KafkaTemplate <String, QuestionResponseDto> kafkaTemplate;

    @Autowired
    KafkaSender(KafkaTemplate<String, QuestionResponseDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendKafka(QuestionResponseDto questionResponseDto, String topicName) {kafkaTemplate.send(topicName, questionResponseDto);}
}
