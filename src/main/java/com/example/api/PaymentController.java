package com.example.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final JposClientService jposClientService;

    public PaymentController(JposClientService jposClientService) {
        this.jposClientService = jposClientService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<?> authorize(@RequestBody PaymentAuthorizeRequest request) {
        try {
            PaymentAuthorizeResponse response = jposClientService.authorize(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    "Failed to process payment authorization: " + e.getMessage()
            );
        }
    }
}