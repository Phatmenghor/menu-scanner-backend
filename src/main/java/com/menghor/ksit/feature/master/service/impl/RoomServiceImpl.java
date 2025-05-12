package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
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

        // Determine the status (default to ACTIVE if not specified)
        Status status = roomRequest.getStatus() != null ?
                roomRequest.getStatus() : Status.ACTIVE;

        // Only check for duplicates if this room will be ACTIVE
        if (status == Status.ACTIVE) {
            // Check if an ACTIVE room with the same name already exists
            boolean activeRoomExists = roomRepository.existsByNameAndStatus(
                    roomRequest.getName(), Status.ACTIVE);

            if (activeRoomExists) {
                throw new DuplicateNameException("Room with name '" +
                        roomRequest.getName() + "' already exists");
            }
        }

        // Proceed with room creation
        RoomEntity room = roomMapper.toEntity(roomRequest);

        // Ensure status is set if it wasn't specified
        if (room.getStatus() == null) {
            room.setStatus(Status.ACTIVE);
        }

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

        // Determine what the status will be after the update
        Status newStatus = roomRequest.getStatus() != null ?
                roomRequest.getStatus() : existingRoom.getStatus();

        // If the new status will be ACTIVE and name is changing, check for duplicates
        if (newStatus == Status.ACTIVE &&
                roomRequest.getName() != null &&
                !roomRequest.getName().equals(existingRoom.getName())) {

            boolean activeRoomExists = roomRepository.existsByNameAndStatus(
                    roomRequest.getName(), Status.ACTIVE);

            if (activeRoomExists) {
                throw new DuplicateNameException("Room with name '" +
                        roomRequest.getName() + "' already exists");
            }
        }

        // If the status is changing to ACTIVE (from non-ACTIVE) and the name isn't changing,
        // we still need to check if another ACTIVE room with the same name exists
        if (newStatus == Status.ACTIVE &&
                existingRoom.getStatus() != Status.ACTIVE) {

            boolean activeRoomWithSameNameExists = roomRepository.existsByNameAndStatusAndIdNot(
                    existingRoom.getName(), Status.ACTIVE, id);

            if (activeRoomWithSameNameExists) {
                throw new DuplicateNameException("Room with name '" +
                        existingRoom.getName() + "' already exists");
            }
        }

        // Proceed with update
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
        room.setStatus(Status.DELETED);

        room = roomRepository.save(room);
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