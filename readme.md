
## Setup

Go to https://start.spring.io/

and select such options

![alt t](img/init1.png)

for dependencies select

![alt t](img/init2.png)

and press generate.

Import new project in IntelliJ.

Setup lombok in IntelliJ
https://projectlombok.org/setup/intellij
remember to enable annotation processing (ctrl+shift+a and search for annotation processing)

Create new instance of postgres db

`docker run -p 54321:5432 -e POSTGRES_USER=app postgres`


## DB Connection

Inside project in resources you should have
application.properties file

setup properties for db connection like that

    spring.flyway.enabled=false

    spring.jpa.hibernate.ddl-auto=create-drop
    spring.jpa.show-sql=true
    spring.jpa.database=postgresql

    spring.datasource.url=jdbc:postgresql://localhost:54321/app
    spring.datasource.username=app
    spring.datasource.password=app
    spring.datasource.driver-class-name=org.postgresql.Driver

![alt t](img/db1.png)

## Entity class

JPA/Hibernate and Spring data are using annotations to tell which objects are db entities

lets add a Orders table.
Create new package `data` and add there Orders.class

```
@Entity
@Data
@Accessors(chain = true)
public class Orders {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Long id;
  private String number;
  private String client;
  private BigDecimal price;
  private String status = "not verified";

}
```

run program and check if Orders table was created.

## Repositories

Lets add now a repository.
Create a new package `repository` and create there a OrderRepository interface.

```
public interface OrderRepository extends CrudRepository<Orders, Long> {}
```

it should inherit CrudRepository where first we put our entity object and as second param we use id type.

## Using repository

How to use repository ?

Modify DemoApplication file to look like this
```

@SpringBootApplication
@Slf4j
public class DemoApplication implements ApplicationRunner {

	final OrderRepository orderRepository;

	public DemoApplication(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String client = "Client";
		orderRepository.save(new Orders().setClient(client).setNumber("Number").setPrice(BigDecimal.valueOf(100L)));
		orderRepository.save(new Orders().setClient(client).setNumber("Number1").setPrice(BigDecimal.valueOf(100L)));
	}
}

```

this code:

```
    final OrderRepository orderRepository;

    public DemoApplication(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
    }
```

is our dependency injection is something that spring is doing for us.
Spring create OrderRepository object and inject it in our DemoApplication class so we can use it here.

this method 
`@Override public void run(ApplicationArguments args) throws Exception `
is an ApplicationRunner method it is running when application starts.

Inside we are using our `orderRepository`.
In orderRepository we don't have any methods but CrudRepository have some, you can check that and we are using one of them `.save()` this method updates or insert new object into db. 

Run program and check if Orders table is not empty.

## Exercise 1

Add new entity object Products and repository for it.
it should have id, name, category, description, price fields.

Then using application runner add few products to db.

## Custom queries

We can add some repository methods by ourself.
Some of them will be handled by spring and in some we have to specify our queries.

```
public interface OrderRepository extends CrudRepository<Orders, Long> {

  Optional<Orders> findByClientAndAndNumber(String client, String number);

  @Query("select o from Orders o")
  List<Orders> findAllCustom();
}
```

this one `Optional<Orders> findByClientAndAndNumber(String client, String number);`
is handled by spring you can notice that during writing it you can use autocomplete.
Method should have same names that fields in our entity objects.

another one 

```
@Query("select o from Orders o")
  List<Orders> findAllCustom();
```

we have to write our own query, this query is not plain sql it is JPQL

https://www.tutorialspoint.com/jpa/jpa_jpql.htm

https://www.baeldung.com/spring-data-jpa-query

## Exercise 2

Write one spring query for products table
and one custom query.

Find products by category and price.
Find all products where price is higher than xxx.

Use them in application runner to test them.

# REST vs HTTP

REST is a way how HTTP should be used.

???

## API

How to create api endpoints.

Create controller package and add there OrdersController.class

```
@RestController
@RequestMapping("/order")
public class OrderController {

  private final OrderRepository orderRepository;

  public OrderController(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @RequestMapping(method = RequestMethod.GET)
  public Iterable<Orders> getAll(){
    return orderRepository.findAll();
  }

  @RequestMapping(path = "/{id}",method = RequestMethod.GET)
  public Orders getOrder(@PathVariable("id") Long id) {
    return orderRepository.findById(id).orElseThrow();
  }

  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public Orders insertOrder(@RequestBody Orders orders) {
    return orderRepository.save(orders);
  }

  @RequestMapping(method = RequestMethod.PUT)
  public Orders updateOrder(@RequestBody Orders orders) {
    return orderRepository.save(orders);
  }

  @RequestMapping(path = "/{id}",method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteOrder(@PathVariable("id") Long id) {
    orderRepository.deleteById(id);
  }

}
```
`@RestController` - tell spring that this is our controller bean/class

