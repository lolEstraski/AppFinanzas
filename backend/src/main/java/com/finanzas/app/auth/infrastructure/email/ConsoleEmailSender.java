package com.finanzas.app.auth.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.finanzas.app.auth.application.EmailSender;

@Component
public class ConsoleEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String rawResetToken) {
        log.info("Password reset requested for {}. Reset token: {}", toEmail, rawResetToken);
    }
}
