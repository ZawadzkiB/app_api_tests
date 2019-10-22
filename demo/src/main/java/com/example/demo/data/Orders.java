package com.example.demo.data;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Accessors(chain = true)
public class Orders {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Long id;
  @NotNull
  private String number;
  private String client;
  private BigDecimal price;
  private String status = "not verified";

//  @OneToMany(
//          cascade = CascadeType.ALL,
//          orphanRemoval = true
//  )
//  private List<Products> productsList = new ArrayList<>();
}
