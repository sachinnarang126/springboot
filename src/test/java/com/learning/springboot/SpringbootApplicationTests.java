package com.learning.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Test-only secret (Base64 of 32 bytes). Never use this value outside tests.
@SpringBootTest(properties = {
		"jwt.secret=dGVzdC1vbmx5LXNlY3JldC1rZXktMzItYnl0ZXMtISE=",
		"spring.datasource.password=${DB_PASSWORD:test}"
})
class SpringbootApplicationTests {

	@Test
	void contextLoads() {
	}

}
