package com.example.SpringDemo.Models;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "flashcard_packs")
public class FlashcardPack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private Conversation conversation;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "flashcardPack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Flashcard> flashcards = new HashSet<>();

    @Column(nullable = false)
    private boolean completed = false;

    public FlashcardPack() {
        this.createdAt = LocalDateTime.now();
        this.flashcards = new HashSet<>();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Flashcard> getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(Set<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    public void addFlashcard(Flashcard flashcard) {
        this.flashcards.add(flashcard);
    }

    public void removeFlashcard(Flashcard flashcard) {
        this.flashcards.remove(flashcard);
    }

    public void clearFlashcards() {
        this.flashcards.clear();
    }




}
