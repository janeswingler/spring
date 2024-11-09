package com.example.SpringDemo.Repositories;

import com.example.SpringDemo.Models.FlashcardPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardPackRepository extends JpaRepository<FlashcardPack, Long> {
//ADDED
    List<FlashcardPack> findByConversation_User_Id(Long conversation_user_id);
}
