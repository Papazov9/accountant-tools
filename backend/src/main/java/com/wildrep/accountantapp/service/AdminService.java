package com.wildrep.accountantapp.service;

import com.wildrep.accountantapp.exceptions.InvalidUserException;
import com.wildrep.accountantapp.exceptions.RoleDoesNotExist;
import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
import com.wildrep.accountantapp.model.Role;
import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.dto.ActivationCodeRequest;
import com.wildrep.accountantapp.model.dto.ValidationCodeRequest;
import com.wildrep.accountantapp.model.enums.RoleEnum;
import com.wildrep.accountantapp.repo.RoleRepository;
import com.wildrep.accountantapp.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Transactional
    public void generateAdminVerificationCode(ActivationCodeRequest request) {
        User user = validateUser(request.username());

        if (user != null) {
            String activationCode = generateActivationCode();
            user.setCodeExpirationDate(LocalDateTime.now().plusHours(1));
            user.setConfirmationCode(activationCode);

            String emailContent = "<p>Hello,</p>"
                    + "<p>This is the admin activation code :</p>"
                    + "<h3>"
                    + user.getConfirmationCode()
                    + "</h3>"
                    + "<p>The code will expire in 1 hour. If you didn't register, please ignore this email.</p>";

            this.emailService.sendEmail(user.getEmail(), "Admin activation Code", emailContent, null);
            this.userRepository.saveAndFlush(user);
            return;
        }

        throw new InvalidUserException("This user has no permission to access this endpoint!");
    }

    @Transactional
    public void validateCode(ValidationCodeRequest validationCodeRequest) {
        User user = validateUser(validationCodeRequest.username());

        if (user != null) {
            String confirmationCode = user.getConfirmationCode();

            if (confirmationCode.equals(validationCodeRequest.validationCode())) {
                user.setCodeExpirationDate(null);
                user.setConfirmationCode(null);
                this.userRepository.saveAndFlush(user);
                return;
            } else {
                throw new IllegalArgumentException("Invalid activation code passed!");
            }
        }
        throw new InvalidUserException("This user has no permission to access this endpoint!");
    }

    private String generateActivationCode() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    private User validateUser(String username) {
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UserDoesNotExistException(username));
        Role role = this.roleRepository.findByRoleName(RoleEnum.ADMIN).orElseThrow(() -> new RoleDoesNotExist(RoleEnum.ADMIN.name()));

        return user.getRoles().contains(role) ? user : null;
    }
}
