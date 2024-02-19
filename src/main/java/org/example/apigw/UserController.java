package org.example.apigw;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.TimeUnit;

@RestController
class UserController {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public UserController(RestTemplate restTemplate, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/api/v1/users")
    public ResponseEntity<String> fetchUsers() {
        String cachedUsers = redisTemplate.opsForValue().get("users");
        if (cachedUsers != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(cachedUsers);
        }
        String users = restTemplate.getForObject("http://localhost:8181/api/v1/users", String.class);
        assert users != null;
        redisTemplate.opsForValue().set("users", users, 1, TimeUnit.MINUTES); // Saving with the key "users"
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(users);
    }

    @PostMapping("/api/v1/login")
    public ResponseEntity<String> loginUser(@RequestBody String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "http://localhost:8181/api/v1/users/login",
                requestBody,
                String.class
        );
        String accessToken = responseEntity.getHeaders().getFirst("access_token");

        return ResponseEntity.ok()
                .header("access_token", accessToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(responseEntity.getBody());
    }
}