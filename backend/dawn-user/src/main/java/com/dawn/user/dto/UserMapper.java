package com.dawn.user.dto;

import com.dawn.user.entity.AddressEntry;
import com.dawn.user.entity.User;
import com.dawn.user.entity.enums.SystemRole;

import java.util.List;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummaryDto toSummary(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getSystemRole());
    }

    public static AddressDto toAddressDto(AddressEntry entry) {
        return new AddressDto(
                entry.getId(),
                entry.getFullName(),
                entry.getPhone(),
                entry.getLine1(),
                entry.getWard(),
                entry.getDistrict(),
                entry.getProvince(),
                entry.isDefault());
    }

    public static AddressEntry toAddressEntry(AddressDto dto) {
        return AddressEntry.builder()
                .id(dto.id())
                .fullName(dto.fullName())
                .phone(dto.phone())
                .line1(dto.street())
                .ward(dto.ward())
                .district(dto.district())
                .province(dto.city())
                .isDefault(dto.isDefault())
                .build();
    }

    public static List<AddressDto> toAddressDtos(List<AddressEntry> entries) {
        return entries.stream().map(UserMapper::toAddressDto).toList();
    }

    public static String resolveRoleLabel(User user) {
        if (user.getSystemRole() == SystemRole.ADMIN) {
            return "ADMIN";
        }

        return user.getSystemRole().name();
    }
}
