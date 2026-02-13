package com.odop.root.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.Vendor;

@Repository
public interface VendorRepository extends MongoRepository<Vendor, String> {

    Vendor findByVendorId(String vendorId);

    Vendor findByEmailAddress(String emailAddress);

    Vendor findByEmailAddressAndBusinessRegistryNumber(
            String emailAddress, String businessRegistryNumber
    );

    List<Vendor> findByLocationDistrictAndLocationState(String locationDistrict, String locationState);
    List<Vendor> findByLocationState(String locationState);
}
