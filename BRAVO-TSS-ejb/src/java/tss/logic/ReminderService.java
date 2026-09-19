package tss.logic;

import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import tss.dao.TimesheetDao;
import tss.entity.Person;
import tss.entity.Timesheet;
import tss.entity.TimesheetStatus;

import java.util.*;

@Startup
@Singleton
@Stateless
public class ReminderService {

    @EJB
    private TimesheetDao timesheetDAO;

    @Resource(lookup = "mail/Mailsession")
    private Session mailSession;

    private static final String MESSAGE_BUNDLE = "tss.web.i18n.messages";


    public void sendReminder(String recipientEmail, String subject, String body) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("test@tss-project.local"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Mail sent successfully!");
        } catch (MessagingException e) {
            System.err.println("Failed to send reminder mail: " + e.getMessage());
        }
    }

    @Schedule(hour = "6", minute = "5", persistent = false)
    public void processDailyReminders() {
        System.out.println("Processing daily aggregated timesheet reminders...");
        Map<String, List<Timesheet>> userRemindersMap = new HashMap<>();

        List<Timesheet> inProgressSheets = timesheetDAO.findInProgressOnLastDay();
        for (Timesheet sheet : inProgressSheets) {
            String employeeEmail = sheet.getContract().getEmployee().getEmailAddress();
            userRemindersMap.computeIfAbsent(employeeEmail, k -> new ArrayList<>()).add(sheet);
        }

        List<Timesheet> employeeSignedSheets = timesheetDAO.findByStatusOnLastDay(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        if (employeeSignedSheets != null) {
            for (Timesheet sheet : employeeSignedSheets) {
                String supervisorEmail = sheet.getContract().getSupervisor().getEmailAddress();
                userRemindersMap.computeIfAbsent(supervisorEmail, k -> new ArrayList<>()).add(sheet);

                for (Person assistant : sheet.getContract().getAssistants()) {
                    userRemindersMap.computeIfAbsent(assistant.getEmailAddress(), k -> new ArrayList<>()).add(sheet);
                }
            }
        }

        List<Timesheet> supervisorSignedSheets = timesheetDAO.findByStatusOnLastDay(TimesheetStatus.SIGNED_BY_SUPERVISOR);
        if (supervisorSignedSheets != null) {
            for (Timesheet sheet : supervisorSignedSheets) {
                for (Person secretary : sheet.getContract().getSecretaries()) {
                    userRemindersMap.computeIfAbsent(secretary.getEmailAddress(), k -> new ArrayList<>()).add(sheet);
                }
            }
        }

        for (Map.Entry<String, List<Timesheet>> entry : userRemindersMap.entrySet()) {
            String recipientEmail = entry.getKey();
            List<Timesheet> urgentSheets = entry.getValue();

            sendReminder(
                    recipientEmail,
                    "Timesheet reminder",
                    buildReminderBody(recipientEmail, urgentSheets)
            );
        }
    }

    private String buildReminderBody(String recipientEmail, List<Timesheet> urgentSheets) {
        StringBuilder body = new StringBuilder();
        //ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, lb.getUserLocale());
        body.append("Hello,\n\n");
        body.append("You have ").append(urgentSheets.size()).append(" timesheet reminder(s).\n\n");

        StringJoiner timesheetLines = new StringJoiner("\n");
        for (Timesheet sheet : urgentSheets) {
            timesheetLines.add("- Timesheet #" + sheet.getId()
                    + " | status: " + sheet.getStatus()
                    + " | start: " + sheet.getStartDate()
                    + " | end: " + sheet.getEndDate());
        }

        body.append(timesheetLines);
        body.append("\n\nPlease review the listed timesheets.\n");
        body.append("Recipient: ").append(recipientEmail).append("\n");
        return body.toString();
    }
}