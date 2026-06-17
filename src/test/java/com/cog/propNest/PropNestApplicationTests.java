package com.cog.propNest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test that verifies the full Spring application context starts.
 *
 * Points at a throwaway MySQL schema ({@code propnest_test}) created on the fly
 * with {@code ddl-auto=create-drop} so it does not depend on the real
 * {@code propnest} database existing. Requires MySQL running on localhost:3306.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/propnest_test?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        "spring.datasource.username=root",
        "spring.datasource.password=root",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl"
})
class PropNestApplicationTests {

	@Test
	void contextLoads() {
	}

}


