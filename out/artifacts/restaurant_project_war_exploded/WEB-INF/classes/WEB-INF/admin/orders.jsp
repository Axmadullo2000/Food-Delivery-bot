<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Админка — Заказы</title>
    <style>
        body { font-family: Arial; margin: 20px; }
        .order { border: 1px solid #ccc; padding: 15px; margin: 10px 0; border-radius: 8px; background: #f9f9f9; }
        .order.pending { background: #fcf3cf; } /* не доставлено */
        .order.delivered { background: #d5f5e3; } /* доставлено */
        button { margin: 5px; padding: 8px 12px; cursor: pointer; }
        .status { font-weight: bold; color: #d35400; }
    </style>
</head>
<body>
    <h1>Admin Panel - Order Management</h1>
    <a href="${pageContext.request.contextPath}/">На главную</a>

    <c:choose>
        <c:when test="${not empty orders}">
            <h2>Active Orders</h2>
            <c:forEach var="order" items="${orders}">
                <c:if test="${order.status != 'DELIVERED'}">
                    <div class="order pending">
                        <p><strong>Заказ #${order.id}</strong> —
                            <span class="status">${order.status}</span> —
                                ${order.total} sum
                        </p>
                        <p>Client: ${order.client.fullName} | ${order.client.phone}</p>
                        <p>Created: ${order.createdAt}</p>
                        <div>
                            <strong>Товары:</strong>
                            <ul>
                                <c:forEach var="item" items="${order.items}">
                                    <li>${item.quantity} × ${item.food.name} — ${item.price * item.quantity} сум</li>
                                </c:forEach>
                            </ul>
                        </div>

                        <!-- Кнопка смены статуса -->
                        <form action="${pageContext.request.contextPath}/admin/order/status" method="post" style="display:inline;">
                            <input type="hidden" name="orderId" value="${order.id}" />
                            <input type="hidden" name="newStatus" value="${'PREPARING'}" />
                            <button type="submit" style="background:#f1c40f">Preparing</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/admin/order/status" method="post" style="display:inline;">
                            <input type="hidden" name="orderId" value="${order.id}" />
                            <input type="hidden" name="newStatus" value="${'IN_DELIVERY'}" />
                            <button type="submit" style="background:#f1c40f">In delivery</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/admin/order/status" method="post" style="display:inline;">
                            <input type="hidden" name="orderId" value="${order.id}" />
                            <input type="hidden" name="newStatus" value="${'DELIVERED'}" />
                            <button type="submit" style="background:#f1c40f">Delivered</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/admin/order/status" method="post" style="display:inline;">
                            <input type="hidden" name="orderId" value="${order.id}" />
                            <input type="hidden" name="newStatus" value="${'CANCELED'}" />
                            <button type="submit" style="background:#f1c40f">Canceled</button>
                        </form>

                    </div>
                </c:if>
            </c:forEach>

            <h2>Delivered Orders</h2>
            <c:forEach var="order" items="${orders}">
                <c:if test="${order.status == 'DELIVERED'}">
                    <div class="order delivered">
                        <p><strong>Заказ #${order.id}</strong> —
                            <span class="status">${order.status}</span> —
                                ${order.total} sum
                        </p>
                        <p>Client: ${order.client.fullName} | ${order.client.phone}</p>
                        <p>Created: ${order.createdAt}</p>
                        <div>
                            <strong>Товары:</strong>
                            <ul>
                                <c:forEach var="item" items="${order.items}">
                                    <li>${item.quantity} × ${item.food.name} — ${item.price * item.quantity} сум</li>
                                </c:forEach>
                            </ul>
                        </div>

                        <strong>Order delivered</strong>
                    </div>
                </c:if>
            </c:forEach>

        </c:when>
        <c:otherwise>
            <p>No orders yet, you can chill-out!</p>
        </c:otherwise>
    </c:choose>

    <h1>Order History:</h1>

    <c:forEach var="orderItem" items="${orderHistory}">
        <div class="order delivered">
            <p><strong>Заказ #${orderItem.id}</strong> —
                <span class="status">${orderItem.status}</span> —
                    ${orderItem.total} sum
            </p>
            <p>Client: ${orderItem.client.fullName} | ${orderItem.client.phone}</p>
            <p>Created: ${orderItem.createdAt}</p>
            <div>
                <strong>Товары:</strong>
                <ul>
                    <c:forEach var="item" items="${orderItem.items}">
                        <li>${item.quantity} × ${item.food.name} — ${item.price * item.quantity} sum</li>
                    </c:forEach>
                </ul>
            </div>

            <strong>Order delivered</strong>
        </div>
    </c:forEach>

</body>
</html>
