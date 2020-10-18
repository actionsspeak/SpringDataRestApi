package com.myapps.ws.mobilespringappws.io.repositories;

import com.myapps.ws.mobilespringappws.io.entity.PasswordResetTokenEntity;

import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetTokenEntity, Long>{
	PasswordResetTokenEntity findByToken(String token);
}
