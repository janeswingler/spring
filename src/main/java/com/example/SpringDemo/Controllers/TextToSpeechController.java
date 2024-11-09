package com.example.SpringDemo.Controllers;

import com.example.SpringDemo.Services.TextToSpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/text-to-speech")
@CrossOrigin(origins = "*")
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService;

    @Autowired
    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping("/synthesize")
    public ResponseEntity<Map<String, String>> synthesizeSpeech(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String languageCode = request.get("languageCode");
        String audioBase64 = textToSpeechService.synthesizeSpeech(text, languageCode);

        Map<String, String> response = new HashMap<>();
        response.put("audioBase64", audioBase64);
        return ResponseEntity.ok(response);
    }
}

