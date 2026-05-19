package uz.pdp.restaurantproject.servlet.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.pdp.restaurantproject.model.enums.OrderStatus;
import uz.pdp.restaurantproject.service.OrderService;

import java.io.IOException;

@WebServlet("/admin/order/status")
public class AdminOrderStatusServlet extends HttpServlet {
    private OrderService orderService;

    @Override
    public void init() {
        orderService = OrderService.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String orderId = req.getParameter("orderId");
        String statusStr = req.getParameter("newStatus");

        if (orderId == null || orderId.isBlank() || statusStr == null || statusStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "orderId and newStatus are required");
            return;
        }

        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            orderService.updateOrderStatus(orderId, newStatus);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        resp.sendRedirect("/admin/orders");
    }
}
