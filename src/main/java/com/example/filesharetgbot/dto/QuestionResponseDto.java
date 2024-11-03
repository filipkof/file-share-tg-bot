package com.example.filesharetgbot.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class QuestionResponseDto {
	List<QuestionsItemDto> questionsItem;
}