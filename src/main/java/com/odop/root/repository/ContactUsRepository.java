package com.odop.root.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.ContactUs;

@Repository
public interface ContactUsRepository extends MongoRepository<ContactUs, String> {

}
