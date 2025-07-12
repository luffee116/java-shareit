package ru.practicum.shareit.request;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;


public class RequestMapper {
    public static ItemRequest toEntity(ItemRequestInputDto itemRequest, User user, LocalDateTime localDateTime) {
        return ItemRequest.builder()
                .description(itemRequest.getDescription())
                .requestor(user)
                .created(localDateTime)
                .build();

    }

    public static ItemRequestDto toDto(ItemRequest itemRequest, List<ItemDto> items) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestorId(itemRequest.getRequestor().getId())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }
}
