package org.example.apigw;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
class WebConfig implements WebMvcConfigurer {

    private String generateJWT(String issuer, String jwtSecret) {
        Instant now = Instant.now();
        Instant expiration = now.plus(72, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer(issuer)
                .withExpiresAt(Date.from(expiration))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        String issuer = "APIGW";
        String secretKey = "89552cfd25b378f191d4dd784b20aacd38697f50c5cc856f97cb15907e9c9f5fb50f42c87135cf7baca4908f9bd1dcbd76881e3181091bbf3a79a2015f998065";
        String jwtToken = generateJWT(issuer, secretKey);
        interceptors.add(new TokenAddingInterceptor(jwtToken));
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
