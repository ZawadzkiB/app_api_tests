package com.example.demo.controller;

import com.example.demo.data.Orders;
import com.example.demo.repository.OrderRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  OrderRepository orderRepository;

  WireMockServer wireMockServer;

  @BeforeEach
  void setup () {
    wireMockServer = new WireMockServer(8282);
    wireMockServer.start();
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
    wireMockServer.resetMappings();
  }

  @Test
  void getAll() {
    Orders order = orderRepository.save(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)));

    List<Orders> ordersList = Arrays.asList(
            given()
              .port(port)
              .basePath("/order")
            .when()
              .get()
            .then()
              .statusCode(200)
              .extract().as(Orders[].class));

    Orders orderFromBody = ordersList.stream().filter(o -> o.getId().equals(order.getId())).findFirst().orElseGet(Orders::new);

    assertThat(orderFromBody).isEqualToIgnoringGivenFields(order,"price");
    assertThat(orderFromBody.getPrice()).isEqualByComparingTo(order.getPrice());
  }

  @Test
  void deleteOne() {
    Orders order = orderRepository.save(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)));

    List<Orders> ordersList = Arrays.asList(spec()
            .get()
            .then()
            .statusCode(200)
            .extract().as(Orders[].class));

    assertThat(ordersList.stream().anyMatch(o -> o.getId().equals(order.getId()))).isTrue();

    given()
            .port(port)
            .basePath("/order")
            .pathParam("id", order.getId())
            .when()
            .delete("/{id}")
            .then()
            .statusCode(204);

    assertThat(orderRepository.findById(order.getId())).isNotPresent();
  }

  @Test
  void insertOne() {
    Orders order = given()
              .port(port)
              .basePath("/order")
              .contentType(ContentType.JSON)
              .body(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)))
            .when()
              .post()
            .then()
              .statusCode(201)
            .extract().as(Orders.class);

    Optional<Orders> ordersOptional = orderRepository.findById(order.getId());

    assertThat(ordersOptional).isPresent();
    assertThat(ordersOptional.get()).isEqualToIgnoringGivenFields(order,"price");
    assertThat(ordersOptional.get().getPrice()).isEqualByComparingTo(order.getPrice());
  }

  @Test
  void whenPutOrderShouldReturn200AndUpdatedBody() {
    Orders orderRequest = orderRepository.save(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)));

    Orders orderResponse = given()
              .port(port)
              .basePath("/order")
              .contentType(ContentType.JSON)
              .body(orderRequest
                      .setNumber("NumberUpdated")
              )
            .when()
              .put()
            .then()
              .statusCode(200)
              .extract().as(Orders.class);

    assertThat(orderRequest).isEqualToIgnoringGivenFields(orderResponse,"price");
    assertThat(orderRequest.getPrice()).isEqualByComparingTo(orderResponse.getPrice());
  }

  @Test
  void insertOneWithoutContentType() {
    given().log().all()
            .port(port)
            .basePath("/order")
            .body(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)))
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(415);
  }

  @Test
  void insertOneWithBadBody() {
    given().log().all()
            .port(port)
            .basePath("/order")
            .contentType(ContentType.JSON)
            .body("{}")
            .when()
            .post()
            .then()
            .log().all()
            .statusCode(400);
  }


  @Test
  void insertRejected() {

    wireMockServer.stubFor(
            post(urlEqualTo("/status")).withRequestBody(equalToIgnoreCase("{\"client\":\"janusz\",\"price\":100}"))
                    .willReturn(
                            aResponse().withBody("{\"status\":\"rejected\"}").withStatus(200).withHeader("content-type","application/json")
                    )
    );

    Orders order = spec()
            .contentType(ContentType.JSON)
            .body(new Orders().setClient("janusz").setNumber("Number").setPrice(BigDecimal.valueOf(100L)))
            .post()
            .then()
            .statusCode(201)
            .extract().as(Orders.class);

    await().atMost(12, SECONDS).untilAsserted(
            () -> {
              Optional<Orders> ordersOptional = orderRepository.findById(order.getId());
              assertThat(ordersOptional.get()).isEqualToIgnoringGivenFields(order,"price", "status");
              assertThat(ordersOptional.get().getStatus()).isEqualTo("rejected");
              assertThat(ordersOptional.get().getPrice()).isEqualByComparingTo(order.getPrice());
            }
    );
  }

  @Test
  void insertAccepted() throws InterruptedException {

    wireMockServer.stubFor(
            post(urlEqualTo("/status"))
                    .willReturn(
                            aResponse().withBody("{\"status\":\"accepted\"}").withStatus(200).withHeader("content-type","application/json")
                    )
    );

    Orders order = spec()
            .contentType(ContentType.JSON)
            .body(new Orders().setClient("client").setNumber("Number").setPrice(BigDecimal.valueOf(100L)))
            .post()
            .then()
            .statusCode(201)
            .extract().as(Orders.class);

    Optional<Orders> ordersOptional = orderRepository.findById(order.getId());

    assertThat(ordersOptional).isPresent();

    Thread.sleep(12000);

    ordersOptional = orderRepository.findById(order.getId());
    assertThat(ordersOptional.get()).isEqualToIgnoringGivenFields(order,"price", "status");
    assertThat(ordersOptional.get().getStatus()).isEqualTo("accepted");
    assertThat(ordersOptional.get().getPrice()).isEqualByComparingTo(order.getPrice());

  }

  private RequestSpecification spec(){
    return given()
            .port(port)
            .basePath("/order")
            .when();
  }

}