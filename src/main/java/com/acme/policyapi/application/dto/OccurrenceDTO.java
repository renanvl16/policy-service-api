package com.acme.policyapi.application.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO para representar uma ocorrência na análise de fraudes.
 *
 * @author Sistema ACME
 */
@Getter
@Setter
public class OccurrenceDTO {
    private String id;
    private Long productId;
    private String type;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
