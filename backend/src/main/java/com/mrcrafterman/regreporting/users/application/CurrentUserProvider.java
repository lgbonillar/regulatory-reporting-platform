package com.mrcrafterman.regreporting.users.application;

import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.infrastructure.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        return userRepository.findByUsername("analyst01")
                .orElseThrow(() -> new IllegalStateException(
                        "Current development user not found"
                ));
    }

    public User getCurrentAdministrator() {
        return userRepository.findByUsername("admin01")
                .orElseThrow(() -> new IllegalStateException(
                        "Current development administrator not found"
                ));
    }

}
