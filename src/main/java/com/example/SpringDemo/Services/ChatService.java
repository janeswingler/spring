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

//added static

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

//        String prompt = "Provide an array of 5 words for the student to study that are relevant to this conversation. Following each word, provide a brief definition and an example usage.";
        String prompt = "Provide an array of 5 words for the student to study that are relevant to this conversation. For each word, please provide it in the following format:\n\n[Number]. [Term] ([Translation]) - Definition: [Definition] Example: [Example Usage] ([Translation])";

        // Use the AI assistant to generate the words
        String response = assistant.chat(prompt);

        // Create a flashcard pack and associate it with the conversation
        FlashcardPack flashcardPack = new FlashcardPack();
        flashcardPack.setConversation(conversation);
        flashcardPack.setName("Flashcards from Conversation " + conversationId);
        flashcardPackRepository.save(flashcardPack);

        // Parse the response and create flashcards
        List<Flashcard> flashcards = parseAndCreateFlashcards(response, flashcardPack);

        // Save flashcards to the repository
        flashcardRepository.saveAll(flashcards);

        // Optionally, update the conversation with the recommended words
        conversation.setRecommendedWords(response);
        conversationRepository.save(conversation);

        return response;
    }



    private List<Flashcard> parseAndCreateFlashcards(String response, FlashcardPack flashcardPack) {
        List<Flashcard> flashcards = new ArrayList<>();

        // Adjusted regex pattern to match all entries in the response
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
            flashcard.setFlashcardPack(flashcardPack); // Associate with flashcard pack
            flashcards.add(flashcard);
        }

        return flashcards;
    }


    public String generateReviewSummary(Long conversationId) {
        // Retrieve the conversation content
        var conversation = conversationRepository.findById(Math.toIntExact(conversationId))
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        String conversationContent = conversation.getConversationContent();

        // Use the assistant to generate a simple feedback summary
        String prompt = "Provide a short feedback summary for the student based on the following conversation. " +
                "Identify areas for improvement and any new vocabulary introduced. Here is the conversation content:\n" +
                conversationContent;

        return assistant.chat(prompt);
    }



    //before adding flashcards WORKS AS OF 10/18 12:40pm
//    public String generateStudyWords(Long conversationId) {
//        var conversation = conversationRepository.findById(Math.toIntExact(conversationId))
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        String conversationContent = conversation.getConversationContent();
//        String prompt = "Provide an array of 5 words for the student to study that are relevant to this conversation. Following each word, provide a brief definition and and an example usage";
//
//        // Use the AI assistant to generate the words
//        String response = assistant.chat(prompt);
//
//        // Assuming the response is a comma-separated string of words like "word1,word2,word3,word4,word5"
//        return response;
//    }


    public List<ChatMessage> getChatHistory(Long conversationId) {
        return chatMessageRepository.findByConversationId(conversationId);
    }

    public ChatMessage chatWithOpenAi(String userMessage, Long userId) {
        // Fetch user details for proficiency and language
        User user = userRepository.findById((long) Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        String proficiencyLevel = user.getProficiencyLevel();
        String language = user.getLanguagesToLearn();

        // Create the initial AI prompt for generating a response in the target language
        String prompt = "You are a " + language + " tutor for a " + proficiencyLevel +
                " student. Respond in simple " + language +
                " that a " + proficiencyLevel + " student can understand. Here is the student's message: \"" + userMessage + "\"";

        // Generate the AI's response
        String aiResponse = assistant.chat(prompt);

        // Now ask the AI to translate its response into English
        String translationPrompt = "Translate the following " + language + " text into English: \"" + aiResponse + "\"";
        String translatedResponse = assistant.chat(translationPrompt);

        // Create a ChatMessage object with both the original AI response and its translation
        ChatMessage aiMessage = new ChatMessage("Language Teacher", aiResponse, translatedResponse, new Date());

        return aiMessage;
    }



    //////BEFORE TOOLTIP TRANSLATION////
//    public String chatWithOpenAi(String userMessage, Long userId) {
//        var user = userRepository.findById(Math.toIntExact(userId))
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Log user details for debugging
//        System.out.println("User ID: " + userId);
//        System.out.println("Languages to Learn: " + user.getLanguagesToLearn());
//        System.out.println("Proficiency Level: " + user.getProficiencyLevel());
//
//        String language = user.getLanguagesToLearn();
//        String proficiencyLevel = user.getProficiencyLevel();
//
//        // Construct the prompt
//        String prompt = "You are a " + language + " tutor for a " + proficiencyLevel +
//                " student. Respond in simple " + language +
//                " that a " + proficiencyLevel + " student can understand. Here is the student's message: \"" + userMessage + "\"";
//
//        System.out.println("Prompt: " + prompt);
//
//        // Send the prompt to the assistant
//        String response = assistant.chat(prompt);
//
//        System.out.println("AI Response: " + response);
//
//        return response;
//    }
    ///////////////////////////

//    public String chatWithOpenAi(String userMessage, Long userId) {
//        // Retrieve user details based on userId
//        var user = userRepository.findById(Math.toIntExact(userId))
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        String language = user.getLanguagesToLearn(); // E.g., "Spanish", "Afrikaans", "French"
//        String proficiencyLevel = user.getProficiencyLevel(); // E.g., "Beginner", "Intermediate", "Advanced"
//
//        String prompt = "You are a " + language + " tutor for a " + proficiencyLevel +
//                " student. Respond in simple " + language +
//                " that a " + proficiencyLevel + " student can understand. Here is the student's message: \"" + userMessage + "\"";
//
//        return assistant.chat(prompt);
//    }


    // Made the following changes:
    // - added proficiencyLevel field
//    public String chatWithOpenAi(String userMessage, String proficiencyLevel) {
//
//        String prompt = "You are a Spanish tutor for a " + proficiencyLevel +
//                " student. Respond in simple Spanish that a " + proficiencyLevel +
//                " student can understand. Here is the student's message: \"" + userMessage + "\"";
//
//
//        return assistant.chat(prompt);
//    }

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
