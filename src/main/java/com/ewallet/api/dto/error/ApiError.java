package com.ewallet.api.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


// This class is used as a DTO that contains the parameters
// for error messages returned by the api.


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {
    private String message; // The error description message.
    private int status; // The HTTP status code.
    private LocalDateTime timestamp; // The exact time when the error occurred
    private String path; // The URL endpoint where the error occurred
}
