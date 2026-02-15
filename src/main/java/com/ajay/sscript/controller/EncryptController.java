package com.ajay.sscript.controller;

import com.ajay.sscript.dto.EncryptRequest;
import com.ajay.sscript.dto.EncryptResponse;
import com.ajay.sscript.service.EncryptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/encrypt")
public class EncryptController {

    private final EncryptService encryptService;

    public EncryptController(EncryptService encryptService) {
        this.encryptService = encryptService;
    }

    @PostMapping
    public EncryptResponse encrypt(@RequestBody EncryptRequest request) {

        String encrypted = encryptService.encrypt(request.getText());

        EncryptResponse response = new EncryptResponse();
        response.setOriginalText(request.getText());
        response.setEncryptedText(encrypted);
        response.setKey(request.getKey());

        return response;
    }
}
