package uz.pdp.restaurantproject.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import uz.pdp.restaurantproject.bot.RestaurantBot;
import uz.pdp.restaurantproject.model.Order;
import uz.pdp.restaurantproject.model.OrderItem;
import uz.pdp.restaurantproject.model.enums.OrderStatus;

public class OrderNotificationService {
    private static OrderNotificationService instance;

    private OrderNotificationService() {}

    public static OrderNotificationService getInstance() {
        if (instance == null) {
            instance = new OrderNotificationService();
        }
        return instance;
    }

    /**
     * Отправляет уведомление клиенту об изменении статуса заказа
     */
    public void notifyStatusChange(Order order, OrderStatus newStatus) {
        String chatId = order.getClient().getChatId();

        System.out.println("Chat id: " + chatId);

        String message = buildNotificationMessage(order, newStatus);


        System.out.println("Message: " + message);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .parseMode("Markdown")
                .build();

        RestaurantBot.getInstance().sendMessage(sendMessage);
    }

    /**
     * Формирует текст уведомления в зависимости от статуса
     */
    private String buildNotificationMessage(Order order, OrderStatus status) {
        StringBuilder message = new StringBuilder();
        String orderNumber = order.getId() != null ? "#" + order.getId() : "";

        System.out.println("Order number: " + orderNumber);

        switch (status) {
            case CONFIRMED -> {
                message.append("✅ *Заказ подтвержден!* ").append(orderNumber).append("\n\n");
                message.append("Ваш заказ принят и передан на кухню.\n");
                message.append("Ожидайте следующее уведомление о начале приготовления.");
            }


            case PREPARING -> {
                System.out.println("Message * " + status.name());
                message.append("👨‍🍳 *Заказ готовится!* ").append(orderNumber).append("\n\n");
                message.append("Наши повара начали готовить ваш заказ.\n");
                message.append("Скоро всё будет готово!");
                System.out.println("Message * " + status.name() + " good!");
            }
            case IN_DELIVERY -> {
                message.append("🚗 *Заказ в пути!* ").append(orderNumber).append("\n\n");
                message.append("Курьер уже везёт ваш заказ.\n");
                message.append("Ожидайте доставку в ближайшее время!");
            }

            case DELIVERED -> {
                message.append("🎉 *Заказ доставлен!* ").append(orderNumber).append("\n\n");
                message.append("Приятного аппетита! 🍽\n");
                message.append("Спасибо, что выбрали Girgitton Express!\n\n");
                message.append("Будем рады видеть вас снова!");
            }

            case CANCELED -> {
                message.append("❌ *Заказ отменён* ").append(orderNumber).append("\n\n");
                message.append("К сожалению, ваш заказ был отменён.\n");
                message.append("Если у вас есть вопросы, свяжитесь с нами.");
            }
        }

        // Добавляем детали заказа
        if (status != OrderStatus.CANCELED) {
            message.append("\n\n*Состав заказа:*\n");
            double total = 0;

            for (OrderItem item : order.getItems()) {
                double linePrice = item.getPrice() * item.getQuantity();
                total += linePrice;
                message.append("• ")
                        .append(item.getQuantity())
                        .append(" × ")
                        .append(item.getFood().getName())
                        .append(" — ")
                        .append(String.format("%.0f", linePrice))
                        .append(" sum\n");
            }
            System.out.println("Msg: " + message);

            message.append("\n*Итого:* ").append(String.format("%.0f", total)).append(" sum");
        }

        return message.toString();
    }

    /**
     * Отправляет уведомление о создании заказа (после checkout)
     */
    public void notifyOrderCreated(Order order) {
        String chatId = order.getClient().getChatId();

        StringBuilder message = new StringBuilder();
        message.append("✨ *Заказ успешно оформлен!*\n\n");
        message.append("Ваш заказ принят и ожидает подтверждения.\n");
        message.append("Мы пришлём вам уведомление, как только заказ будет подтверждён.\n\n");

        message.append("*Детали заказа:*\n");
        double total = 0;

        for (OrderItem item : order.getItems()) {
            double linePrice = item.getPrice() * item.getQuantity();
            total += linePrice;
            message.append("• ")
                    .append(item.getQuantity())
                    .append(" × ")
                    .append(item.getFood().getName())
                    .append(" — ")
                    .append(String.format("%.0f", linePrice))
                    .append(" sum\n");
        }

        message.append("\n*Итого:* ").append(String.format("%.0f", total)).append(" sum");

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message.toString())
                .parseMode("Markdown")
                .build();

        RestaurantBot.getInstance().sendMessage(sendMessage);
    }
}
