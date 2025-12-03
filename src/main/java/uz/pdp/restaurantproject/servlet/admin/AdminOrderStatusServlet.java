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

        OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());

        System.out.println("Order newStatus: " + newStatus);
        orderService.updateOrderStatus(orderId, newStatus);

        resp.sendRedirect("/admin/orders");
    }
}
