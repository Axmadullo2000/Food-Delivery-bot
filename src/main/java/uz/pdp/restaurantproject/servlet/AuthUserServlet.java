package uz.pdp.restaurantproject.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.pdp.restaurantproject.repository.impl.AuthUserRepository;
import uz.pdp.restaurantproject.service.AuthUserService;

import java.io.IOException;

/**
 * Placeholder servlet kept while the auth user feature is under construction.
 * The {@code doGet} simply confirms the repository wiring works.
 */
@WebServlet("/test")
public class AuthUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthUserRepository authUserRepository = AuthUserService.getInstance().getAuthUserRepository();
        authUserRepository.findAll(null);
        resp.sendRedirect("/food");
    }
}
