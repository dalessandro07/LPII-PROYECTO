package com.app.web.service.Imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.app.web.entity.UsuarioEntity;
import com.app.web.repository.UsuarioRepository;
import com.app.web.service.UsuarioService;
import com.app.web.utils.Utilitarios;

import jakarta.servlet.http.HttpSession;

@Service
public class UsuarioServiceImp implements UsuarioService{

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Override
	public void crearUsuario(UsuarioEntity usuarioEntity, Model model, MultipartFile foto) {
		String nombreFoto = Utilitarios.guardarImagen(foto);
		usuarioEntity.setUrlImagen(nombreFoto);
		
		String passwordHash = Utilitarios.extraerHash(usuarioEntity.getPassword());
		usuarioEntity.setPassword(passwordHash);
		
		usuarioRepository.save(usuarioEntity);
		
		model.addAttribute("registroCorrecto", "Registro Correcto");
		model.addAttribute("usuario", new UsuarioEntity());
	}

	@Override
	public boolean validarUsuario(UsuarioEntity usuarioEntity, HttpSession session) {
		UsuarioEntity usuarioEncontradoPorCorreo = usuarioRepository.findByCorreo(usuarioEntity.getCorreo());
		if (usuarioEncontradoPorCorreo == null) {
			return false;
		} if(!Utilitarios.checkPassword(usuarioEntity.getPassword(), usuarioEncontradoPorCorreo.getPassword())){
			return false;

		}
		
		session.setAttribute("usuario", usuarioEncontradoPorCorreo.getCorreo());
		
		return true;
	}

	@Override
	public UsuarioEntity buscarUsuarioPorCorreo(String correo) {
		return usuarioRepository.findByCorreo(correo);
	}

}
