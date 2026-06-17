package com.cog.propNest.module
    .propertyListingPortfolio.repository;

import com.cog.propNest.module
    .propertyListingPortfolio.entity.Property;
import org.springframework.data.jpa.repository
    .JpaRepository;
import java.util.List;

public interface PropertyRepository
        extends JpaRepository<Property, Integer> {

    List<Property> findByOwnerIDAndStatus(
            Long ownerID,
            Property.PropertyStatus status);

    List<Property> findByTypeAndStatus(
            Property.PropertyType type,
            Property.PropertyStatus status);

    List<Property> findByCityIgnoreCaseAndStatus(
            String city,
            Property.PropertyStatus status);

    List<Property> findByStatus(
            Property.PropertyStatus status);

    boolean existsByOwnerIDAndPropertyNameAndCity(
            Long ownerID,
            String propertyName,
            String city);
}
