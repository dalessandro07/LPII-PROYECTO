package com.app.web.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tb_producto")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productoId;

	private String nombre;
	private Integer stock;
	private Double precio;
	private String urlImagen;
}
