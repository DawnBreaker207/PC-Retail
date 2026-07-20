package com.dawn.user.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.user.controller.request.AddressRequest;
import com.dawn.user.controller.request.UpdateProfileRequest;
import com.dawn.user.controller.response.UserResponse;
import com.dawn.user.dto.AddressDto;
import com.dawn.user.dto.UserMapper;
import com.dawn.user.entity.AddressEntry;
import com.dawn.user.entity.User;
import com.dawn.user.port.in.AddressUseCase;
import com.dawn.user.port.in.ProfileUseCase;
import com.dawn.user.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements ProfileUseCase, AddressUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return toResponse(loadCurrentUser());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = loadCurrentUser();

        if (StringUtils.hasText(request.fullName())) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getAddresses() {
        return UserMapper.toAddressDtos(loadCurrentUser().getAddresses());
    }

    @Override
    @Transactional
    public AddressDto addAddress(AddressRequest request) {
        User user = loadCurrentUser();
        List<AddressEntry> addresses = new ArrayList<>(user.getAddresses());

        AddressEntry entry = toEntry(request);
        entry.setId(UUID.randomUUID().toString());

        if (entry.isDefault()) {
            addresses.forEach(address -> address.setDefault(false));
        } else if (addresses.isEmpty()) {
            entry.setDefault(true);
        }

        addresses.add(entry);
        user.setAddresses(addresses);
        userRepository.save(user);
        return UserMapper.toAddressDto(entry);
    }

    @Override
    @Transactional
    public AddressDto updateAddress(String addressId, AddressRequest request) {
        User user = loadCurrentUser();
        List<AddressEntry> addresses = new ArrayList<>(user.getAddresses());

        AddressEntry updated = addresses.stream()
                .filter(address -> address.getId().equals(addressId))
                .findFirst()
                .map(address -> {
                    AddressEntry entry = toEntry(request);
                    entry.setId(addressId);
                    return entry;
                })
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.User.ADDRESS_NOT_FOUND, addressId)));

        if (updated.isDefault()) {
            addresses.forEach(address -> address.setDefault(false));
        }

        addresses.replaceAll(address -> address.getId().equals(addressId) ? updated : address);
        user.setAddresses(addresses);
        userRepository.save(user);
        return UserMapper.toAddressDto(updated);
    }

    @Override
    @Transactional
    public void deactivateAccount() {
        User user = loadCurrentUser();
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAddress(String addressId) {
        User user = loadCurrentUser();
        List<AddressEntry> addresses = user.getAddresses().stream()
                .filter(address -> !address.getId().equals(addressId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        if (addresses.size() == user.getAddresses().size()) {
            throw new ResourceNotFoundException(Message.format(Message.User.ADDRESS_NOT_FOUND, addressId));
        }

        if (addresses.stream().noneMatch(AddressEntry::isDefault) && !addresses.isEmpty()) {
            addresses.getFirst().setDefault(true);
        }

        user.setAddresses(addresses);
        userRepository.save(user);
    }

    private User loadCurrentUser() {
        return userRepository.findByIdAndDeletedAtIsNull(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.User.NOT_FOUND, SecurityUtils.getCurrentUserId())));
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                UserMapper.resolveRoleLabel(user));
    }

    private static AddressEntry toEntry(AddressRequest request) {
        return AddressEntry.builder()
                .fullName(request.fullName())
                .phone(request.phone())
                .line1(request.street())
                .ward(request.ward())
                .district(request.district())
                .province(request.city())
                .isDefault(request.isDefault())
                .build();
    }
}
