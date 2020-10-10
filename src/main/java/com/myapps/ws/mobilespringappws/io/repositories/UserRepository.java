package com.myapps.ws.mobilespringappws.io.repositories;

import com.myapps.ws.mobilespringappws.io.entity.UserEntity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    UserEntity findByEmail(String email);  
    
    UserEntity findByUserId(String userId);
}
