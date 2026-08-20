package com.waimai.platform.service;

import com.waimai.platform.dto.AddressRequest;
import com.waimai.platform.dto.AddressResponse;
import com.waimai.platform.exception.BusinessException;
import com.waimai.platform.mapper.AddressMapper;
import com.waimai.platform.mapper.UserMapper;
import com.waimai.platform.model.Address;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressMapper addressMapper;
    private final UserMapper userMapper;

    public AddressService(AddressMapper addressMapper, UserMapper userMapper) {
        this.addressMapper = addressMapper;
        this.userMapper = userMapper;
    }

    public List<AddressResponse> list(String username) {
        return addressMapper.findByUserId(userId(username)).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse create(String username, AddressRequest request) {
        Long userId = userId(username);
        boolean makeDefault = Boolean.TRUE.equals(request.isDefault()) || addressMapper.findByUserId(userId).isEmpty();
        if (makeDefault) {
            addressMapper.clearDefault(userId);
        }
        Address address = build(null, userId, request, makeDefault ? 1 : 0);
        addressMapper.insert(address);
        return AddressResponse.from(address);
    }

    @Transactional
    public AddressResponse update(String username, Long id, AddressRequest request) {
        Long userId = userId(username);
        Address existing = requireOwned(id, userId);
        boolean makeDefault = Boolean.TRUE.equals(request.isDefault());
        if (makeDefault) {
            addressMapper.clearDefault(userId);
        }
        int defaultValue = makeDefault ? 1 : existing.getIsDefault();
        Address address = build(id, userId, request, defaultValue);
        addressMapper.update(address);
        return AddressResponse.from(address);
    }

    @Transactional
    public void setDefault(String username, Long id) {
        Long userId = userId(username);
        requireOwned(id, userId);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(id, userId);
    }

    @Transactional
    public void delete(String username, Long id) {
        Long userId = userId(username);
        Address existing = requireOwned(id, userId);
        addressMapper.deleteOwned(id, userId);
        if (existing.getIsDefault() == 1) {
            List<Address> remaining = addressMapper.findByUserId(userId);
            if (!remaining.isEmpty()) {
                addressMapper.setDefault(remaining.getFirst().getId(), userId);
            }
        }
    }

    private Address requireOwned(Long id, Long userId) {
        Address address = addressMapper.findOwned(id, userId);
        if (address == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "收货地址不存在");
        }
        return address;
    }

    private Long userId(String username) {
        var user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        return user.getId();
    }

    private Address build(Long id, Long userId, AddressRequest request, int isDefault) {
        Address address = new Address();
        address.setId(id);
        address.setUserId(userId);
        address.setContactName(request.contactName());
        address.setContactPhone(request.contactPhone());
        address.setProvince(request.province());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setDetailAddress(request.detailAddress());
        address.setIsDefault(isDefault);
        return address;
    }
}
