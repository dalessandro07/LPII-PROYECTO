package com.app.web.service;

import java.util.List;

import com.app.web.entity.ProductoEntity;

public interface ProductoService {
	
	List<ProductoEntity> buscarTodosProductos();
	ProductoEntity buscarProductoPorId(Integer id);

}
