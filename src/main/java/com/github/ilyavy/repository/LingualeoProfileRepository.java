package com.github.ilyavy.repository;

import com.github.ilyavy.model.LingualeoProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LingualeoProfileRepository extends ReactiveCrudRepository<LingualeoProfile, String> {

}
