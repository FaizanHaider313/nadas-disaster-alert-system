package com.disaster.alert.service;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.Multipart;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.List;
import java.util.Properties;

/**
 * Sends email notifications to users when a hazard report is approved.
 * Uses Gmail SMTP with App Password.
 * Pattern: Service Layer
 */
public class EmailService {

    private static final String FROM_EMAIL   = "nadas.alerts@gmail.com";
    private static final String APP_PASSWORD = "lmiotlrofphbkkrw";

    /**
     * Sends a disaster alert email to a list of recipients.
     */
    public void sendAlertToUsers(List<String> recipientEmails,
                                 String hazardType,
                                 String location,
                                 String description) {

        if (recipientEmails == null || recipientEmails.isEmpty()) {
            System.out.println("[EmailService] No users to notify.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        // ✅ No emoji in subject — plain text only to avoid ? characters
        String subject = "NADAS ALERT - " + hazardType + " Warning in " + location;
        String body    = buildEmailBody(hazardType, location, description);

        int sent = 0, failed = 0;

        for (String email : recipientEmails) {
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(FROM_EMAIL, "NADAS Alert System"));
                msg.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(email));
                msg.setSubject(subject);
                msg.setContent(body, "text/html; charset=utf-8");
                Transport.send(msg);
                sent++;
                System.out.println("[EmailService] Sent to: " + email);
            } catch (Exception e) {
                failed++;
                System.err.println("[EmailService] Failed for "
                        + email + ": " + e.getMessage());
            }
        }

        System.out.println("[EmailService] Done — sent: "
                + sent + ", failed: " + failed);
    }

    private String buildEmailBody(String hazardType,
                                  String location,
                                  String description) {
        return """
            <html>
            <body style="font-family:Arial,sans-serif;background:#f7fafc;padding:20px;">
              <div style="max-width:600px;margin:auto;background:white;
                          border-radius:12px;padding:30px;
                          border-left:6px solid #e53e3e;">

                <h2 style="color:#e53e3e;margin-top:0;">
                  OFFICIAL DISASTER ALERT | NADAS
                </h2>

                <p style="color:#555;font-size:14px;">
                  An official disaster alert has been issued for your city.
                  Please take necessary precautions immediately.
                </p>

                <table style="width:100%;border-collapse:collapse;
                               margin:20px 0;font-size:14px;">
                  <tr style="background:#fff5f5;">
                    <td style="padding:10px;font-weight:bold;color:#c53030;width:140px;">
                      Hazard Type
                    </td>
                    <td style="padding:10px;color:#333;">
                """ + hazardType + """
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:10px;font-weight:bold;color:#c53030;">
                      Location
                    </td>
                    <td style="padding:10px;color:#333;">
                """ + location + """
                    </td>
                  </tr>
                  <tr style="background:#fff5f5;">
                    <td style="padding:10px;font-weight:bold;color:#c53030;">
                      Details
                    </td>
                    <td style="padding:10px;color:#333;">
                """ + description + """
                    </td>
                  </tr>
                </table>

                <p style="color:#888;font-size:12px;margin-top:20px;">
                  This alert was verified and approved by the NADAS admin team.<br/>
                  Stay safe and follow local emergency guidelines.
                </p>

                <div style="background:#e53e3e;color:white;padding:12px 20px;
                             border-radius:8px;text-align:center;
                             font-weight:bold;margin-top:20px;">
                  NADAS | Natural and Artificial Disaster Alert System
                </div>
              </div>
            </body>
            </html>
            """;
    }

    public void sendAlertToUsers(List<String> recipientEmails,
                                 String hazardType,
                                 String location,
                                 String description,
                                 String photoPath) {  // ADD THIS PARAMETER

        if (recipientEmails == null || recipientEmails.isEmpty()) {
            System.out.println("[EmailService] No users to notify.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        String subject = "NADAS ALERT - " + hazardType + " Warning in " + location;
        String body    = buildEmailBody(hazardType, location, description);

        int sent = 0, failed = 0;

        for (String email : recipientEmails) {
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(FROM_EMAIL, "NADAS Alert System"));
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
                msg.setSubject(subject);

                // Create multipart message
                Multipart multipart = new MimeMultipart();

                // Part 1: HTML body
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(body, "text/html; charset=utf-8");
                multipart.addBodyPart(htmlPart);

                // Part 2: Photo attachment (if exists)
                if (photoPath != null && !photoPath.isEmpty()) {
                    java.io.File photoFile = new java.io.File(photoPath);
                    if (photoFile.exists()) {
                        MimeBodyPart attachPart = new MimeBodyPart();
                        attachPart.attachFile(photoFile);
                        multipart.addBodyPart(attachPart);
                    }
                }

                msg.setContent(multipart);
                Transport.send(msg);
                sent++;
                System.out.println("[EmailService] Sent to: " + email);
            } catch (Exception e) {
                failed++;
                System.err.println("[EmailService] Failed for " + email + ": " + e.getMessage());
            }
        }

        System.out.println("[EmailService] Done — sent: " + sent + ", failed: " + failed);
    }
}