package com.conel.market.service.email;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    @Value("${app.email.sender-name}")
    private String senderName;

    /**
     * Sends an HTML email verification message to the user.
     * Used during registration to confirm email ownership.
     *
     * @param toEmail The recipient's email address
     * @param firstName User's first name (for personalization)
     * @param verificationUrl The link user clicks to verify email
     * @throws BusinessException if email sending fails
     */
    public void sendVerificationEmail(String toEmail, String firstName, String verificationUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from, senderName);
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Bazaar Account Email");

            // Build HTML email template
            String htmlContent = buildVerificationEmailHtml(firstName, verificationUrl);
            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(message);

            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage(), e);
            throw new BusinessException(ErrorCode.ERR_SENDING_ACTIVATION_EMAIL);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds the HTML content for verification emails.
     * Professional, branded template with CTA button.
     */
    private String buildVerificationEmailHtml(String firstName, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 0;
                        }
                        .email-container {
                            max-width: 600px;
                            margin: 20px auto;
                            background-color: #ffffff;
                            border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 700;
                        }
                        .content {
                            padding: 40px 30px;
                            line-height: 1.6;
                            color: #333333;
                        }
                        .greeting {
                            font-size: 18px;
                            margin-bottom: 20px;
                            font-weight: 600;
                        }
                        .message {
                            font-size: 14px;
                            margin-bottom: 30px;
                            color: #555555;
                        }
                        .cta-button {
                            display: inline-block;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white !important;
                            padding: 14px 32px;
                            text-decoration: none;
                            border-radius: 6px;
                            font-weight: 600;
                            font-size: 16px;
                            margin: 20px 0;
                            text-align: center;
                            cursor: pointer;
                            transition: opacity 0.3s ease;
                        }
                        .cta-button:hover {
                            opacity: 0.9;
                        }
                        .fallback-link {
                            margin-top: 30px;
                            padding-top: 20px;
                            border-top: 1px solid #e5e5e5;
                            font-size: 12px;
                            color: #999999;
                        }
                        .fallback-link a {
                            color: #667eea;
                            text-decoration: underline;
                        }
                        .footer {
                            background-color: #f9f9f9;
                            padding: 20px 30px;
                            text-align: center;
                            font-size: 12px;
                            color: #888888;
                            border-top: 1px solid #e5e5e5;
                        }
                        .security-note {
                            background-color: #e8f4f8;
                            padding: 12px 15px;
                            border-left: 4px solid #0088cc;
                            margin-top: 20px;
                            font-size: 13px;
                            color: #0056b3;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <h1>🛒 Bazaar</h1>
                            <p style="margin: 10px 0 0 0; opacity: 0.9;">Welcome to our marketplace</p>
                        </div>
                        <div class="content">
                            <div class="greeting">Hi """ + firstName + """,</div>
                            <div class="message">
                                Thank you for signing up with Bazaar! To complete your registration and unlock full access to our platform, please verify your email address by clicking the button below.
                            </div>
                            <a href=\"""" + verificationUrl + """\" class="cta-button">Verify Email Address</a>
                            <div class="fallback-link">
                                If the button above doesn't work, copy and paste this link into your browser:<br>
                                <a href=\"""" + verificationUrl + """\" style="word-break: break-all;">""" + verificationUrl + """</a>
                            </div>
                            <div class="security-note">
                                <strong>⚠️ Security Note:</strong> This link will expire in 24 hours. If you didn't create this account, please ignore this email.
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2024 Bazaar. All rights reserved. | <a href="#" style="color: #888;">Privacy Policy</a> | <a href="#" style="color: #888;">Terms of Service</a></p>
                            <p>This is an automated message. Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
