package com.cog.propNest.seeder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Standalone, runnable utility that inserts 1000 mock {@code tenant_application}
 * rows using plain JDBC and built-in random data generation (no external
 * library, so it runs anywhere regardless of network/Maven access).
 *
 * <p>It is NOT a Spring bean and does not start the application — run its
 * {@link #main(String[])} directly from the IDE (right-click → Run Java).
 *
 * <p>It honours the foreign keys on {@code tenant_application}: it reads the
 * existing {@code propertyId}s and {@code unitId}s from the database and only
 * assigns those, so the inserts never violate the FK constraints.
 */
public class TenantApplicationDataSeeder {

    // ── Database connection (matches application.properties) ──
    private static final String URL =
        "jdbc:mysql://localhost:3306/propnest_tenant?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    // Default number of records to insert when no argument is given.
    // Override from the command line / IDE run config, e.g. arg "50000".
    private static final int DEFAULT_RECORDS = 1000;

    // Allowed status values (single-letter ENUM in the DB).
    private static final String[] STATUSES = {"S", "U", "A", "R"};

    // Simple pools used to build realistic-looking fake data.
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Alice", "Bob", "Sara", "Michael", "Emily", "David",
        "Linda", "James", "Maria", "Robert", "Priya", "Arjun", "Neha", "Rahul",
        "Anita", "Vikram", "Sneha", "Karan", "Pooja", "Amit", "Divya", "Suresh"
    };
    private static final String[] LAST_NAMES = {
        "Doe", "Smith", "Brown", "Johnson", "Williams", "Patel", "Sharma",
        "Kumar", "Reddy", "Nair", "Gupta", "Singh", "Mehta", "Iyer", "Das",
        "Verma", "Rao", "Khan", "Joshi", "Pillai", "Bose", "Chopra"
    };
    private static final String[] EMAIL_DOMAINS = {
        "gmail.com", "yahoo.com", "outlook.com", "example.com", "hotmail.com"
    };

    private static final String INSERT_SQL =
        "INSERT INTO tenant_application "
        + "(propertyId, unitId, applicantName, email, phone, nationalIdref, "
        + " monthlyIncome, applicationDate, status) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static void main(String[] args) {
        Random random = new Random();

        // Number of rows to insert: first command-line arg, else the default.
        int numRecords = DEFAULT_RECORDS;
        if (args.length > 0) {
            try {
                numRecords = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid count '" + args[0]
                    + "', falling back to " + DEFAULT_RECORDS);
            }
        }
        System.out.println("Seeding " + numRecords + " tenant_application records...");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            // FK targets must already exist — read them from the DB.
            List<Integer> propertyIds = fetchIds(conn, "SELECT propertyId FROM property");
            List<Integer> unitIds = fetchIds(conn, "SELECT unitId FROM unit");

            if (propertyIds.isEmpty()) {
                System.err.println("No rows in 'property' table. "
                    + "Insert at least one property before seeding (propertyId is a NOT NULL foreign key).");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                for (int i = 1; i <= numRecords; i++) {
                    String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                    String last = LAST_NAMES[random.nextInt(LAST_NAMES.length)];

                    // propertyId — random existing property (required FK)
                    ps.setInt(1, propertyIds.get(random.nextInt(propertyIds.size())));

                    // unitId — random existing unit, or NULL (nullable FK)
                    if (unitIds.isEmpty()) {
                        ps.setNull(2, Types.INTEGER);
                    } else {
                        ps.setInt(2, unitIds.get(random.nextInt(unitIds.size())));
                    }

                    ps.setString(3, trim(first + " " + last, 100));
                    ps.setString(4, trim(buildEmail(first, last, random), 100));
                    ps.setString(5, randomDigits(random, 10));                // fits VARCHAR(15)
                    ps.setString(6, "NID-" + randomDigits(random, 12));       // national id ref
                    ps.setBigDecimal(7, randomIncome(random));                // DECIMAL(10,2)
                    ps.setDate(8, randomPastDate(random));                    // applicationDate
                    ps.setString(9, STATUSES[random.nextInt(STATUSES.length)]);

                    ps.addBatch();

                    // flush in batches of 200 to keep memory low
                    if (i % 200 == 0) {
                        ps.executeBatch();
                        System.out.println("Inserted " + i + " / " + numRecords + " ...");
                    }
                }
                ps.executeBatch(); // remaining rows
            }

            conn.commit();
            System.out.println("Done. " + numRecords + " tenant_application records inserted.");
        } catch (Exception e) {
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Reads a single integer column from the given query into a list. */
    private static List<Integer> fetchIds(Connection conn, String sql) throws Exception {
        List<Integer> ids = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    /** e.g. john.doe437@gmail.com — number keeps it unlikely to collide. */
    private static String buildEmail(String first, String last, Random random) {
        return (first + "." + last + random.nextInt(1000)).toLowerCase()
            + "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
    }

    /** A string of n random digits (no library needed). */
    private static String randomDigits(Random random, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /** A realistic monthly income between 20,000 and 200,000 with 2 decimals. */
    private static BigDecimal randomIncome(Random random) {
        double value = 20000 + (random.nextDouble() * 180000);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    /** A random date within the last ~3 years. */
    private static Date randomPastDate(Random random) {
        LocalDate date = LocalDate.now().minusDays(random.nextInt(365 * 3));
        return Date.valueOf(date);
    }

    /** Defensive truncation so generated strings never overflow the column. */
    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
