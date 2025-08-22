package com.teamproject.sellog.domain.recommend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.domain.recommend.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
}