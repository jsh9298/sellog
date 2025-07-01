package com.teamproject.sellog.domain.post.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.post.model.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

}
