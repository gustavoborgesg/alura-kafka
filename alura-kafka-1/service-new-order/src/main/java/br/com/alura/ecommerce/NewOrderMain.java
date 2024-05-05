package br.com.alura.ecommerce;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (var orderDispacher = new KafkaDispatcher<Order>()) {
            try (var emailDispacher = new KafkaDispatcher<Email>()) {
                for (var i = 0; i < 10; i++) {

                    var userId = UUID.randomUUID().toString();
                    var orderId = UUID.randomUUID().toString();
                    var amount = BigDecimal.valueOf(Math.random() * 5000 + 1);
                    var order = new Order(userId, orderId, amount);

                    orderDispacher.send("ECOMMERCE_NEW_ORDER", userId, order);

                    Email email = new Email(
                            "Email subject",
                            "Thank you for your order! We are processing your order!"
                    );
                    emailDispacher.send("ECOMMERCE_SEND_EMAIL", userId, email);
                }
            }
        }
    }
}
