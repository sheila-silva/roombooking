package com.roombooking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "Name must not be blank.")
    private String name;

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Email must be a valid address.")
    private String email;
}
