package tss.web;

import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import tss.dao.PersonDao;
import tss.dao.TimesheetDao;
import tss.entity.Person;
import tss.entity.Timesheet;
import tss.entity.TimesheetStatus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringJoiner;

@Startup
@Singleton
public class ReminderService {

    @EJB
    private TimesheetDao timesheetDAO;

    @EJB
    private PersonDao personDao;

    @Resource(lookup = "mail/Mailsession")
    private Session mailSession;

    private static final String MESSAGE_BUNDLE = "tss.web.i18n.messages";
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();


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

    //For testing set: hour = "*" minute = "*" second = "0" - it repeats every minute
    @Schedule(hour = "6", minute = "0", persistent = false)
    public void processDailyReminders() {
        System.out.println("Processing daily aggregated timesheet reminders...");
        Map<String, List<Timesheet>> userRemindersMap = new HashMap<>();

        // RE1: Employee Reminders (IN_PROGRESS)
        List<Timesheet> inProgressSheets = timesheetDAO.findPendingRemindersByStatus(TimesheetStatus.IN_PROGRESS);
        for (Timesheet sheet : inProgressSheets) {
            String employeeEmail = sheet.getContract().getEmployee().getEmailAddress();
            userRemindersMap.computeIfAbsent(employeeEmail, k -> new ArrayList<>()).add(sheet);
        }

        // RE2: Supervisor and Assistant Reminders (SIGNED_BY_EMPLOYEE)
        List<Timesheet> employeeSignedSheets = timesheetDAO.findPendingRemindersByStatus(TimesheetStatus.SIGNED_BY_EMPLOYEE);
        if (employeeSignedSheets != null) {
            for (Timesheet sheet : employeeSignedSheets) {
                String supervisorEmail = sheet.getContract().getSupervisor().getEmailAddress();
                userRemindersMap.computeIfAbsent(supervisorEmail, k -> new ArrayList<>()).add(sheet);

                for (Person assistant : sheet.getContract().getAssistants()) {
                    userRemindersMap.computeIfAbsent(assistant.getEmailAddress(), k -> new ArrayList<>()).add(sheet);
                }
            }
        }

        // RE3: Secretary Reminders (SIGNED_BY_SUPERVISOR)
        List<Timesheet> supervisorSignedSheets = timesheetDAO.findPendingRemindersByStatus(TimesheetStatus.SIGNED_BY_SUPERVISOR);
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
            ResourceBundle bundle = resolveBundle(recipientEmail);

            sendReminder(
                    recipientEmail,
                    bundle.getString("reminder.subject"),
                    buildReminderBody(recipientEmail, urgentSheets, bundle)
            );

////15 seconds delay for development testing(MailTrap free version doesn't allow sending all messages at once)
//            try {
//                Thread.sleep(15000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }

        }
    }

    private String buildReminderBody(String recipientEmail, List<Timesheet> urgentSheets, ResourceBundle bundle) {
        StringBuilder body = new StringBuilder();
        body.append(bundle.getString("reminder.greeting")).append("\n\n");
        body.append(MessageFormat.format(bundle.getString("reminder.count"), urgentSheets.size())).append("\n\n");

        StringJoiner timesheetLines = new StringJoiner("\n");
        for (Timesheet sheet : urgentSheets) {
            timesheetLines.add(MessageFormat.format(
                    bundle.getString("reminder.timesheet.line"),
                    sheet.getId(),
                    translateStatus(sheet.getStatus(), bundle),
                    sheet.getStartDate(),
                    sheet.getEndDate()));
        }

        body.append(timesheetLines);
        body.append("\n\n").append(bundle.getString("reminder.review")).append("\n");
        body.append(MessageFormat.format(bundle.getString("reminder.recipient"), recipientEmail)).append("\n");
        return body.toString();
    }

    private ResourceBundle resolveBundle(String recipientEmail) {
        return ResourceBundle.getBundle(MESSAGE_BUNDLE, resolveLocale(recipientEmail));
    }

    private Locale resolveLocale(String recipientEmail) {
        Person person = personDao.getPerson(recipientEmail);

        if (person != null && person.getPreferredLanguage() != null) {
            return Locale.of(person.getPreferredLanguage().name().toLowerCase());
        }

        System.err.println("Could not resolve language for " + recipientEmail + ". Defaulting to server locale.");
        return DEFAULT_LOCALE;
    }

    private String translateStatus(TimesheetStatus status, ResourceBundle bundle) {
        if (status == null) {
            return "";
        }

        String key = "reminder.timesheet.status." + status.name();
        return bundle.containsKey(key) ? bundle.getString(key) : status.name();
    }
}