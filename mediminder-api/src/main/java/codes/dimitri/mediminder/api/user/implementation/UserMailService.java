package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.UserMailFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
class UserMailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final UserProperties properties;

    public void sendVerificationMail(UserEntity entity) {
        String content = getVerificationMailContent(entity);
        try {
            mailSender.send(createMessage(entity, "Welcome to Mediminder", content));
        } catch (MessagingException | MailException ex) {
            throw new UserMailFailedException("Could not send e-mail to verify user with ID '" + entity.getId() + "'", ex);
        }
    }

    public void sendPasswordResetMail(UserEntity entity) {
        String content = getPasswordResetMailContent(entity);
        try {
            mailSender.send(createMessage(entity, "Password reset", content));
        } catch (MessagingException | MailException ex) {
            throw new UserMailFailedException("Could not send e-mail to reset password for user with ID '" + entity.getId() + "'", ex);
        }
    }

    private MimeMessage createMessage(UserEntity entity, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
        message.setText(content, true);
        message.setSubject(subject);
        message.setFrom(properties.noreplyAddress());
        message.setTo(entity.getEmail());
        return mimeMessage;
    }

    private String getVerificationMailContent(UserEntity entity) {
        Context context = new Context();
        String verificationUrl = String.format(properties.verificationUrl(), entity.getVerificationCode());
        context.setVariable("applicationUrl", verificationUrl);
        context.setVariable("name", entity.getName());
        return templateEngine.process("user-verify", context);
    }

    private String getPasswordResetMailContent(UserEntity entity) {
        Context context = new Context();
        String verificationUrl = String.format(properties.passwordResetUrl(), entity.getPasswordResetCode());
        context.setVariable("applicationUrl", verificationUrl);
        return templateEngine.process("user-password-reset", context);
    }
}
