package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class BookingDto {
    Long id;
    Long bookerId;
}
