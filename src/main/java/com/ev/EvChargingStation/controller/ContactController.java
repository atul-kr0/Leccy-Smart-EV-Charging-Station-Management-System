package com.ev.EvChargingStation.controller;

import com.ev.EvChargingStation.dto.contact.ContactRequestDTO;
import com.ev.EvChargingStation.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<String> sendMessage(
            @Valid @RequestBody ContactRequestDTO request) {

        contactService.sendMessage(request);

        return ResponseEntity.ok("Message sent successfully");
    }
}