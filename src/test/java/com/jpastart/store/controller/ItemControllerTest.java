package com.jpastart.store.controller;

import com.jpastart.store.domain.item.Book;
import com.jpastart.store.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemControllerTest {

    @Autowired
    EntityManager em;

    @DisplayName("1.")
    @Test
    void test_1() {
        Book book = em.find(Book.class, 1L);

        // TX
        book.setName("asd");

        // 변경감지 == Dirty Checking
    }

}