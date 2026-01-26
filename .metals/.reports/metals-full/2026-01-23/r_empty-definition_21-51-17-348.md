error id: file://<WORKSPACE>/services/auth-service/src/main/java/com/matchsentinel/auth/controller/AuthController.java:_empty_/RegisterRequest#
file://<WORKSPACE>/services/auth-service/src/main/java/com/matchsentinel/auth/controller/AuthController.java
empty definition using pc, found symbol in pc: _empty_/RegisterRequest#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 419
uri: file://<WORKSPACE>/services/auth-service/src/main/java/com/matchsentinel/auth/controller/AuthController.java
text:
```scala
package com.matchsentinel.auth.controller;

import com.matchsentinel.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRe@@quest request
    ) {
        return ResponseEntity.ok("Registration request received");
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RegisterRequest#