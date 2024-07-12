package pupket.togedogserver.domain.user.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempController {
    @GetMapping("/favicon.ico")
    public String test() {
        return null;
    }
}
