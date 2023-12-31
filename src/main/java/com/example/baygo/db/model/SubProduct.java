package com.example.baygo.db.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import static jakarta.persistence.CascadeType.*;

@Getter
@Setter
@Entity
@Table(name = "sub_products")
@NoArgsConstructor
@AllArgsConstructor
public class SubProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_product_gen")
    @SequenceGenerator(name = "sub_product_gen", sequenceName = "sub_product_seq", allocationSize = 1, initialValue = 33)
    private Long id;
    private String colorHexCode;
    private String color;
    private String mainImage;
    @ElementCollection
    private List<String> images;
    private BigDecimal price;
    @Column(length = 2000)
    private String description;
    private int articulBG;
    private String articulOfSeller;
    private int height;
    private int width;
    private int length;
    private double weight;
    private boolean isFashion;
    private boolean isDeleted;

    @OneToMany(mappedBy = "subProduct", cascade = ALL)
    private List<Size> sizes;

    @ManyToOne(cascade = {PERSIST, MERGE, REFRESH, DETACH})
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(cascade = {PERSIST, MERGE, REFRESH, DETACH})
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @OneToMany(mappedBy = "subProduct", cascade = ALL)
    private List<Review> reviews;

    @ManyToMany(mappedBy = "favorites", cascade = {PERSIST, MERGE, REFRESH, DETACH})
    private List<Buyer> buyers;

    @ManyToMany(mappedBy = "subProducts",cascade = {PERSIST, MERGE, REFRESH, DETACH})
    private List<FbsWarehouse> fbsWarehouse;
}