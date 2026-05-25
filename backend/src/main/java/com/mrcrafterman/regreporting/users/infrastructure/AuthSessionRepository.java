package com.mrcrafterman.regreporting.users.infrastructure;

import com.mrcrafterman.regreporting.users.domain.AuthSession;
import com.mrcrafterman.regreporting.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByIdAndRevokedAtIsNull(UUID id);

    Optional<AuthSession> findByUserAndRevokedAtIsNull(User user);

}
