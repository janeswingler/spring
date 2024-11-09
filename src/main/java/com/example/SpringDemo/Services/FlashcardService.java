package com.example.SpringDemo.Services;

import com.example.SpringDemo.Models.Flashcard;
import com.example.SpringDemo.Repositories.FlashcardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashcardService {

    @Autowired
    private FlashcardRepository flashcardRepository;

    public List<Flashcard> getFlashcardsByPackId(Long packId) {
        return flashcardRepository.findByFlashcardPack_Id(packId);
    }

    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }

    public Flashcard getFlashcardById(Long id) {
        return flashcardRepository.findById(id).orElse(null);
    }

    public Flashcard saveFlashcard(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);
    }

    public void deleteFlashcard(Long id) {
        flashcardRepository.deleteById(id);
    }
}

