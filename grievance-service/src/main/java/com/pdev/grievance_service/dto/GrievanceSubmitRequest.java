package com.pdev.grievance_service.dto;

import com.pdev.grievance_service.model.GrievanceCategory;
import com.pdev.grievance_service.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceSubmitRequest {

    // citizenId is optional in the request; server will derive from the authenticated principal when omitted.
    private String citizenId;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private GrievanceCategory category;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private String attachmentUrl;
}
