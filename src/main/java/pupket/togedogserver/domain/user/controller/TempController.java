package pupket.togedogserver.domain.user.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempController {
    @GetMapping("/favicon.ico")
    public void test() {
    }

    @GetMapping("/health-check")
    public ResponseEntity<Void> healthTestAnser() {
        return ResponseEntity.ok().build();
    }
}
