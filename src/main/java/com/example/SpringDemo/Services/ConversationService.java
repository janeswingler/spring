package com.example.SpringDemo.Services;

import com.example.SpringDemo.Models.Conversation;
import com.example.SpringDemo.Models.User;
import com.example.SpringDemo.Repositories.ConversationRepository;
import com.example.SpringDemo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    public Conversation getConversationById(int id) {
        return conversationRepository.findById(id).orElse(null);
    }

    public Conversation saveConversation(Conversation conversation) {
        // Fetch the user based on the provided user ID
        User user = userRepository.findById(conversation.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set the user for the conversation
        conversation.setUser(user);

        // Save and return the conversation
        return conversationRepository.save(conversation);
    }

    public void deleteConversation(int id) {
        conversationRepository.deleteById(id);
    }
}
