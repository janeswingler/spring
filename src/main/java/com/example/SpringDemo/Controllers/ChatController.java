package com.example.SpringDemo.Controllers;

import com.example.SpringDemo.Models.ChatMessage;
import com.example.SpringDemo.Services.ChatService;
import com.example.SpringDemo.Models.Conversation;
import com.example.SpringDemo.Repositories.ConversationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ConversationRepository conversationRepository;

    public ChatController(ChatService chatService, ConversationRepository conversationRepository) {
        this.chatService = chatService;
        this.conversationRepository = conversationRepository; // Inject the repository
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, Long>> createNewConversation(@RequestParam Long userId) {
        Long conversationId = chatService.createNewConversation(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("conversationId", conversationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversation/{id}/feedback")
    public ResponseEntity<String> getConversationFeedback(@PathVariable Long id) {
        String feedback = chatService.generateReviewSummary(id);
        return ResponseEntity.ok(feedback);
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, Object> payload) {
        Long conversationId = ((Number) payload.get("conversationId")).longValue();
        String messageText = (String) payload.get("messageText");

        // Get the user ID associated with the conversation
        Conversation conversation = conversationRepository.findById(Math.toIntExact(conversationId))
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        Long userId = conversation.getUser().getId();

        // Save the user's message first
        ChatMessage savedUserMessage = chatService.saveMessage(conversationId, messageText, null);


        // Get the AI's response and its translation
        ChatMessage aiMessage = chatService.chatWithOpenAi(messageText, userId);

        // Save the AI's response and translation in the same conversation
        chatService.saveMessage(conversationId, aiMessage.getText(), aiMessage.getTranslation());

        return ResponseEntity.ok(aiMessage);
    }





    ////////BEFORE TRANSLATION TOOLTIP//////////////
//    @PostMapping("/message")
//    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, Object> payload) {
//        Long conversationId = ((Number) payload.get("conversationId")).longValue();
//        String messageText = (String) payload.get("messageText");
//
//        // Get the user ID associated with the conversation
//        Conversation conversation = conversationRepository.findById(Math.toIntExact(conversationId))
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//        Long userId = Long.valueOf(conversation.getUser().getId());
//
//        // Save the user's message first
//        ChatMessage savedUserMessage = chatService.saveMessage(conversationId, messageText);
//
//        // Get the response from the OpenAI model with user details
//        String botResponse = chatService.chatWithOpenAi(messageText, userId);
//
//        // Save the bot's response in the same conversation
//        ChatMessage botMessage = chatService.saveMessage(conversationId, botResponse);
//
//        return ResponseEntity.ok(botMessage);
//    }

////////////////////////////////

//    @PostMapping("/message")
//    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, Object> payload) {
//        Long conversationId = ((Number) payload.get("conversationId")).longValue();
//        String messageText = (String) payload.get("messageText");
//
//        // Save the user's message first
//        ChatMessage savedUserMessage = chatService.saveMessage(conversationId, messageText);
//
//        // Get the response from the OpenAI model
//        String botResponse = chatService.chatWithOpenAi(messageText, "beginner");
//
//        // Save the bot's response in the same conversation
//        ChatMessage botMessage = chatService.saveMessage(conversationId, botResponse);
//
//        return ResponseEntity.ok(botMessage);
//    }

//    @PostMapping("/message")
//    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, Object> payload) {
//        Long conversationId = ((Number) payload.get("conversationId")).longValue();
//        String messageText = (String) payload.get("messageText");
//        ChatMessage savedMessage = chatService.saveMessage(conversationId, messageText);
//        return ResponseEntity.ok(savedMessage);
//    }

    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endConversation(@RequestBody Map<String, Object> payload) {
        long conversationId = ((Number) payload.get("conversationId")).longValue();

        // Fetch the conversation
        var conversation = conversationRepository.findById(Math.toIntExact(conversationId))
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Generate study words
        String recommendedWords = chatService.generateStudyWords(conversationId);

        // Update the conversation with recommended words
        conversation.setRecommendedWords(recommendedWords);
        conversationRepository.save(conversation);

        // Return the words to the frontend
        Map<String, Object> response = new HashMap<>();
        response.put("recommendedWords", recommendedWords.split(","));
        return ResponseEntity.ok(response);
    }



    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long conversationId) {
        List<ChatMessage> chatHistory = chatService.getChatHistory(conversationId);
        return ResponseEntity.ok(chatHistory);
    }

    @PostMapping("/openai")
    public ResponseEntity<String> chatWithOpenAi(@RequestBody Map<String, Object> payload) {
        String userMessage = (String) payload.get("messageText");
        Long userId = ((Number) payload.get("userId")).longValue();

        // Get the response from the chat service using the user's message and ID.
        String response = String.valueOf(chatService.chatWithOpenAi(userMessage, userId));
        return ResponseEntity.ok(response);
    }


//    @PostMapping("/openai")
//    public ResponseEntity<String> chatWithOpenAi(@RequestBody Map<String, String> payload) {
//        String userMessage = payload.get("messageText");
//        String response = chatService.chatWithOpenAi(userMessage, "beginner");
//        return ResponseEntity.ok(response);
//    }
}

