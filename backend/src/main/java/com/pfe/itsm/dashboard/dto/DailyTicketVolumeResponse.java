package com.pfe.itsm.dashboard.dto;

import java.time.LocalDate;

public record DailyTicketVolumeResponse(
        LocalDate date,
        long total
) {
}
