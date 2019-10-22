package com.example.demo.pact;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.example.demo.data.Orders;
import com.example.demo.repository.OrderRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "StatusVerifier", port = "8282")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatusContractTest {

  @Autowired
  OrderRepository orderRepository;

  @LocalServerPort
  private int port;

  @Pact(provider = "StatusVerifier", consumer = "OrderApp")
  public RequestResponsePact createPact(PactDslWithProvider builder) {
    return builder
            .given("accepted state")
            .uponReceiving("Order which should be accepted")
            .path("/status")
            .method("POST")
            .body(new PactDslJsonBody().asBody()
                    .stringValue("client", "client")
                    .numberValue("price", 100))
            .willRespondWith()
            .status(200)
            .body(new PactDslJsonBody().asBody()
                    .stringValue("status", "accepted"))
            .toPact();
  }

  @Test
  void insertAccepted() {

    Orders order = given()
            .port(port)
            .basePath("/order")
            .when()
            .contentType(ContentType.JSON)
            .body(new Orders().setClient("client").setNumber("Number").setPrice(BigDecimal.valueOf(100L)))
            .post()
            .then()
            .statusCode(201)
            .extract().as(Orders.class);

    await().atMost(30, SECONDS).with().pollInterval(1L, SECONDS).untilAsserted(
            () -> {
              Optional<Orders> ordersOptional = orderRepository.findById(order.getId());
              assertThat(ordersOptional.get()).isEqualToIgnoringGivenFields(order, "price", "status");
              assertThat(ordersOptional.get().getStatus()).isEqualTo("accepted");
              assertThat(ordersOptional.get().getPrice()).isEqualByComparingTo(order.getPrice());
            });

  }

}

