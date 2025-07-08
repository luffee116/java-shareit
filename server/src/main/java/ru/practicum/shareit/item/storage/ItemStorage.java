package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemStorage extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.ownerId = :ownerId ORDER BY i.id")
    List<Item> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query(" select i from Item i " +
            "where upper(i.name) like upper(concat('%', :text, '%')) " +
            "   or upper(i.description) like upper(concat('%', :text, '%'))")
    List<Item> search(@Param("text") String text);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.ownerId = :ownerId")
    long countByOwnerId(@Param("ownerId") Long ownerId);

    List<Item> findItemsByRequestId(@Param("requestId") Long requestId);

}