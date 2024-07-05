package com.app.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.archive.internal.ByteArrayInputStreamAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.web.entity.DetallePedidoEntity;
import com.app.web.entity.PedidoEntity;
import com.app.web.entity.ProductoEntity;
import com.app.web.entity.UsuarioEntity;
import com.app.web.model.Pedido;
import com.app.web.repository.PedidoRepository;
import com.app.web.service.PedidoService;
import com.app.web.service.ProductoService;
import com.app.web.service.UsuarioService;
import com.app.web.service.Imp.PdfService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProductoController {
	
	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	ProductoService productoService;
	
	@Autowired
	private PdfService pdfService;

	@GetMapping("/menu")
	public String showMenu(HttpSession session, Model model) {
		if (session.getAttribute("usuario") == null) {
			return "redirect:/";
		}

		String correo = session.getAttribute("usuario").toString();
		UsuarioEntity usuarioEntity = usuarioService.buscarUsuarioPorCorreo(correo);
		model.addAttribute("foto", usuarioEntity.getUrlImagen());

		List<ProductoEntity> productos = productoService.buscarTodosProductos();
		model.addAttribute("productos", productos);

		List<Pedido> productoSession = session.getAttribute("carrito") == null ? new ArrayList<Pedido>()
				: (List<Pedido>) session.getAttribute("carrito");
		model.addAttribute("cant_carrito", productoSession.size());

		List<DetallePedidoEntity> detallePedidoEntityList = new ArrayList<DetallePedidoEntity>();
		Double total = 0.0;

		for (Pedido pedido : productoSession) {
			DetallePedidoEntity detallePedidoEntity = new DetallePedidoEntity();
			ProductoEntity productoEntity = productoService.buscarProductoPorId(pedido.getProductoId());
			detallePedidoEntity.setProductoEntity(productoEntity);
			detallePedidoEntity.setCantidad(pedido.getCantidad());
			detallePedidoEntityList.add(detallePedidoEntity);
			total += pedido.getCantidad() * productoEntity.getPrecio();
		}
		model.addAttribute("carrito", detallePedidoEntityList);
		model.addAttribute("total", total);

		return "menu";
	}

	@PostMapping("/agregar_producto")
	public String agregarProducto(HttpSession session, @RequestParam("prodId") String prod,
	        @RequestParam("cant") String cant) {
	    List<Pedido> productos = null;
	    if (session.getAttribute("carrito") == null) {
	        productos = new ArrayList<>();
	    } else {
	        productos = (List<Pedido>) session.getAttribute("carrito");
	    }

	    Integer cantidad = Integer.parseInt(cant);
	    Integer prodId = Integer.parseInt(prod);
	    
	    boolean productoExistente = false;
	    for (Pedido pedido : productos) {
	        if (pedido.getProductoId().equals(prodId)) {
	            pedido.setCantidad(pedido.getCantidad() + cantidad);
	            productoExistente = true;
	            break;
	        }
	    }

	    if (!productoExistente) {
	        Pedido pedido = new Pedido(cantidad, prodId);
	        productos.add(pedido);
	    }
	    
	    session.setAttribute("carrito", productos);
	    return "redirect:/menu";
	}

	@GetMapping("/generar_pdf")
	public ResponseEntity<InputStreamResource> generarPdf(HttpSession session) throws IOException {
	    Long ultimaFacturaId = (Long) session.getAttribute("ultimaFacturaId");
	    if (ultimaFacturaId == null) {
	        // Maneja el caso cuando no hay una factura reciente
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	    }

	    PedidoEntity pedidoEntity = pedidoService.buscarPedidoPorId(ultimaFacturaId);
	    if (pedidoEntity == null) {
	        // Maneja el caso cuando la factura no se encuentra
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	    }

	    List<DetallePedidoEntity> detallePedidoEntityList = pedidoEntity.getDetallePedido();
	    Double total = detallePedidoEntityList.stream()
	                                          .mapToDouble(d -> d.getCantidad() * d.getProductoEntity().getPrecio())
	                                          .sum();

	    // No es necesario formatear aquí si los precios ya están formateados en la base de datos
	    DecimalFormat df = new DecimalFormat("#.00");
	    String totalFormateado = df.format(total);

	    Map<String, Object> datosPdf = new HashMap<>();
	    datosPdf.put("factura", detallePedidoEntityList);
	    datosPdf.put("precio_total", totalFormateado);

	    ByteArrayInputStream pdfBytes = pdfService.generarPdfHtml("template_pdf", datosPdf);

	    HttpHeaders httpHeaders = new HttpHeaders();
	    httpHeaders.add("Content-Disposition", "inline; filename=productos.pdf");

	    return ResponseEntity.ok()
	                         .headers(httpHeaders)
	                         .contentType(MediaType.APPLICATION_PDF)
	                         .body(new InputStreamResource(pdfBytes));
	}

}
