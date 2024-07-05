package com.app.web.service;

import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.app.web.entity.UsuarioEntity;

import jakarta.servlet.http.HttpSession;

public interface UsuarioService {

	void crearUsuario(UsuarioEntity usuarioEntity, Model model, MultipartFile foto);
	boolean validarUsuario(UsuarioEntity usuarioEntity, HttpSession session);
	UsuarioEntity buscarUsuarioPorCorreo(String correo);
}
