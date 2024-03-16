package com.smart.dao;

import com.smart.entities.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

//    Pageable will have two values i.e. currentPage and contacts per page
    public Page<Contact> findByUser_id(int userId, Pageable pageable);
}
