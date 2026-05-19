package uz.pdp.restaurantproject.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import uz.pdp.restaurantproject.bot.RestaurantBot;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.enums.OrderStatus;

public final class OrderNotificationService {
    private static final OrderNotificationService INSTANCE = new OrderNotificationService();

    private OrderNotificationService() {}

    public static OrderNotificationService getInstance() {
        return INSTANCE;
    }

    /** Sends a status-change notification to the client. */
    public void notifyStatusChange(Order order, OrderStatus newStatus) {
        send(order.getClient().getChatId(), buildNotificationMessage(order, newStatus));
    }

    /** Sends a confirmation message right after checkout. */
    public void notifyOrderCreated(Order order) {
        StringBuilder message = new StringBuilder()
                .append("✨ *Заказ успешно оформлен!*\n\n")
                .append("Ваш заказ принят и ожидает подтверждения.\n")
                .append("Мы пришлём вам уведомление, как только заказ будет подтверждён.\n\n")
                .append("*Детали заказа:*\n");
        appendItemsAndTotal(message, order);
        send(order.getClient().getChatId(), message.toString());
    }

    private static String buildNotificationMessage(Order order, OrderStatus status) {
        StringBuilder message = new StringBuilder();
        String orderNumber = order.getId() != null ? "#" + order.getId() : "";

        switch (status) {
            case CONFIRMED -> message
                    .append("✅ *Заказ подтвержден!* ").append(orderNumber).append("\n\n")
                    .append("Ваш заказ принят и передан на кухню.\n")
                    .append("Ожидайте следующее уведомление о начале приготовления.");
            case PREPARING -> message
                    .append("👨‍🍳 *Заказ готовится!* ").append(orderNumber).append("\n\n")
                    .append("Наши повара начали готовить ваш заказ.\n")
                    .append("Скоро всё будет готово!");
            case IN_DELIVERY -> message
                    .append("🚗 *Заказ в пути!* ").append(orderNumber).append("\n\n")
                    .append("Курьер уже везёт ваш заказ.\n")
                    .append("Ожидайте доставку в ближайшее время!");
            case DELIVERED -> message
                    .append("🎉 *Заказ доставлен!* ").append(orderNumber).append("\n\n")
                    .append("Приятного аппетита! 🍽\n")
                    .append("Спасибо, что выбрали Girgitton Express!\n\n")
                    .append("Будем рады видеть вас снова!");
            case CANCELED -> message
                    .append("❌ *Заказ отменён* ").append(orderNumber).append("\n\n")
                    .append("К сожалению, ваш заказ был отменён.\n")
                    .append("Если у вас есть вопросы, свяжитесь с нами.");
            default -> { /* CART / CREATED don't trigger notifications. */ }
        }

        if (status != OrderStatus.CANCELED && status != OrderStatus.CART) {
            message.append("\n\n*Состав заказа:*\n");
            appendItemsAndTotal(message, order);
        }
        return message.toString();
    }

    private static void appendItemsAndTotal(StringBuilder sb, Order order) {
        double total = 0;
        for (OrderItem item : order.getItems()) {
            double linePrice = item.getPrice() * item.getQuantity();
            total += linePrice;
            sb.append("• ")
                    .append(item.getQuantity()).append(" × ")
                    .append(item.getFood().getName()).append(" — ")
                    .append(String.format("%.0f", linePrice)).append(" sum\n");
        }
        sb.append("\n*Итого:* ").append(String.format("%.0f", total)).append(" sum");
    }

    private static void send(String chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .build();
        RestaurantBot.getInstance().sendMessage(sendMessage);
    }
}
