package com.waimai.platform.controller;

import com.waimai.platform.dto.AddressRequest;
import com.waimai.platform.dto.AddressResponse;
import com.waimai.platform.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressResponse> list(Authentication authentication) {
        return addressService.list(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(Authentication authentication, @Valid @RequestBody AddressRequest request) {
        return addressService.create(authentication.getName(), request);
    }

    @PutMapping("/{id}")
    public AddressResponse update(
            Authentication authentication, @PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return addressService.update(authentication.getName(), id, request);
    }

    @PutMapping("/{id}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setDefault(Authentication authentication, @PathVariable Long id) {
        addressService.setDefault(authentication.getName(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        addressService.delete(authentication.getName(), id);
    }
}
