package com.example.SpringDemo.Controllers;

import com.example.SpringDemo.Models.FlashcardPack;
import com.example.SpringDemo.Services.FlashcardPackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard-packs")
@CrossOrigin(origins = "*")
public class FlashcardPackController {

    private final FlashcardPackService flashcardPackService;

    public FlashcardPackController(FlashcardPackService flashcardPackService) {
        this.flashcardPackService = flashcardPackService;
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> markFlashcardPackAsCompleted(@PathVariable Long id) {
        flashcardPackService.markAsCompleted(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FlashcardPack>> getFlashcardPacksByUser(@PathVariable Long userId) {
        List<FlashcardPack> packs = flashcardPackService.getFlashcardPacksByUserId(Math.toIntExact(userId));
        return ResponseEntity.ok(packs);
    }

    @GetMapping
    public List<FlashcardPack> getAllFlashcardPacks() {
        return flashcardPackService.getAllFlashcardPacks();
    }

    @GetMapping("/{id}")
    public FlashcardPack getFlashcardPackById(@PathVariable Long id) {
        return flashcardPackService.getFlashcardPackById(id);
    }

    @PostMapping
    public FlashcardPack createFlashcardPack(@RequestBody FlashcardPack flashcardPack) {
        return flashcardPackService.saveFlashcardPack(flashcardPack);
    }

    @DeleteMapping("/{id}")
    public void deleteFlashcardPack(@PathVariable Long id) {
        flashcardPackService.deleteFlashcardPack(id);
    }
}
