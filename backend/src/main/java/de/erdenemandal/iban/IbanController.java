package de.erdenemandal.iban;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/iban")
public class IbanController {

    private final IbanService ibanService;

    // Constructor injection: Spring provides the service
    public IbanController(IbanService ibanService) {
        this.ibanService = ibanService;
    }

    @PostMapping("/validate")
    public IbanResult validate(@RequestBody IbanRequest request) {
        return ibanService.check(request.iban());
    }
   
    @GetMapping("/validate")
    public IbanResult validateGet(@RequestParam String iban, String accountholder) {
        return ibanService.check(iban);
    }
}