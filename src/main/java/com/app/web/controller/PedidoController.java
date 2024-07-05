package com.app.web.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.web.entity.DetallePedidoEntity;
import com.app.web.entity.PedidoEntity;
import com.app.web.entity.ProductoEntity;
import com.app.web.entity.UsuarioEntity;
import com.app.web.model.Pedido;
import com.app.web.repository.PedidoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class PedidoController {
	
	@Autowired
	private PedidoRepository pedidoRepository;
	
	@GetMapping("/guardar_factura")
	public String guardarFactura(HttpSession session) {
	    String CorreoString = (String) session.getAttribute("usuario");
	    UsuarioEntity usuarioEntity = new UsuarioEntity();
	    usuarioEntity.setCorreo(CorreoString);

	    PedidoEntity pedidoEntity = new PedidoEntity();
	    pedidoEntity.setFechaCompra(LocalDate.now());
	    pedidoEntity.setUsuarioEntity(usuarioEntity);

	    List<DetallePedidoEntity> detallePedidoEntityList = new ArrayList<>();

	    List<Pedido> productoSession = session.getAttribute("carrito") == null ? new ArrayList<Pedido>()
	            : (List<Pedido>) session.getAttribute("carrito");

	    for (Pedido pedido : productoSession) {
	        DetallePedidoEntity detallePedidoEntity = new DetallePedidoEntity();  

	        ProductoEntity productoEntity = new ProductoEntity();
	        productoEntity.setProductoId(pedido.getProductoId().longValue());

	        detallePedidoEntity.setProductoEntity(productoEntity);
	        detallePedidoEntity.setCantidad(pedido.getCantidad());
	        detallePedidoEntity.setPedidoEntity(pedidoEntity);
	        detallePedidoEntityList.add(detallePedidoEntity);
	    }

	    pedidoEntity.setDetallePedido(detallePedidoEntityList);
	    pedidoRepository.save(pedidoEntity);
	    
	    // Guarda el identificador de la última factura en la sesión
	    session.setAttribute("ultimaFacturaId", pedidoEntity.getPedidoId());

	    session.removeAttribute("carrito");

	    return "redirect:/menu?showModal=true";
	}


}
