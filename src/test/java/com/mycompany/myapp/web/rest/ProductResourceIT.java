package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.JhipsterSampleApplicationApp;
import com.mycompany.myapp.config.TestSecurityConfiguration;
import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ProductResource} REST controller.
 */
@SpringBootTest(classes = { JhipsterSampleApplicationApp.class, TestSecurityConfiguration.class })
@AutoConfigureMockMvc
@WithMockUser
public class ProductResourceIT {

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_USER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_USER_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc restProductMockMvc;

    private Product product;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Product createEntity() {
        Product product = new Product()
            .userId(DEFAULT_USER_ID)
            .userName(DEFAULT_USER_NAME)
            .email(DEFAULT_EMAIL);
        return product;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Product createUpdatedEntity() {
        Product product = new Product()
            .userId(UPDATED_USER_ID)
            .userName(UPDATED_USER_NAME)
            .email(UPDATED_EMAIL);
        return product;
    }

    @BeforeEach
    public void initTest() {
        productRepository.deleteAll();
        product = createEntity();
    }

    @Test
    public void createProduct() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();
        // Create the Product
        restProductMockMvc.perform(post("/api/products").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(product)))
            .andExpect(status().isCreated());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate + 1);
        Product testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testProduct.getUserName()).isEqualTo(DEFAULT_USER_NAME);
        assertThat(testProduct.getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    public void createProductWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = productRepository.findAll().size();

        // Create the Product with an existing ID
        product.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductMockMvc.perform(post("/api/products").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(product)))
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void getAllProducts() throws Exception {
        // Initialize the database
        productRepository.save(product);

        // Get all the productList
        restProductMockMvc.perform(get("/api/products?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(product.getId())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].userName").value(hasItem(DEFAULT_USER_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }
    
    @Test
    public void getProduct() throws Exception {
        // Initialize the database
        productRepository.save(product);

        // Get the product
        restProductMockMvc.perform(get("/api/products/{id}", product.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(product.getId()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.userName").value(DEFAULT_USER_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL));
    }
    @Test
    public void getNonExistingProduct() throws Exception {
        // Get the product
        restProductMockMvc.perform(get("/api/products/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateProduct() throws Exception {
        // Initialize the database
        productRepository.save(product);

        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // Update the product
        Product updatedProduct = productRepository.findById(product.getId()).get();
        updatedProduct
            .userId(UPDATED_USER_ID)
            .userName(UPDATED_USER_NAME)
            .email(UPDATED_EMAIL);

        restProductMockMvc.perform(put("/api/products").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedProduct)))
            .andExpect(status().isOk());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
        Product testProduct = productList.get(productList.size() - 1);
        assertThat(testProduct.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testProduct.getUserName()).isEqualTo(UPDATED_USER_NAME);
        assertThat(testProduct.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    public void updateNonExistingProduct() throws Exception {
        int databaseSizeBeforeUpdate = productRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(put("/api/products").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(product)))
            .andExpect(status().isBadRequest());

        // Validate the Product in the database
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteProduct() throws Exception {
        // Initialize the database
        productRepository.save(product);

        int databaseSizeBeforeDelete = productRepository.findAll().size();

        // Delete the product
        restProductMockMvc.perform(delete("/api/products/{id}", product.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Product> productList = productRepository.findAll();
        assertThat(productList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
