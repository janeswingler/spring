package com.example.SpringDemo.Controllers;

import com.example.SpringDemo.Models.Conversation;
import com.example.SpringDemo.Services.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<Conversation> getAllConversations() {
        return conversationService.getAllConversations();
    }

    @GetMapping("/{id}")
    public Conversation getConversationById(@PathVariable int id) {
        return conversationService.getConversationById(id);
    }

    @PostMapping
    public Conversation createConversation(@RequestBody Conversation conversation) {
        return conversationService.saveConversation(conversation);
    }

    @DeleteMapping("/{id}")
    public void deleteConversation(@PathVariable int id) {
        conversationService.deleteConversation(id);
    }
}
