package com.virinchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virinchi.model.NewsletterSubscription;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    boolean existsByEmailIgnoreCase(String email);
    NewsletterSubscription findTopByEmailIgnoreCaseOrderByIdDesc(String email);
}

