package com.app.web.service.Imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.web.entity.PedidoEntity;
import com.app.web.repository.PedidoRepository;
import com.app.web.service.PedidoService;

@Service
public class PedidoServiceImp implements PedidoService{

	@Autowired
	private PedidoRepository pedidoRepository;
	
	@Override
	public PedidoEntity buscarPedidoPorId(Long pedidoId) {
		
		return pedidoRepository.findById(pedidoId).get();
	}

}
