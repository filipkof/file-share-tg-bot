package pojos;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@NoArgsConstructor
@Data
public class QuestionResponse {
	List<QuestionsItem> questionsItem;
}