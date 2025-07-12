package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.RequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemStorage itemStorage;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestInputDto requestDto) {
        User requestor = UserMapper.toUser(userService.get(userId));
        ItemRequest request = RequestMapper.toEntity(requestDto, requestor, LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(request);
        return convertToDto(savedRequest, List.of());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userService.checkUserExists(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        Map<Long, List<ItemDto>> itemsMap = createItemsMap(requests);

        return requests.stream()
                .map(request -> convertToDto(request, itemsMap.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        userService.checkUserExists(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<ItemRequest> requests = itemRequestRepository.findAllByOtherUsers(userId, pageable);

        Map<Long, List<ItemDto>> itemsMap = createItemsMap(requests);

        return requests.stream()
                .map(request -> convertToDto(request, itemsMap.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }


    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userService.checkUserExists(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID=" + requestId + " не найден"));

        List<Item> items = itemStorage.findItemsByRequestId(requestId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return convertToDto(request, itemDtos);
    }


    private ItemRequestDto convertToDto(ItemRequest request, List<ItemDto> items) {
        return RequestMapper.toDto(request, items);
    }

    private Map<Long, List<ItemDto>> createItemsMap(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> allItems = itemStorage.findAllByRequestIds(requestIds);

        return allItems.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getRequest().getId(),
                        Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                ));
    }
}
