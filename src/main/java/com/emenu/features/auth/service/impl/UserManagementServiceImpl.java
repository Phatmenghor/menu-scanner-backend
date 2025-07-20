package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.MessageType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.request.UserMessageRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.response.UserSummaryResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.UserManagementService;
import com.emenu.features.auth.specication.UserSpecification;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.features.messaging.mapper.MessageMapper;
import com.emenu.features.messaging.models.Message;
import com.emenu.features.messaging.repository.MessageRepository;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageRepository messageRepository;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    // User CRUD
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user: {}", request.getEmail());

        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getEmail());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getUsers(UserFilterRequest filter) {
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Specification<User> spec = UserSpecification.buildSpecification(filter);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserSummaryResponse> content = userMapper.toSummaryResponseList(userPage.getContent());

        return PaginationResponse.<UserSummaryResponse>builder()
                .content(content)
                .pageNo(userPage.getNumber() + 1)
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userMapper.updateEntity(request, user);

        if (request.getRoles() != null) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getEmail());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.softDelete();
        userRepository.save(user);
        
        log.info("User deleted successfully: {}", user.getEmail());
    }

    @Override
    public void activateUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("User activated successfully: {}", user.getEmail());
        sendStatusChangeMessage(user, "activated");
    }

    @Override
    public void deactivateUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", user.getEmail());
        sendStatusChangeMessage(user, "deactivated");
    }

    @Override
    public void lockUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.LOCKED);
        userRepository.save(user);
        
        log.info("User locked successfully: {}", user.getEmail());
        sendStatusChangeMessage(user, "locked");
    }

    @Override
    public void unlockUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("User unlocked successfully: {}", user.getEmail());
        sendStatusChangeMessage(user, "unlocked");
    }

    // User Messaging
    @Override
    public void sendMessageToUser(UUID userId, UserMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        User recipient = userRepository.findByIdAndIsDeletedFalse(request.getRecipientId())
                .orElseThrow(() -> new UserNotFoundException("Recipient not found"));

        Message message = new Message();
        message.setSenderId(currentUser.getId());
        message.setSenderEmail(currentUser.getEmail());
        message.setSenderName(currentUser.getFullName());
        message.setRecipientId(recipient.getId());
        message.setRecipientEmail(recipient.getEmail());
        message.setRecipientName(recipient.getFullName());
        message.setSubject(request.getSubject());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setPriority(request.getPriority());
        message.setBusinessId(currentUser.getBusinessId());

        messageRepository.save(message);
        log.info("Message sent to user: {}", recipient.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageResponse> getUserMessages(UUID userId, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findByRecipientIdAndIsDeletedFalse(userId, pageable);
        List<MessageResponse> content = messageMapper.toResponseList(messagePage.getContent());

        return PaginationResponse.<MessageResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    @Override
    public void markMessageAsRead(UUID messageId) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.markAsRead();
        messageRepository.save(message);
        
        log.info("Message marked as read: {}", messageId);
    }

    // Profile Management
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User currentUser = securityUtils.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public UserResponse updateCurrentUserProfile(UserUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        userMapper.updateEntity(request, currentUser);
        User updatedUser = userRepository.save(currentUser);
        
        log.info("Profile updated for user: {}", currentUser.getEmail());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageResponse> getCurrentUserMessages(int pageNo, int pageSize) {
        User currentUser = securityUtils.getCurrentUser();
        return getUserMessages(currentUser.getId(), pageNo, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageResponse> getCurrentUserUnreadMessages(int pageNo, int pageSize) {
        User currentUser = securityUtils.getCurrentUser();
        
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findUnreadByRecipientIdAndIsDeletedFalse(currentUser.getId(), pageable);
        List<MessageResponse> content = messageMapper.toResponseList(messagePage.getContent());

        return PaginationResponse.<MessageResponse>builder()
                .content(content)
                .pageNo(messagePage.getNumber() + 1)
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    // Helper methods
    private void sendStatusChangeMessage(User user, String action) {
        try {
            Message message = new Message();
            message.setSenderId(null); // System message
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientId(user.getId());
            message.setRecipientEmail(user.getEmail());
            message.setRecipientName(user.getFullName());
            message.setSubject("Account Status Update");
            message.setContent(String.format("Your account has been %s.", action));
            message.setMessageType(MessageType.NOTIFICATION);
            message.setBusinessId(user.getBusinessId());

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send status change message to: {}", user.getEmail(), e);
        }
    }
}