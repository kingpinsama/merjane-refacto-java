package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.SetupDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;

// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.*;

// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SetupDatabase
@SpringBootTest
@AutoConfigureMockMvc
class MyControllerIntegrationTests {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        @Test
        public void processOrderShouldReturn() throws Exception {
                List<Product> allProducts = createProducts();
                Set<Product> orderItems = new HashSet<Product>(allProducts);
                Order order = createOrder(orderItems);
                productRepository.saveAll(allProducts);
                order = orderRepository.save(order);
                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                                .andExpect(status().isOk());
                Order resultOrder = orderRepository.findById(order.getId()).get();
                assertEquals(resultOrder.getId(), order.getId());
        }

    @Test
    public void processOrderShouldHandleFlashSaleProductsCorrectly() throws Exception {
        // Arrange
        List<Product> allProducts = createProducts();
        allProducts.get(0).setFlashSaleQuantity(10);// Make the first product a flash sale product.
        Set<Product> orderItems = new HashSet<Product>(allProducts);
        Order order = createOrder(orderItems);
        productRepository.saveAll(allProducts);
        order = orderRepository.save(order);

        // Act
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // Assert
        Product product = productRepository.findById(allProducts.get(0).getId()).get();
        assertEquals("FLASHSALE", product.getType());
        assertEquals(Optional.of(9), Optional.of(product.getFlashSaleQuantity()));
        assertEquals(Optional.of(29), Optional.of(product.getAvailable()));
    }

    @Test
    public void processOrderShouldHandleExpirableProductsCorrectly() throws Exception {
        // Arrange
        List<Product> allProducts = createProducts();
        allProducts.get(1).setAvailable(10);
        Set<Product> orderItems = new HashSet<Product>(allProducts);
        Order order = createOrder(orderItems);
        productRepository.saveAll(allProducts);
        order = orderRepository.save(order);

        // Act
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // Assert
        Product product = productRepository.findById(allProducts.get(1).getId()).get();
        assertEquals(Optional.of(9), Optional.of(product.getAvailable()));
    }
        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }

        private static List<Product> createProducts() {
                List<Product> products = new ArrayList<>();

                products.add(new Product(null, 15, 30, "FLASHSALE", "Flashable Dummy", null, null, null, LocalDate.now().minusDays(300),
                    LocalDate.now().plusDays(500), 10));


                products.add(new Product(null, 15, 30, "EXPIRABLE", "Butter", LocalDate.now().plusDays(26), null,
                    null, null, null, null));

                                products.add(new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null, null, null, null));
                products.add(new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null, null, null, null));
                                products.add(new Product(null, 90, 6, "EXPIRABLE", "Milk", LocalDate.now().minusDays(2), null, null, null, null, null));
                products.add(new Product(null, 15, 30, "SEASONAL", "Watermelon", null, LocalDate.now().minusDays(2),
                                LocalDate.now().plusDays(58), null, null, null));
                products.add(new Product(null, 15, 30, "SEASONAL", "Grapes", null, LocalDate.now().plusDays(180),
                                LocalDate.now().plusDays(240), null, null, null));

            return products;

        }
}
