package com.app.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.web.entity.ProductoEntity;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long>{

}
