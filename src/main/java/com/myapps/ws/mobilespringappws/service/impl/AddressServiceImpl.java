package com.myapps.ws.mobilespringappws.service.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.myapps.ws.mobilespringappws.io.entity.AddressEntity;
import com.myapps.ws.mobilespringappws.io.entity.UserEntity;
import com.myapps.ws.mobilespringappws.io.repositories.AddressRepository;
import com.myapps.ws.mobilespringappws.io.repositories.UserRepository;
import com.myapps.ws.mobilespringappws.service.AddressService;
import com.myapps.ws.mobilespringappws.shared.dto.AddressDTO;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressRepository addressRepository;

    @Override
    public List<AddressDTO> getAddresses(String userId) {

        List<AddressDTO> returnValue = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();

        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null) return returnValue;

        Iterable<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);
        for(AddressEntity addressEntity: addresses){
            returnValue.add(modelMapper.map(addressEntity, AddressDTO.class));
        }

        return returnValue;
    }

    @Override
    public AddressDTO getAddress(String addressId) {
  
        ModelMapper modelMapper = new ModelMapper();

        AddressEntity addressEntity = addressRepository.findByAddressId(addressId);

        return modelMapper.map(addressEntity, AddressDTO.class);
    }
}
