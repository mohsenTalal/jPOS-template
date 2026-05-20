package com.example.card;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardProvisioningService cardService;

    // Provision a new card
    @PostMapping("/provision")
    public ResponseEntity<?> provisionCard(
            @RequestBody CardProvisionRequest req) {
        try {
            CardAccount card = cardService.provisionCard(req);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get card by ID
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCard(@PathVariable String cardId) {
        CardAccount card = cardService.getCard(cardId);
        if (card == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }

    // Get all cards for a customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CardAccount>> getCustomerCards(
            @PathVariable String customerId) {
        return ResponseEntity.ok(
                cardService.getCustomerCards(customerId));
    }

    // Block a card
    @PutMapping("/{cardId}/block")
    public ResponseEntity<?> blockCard(@PathVariable String cardId) {
        CardAccount card = cardService.blockCard(cardId);
        if (card == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }

    // Activate a card
    @PutMapping("/{cardId}/activate")
    public ResponseEntity<?> activateCard(@PathVariable String cardId) {
        CardAccount card = cardService.activateCard(cardId);
        if (card == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }
}