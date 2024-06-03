package br.com.alura.ecommerce;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderServlet extends HttpServlet {

    private final KafkaDispatcher<Order> orderDispacher = new KafkaDispatcher<>();
    private final KafkaDispatcher<Email> emailDispacher = new KafkaDispatcher<>();

    @Override
    public void destroy() {
        super.destroy();
        orderDispacher.close();
        emailDispacher.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // we are not caring about any security issues, we are only
            // showing hot to use http as an entry point
            var email = req.getParameter("email");
            var amount = new BigDecimal(req.getParameter("amount"));

            var orderId = UUID.randomUUID().toString();

            var order = new Order(orderId, amount, email);
            orderDispacher.send(
                    "ECOMMERCE_NEW_ORDER",
                    email,
                    new CorrelationId(NewOrderServlet.class.getSimpleName()),
                    order
            );

            var emailCode = new Email(
                    "Email subject",
                    "Thank you for your order! We are processing your order!"
            );
            emailDispacher.send(
                    "ECOMMERCE_SEND_EMAIL",
                    email,
                    new CorrelationId(NewOrderServlet.class.getSimpleName()),
                    emailCode
            );

            System.out.println("New order sent successfully.");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("New order sent successfully.");
        } catch (ExecutionException | InterruptedException e) {
            throw new ServletException(e);
        }
    }
}
