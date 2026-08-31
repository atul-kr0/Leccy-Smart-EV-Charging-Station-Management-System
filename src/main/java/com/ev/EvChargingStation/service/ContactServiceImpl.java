package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.contact.ContactRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final RestClient restClient;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${contact.email}")
    private String contactEmail;

    @Override
    public void sendMessage(ContactRequestDTO request) {

        String html = """
                <h2>New Leccy Contact Message</h2>

                <p><strong>Name:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Subject:</strong> %s</p>

                <hr>

                <p><strong>Message:</strong></p>
                <p>%s</p>
                """.formatted(
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );

        Map<String, Object> body = Map.of(
                "from", "Leccy Contact <onboarding@resend.dev>",
                "to", contactEmail,
                "subject", "Leccy Contact: " + request.getSubject(),
                "html", html,
                "reply_to", request.getEmail()
        );

        restClient.post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}