package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.Role;
import com.wildrep.accountantapp.model.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleEnum roleEnum);
}
