package dev.lgbonillar.regreporting.users.infrastructure;

import dev.lgbonillar.regreporting.users.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

}
