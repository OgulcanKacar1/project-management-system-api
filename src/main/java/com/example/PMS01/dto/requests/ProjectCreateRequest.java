package com.example.PMS01.dto.requests;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {

    @NotBlank(message = "Proje adı boş olamaz")
    @Size(max = 255, message = "Proje adı en fazla 255 karakter olabilir")
    private String name;

    private String description;
}
