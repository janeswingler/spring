package com.example.SpringDemo.Models;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "flashcards")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_pack_id", nullable = false)
    @JsonIgnore
    private FlashcardPack flashcardPack;

    @Column(nullable = false, length = 100)
    private String term;

    @Column(nullable = false, length = 100)
    private String translation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(name = "example_usage", columnDefinition = "TEXT")
    private String exampleUsage;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FlashcardPack getFlashcardPack() {
        return flashcardPack;
    }

    public void setFlashcardPack(FlashcardPack flashcardPack) {
        this.flashcardPack = flashcardPack;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getExampleUsage() {
        return exampleUsage;
    }

    public void setExampleUsage(String exampleUsage) {
        this.exampleUsage = exampleUsage;
    }
    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
