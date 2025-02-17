package org.example.service;

public interface VerificationService {
    String generateVerificationCode(String email);
    boolean canSendCode(String email);
    boolean verifyCode(String email, String code);
    void clearCode(String email);
}
