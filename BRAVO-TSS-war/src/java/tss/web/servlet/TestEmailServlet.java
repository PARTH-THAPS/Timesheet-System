package tss.web.servlet;

/**
 */

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tss.logic.ReminderService;
import java.io.IOException;

@WebServlet("/test-email")
public class TestEmailServlet extends HttpServlet {

    @Inject
    private ReminderService reminderService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        reminderService.sendReminder("example@gmail.com");

        resp.setContentType("text/plain");
        resp.getWriter().write("Email trigger command sent! Check Mail.");
    }
}
