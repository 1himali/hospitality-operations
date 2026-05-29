package com.hospitality.operations.domain.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssistantQueryDto {

    @NotBlank(message = "Query is required")
    @Size(max = 500)
    private String query;
}
