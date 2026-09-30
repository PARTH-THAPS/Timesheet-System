package tss.web.filter;

import jakarta.ejb.EJB;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import tss.dto.PersonDTO;
import tss.dto.User;
import tss.logic.PersonLogic;
import tss.logic.UserLogic;

@WebFilter("/views/*")
public class ConsentFilter implements Filter {

    @EJB
    private UserLogic userLogic;

    @EJB
    private PersonLogic personLogic;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest
                = (HttpServletRequest) request;

        HttpServletResponse httpResponse
                = (HttpServletResponse) response;

        /*
         * Not authenticated yet.
         * Container authentication handles this normally.
         */
        if (httpRequest.getUserPrincipal() == null) {
            chain.doFilter(request, response);
            return;
        }

        String requestUri
                = httpRequest.getRequestURI();

        if (requestUri.endsWith("/views/consent.xhtml")
                || requestUri.endsWith("/views/login.xhtml")) {

            chain.doFilter(request, response);
            return;
        }

        User user = userLogic.getCurrentUser();

        if (user == null) {
            chain.doFilter(request, response);
            return;
        }

        PersonDTO person
                = personLogic.findPerson(user.getId());

        if (!person.isConsent()) {

            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                    + "/views/consent.xhtml"
            );

            return;
        }

        chain.doFilter(request, response);
    }
}
