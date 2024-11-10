package com.example.SpringDemo.Services;

import com.example.SpringDemo.Models.*;
import com.example.SpringDemo.Repositories.*;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Service
public class ChatService {

    //ADDED
    @Autowired
    private FlashcardPackRepository flashcardPackRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    private Assistant assistant;
    private StreamingAssistant streamingAssistant;

    interface Assistant {
        String chat(String message);
    }

    interface StreamingAssistant {
        TokenStream chat(String message);
    }

    @PostConstruct
    public void init() {
        var memory = TokenWindowChatMemory.withMaxTokens(2000, new OpenAiTokenizer("gpt-3.5-turbo"));

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(OpenAiChatModel.withApiKey(OPENAI_API_KEY))
                .chatMemory(memory)
                .build();

        streamingAssistant = AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(OpenAiStreamingChatModel.withApiKey(OPENAI_API_KEY))
                .chatMemory(memory)
                .build();
    }

    public Long createNewConversation(Long userId) {
        Conversation conversation = new Conversation();
        conversation.setUser(userRepository.findById((long) Math.toIntExact(userId)).orElseThrow(() -> new RuntimeException("User not found")));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setConversationContent("Start of the conversation.");
        return conversationRepository.save(conversation).getId();
    }



    public ChatMessage saveMessage(Long conversationId, String messageText, String translation) {
        Conversation conversation = conversationRepository.findById(Math.toIntExact(conversationId))
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        ChatMessage message = new ChatMessage("user", messageText, translation, new Date());
        message.setConversation(conversation);
        return chatMessageRepository.save(message);
    }


    public String generateStudyWords(Long conversationId) {
        var conversation = conversationRepository.findById(Math.toIntExact(conversationId))
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        String prompt = "Provide an array of 5 words for the student to study that are relevant to this conversation. For each word, please provide it in the following format:\n\n[Number]. [Term] ([Translation]) - Definition: [Definition] Example: [Example Usage] ([Translation])";

        String response = assistant.chat(prompt);

        FlashcardPack flashcardPack = new FlashcardPack();
        flashcardPack.setConversation(conversation);
        flashcardPack.setName("Flashcards from Conversation " + conversationId);
        flashcardPackRepository.save(flashcardPack);

        List<Flashcard> flashcards = parseAndCreateFlashcards(response, flashcardPack);

        flashcardRepository.saveAll(flashcards);

        conversation.setRecommendedWords(response);
        conversationRepository.save(conversation);

        return response;
    }



    private List<Flashcard> parseAndCreateFlashcards(String response, FlashcardPack flashcardPack) {
        List<Flashcard> flashcards = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(\\d+)\\.\\s*(.*?)\\s*(?:\\((.*?)\\))?\\s*-\\s*Definition:\\s*(.*?)\\s*Example:\\s*(.*?)(?=\\n\\d+\\.|$)",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            String number = matcher.group(1).trim();
            String term = matcher.group(2).trim();
            String translation = matcher.group(3) != null ? matcher.group(3).trim() : "";
            String definition = matcher.group(4).trim();
            String exampleUsage = matcher.group(5).trim();

            Flashcard flashcard = new Flashcard();
            flashcard.setTerm(term);
            flashcard.setTranslation(translation);
            flashcard.setDefinition(definition);
            flashcard.setExampleUsage(exampleUsage);
            flashcard.setFlashcardPack(flashcardPack);
            flashcards.add(flashcard);
        }

        return flashcards;
    }


    public String generateReviewSummary(Long conversationId) {

        var conversation = conversationRepository.findById(Math.toIntExact(conversationId))
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        String conversationContent = conversation.getConversationContent();

        String prompt = "Provide a short feedback summary for the student based on the following conversation. " +
                "Identify areas for improvement and any new vocabulary introduced. Here is the conversation content:\n" +
                conversationContent;

        return assistant.chat(prompt);
    }

    public List<ChatMessage> getChatHistory(Long conversationId) {
        return chatMessageRepository.findByConversationId(conversationId);
    }

    public ChatMessage chatWithOpenAi(String userMessage, Long userId) {

        User user = userRepository.findById((long) Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        String proficiencyLevel = user.getProficiencyLevel();
        String language = user.getLanguagesToLearn();

        String prompt = "You are a " + language + " tutor for a " + proficiencyLevel +
                " student. Respond in simple " + language +
                " that a " + proficiencyLevel + " student can understand. Here is the student's message: \"" + userMessage + "\"";

        String aiResponse = assistant.chat(prompt);

        String translationPrompt = "Translate the following " + language + " text into English: \"" + aiResponse + "\"";
        String translatedResponse = assistant.chat(translationPrompt);

        ChatMessage aiMessage = new ChatMessage("Language Teacher", aiResponse, translatedResponse, new Date());

        return aiMessage;
    }

    public Flux<String> chatStream(String message) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        streamingAssistant.chat(message)
                .onNext(sink::tryEmitNext)
                .onComplete(c -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }
}
