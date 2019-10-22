package com.example.demo.repository;

import com.example.demo.data.Orders;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Orders, Long> {

  List<Orders> findAllByClientAndNumber(String client, String number);

  @Query("select o from Orders o where o.client = :client")
  List<Orders> findAllOrdersByClient(@Param("client") String client);

  @Query(value = "select * from Orders order by price desc limit 2", nativeQuery = true)
  Orders getOneOrder();

}
