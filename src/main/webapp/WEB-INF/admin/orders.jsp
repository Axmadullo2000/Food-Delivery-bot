<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Admin Order Control</title>
</head>
<body>
    <h2>
        Admin Order Control
    </h2>

    <c:if test="${not empty orders}">
        <c:forEach var="order" items="${orders}">
            <div class="order">
                <p>Order ID: <c:out value="${order.id}" /></p>
                <p>Status: <c:out value="${order.status}" /></p>
                <p>Total: <c:out value="${order.total}" /> sum</p>

                <c:if test="${not empty order.items}">
                    <ul>
                        <c:forEach var="item" items="${order.items}">
                            <li>
                                <c:out value="${item.food.name}" /> x <c:out value="${item.quantity}" />
                                — <c:out value="${item.price * item.quantity}" /> sum
                            </li>
                            <form action="<%= request.getContextPath() %>/admin/order/status" method="post">
                                <input type="hidden" name="orderId" value="${order.id}" />
                                <input type="hidden" name="status" value="DELIVERED" />
                                <button type="submit">Mark as Delivered</button>
                            </form>
                            ${order.status}
                        </c:forEach>
                    </ul>
                </c:if>
            </div>
            <hr/>
        </c:forEach>
    </c:if>

    <c:if test="${empty orders}">
        <p>No orders found.</p>
    </c:if>




</body>
</html>
