package com.myapps.ws.mobilespringappws.service;

import java.util.List;

import com.myapps.ws.mobilespringappws.shared.dto.AddressDTO;

public interface AddressService {
    public List<AddressDTO> getAddresses(String userId);
    public AddressDTO getAddress(String addressId);
}
