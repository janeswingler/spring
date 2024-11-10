package com.example.SpringDemo.Repositories;
import com.example.SpringDemo.Models.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByFlashcardPack_Id(Long packId);
}