`@RequestMapping("/order")` - path to our endpoints

`@RequestMapping(path = "/{id}",method = RequestMethod.GET)` - {id} path param, method is which http method will be mapped to that code

`@ResponseStatus(HttpStatus.NO_CONTENT)` - what response status should be returned

## Swagger

To enable swagger we have to add those two dependencies

```
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>
```

and add a configuration class for spring

```
@Configuration
@EnableSwagger2
public class SwaggerConfig {                                    
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.any())              
          .paths(PathSelectors.any())                          
          .build();                                           
    }
}
```

run app and open
http://localhost:8080/swagger-ui.html

you should have view similar to this:
![alt t](img/swagger1.png)

## Exercise 3

Create same CRUD api for Product and check if swagger is updated.

## RestAssured + AssertJ

Add rest assured and assertJ dependencies
```
    <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured-all</artifactId>
            <version>4.1.1</version>
            <scope>test</scope>
    </dependency>

		<dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>4.1.1</version>
            <scope>test</scope>
		</dependency>

    <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.11.1</version>
            <scope>test</scope>
    </dependency>

	  <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.5.2</version>
		</dependency>
```
Lets now write test for getting products

```
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  OrderRepository orderRepository;

  @Test
  void getAll() {
    Orders order = orderRepository.save(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)));

    List<Orders> ordersList = Arrays.asList(given()
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

}
```

this is how delete test should look like

```
@Test
  void deleteOne() {
    Orders order = orderRepository.save(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)));

    List<Orders> ordersList = Arrays.asList(given()
            .port(port)
            .basePath("/order")
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract().as(Orders[].class));

    assertThat(ordersList.stream().anyMatch(o -> o.getId().equals(order.getId()))).isTrue();

    given().port(port)
            .basePath("/order")
            .when()
            .pathParam("id", order.getId())
            .delete("/{id}")
            .then()
            .statusCode(204);

    assertThat(orderRepository.findById(order.getId())).isEmpty();
  }
```

and here test for inserting

```
@Test
  void insertOne() {
    Orders order = given().port(port)
            .basePath("/order")
            .when()
            .contentType(ContentType.JSON)
            .body(new Orders().setClient("clientTest1").setNumber("Number").setPrice(BigDecimal.valueOf(100L)))
            .post()
            .then()
            .statusCode(201)
            .extract().as(Orders.class);

    Optional<Orders> ordersOptional = orderRepository.findById(order.getId());

    assertThat(ordersOptional).isNotEmpty();
    assertThat(ordersOptional.get()).isEqualToIgnoringGivenFields(order,"price");
    assertThat(ordersOptional.get().getPrice()).isEqualByComparingTo(order.getPrice());
  }
```

Look that everything before get(),post() or any other http method in rest assured returns `RequestSpecification` object
so this is something that can be extracted, some base configuration for endpoint and later reused in some tests.

```
private RequestSpecification spec(){
    return given()
            .port(port)
            .basePath("/order")
            .when();
  }
```

## Exercise 4

Write rest assured tests for orders update and products endpoints

## Async

What types of async communication we can have ??


Lets run our order verification system.
`java -jar wiremock-standalone-2.25.1.jar --port 8282 -v`

check mappings folders and json inside.

We also have to modify our app to send status verification request.

We will use for that purpose rest template.
First we have to create a RestTemplate bean() in our Application class.

```
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
```

Then we can inject it into our controller class.

```
  @Autowired
  RestTemplate restTemplate;
```

and to make async call we have to do it in separate thread so lets create new thread pool.
```
ExecutorService executor = Executors.newSingleThreadExecutor();
```

then we can modify our api insert method

```
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

      ResponseEntity<StatusResponse> status = restTemplate.postForEntity("http://localhost:8282/status",
                new HttpEntity<>(new StatusRequest().setClient(orders.getClient()).setPrice(orders.getPrice())),
                        StatusResponse.class);

        log.info("Order verified with status: {}", Objects.requireNonNull(status.getBody()).getStatus());

        orderRepository.save(persistedOrder.setStatus(status.getBody().getStatus()));
    });

    return persistedOrder;
  }
```

and we have add two inner classes for our status request and body handling

```
  @Data
  @NoArgsConstructor
  @Accessors(chain = true)
  static class StatusRequest {
    private String client;
    private BigDecimal price;
  }

  @Data
  @NoArgsConstructor
  static class StatusResponse {
    private String status;
  }
```

## Exercise 5

Write tests for checking if order was rejected or accepted.

## Awaitility

https://github.com/awaitility/awaitility

```
<dependency>
      <groupId>org.awaitility</groupId>
      <artifactId>awaitility</artifactId>
      <version>4.0.1</version>
      <scope>test</scope>
</dependency>
```

using awaitility with assertJ
https://github.com/awaitility/awaitility/wiki/Usage#using-assertj-or-fest-assert 


