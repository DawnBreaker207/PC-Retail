package com.dawn.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressEntry {

    private String id;

    private String fullName;

    private String phone;

    private String line1;

    private String ward;

    private String district;

    private String province;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
