package uz.pdp.restaurantproject.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.pdp.restaurantproject.model.AuthUser;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.repository.impl.AuthUserRepository;
import uz.pdp.restaurantproject.service.AuthUserService;

import java.io.IOException;
import java.util.List;

@WebServlet("/test")
public class AuthUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthUserRepository authUserRepository = AuthUserService.getInstance().getAuthUserRepository();
        DataDto<List<AuthUser>> authUsers = authUserRepository.findAll(null);
        resp.sendRedirect("/food");
    }
}
