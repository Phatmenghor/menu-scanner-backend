package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.room.request.RoomFilterDto;
import com.menghor.ksit.feature.master.dto.room.request.RoomRequestDto;
import com.menghor.ksit.feature.master.dto.room.response.RoomResponseDto;
import com.menghor.ksit.feature.master.mapper.RoomMapper;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.repository.RoomRepository;
import com.menghor.ksit.feature.master.service.RoomService;
import com.menghor.ksit.feature.master.specification.RoomSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public RoomResponseDto createRoom(RoomRequestDto roomRequest) {
        RoomEntity room = roomMapper.toEntity(roomRequest);
        log.info("Creating room: {}", room);

        RoomEntity roomSave = roomRepository.save(room);
        return roomMapper.toResponseDto(roomSave);
    }

    public RoomResponseDto getRoomById(Long id) {
        log.info("Getting room by id: {}", id);
        RoomEntity room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room " + id + " not found. Please try again."));

        return roomMapper.toResponseDto(room);
    }

    public RoomResponseDto updateRoomById(RoomRequestDto roomRequest, Long id) {
        log.info("Updating room by id: {}", id);
        RoomEntity room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room " + id + " not found. Please try again."));

        room.setName(roomRequest.getName());
        room.setStatus(roomRequest.getStatus());

        RoomEntity roomUpdate = roomRepository.save(room);
        return roomMapper.toResponseDto(roomUpdate);
    }

    public RoomResponseDto deleteRoomById(Long id) {
        log.info("Deleting room by id: {}", id);
        RoomEntity room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room " + id + " not found. Please try again."));

        roomRepository.delete(room);
        return roomMapper.toResponseDto(room);
    }

    // Get all room
    public CustomPaginationResponseDto<RoomResponseDto> getAllRoom(RoomFilterDto filterDto) {
        log.info("Getting all user rooms {}", filterDto);
        return getRoomWithSpecification(filterDto, RoomSpecification::combine);
    }

    private CustomPaginationResponseDto<RoomResponseDto> getRoomWithSpecification(
            RoomFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default pagination
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Specification<RoomEntity> spec = specificationCreator.createSpecification(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        Page<RoomEntity> roomPage = roomRepository.findAll(spec, pageable);

        // Optional status correction
        roomPage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                room.setStatus(Status.ACTIVE);
                roomRepository.save(room);
            }
        });

        return roomMapper.toRoomAllResponseDto(roomPage);
    }

    @FunctionalInterface
    private interface SpecificationCreator {
        Specification<RoomEntity> createSpecification(String name, Status status);
    }
}
