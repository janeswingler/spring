package com.example.SpringDemo.Repositories;

import com.example.SpringDemo.Models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    // Custom queries can be added here
}
