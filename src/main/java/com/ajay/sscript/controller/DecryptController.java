package com.ajay.sscript.controller;

import com.ajay.sscript.dto.DecryptRequest;
import com.ajay.sscript.dto.DecryptResponse;
import com.ajay.sscript.service.DecryptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/decrypt")
public class DecryptController {

    private final DecryptService decryptService;

    public DecryptController(DecryptService decryptService) {
        this.decryptService = decryptService;
    }

    @PostMapping
    public DecryptResponse decrypt(@RequestBody DecryptRequest request) {
        String decrypted = decryptService.decrypt(request.getEncryptedText());

        DecryptResponse response = new DecryptResponse();
        response.setEncryptedText(request.getEncryptedText());
        response.setDecryptedText(decrypted);
        response.setKey(request.getKey());

        return response;
    }
}
