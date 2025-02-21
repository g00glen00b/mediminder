package codes.dimitri.mediminder.api.user.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder"
})
@Sql("classpath:test-data/user.sql")
class UserEntityRepositoryTest {
    @Autowired
    private UserEntityRepository repository;

    @ParameterizedTest
    @CsvSource({
        "code1,false",
        "code2,false",
        "code3,true",
        "code4,true"
    })
    void findByVerificationCode(String code, boolean exists) {
        Optional<UserEntity> result = repository.findByVerificationCode(code);
        assertThat(result.isPresent()).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "me1@example.org,true",
        "me2@example.org,true",
        "doesnotexist@example.org,false"
    })
    void findByEmail(String email, boolean exists) {
        Optional<UserEntity> result = repository.findByEmail(email);
        assertThat(result.isPresent()).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "me1@example.org,true",
        "me2@example.org,true",
        "doesnotexist@example.org,false"
    })
    void existsByEmail(String email, boolean exists) {
        boolean result = repository.existsByEmail(email);
        assertThat(result).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "code1,true",
        "code2,true",
        "code3,false",
        "code4,false"
    })
    void findByPasswordResetCode(String code, boolean exists) {
        Optional<UserEntity> result = repository.findByPasswordResetCode(code);
        assertThat(result.isPresent()).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "code1,true",
        "code2,true",
        "code3,false",
        "code4,false"
    })
    void existsByPasswordResetCode(String code, boolean exists) {
        boolean result = repository.existsByPasswordResetCode(code);
        assertThat(result).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "code1,false",
        "code2,false",
        "code3,true",
        "code4,true"
    })
    void existsByVerificationCode(String code, boolean exists) {
        boolean result = repository.existsByVerificationCode(code);
        assertThat(result).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-06-27T10:00:01Z,2",
        "2024-06-27T10:00:00Z,1",
        "2024-06-26T10:00:01Z,1",
        "2024-06-26T10:00:00Z,0",
    })
    void findAllWithVerificationCodeAndLastModifiedBefore(Instant instant, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<UserEntity> result = repository.findAllWithVerificationCodeAndLastModifiedBefore(instant, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-06-29T10:00:01Z,2",
        "2024-06-29T10:00:00Z,1",
        "2024-06-28T10:00:01Z,1",
        "2024-06-28T10:00:00Z,0",
    })
    void findAllWithPasswordResetCodeAndLastModifiedBefore(Instant instant, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<UserEntity> result = repository.findAllWithPasswordResetCodeAndLastModifiedBefore(instant, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }
}