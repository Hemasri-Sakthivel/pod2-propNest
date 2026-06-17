package com.cog.propNest.module
    .propertyListingPortfolio.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unitID")
    private Integer unitID;

    @Column(name = "propertyID", nullable = false)
    private Integer propertyID;

    // BIGINT in DB → references propnest_iam.users.userId
    @Column(name = "tenantID")
    private Long tenantID;

    @Column(name = "unitNumber", nullable = false)
    private String unitNumber;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Convert(converter = UnitTypeConverter.class)
    @Column(name = "type", nullable = false)
    private UnitType type;

    @Column(name = "areaSqFt", nullable = false)
    private BigDecimal areaSqFt;

    @Convert(converter = FurnishingTypeConverter.class)
    @Column(name = "furnishing", nullable = false)
    private FurnishingType furnishing;

    @Column(name = "listedRent", nullable = false)
    private BigDecimal listedRent;

    @Convert(converter = UnitStatusConverter.class)
    @Column(name = "status", nullable = false)
    private UnitStatus status;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null)
            this.status = UnitStatus.Vacant;
    }

    // word = full word accepted in requests, code = shortcode returned in responses
    public enum UnitType {
        Studio("ST", "Studio"),
        OneBHK("1B", "1BHK"),
        TwoBHK("2B", "2BHK"),
        ThreeBHK("3B", "3BHK"),
        Office("OF", "Office"),
        Shop("SH", "Shop");

        private final String code;
        private final String word;
        UnitType(String code, String word) {
            this.code = code;
            this.word = word;
        }
        public String getCode() { return code; }
        public String getWord() { return word; }

        public static UnitType fromWord(String word) {
            for (UnitType t : values())
                if (t.word.equals(word)) return t;
            throw new IllegalArgumentException(
                "Unknown unit type word: " + word);
        }

        public static UnitType fromCode(String code) {
            for (UnitType t : values())
                if (t.code.equals(code)) return t;
            throw new IllegalArgumentException(
                "Unknown unit type code: " + code);
        }
    }

    public enum FurnishingType {
        Unfurnished("UF"), SemiFurnished("SF"), Furnished("FR");

        private final String code;
        FurnishingType(String code) { this.code = code; }
        public String getCode() { return code; }

        public static FurnishingType fromWord(String word) {
            return FurnishingType.valueOf(word);
        }

        public static FurnishingType fromCode(String code) {
            for (FurnishingType f : values())
                if (f.code.equals(code)) return f;
            throw new IllegalArgumentException(
                "Unknown furnishing code: " + code);
        }
    }

    public enum UnitStatus {
        Vacant("VC"), Occupied("OC"), Reserved("RS"), Maintenance("MN");

        private final String code;
        UnitStatus(String code) { this.code = code; }
        public String getCode() { return code; }

        public static UnitStatus fromWord(String word) {
            return UnitStatus.valueOf(word);
        }

        public static UnitStatus fromCode(String code) {
            for (UnitStatus s : values())
                if (s.code.equals(code)) return s;
            throw new IllegalArgumentException(
                "Unknown unit status code: " + code);
        }
    }
}
