package com.example.filesharetgbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.util.Objects;

@Component
public class ShareBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;

    public ShareBot() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    @Override
    public String getBotToken() {
        return "7449713514:AAHRM8prAUGN9d5HMDi33GHT--PrBmQRwwo";
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            SendMessage message = SendMessage
                    .builder()
                    .chatId(chatId)
                    .text(message_text)
                    .build();
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (update.hasMessage() && update.getMessage().hasDocument()) {
            String fileId = update.getMessage().getDocument().getFileId();
            Long chat_id = update.getMessage().getChatId();
            String filePath = getFilePath(fileId);
            String fileContent;
            try {
                fileContent = extractFieldFromJson(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            SendMessage message = SendMessage
                    .builder()
                    .chatId(chat_id)
                    .text(fileContent)
                    .build();
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getFilePath(String fileId) {
        Objects.requireNonNull(fileId);

         GetFile getFileMethod = new GetFile(fileId);
            try {
                File doc = telegramClient.execute(getFileMethod);
                return doc.getFilePath();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        return null;
    }

    public java.io.File downloadJsonFile(String filePath) {
        try {
            return telegramClient.downloadFile(filePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String extractFieldFromJson (String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;

        jsonNode = objectMapper.readTree(downloadJsonFile(filePath));

      return jsonNode.path("text").asText("Файл пустой");
    }
}
