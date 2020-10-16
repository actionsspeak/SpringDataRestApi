package com.myapps.ws.mobilespringappws.ui.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.myapps.ws.mobilespringappws.exceptions.UserServiceException;
import com.myapps.ws.mobilespringappws.service.AddressService;
import com.myapps.ws.mobilespringappws.service.UserService;
import com.myapps.ws.mobilespringappws.shared.dto.AddressDTO;
import com.myapps.ws.mobilespringappws.shared.dto.UserDTO;
import com.myapps.ws.mobilespringappws.ui.model.request.RequestOperationName;
import com.myapps.ws.mobilespringappws.ui.model.request.UserDetailsRequestModel;
import com.myapps.ws.mobilespringappws.ui.model.response.AddressesRest;
import com.myapps.ws.mobilespringappws.ui.model.response.ErrorMessages;
import com.myapps.ws.mobilespringappws.ui.model.response.OperationStatusModel;
import com.myapps.ws.mobilespringappws.ui.model.response.RequestOperationStatus;
import com.myapps.ws.mobilespringappws.ui.model.response.UserRest;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AddressService addressService;

    @GetMapping(path = "/{id}", 
        produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public UserRest getUser(@PathVariable String id) {

        UserRest returnValue = new UserRest();

        UserDTO userDto = userService.getUserByUserId(id);
        ModelMapper modelMapper = new ModelMapper();
        returnValue = modelMapper.map(userDto, UserRest.class);

        return returnValue;
    }
   
    @PostMapping(
        produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
        consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {

        if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        UserRest returnValue = new UserRest();

        // UserDTO userDto = new UserDTO();
        // BeanUtils.copyProperties(userDetails, userDto);
        ModelMapper modelMapper =  new ModelMapper();
        UserDTO userDto = modelMapper.map(userDetails, UserDTO.class);

        UserDTO createdUser = userService.createUser(userDto);
        //BeanUtils.copyProperties(createdUser, returnValue);
        returnValue = modelMapper.map(createdUser, UserRest.class);
        
        return returnValue;
    }

    @PutMapping(
        path = "/{id}",
        produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
        consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
    )
    public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
        UserRest returnValue = new UserRest();

        UserDTO userDto = new UserDTO();
        ModelMapper modelMapper = new ModelMapper();
        // BeanUtils.copyProperties(userDetails, userDto);
        userDto = modelMapper.map(userDetails, UserDTO.class);

        UserDTO updatedUser = userService.updateUser(id, userDto);
        // BeanUtils.copyProperties(updatedUser, returnValue);
        returnValue = modelMapper.map(updatedUser, UserRest.class);

        return returnValue;
    }

    @DeleteMapping(
        path = "/{id}", 
        produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public OperationStatusModel deleteUser(@PathVariable String id) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());
        
        userService.deleteUser(id);
        
        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }

    @GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public List<UserRest> getUsers(@RequestParam(value="page", defaultValue = "0") int page,
            @RequestParam(value="limit", defaultValue = "50") int limit) {
        List<UserRest> returnValue = new ArrayList<>();

        List<UserDTO> users = userService.getUsers(page, limit);

      /*for(UserDTO userDto : users) {
            UserRest userModel = new UserRest();
            BeanUtils.copyProperties(userDto, userModel);
            returnValue.add(userModel);
        } */

        Type listType = new TypeToken<List<UserRest>>() {
		}.getType();
		returnValue = new ModelMapper().map(users, listType);

        return returnValue;
    }

    //http://localhost:8080/mobile-app-ws/users/oasdiufoasdf/addresses
    @GetMapping(path = "/{id}/addresses", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
    public ResponseEntity<?> getAddresses(@PathVariable String id) {

        List<AddressesRest> addressesListRestModel = new ArrayList<>();
        CollectionModel<AddressesRest> resources = CollectionModel.empty();

        List<AddressDTO> addressesDTO = addressService.getAddresses(id);

        if(!addressesDTO.isEmpty()) {
            Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
            addressesListRestModel = new ModelMapper().map(addressesDTO, listType);

            resources = CollectionModel.of(addressesListRestModel);

            for (AddressesRest addressesRest : resources) {
                Link addressesLink = linkTo(methodOn(UserController.class).getAddresses(id)).withSelfRel();
                addressesRest.add(addressesLink);
                
                Link addressLink = linkTo(methodOn(UserController.class).getAddress(id, addressesRest.getAddressId())).withRel("address");
                addressesRest.add(addressLink);
                
                Link userLink = linkTo(methodOn(UserController.class).getUser(id)).withRel("user");
				addressesRest.add(userLink);
            }
        }
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    //http://localhost:8080/mobile-app-ws/users/oasdiufoasdf/addresses/pasaflaLJsal
    @GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
    public ResponseEntity<?> getAddress(@PathVariable String userId, @PathVariable String addressId) {
        AddressDTO addressDTO = addressService.getAddress(addressId);

        ModelMapper modelMapper = new ModelMapper();
        Link addressLink = linkTo(methodOn(UserController.class).getAddress(userId, addressId)).withSelfRel();
        Link addressesLink = linkTo(methodOn(UserController.class).getAddresses(userId)).withRel("addresses");
        Link userLink = linkTo(methodOn(UserController.class).getUser(userId)).withRel("user");

        AddressesRest addressesRestModel = modelMapper.map(addressDTO, AddressesRest.class);
        EntityModel<AddressesRest> resource = EntityModel.of(addressesRestModel);
        resource.add(addressLink);
        resource.add(addressesLink);
        resource.add(userLink); 

        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /*
     * http://localhost:8080/mobile-app-ws/users/email-verification?token=saDasdASD
     */
    @GetMapping(path = "/email-verification", produces = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE })
    public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {

        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());

        boolean isVerified = userService.verifyEmailToken(token);

        if (isVerified) {
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        } else {
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }

        return returnValue;
    }
}
