package com.example.SpringDemo.Services;

import com.example.SpringDemo.Models.FlashcardPack;
import com.example.SpringDemo.Repositories.FlashcardPackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashcardPackService {

    @Autowired
    private FlashcardPackRepository flashcardPackRepository;

    public FlashcardPackService(FlashcardPackRepository flashcardPackRepository) {
        this.flashcardPackRepository = flashcardPackRepository;
    }

    public List<FlashcardPack> getFlashcardPacksByUserId(Integer userId) {
        return flashcardPackRepository.findByConversation_User_Id(Long.valueOf(userId));
    }

    public List<FlashcardPack> getAllFlashcardPacks() {
        return flashcardPackRepository.findAll();
    }

    public FlashcardPack getFlashcardPackById(Long id) {
        return flashcardPackRepository.findById(id).orElse(null);
    }

    public FlashcardPack saveFlashcardPack(FlashcardPack flashcardPack) {
        return flashcardPackRepository.save(flashcardPack);
    }

    public void deleteFlashcardPack(Long id) {
        flashcardPackRepository.deleteById(id);
    }

    public void markAsCompleted(Long id) {
        FlashcardPack pack = flashcardPackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flashcard pack not found"));
        pack.setCompleted(true);
        flashcardPackRepository.save(pack);
    }
}

