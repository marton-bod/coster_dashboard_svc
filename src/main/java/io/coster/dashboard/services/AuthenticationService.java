package io.coster.dashboard.services;

import io.coster.usermanagementsvc.contract.AuthenticationResponse;
import io.coster.usermanagementsvc.contract.ValidationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService {

    @Autowired
    private RestTemplate restTemplate;

    private final String validationUrl;

    public AuthenticationService(@Value("${user.service.host}") String userServiceHost,
                                 @Value("${user.service.port}") int userServicePort) {
        validationUrl = (userServiceHost.startsWith("http://")) ? "" : "http://"
                + userServiceHost + ":" + userServicePort + "/auth/validate";
    }


    public boolean isUserAndTokenValid(String userId, String token) {
        ValidationRequest validationRequest = new ValidationRequest(userId, token);
        ResponseEntity<AuthenticationResponse> response
                = restTemplate.postForEntity(validationUrl, validationRequest, AuthenticationResponse.class);
        return response.getBody() != null && response.getBody().isValid();
    }

}
