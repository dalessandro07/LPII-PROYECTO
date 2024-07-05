package com.app.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.web.entity.PedidoEntity;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long>{
	

}
