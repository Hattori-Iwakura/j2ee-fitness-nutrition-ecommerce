package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.AuthProvider;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.Role;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String emailRaw = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String email = emailRaw != null ? emailRaw.toString().trim().toLowerCase(Locale.ROOT) : null;

        Optional<User> existingUser = email != null ? userRepository.findByEmailIgnoreCase(email) : Optional.empty();

        if (existingUser.isEmpty() && email != null) {
            User newUser = User.builder()
                    .fullName(name)
                    .email(email)
                    .password(null)
                    .role(Role.USER)
                    .authProvider(AuthProvider.GOOGLE)
                    .enabled(true)
                    .build();
            userRepository.save(newUser);
        }

        return oAuth2User;
    }
}
