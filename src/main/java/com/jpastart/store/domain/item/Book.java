package com.jpastart.store.domain.item;

import com.jpastart.store.domain.categoty.Category;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

@Entity
@Getter
@Setter
@DiscriminatorValue("B")
@NoArgsConstructor
public class Book extends Item {

    private String author;

    private String isbn;

    @Builder
    public Book(Long id, String name, int price, int stockQuantity, List<Category> categories, String author, String isbn) {
        super(id, name, price, stockQuantity, categories);
        this.author = author;
        this.isbn = isbn;
    }

}
