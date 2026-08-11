package tss.logic;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.*;

@Stateless
public class ReminderService {

    @Resource(lookup = "mail/Mailsession")
    private Session mailSession;

    public void sendReminder(String recipientEmail) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("test@tss-project.local"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("example subject");
            message.setText("Success!");

            Transport.send(message);
            System.out.println("Mail sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}