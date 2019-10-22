package com.example.demo.controller;

import com.example.demo.data.Orders;
import com.example.demo.repository.OrderRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

  private final OrderRepository orderRepository;

  @Autowired
  RestTemplate restTemplate;

  //url -> http://localhost:8282/status
  @Value("${status.url}")
  String statusUrl;

  ExecutorService executor = Executors.newSingleThreadExecutor();

  public OrderController(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @RequestMapping(method = RequestMethod.GET)
  public Iterable<Orders> getAll() {
    return orderRepository.findAll();
  }


  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public Orders getOrder(@PathVariable("id") Long id) {
    return orderRepository.findById(id)
            .orElseThrow(
                    () -> new EntityNotFoundException("Order not found")
            );
  }

  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public Orders insertOrder(@RequestBody Orders orders) {
    Orders persistedOrder = orderRepository.save(orders);

    executor.execute(() -> {
      log.info("Send order for verification: {}", orders);

      try {
        TimeUnit.SECONDS.sleep(new Random().nextInt(10));
      } catch (InterruptedException e) {
        log.error(e.getMessage());
        Thread.currentThread().interrupt();
      }

      ResponseEntity<StatusResponse> status = restTemplate.postForEntity(statusUrl,
              new HttpEntity<>(new StatusRequest().setClient(orders.getClient()).setPrice(orders.getPrice())),
              StatusResponse.class);

      log.info("Order verified with status: {}", Objects.requireNonNull(status.getBody()).getStatus());

      orderRepository.save(persistedOrder.setStatus(status.getBody().getStatus()));
    });

    return persistedOrder;
  }

  @RequestMapping(method = RequestMethod.PUT)
  public Orders updateOrder(@RequestBody Orders orders) {
    return orderRepository.save(orders);
  }

  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteOrder(@PathVariable("id") Long id) {
    orderRepository.deleteById(id);
  }

  @Data
  @NoArgsConstructor
  @Accessors(chain = true)
  static class StatusRequest {
    private String client;
    private BigDecimal price;
  }

  @Data
  @NoArgsConstructor
  @Accessors(chain = true)
  public static class StatusResponse {
    private String status;
  }

}
