package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.JhipsterSampleApplicationApp;
import com.mycompany.myapp.config.TestSecurityConfiguration;
import com.mycompany.myapp.domain.Review;
import com.mycompany.myapp.repository.ReviewRepository;

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
 * Integration tests for the {@link ReviewResource} REST controller.
 */
@SpringBootTest(classes = { JhipsterSampleApplicationApp.class, TestSecurityConfiguration.class })
@AutoConfigureMockMvc
@WithMockUser
public class ReviewResourceIT {

    private static final String DEFAULT_REVIEW_ID = "AAAAAAAAAA";
    private static final String UPDATED_REVIEW_ID = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Integer DEFAULT_RATING = 1;
    private static final Integer UPDATED_RATING = 2;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MockMvc restReviewMockMvc;

    private Review review;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Review createEntity() {
        Review review = new Review()
            .reviewId(DEFAULT_REVIEW_ID)
            .content(DEFAULT_CONTENT)
            .rating(DEFAULT_RATING);
        return review;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Review createUpdatedEntity() {
        Review review = new Review()
            .reviewId(UPDATED_REVIEW_ID)
            .content(UPDATED_CONTENT)
            .rating(UPDATED_RATING);
        return review;
    }

    @BeforeEach
    public void initTest() {
        reviewRepository.deleteAll();
        review = createEntity();
    }

    @Test
    public void createReview() throws Exception {
        int databaseSizeBeforeCreate = reviewRepository.findAll().size();
        // Create the Review
        restReviewMockMvc.perform(post("/api/reviews").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(review)))
            .andExpect(status().isCreated());

        // Validate the Review in the database
        List<Review> reviewList = reviewRepository.findAll();
        assertThat(reviewList).hasSize(databaseSizeBeforeCreate + 1);
        Review testReview = reviewList.get(reviewList.size() - 1);
        assertThat(testReview.getReviewId()).isEqualTo(DEFAULT_REVIEW_ID);
        assertThat(testReview.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testReview.getRating()).isEqualTo(DEFAULT_RATING);
    }

    @Test
    public void createReviewWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = reviewRepository.findAll().size();

        // Create the Review with an existing ID
        review.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restReviewMockMvc.perform(post("/api/reviews").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(review)))
            .andExpect(status().isBadRequest());

        // Validate the Review in the database
        List<Review> reviewList = reviewRepository.findAll();
        assertThat(reviewList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void getAllReviews() throws Exception {
        // Initialize the database
        reviewRepository.save(review);

        // Get all the reviewList
        restReviewMockMvc.perform(get("/api/reviews?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(review.getId())))
            .andExpect(jsonPath("$.[*].reviewId").value(hasItem(DEFAULT_REVIEW_ID)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].rating").value(hasItem(DEFAULT_RATING)));
    }
    
    @Test
    public void getReview() throws Exception {
        // Initialize the database
        reviewRepository.save(review);

        // Get the review
        restReviewMockMvc.perform(get("/api/reviews/{id}", review.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(review.getId()))
            .andExpect(jsonPath("$.reviewId").value(DEFAULT_REVIEW_ID))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.rating").value(DEFAULT_RATING));
    }
    @Test
    public void getNonExistingReview() throws Exception {
        // Get the review
        restReviewMockMvc.perform(get("/api/reviews/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateReview() throws Exception {
        // Initialize the database
        reviewRepository.save(review);

        int databaseSizeBeforeUpdate = reviewRepository.findAll().size();

        // Update the review
        Review updatedReview = reviewRepository.findById(review.getId()).get();
        updatedReview
            .reviewId(UPDATED_REVIEW_ID)
            .content(UPDATED_CONTENT)
            .rating(UPDATED_RATING);

        restReviewMockMvc.perform(put("/api/reviews").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedReview)))
            .andExpect(status().isOk());

        // Validate the Review in the database
        List<Review> reviewList = reviewRepository.findAll();
        assertThat(reviewList).hasSize(databaseSizeBeforeUpdate);
        Review testReview = reviewList.get(reviewList.size() - 1);
        assertThat(testReview.getReviewId()).isEqualTo(UPDATED_REVIEW_ID);
        assertThat(testReview.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testReview.getRating()).isEqualTo(UPDATED_RATING);
    }

    @Test
    public void updateNonExistingReview() throws Exception {
        int databaseSizeBeforeUpdate = reviewRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReviewMockMvc.perform(put("/api/reviews").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(review)))
            .andExpect(status().isBadRequest());

        // Validate the Review in the database
        List<Review> reviewList = reviewRepository.findAll();
        assertThat(reviewList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteReview() throws Exception {
        // Initialize the database
        reviewRepository.save(review);

        int databaseSizeBeforeDelete = reviewRepository.findAll().size();

        // Delete the review
        restReviewMockMvc.perform(delete("/api/reviews/{id}", review.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Review> reviewList = reviewRepository.findAll();
        assertThat(reviewList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