## Exercise 6

Apply awaitility to your tests


## Wiremock

http://wiremock.org/

we can also use it inside tests, don't have to run standalone.

```
    <dependency>
        <groupId>com.github.tomakehurst</groupId>
        <artifactId>wiremock</artifactId>
        <version>2.24.1</version>
    </dependency>
```

we can now add a wiremock server to our tests

```
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
```

in before each we are creating new server and after each we are cleaning na stopping our server.

Inside test we can add our mappings.
For rejected test it will look like this:

```
  @Test
  void insertRejected() {

    wireMockServer.stubFor(
            post(urlEqualTo("/status")).withRequestBody(equalToIgnoreCase("{\"client\":\"janusz\",\"price\":100}"))
                    .willReturn(
                            aResponse().withBody("{\"status\":\"rejected\"}").withStatus(200).withHeader("content-type","application/json")
                    )
    );

    ...
```

## Exercise 7

Write stub mappings for accepted test.
Write another test where we will receive a "undefined" status from our order verification system.
We will be receiving that status when price is 0.00.

## Pact Contracts

https://docs.pact.io/

Mocks can sometimes lead to green tests but errors in production.
Solution for that is Consumer driven contracts or provider driven contracts.

### Consumer part

in our oder app pom.xml we have to override kotlin version
```
        <properties>
          ...
          <kotlin.version>1.3.50</kotlin.version>
        </properties>
```

and add pact dependency

```
    <dependency>
      <groupId>au.com.dius</groupId>
      <artifactId>pact-jvm-consumer-junit5</artifactId>
      <version>4.0.1</version>
    </dependency>
```

and add a test for accepted status

```
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "StatusVerifier", port = "8282")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatusContractTest {

  @Autowired
  OrderRepository orderRepository;
  
  @Autowired
  RestTemplate restTemplate;
  
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
```

this part with @Pact is our mock something similar to wiremock stubFor(), we are describing there who is consumer and who is provider of that contract.

we have to extends our test with:
```
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "StatusVerifier", port = "8282")
```

@PactTestFor run a mock server which will be containing our @Pact base on provider name. By default it runs on localhost
and here we set also on which port it should run.

@Test part looks like normal test which we had before.

After running test and if test will pass there will be pact file generated.

You can find it in `target/pacts` location

This file should be shared with provider to make sure it
will works also on his side.

### Pact sharing

There are different approaches for sharing pacts. It can be stored to some folder, it can shared on some artifacts repositories together wit jar files but there is also something called `pact broker`

There is saas solution of pact broker https://pactflow.io/ 

but we will set it up locally.
Check docker-compose file and you will see there two images, one for pact broker and second for db for it.
Just run `docker-compose up -d` and go to `localhost:9292`

In order to share pacts from consumer side we have to add maven plugin to it.

```
            <plugin>
                <groupId>au.com.dius</groupId>
                <artifactId>pact-jvm-provider-maven</artifactId>
                <version>4.0.0</version>
                <configuration>
                    <pactBrokerUrl>http://localhost:9292/</pactBrokerUrl>
                </configuration>
            </plugin>
```

we have our pact broker open so no auth needed but all can be configured in `<configuration>` block.

When we have our pact generated in target folder then we can share them using
`mvn pact:publish` command.

Then you should see new pact on `localhost:9292`

### Provider

To test provider we have to prepare real app for status verification, not wiremock stubs.
So let's create that fast.

pom.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.9.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example.status</groupId>
    <artifactId>status</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>11</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured-all</artifactId>
            <version>4.1.1</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>4.1.1</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.11.1</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.5.2</version>
        </dependency>

        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility</artifactId>
            <version>4.0.1</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>au.com.dius</groupId>
            <artifactId>pact-jvm-provider-junit5</artifactId>
            <version>4.0.1</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>


</project>
```

StatusApplication.class
```
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
```

we also need to add application.properties in resources folder
and set there `server.port=8282`

and last thing which we should do is add provider test

```
@Provider("StatusVerifier")
@ExtendWith(SpringExtension.class)
@PactBroker(host = "localhost", port = "9292")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProviderContractTest {

  @LocalServerPort
  private int port;

  @BeforeEach
  void before(PactVerificationContext context) {
    System.setProperty("pact.verifier.publishResults","true");
    context.setTarget(new HttpTestTarget("localhost", port));
  }

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @State("accepted state")
  void state(){
  }

}
```

after running that test we should see on our pact broker that our pact was verified.

![alt t](img/pact1.png)

## API tests as e2e or as integration tests

It depends on app you are testing.

http://blog.codepipes.com/testing/software-testing-antipatterns.html
https://medium.com/@copyconstruct/testing-microservices-the-sane-way-9bb31d158c16

- Single page apps

- Monolith

- Microservices (gateway, domains)
