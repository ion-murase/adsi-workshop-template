package com.example.attendance.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(
        @NotBlank(message = "却下理由は必須です")
        @Size(max = 1000, message = "却下理由は1000文字以内で入力してください")
        String comment
) {}
