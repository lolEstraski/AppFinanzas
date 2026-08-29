package com.finanzas.app.auth.application;

public interface EmailSender {

    void sendPasswordResetEmail(String toEmail, String rawResetToken);
}
