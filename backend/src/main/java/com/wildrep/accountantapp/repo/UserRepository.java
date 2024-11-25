package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.enums.RoleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(@NotBlank @Size(min = 4, max = 20) String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByStripeCustomerId(String customerId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    List<User> findByRole(RoleEnum roleName);
}
