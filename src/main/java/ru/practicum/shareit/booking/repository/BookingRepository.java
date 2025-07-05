package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status);

    List<Booking> findByItemId(Long itemId);

    Boolean existsByBookerIdAndItemIdAndEndBefore(
            Long bookerId, Long itemId, LocalDateTime date);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item IN :items AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    List<Booking> findApprovedBookingsForItems(@Param("items") List<Item> items);
}
