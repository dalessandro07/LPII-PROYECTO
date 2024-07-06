package com.app.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		model.addAttribute("nombre", usuarioEntity.getNombre());
		model.addAttribute("celular", usuarioEntity.getCelular());
		model.addAttribute("correo", usuarioEntity.getCorreo());

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
	public String agregarProducto(HttpSession session, @RequestParam("prodId") Integer prodId,
			@RequestParam("cant") Integer cant, @RequestParam(value = "showCart", required = false) String showCart) {
		List<Pedido> productos = (List<Pedido>) session.getAttribute("carrito");
		if (productos == null) {
			productos = new ArrayList<>();
		}
		boolean encontrado = false;
		for (Pedido pedido : productos) {
			if (pedido.getProductoId().equals(prodId)) {
				pedido.setCantidad(pedido.getCantidad() + cant);
				encontrado = true;
				break;
			}
		}
		if (!encontrado) {
			Pedido nuevoPedido = new Pedido();
			nuevoPedido.setProductoId(prodId);
			nuevoPedido.setCantidad(cant);
			productos.add(nuevoPedido);
		}
		session.setAttribute("carrito", productos);
		return showCart != null ? "redirect:/menu?showCart=true" : "redirect:/menu";
	}

	@PostMapping("/eliminar_producto")
	public String eliminarProducto(HttpSession session, @RequestParam("prodId") Integer prodId,
			@RequestParam(value = "showCart", required = false) String showCart) {
		List<Pedido> productos = (List<Pedido>) session.getAttribute("carrito");
		if (productos != null) {
			productos.removeIf(p -> p.getProductoId().equals(prodId));
			session.setAttribute("carrito", productos);
		}

		if (productos == null || productos.isEmpty()) {
			return "redirect:/menu";
		} else {
			return "redirect:/menu?showCart=true";
		}
	}

	@PostMapping("/modificar_cantidad")
	public String modificarCantidad(HttpSession session, @RequestParam("prodId") Integer prodId,
			@RequestParam("accion") String accion,
			@RequestParam(value = "showCart", required = false) String showCart) {
		List<Pedido> productos = (List<Pedido>) session.getAttribute("carrito");
		if (productos != null) {
			for (Pedido pedido : productos) {
				if (pedido.getProductoId().equals(prodId)) {
					if ("incrementar".equals(accion)) {
						pedido.setCantidad(pedido.getCantidad() + 1);
					} else if ("decrementar".equals(accion) && pedido.getCantidad() > 1) {
						pedido.setCantidad(pedido.getCantidad() - 1);
					}
					break;
				}
			}
			session.setAttribute("carrito", productos);
		}
		return "redirect:/menu?showCart=true";
	}

	@GetMapping("/generar_pdf")
	public ResponseEntity<InputStreamResource> generarPdf(HttpSession session) throws IOException {
		Long ultimaFacturaId = (Long) session.getAttribute("ultimaFacturaId");
		if (ultimaFacturaId == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		PedidoEntity pedidoEntity = pedidoService.buscarPedidoPorId(ultimaFacturaId);
		if (pedidoEntity == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		List<DetallePedidoEntity> detallePedidoEntityList = pedidoEntity.getDetallePedido();
		BigDecimal total = detallePedidoEntityList.stream()
				.map(d -> BigDecimal.valueOf(d.getCantidad())
						.multiply(BigDecimal.valueOf(d.getProductoEntity().getPrecio())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalFormateado = total.setScale(2, RoundingMode.HALF_UP);

		Map<String, Object> datosPdf = new HashMap<>();
		datosPdf.put("factura", detallePedidoEntityList);
		datosPdf.put("precio_total", totalFormateado.toString());

		ByteArrayInputStream pdfBytes = pdfService.generarPdfHtml("template_pdf", datosPdf);

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Disposition", "inline; filename=productos.pdf");

		return ResponseEntity.ok().headers(httpHeaders).contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(pdfBytes));
	}

}
