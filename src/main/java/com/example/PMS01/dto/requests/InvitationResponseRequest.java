package com.example.PMS01.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseRequest {
    private Long invitationId;
    private boolean accept;
}
