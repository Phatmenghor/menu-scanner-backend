package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.filter.RoomFilterDto;
import com.menghor.ksit.feature.master.dto.request.RoomRequestDto;
import com.menghor.ksit.feature.master.dto.response.RoomResponseDto;
import com.menghor.ksit.feature.master.dto.update.RoomUpdateDto;
import com.menghor.ksit.feature.master.mapper.RoomMapper;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.repository.RoomRepository;
import com.menghor.ksit.feature.master.service.RoomService;
import com.menghor.ksit.feature.master.specification.RoomSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public RoomResponseDto createRoom(RoomRequestDto roomRequest) {
        log.info("Creating new room with name: {}", roomRequest.getName());

        RoomEntity room = roomMapper.toEntity(roomRequest);
        RoomEntity savedRoom = roomRepository.save(room);

        log.info("Room created successfully with ID: {}", savedRoom.getId());
        return roomMapper.toResponseDto(savedRoom);
    }

    @Override
    public RoomResponseDto getRoomById(Long id) {
        log.info("Fetching room by ID: {}", id);

        RoomEntity room = findRoomById(id);

        log.info("Retrieved room with ID: {}", id);
        return roomMapper.toResponseDto(room);
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoomById(RoomUpdateDto roomRequest, Long id) {
        log.info("Updating room with ID: {}", id);

        // Find the existing entity
        RoomEntity existingRoom = findRoomById(id);

        // Use MapStruct to update only non-null fields
        roomMapper.updateEntityFromDto(roomRequest, existingRoom);

        // Save the updated entity
        RoomEntity updatedRoom = roomRepository.save(existingRoom);
        log.info("Room updated successfully with ID: {}", id);

        return roomMapper.toResponseDto(updatedRoom);
    }

    @Override
    @Transactional
    public RoomResponseDto deleteRoomById(Long id) {
        log.info("Deleting room with ID: {}", id);

        RoomEntity room = findRoomById(id);

        roomRepository.delete(room);
        log.info("Room deleted successfully with ID: {}", id);

        return roomMapper.toResponseDto(room);
    }

    @Override
    public CustomPaginationResponseDto<RoomResponseDto> getAllRoom(RoomFilterDto filterDto) {
        log.info("Fetching all rooms with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<RoomEntity> spec = RoomSpecification.combine(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        // Execute query with specification and pagination
        Page<RoomEntity> roomPage = roomRepository.findAll(spec, pageable);

        // Apply status correction for any null statuses
        roomPage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for room ID: {}", room.getId());
                room.setStatus(Status.ACTIVE);
                roomRepository.save(room);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<RoomResponseDto> response = roomMapper.toRoomAllResponseDto(roomPage);
        log.info("Retrieved {} rooms (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    /**
     * Helper method to find a room by ID or throw NotFoundException
     */
    private RoomEntity findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found with ID: {}", id);
                    return new NotFoundException("Room id " + id + " not found. Please try again.");
                });
    }
}