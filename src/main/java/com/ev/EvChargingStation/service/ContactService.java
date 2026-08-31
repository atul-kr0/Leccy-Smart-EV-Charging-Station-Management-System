package com.ev.EvChargingStation.service;

import com.ev.EvChargingStation.dto.contact.ContactRequestDTO;

public interface ContactService {

    void sendMessage(ContactRequestDTO request);
}