package org.example.service;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
}