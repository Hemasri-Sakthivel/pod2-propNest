package com.cog.propNest.module
    .propertyListingPortfolio.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "property")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "propertyID")
    private Integer propertyID;

    // BIGINT in DB → references propnest_iam.users.userId
    @Column(name = "ownerID", nullable = false)
    private Long ownerID;

    @Column(name = "propertyName", nullable = false)
    private String propertyName;

    @Convert(converter = PropertyTypeConverter.class)
    @Column(name = "type", nullable = false)
    private PropertyType type;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "totalUnits")
    private Integer totalUnits;

    @Column(name = "yearBuilt")
    private Integer yearBuilt;

    @Convert(converter = PropertyStatusConverter.class)
    @Column(name = "status", nullable = false)
    private PropertyStatus status;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null)
            this.status = PropertyStatus.Active;
    }

    // Request bodies use the full word (enum name); responses return the code.
    public enum PropertyType {
        Residential("RS"), Commercial("CM"), Mixed("MX");

        private final String code;
        PropertyType(String code) { this.code = code; }
        public String getCode() { return code; }

        public static PropertyType fromWord(String word) {
            return PropertyType.valueOf(word);
        }

        public static PropertyType fromCode(String code) {
            for (PropertyType t : values())
                if (t.code.equals(code)) return t;
            throw new IllegalArgumentException(
                "Unknown property type code: " + code);
        }
    }

    public enum PropertyStatus {
        Active("AC"), UnderMaintenance("UM"), Delisted("DL");

        private final String code;
        PropertyStatus(String code) { this.code = code; }
        public String getCode() { return code; }

        public static PropertyStatus fromWord(String word) {
            return PropertyStatus.valueOf(word);
        }

        public static PropertyStatus fromCode(String code) {
            for (PropertyStatus s : values())
                if (s.code.equals(code)) return s;
            throw new IllegalArgumentException(
                "Unknown property status code: " + code);
        }
    }
}
