package com.example.filesharetgbot;

import com.example.filesharetgbot.service.KafkaSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import com.example.filesharetgbot.dto.QuestionResponseDto;
import com.example.filesharetgbot.dto.QuestionsItemDto;

import java.io.IOException;
import java.util.List;


@Component
public class FileShareTgBot extends TelegramLongPollingBot {
    private final KafkaSender kafkaSender;

    public FileShareTgBot(KafkaSender kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    @Override
    public String getBotUsername() {
        return "filipkofbot"; // Замените на имя вашего бота
    }

    @Override
    public String getBotToken() {
        return "1225535740:AAFbBqMojlosB9il0O7jnP9bC6GgFt9Yrps"; // Вставьте ваш токен
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(message);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (update.hasMessage() && update.getMessage().hasDocument()) {
            Long chatId = update.getMessage().getChatId();
            String fileId = update.getMessage().getDocument().getFileId();
            InputFile inputFile = new InputFile(fileId);

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(String.valueOf(chatId));
            sendDocument.setDocument(inputFile);

            processJsonFile(fileId, String.valueOf(chatId));
            kafkaSendMessage(fileId, String.valueOf(chatId));

            try {
                execute(sendDocument);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public java.io.File downloadJsonFile(String fileId) {
        try {
            // Получаем файл по fileId
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = execute(getFile);

            // Загружаем файл
            return downloadFile(file.getFilePath());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    public QuestionResponseDto readJsonFile(java.io.File downloadJsonFile) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(downloadJsonFile, QuestionResponseDto.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendJsonContentAsMessage(String chatId, String readJsonFile) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(readJsonFile);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void processJsonFile(String fileId, String chatId) {
        java.io.File jsonFile = downloadJsonFile(fileId);
        if (jsonFile != null) {
            String jsonContent = String.valueOf(readJsonFile(jsonFile));
            sendJsonContentAsMessage(chatId, jsonContent);
        }
    }

    public void kafkaSendMessage(String fileId, String chatId) {
        java.io.File jsonFile = downloadJsonFile(fileId);
        if (jsonFile != null) {
            QuestionResponseDto jsonContent = readJsonFile(jsonFile);
            kafkaSender.sendKafka(jsonContent, "QuestionsFromTg");
        }

    }

}
