package com.hospitality.operations.domain.assistant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.domain.assistant.dto.AssistantQueryDto;
import com.hospitality.operations.domain.assistant.dto.AssistantResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/query")
    public ResponseEntity<AssistantResponseDto> query(@Valid @RequestBody AssistantQueryDto dto) {
        AssistantResponseDto response = assistantService.processQuery(dto);
        return ResponseEntity.ok(response);
    }
}
