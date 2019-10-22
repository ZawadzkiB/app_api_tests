package com.example.status;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@SpringBootApplication
@Slf4j
@RestController("/status")
public class StatusApplication {

  public static void main(String[] args) {
    SpringApplication.run(StatusApplication.class, args);
  }

  @RequestMapping(method = RequestMethod.POST)
  public StatusResponse verify(@RequestBody StatusRequest statusRequest) {
    log.info("verifying request {}", statusRequest);
    if (statusRequest.client.equalsIgnoreCase("janusz")) {
      return new StatusResponse().setStatus("rejected");
    }
    else if(statusRequest.price.compareTo(BigDecimal.ZERO)==0) {
      return new StatusResponse().setStatus("undefined");
    }
    else {
      return new StatusResponse().setStatus("accepted");
    }
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
  static class StatusResponse {
    private String status;
  }

}
