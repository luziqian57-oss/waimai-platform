package com.waimai.platform.dto;

import com.waimai.platform.model.Address;

public record AddressResponse(
        Long id, String contactName, String contactPhone, String province, String city,
        String district, String detailAddress, boolean isDefault) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(), address.getContactName(), address.getContactPhone(), address.getProvince(),
                address.getCity(), address.getDistrict(), address.getDetailAddress(), address.getIsDefault() == 1
        );
    }
}
