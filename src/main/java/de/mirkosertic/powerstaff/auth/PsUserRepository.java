package de.mirkosertic.powerstaff.auth;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

interface PsUserRepository extends CrudRepository<PsUser, String> {

    @Modifying
    @Query("UPDATE ps_user SET password_hash = :passwordHash, must_change_password = FALSE WHERE username = :username")
    void updatePassword(@Param("username") String username, @Param("passwordHash") String passwordHash);
}
