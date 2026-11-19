package de.erdenemandal.iban;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iban")
public class IbanController {

    private final IbanService ibanService;

    // DI: Spring liefert den Service
    public IbanController(IbanService ibanService) {
        this.ibanService = ibanService;
    }

    @PostMapping("/validate")
    public IbanResult validate(@RequestBody IbanRequest request) {
        return ibanService.check(request.iban());
    }
}