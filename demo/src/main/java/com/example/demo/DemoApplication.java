package com.example.demo;

import com.example.demo.data.Orders;
import com.example.demo.data.Products;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@SpringBootApplication
@Slf4j
public class DemoApplication implements ApplicationRunner {

	final OrderRepository orderRepository;
	final ProductsRepository productsRepository;

	public DemoApplication(OrderRepository orderRepository,
												 ProductsRepository productsRepository) {
		this.orderRepository = orderRepository;
		this.productsRepository = productsRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

	@Override
	public void run(ApplicationArguments args) throws Exception {
//		productsRepository.save(new Products().setCategory("cat1").setName("name1").setPrice(BigDecimal.valueOf(111)));
//
//		orderRepository.save(new Orders().setClient("client1").setNumber("Number1").setPrice(BigDecimal.valueOf(100L)));
//		orderRepository.save(new Orders().setClient("client1").setNumber("Number1").setPrice(BigDecimal.valueOf(200)));
//		orderRepository.save(new Orders().setClient("client2").setNumber("Number1").setPrice(BigDecimal.valueOf(300)));
//		orderRepository.save(new Orders().setClient("client3").setNumber("Number1").setPrice(BigDecimal.valueOf(400)));
//
//		orderRepository.findAll().forEach(System.out::println);
//		productsRepository.findAll().forEach(System.out::println);
//
//		log.info(orderRepository.getOneOrder().toString());
//
//		log.info("Orders: \n" + orderRepository.findAllByClientAndNumber("client1", "Number1").toString());
	}
}
