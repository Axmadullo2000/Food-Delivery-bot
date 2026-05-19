package uz.pdp.restaurantproject.servlet.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.service.OrderService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/orders")
public class AdminOrdersServlet extends HttpServlet {
    private static final OrderService orderService = OrderService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<Boolean, List<Order>> partitioned = orderService.partitionActiveAndHistory();
        req.setAttribute("orders", partitioned.get(Boolean.TRUE));
        req.setAttribute("orderHistory", partitioned.get(Boolean.FALSE));
        req.getRequestDispatcher("/WEB-INF/admin/orders.jsp").forward(req, resp);
    }
}
