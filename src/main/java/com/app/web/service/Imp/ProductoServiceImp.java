package com.app.web.service.Imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.web.entity.ProductoEntity;
import com.app.web.repository.ProductoRepository;
import com.app.web.service.ProductoService;

@Service
public class ProductoServiceImp implements ProductoService{
	
	@Autowired
	private ProductoRepository productoRepository;

	@Override
	public List<ProductoEntity> buscarTodosProductos() {
		
		return productoRepository.findAll();
	}

	@Override
	public ProductoEntity buscarProductoPorId(Integer id) {
		
		return productoRepository.findById(id.longValue()).get();
	}

}
