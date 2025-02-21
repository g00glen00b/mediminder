package codes.dimitri.mediminder.api.user.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByVerificationCode(String verificationCode);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserEntity> findByPasswordResetCode(String passwordResetCode);
    boolean existsByPasswordResetCode(String passwordResetCode);
    boolean existsByVerificationCode(String verificationCode);
    @Query("select u from UserEntity u where u.lastModifiedDate < ?1 and u.verificationCode is not null")
    Page<UserEntity> findAllWithVerificationCodeAndLastModifiedBefore(Instant lastModifiedBefore, Pageable pageable);
    @Query("select u from UserEntity u where u.lastModifiedDate < ?1 and u.passwordResetCode is not null")
    Page<UserEntity> findAllWithPasswordResetCodeAndLastModifiedBefore(Instant lastModifiedBefore, Pageable pageable);
}
