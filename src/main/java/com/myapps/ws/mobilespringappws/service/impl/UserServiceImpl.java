package com.myapps.ws.mobilespringappws.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.myapps.ws.mobilespringappws.exceptions.UserServiceException;
import com.myapps.ws.mobilespringappws.io.entity.UserEntity;
import com.myapps.ws.mobilespringappws.io.repositories.UserRepository;
import com.myapps.ws.mobilespringappws.service.UserService;
import com.myapps.ws.mobilespringappws.shared.AmazonSES;
import com.myapps.ws.mobilespringappws.shared.Utils;
import com.myapps.ws.mobilespringappws.shared.dto.AddressDTO;
import com.myapps.ws.mobilespringappws.shared.dto.UserDTO;
import com.myapps.ws.mobilespringappws.ui.model.response.ErrorMessages;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AmazonSES amazonSES;

    @Override
    public UserDTO createUser(UserDTO user) {

        if (userRepository.findByEmail(user.getEmail()) != null)
            throw new RuntimeException("Record already exists!");

        for(int i=0; i<user.getAddresses().size(); i++) {
            AddressDTO address = user.getAddresses().get(i);
            address.setUserDetails(user);
            address.setAddressId(utils.generateAddressId(30));
            user.getAddresses().set(i, address);
        }

        ModelMapper modelMapper =  new ModelMapper();            
        // BeanUtils.copyProperties(user, userEntity);
        UserEntity userEntity = modelMapper.map(user, UserEntity.class);

        String publicUserId = utils.generateUserId(30);
        userEntity.setUserId(publicUserId);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
        userEntity.setEmailVerificationStatus(false);

        UserEntity storedUserDetails = userRepository.save(userEntity);

        // BeanUtils.copyProperties(storedUserDetails, returnValue);
        UserDTO returnValue = modelMapper.map(storedUserDetails, UserDTO.class);

        // Send an email message to user to verify their email address
		// amazonSES.verifyEmail(returnValue); //Amazon SES commented. Need to have the SES configured prior using this

        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null)
            throw new UsernameNotFoundException(email);

        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), 
            userEntity.getEmailVerificationStatus(), 
            true, true, 
            true,	new ArrayList<>());
        // return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
    }

    @Override
    public UserDTO getUser(String email) {

        UserEntity userEntity = userRepository.findByEmail(email);

        UserDTO returnValue = new UserDTO();
        if (userEntity == null)
            throw new UsernameNotFoundException(email);
        BeanUtils.copyProperties(userEntity, returnValue);

        return returnValue;
    }

    @Override
    public UserDTO getUserByUserId(String userId) {
        UserDTO returnValue = new UserDTO();

        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null)
            throw new UsernameNotFoundException("User with ID: " + userId + "not found");

        // BeanUtils.copyProperties(userEntity, returnValue);
        ModelMapper modelMapper = new ModelMapper();
        returnValue = modelMapper.map(userEntity, UserDTO.class);

        return returnValue;
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO user) {

        UserDTO returnValue = new UserDTO();
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null)
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());

        UserEntity updtedUserDetails = userRepository.save(userEntity);

        // BeanUtils.copyProperties(updtedUserDetails, returnValue);
        returnValue = new ModelMapper().map(updtedUserDetails, UserDTO.class);

        return returnValue;
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null)
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userRepository.delete(userEntity);
    }

    @Override
    public List<UserDTO> getUsers(int page, int limit) {

        List<UserDTO> returnValue = new ArrayList<>();

        if(page>0) page = page-1;

        Pageable pageableRequest = PageRequest.of(page, limit);
        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
        List<UserEntity> users = usersPage.getContent();
        ModelMapper modelMapper = new ModelMapper();

        for(UserEntity userEntity : users) {
            UserDTO userDto = new UserDTO();
            // BeanUtils.copyProperties(userEntity, userDto);
            userDto = modelMapper.map(userEntity, UserDTO.class);
            returnValue.add(userDto);
        }

        return returnValue;
    }

    @Override
	public boolean verifyEmailToken(String token) {
	    boolean returnValue = false;

        // Find user by token
        UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);

        if (userEntity != null) {
            boolean hastokenExpired = Utils.hasTokenExpired(token);
            if (!hastokenExpired) {
                userEntity.setEmailVerificationToken(null);
                userEntity.setEmailVerificationStatus(Boolean.TRUE);
                userRepository.save(userEntity);
                returnValue = true;
            }
        }

        return returnValue;
	}
    
}
