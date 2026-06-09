package com.pfe.itsm.auth.service;

import com.pfe.itsm.auth.config.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    public void sendEmailVerification(String to, String link) {
        send(
                to,
                "Verification de votre adresse email",
                """
                Bonjour,

                Veuillez confirmer votre adresse email en ouvrant le lien suivant :
                %s

                Si vous n'etes pas a l'origine de cette demande, ignorez ce message.
                """.formatted(link)
        );
    }

    public void sendInvitation(String to, String link) {
        send(
                to,
                "Invitation a la plateforme support IT",
                """
                Bonjour,

                Un administrateur vous a invite a rejoindre la plateforme support IT.
                Veuillez finaliser votre compte avec le lien suivant :
                %s

                Si vous n'etes pas concerne par cette invitation, ignorez ce message.
                """.formatted(link)
        );
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.from());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

