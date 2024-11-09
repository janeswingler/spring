package com.example.SpringDemo.Models;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference//////////////////////////////////
    private User user;

    @Column(name = "conversation_content", columnDefinition = "TEXT", nullable = false)
    private String conversationContent;

    @Column(name = "recommended_study_topics", columnDefinition = "TEXT")
    private String recommendedStudyTopics;

    @Column(name = "recommended_words", columnDefinition = "TEXT")
    private String recommendedWords;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FlashcardPack> flashcardPacks;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ChatMessage> chatMessages;


    public Conversation() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters for all fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getConversationContent() {
        return conversationContent;
    }

    public void setConversationContent(String conversationContent) {
        this.conversationContent = conversationContent;
    }

    public String getRecommendedStudyTopics() {
        return recommendedStudyTopics;
    }

    public void setRecommendedStudyTopics(String recommendedStudyTopics) {
        this.recommendedStudyTopics = recommendedStudyTopics;
    }

    public String getRecommendedWords() {
        return recommendedWords;
    }

    public void setRecommendedWords(String recommendedWords) {
        this.recommendedWords = recommendedWords;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}

