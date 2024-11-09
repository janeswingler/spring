package com.example.SpringDemo.Services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.Base64;

@Service
public class TextToSpeechService {

    private static final String CREDENTIALS_PATH = "src/main/resources/google-cloud-service-account.json";

    public String synthesizeSpeech(String text, String languageCode) {
        try (TextToSpeechClient textToSpeechClient = createTextToSpeechClient()) {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode) // For example, "af-ZA" for Afrikaans
                    .setSsmlGender(SsmlVoiceGender.FEMALE)
                    .build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();

            // Convert audio content to Base64 string for front-end
            return Base64.getEncoder().encodeToString(audioContents.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to synthesize speech: " + e.getMessage(), e);
        }
    }

    private TextToSpeechClient createTextToSpeechClient() throws Exception {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(CREDENTIALS_PATH));
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return TextToSpeechClient.create(settings);
    }
}
