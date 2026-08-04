package com.pfe.itsm.auth.repository;

import com.pfe.itsm.auth.domain.RefreshToken;
import com.pfe.itsm.users.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken token set token.revokedAt = CURRENT_TIMESTAMP where token.user = :user and token.revokedAt is null")
    int revokeActiveTokensFor(UserAccount user);
}
