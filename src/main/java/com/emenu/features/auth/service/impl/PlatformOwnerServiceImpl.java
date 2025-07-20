package com.emenu.features.auth.service.impl;

import com.emenu.enums.*;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.PlatformMessageRequest;
import com.emenu.features.auth.dto.request.PlatformUserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.response.PlatformStatsResponse;
import com.emenu.features.auth.dto.response.PlatformUserResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.auth.dto.update.PlatformUserUpdateRequest;
import com.emenu.features.auth.mapper.BusinessMapper;
import com.emenu.features.auth.mapper.CustomerMapper;
import com.emenu.features.auth.mapper.PlatformMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.PlatformOwnerService;
import com.emenu.features.auth.specication.BusinessSpecification;
import com.emenu.features.auth.specication.UserSpecification;
import com.emenu.features.messaging.dto.filter.MessageFilterRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.features.messaging.mapper.MessageMapper;
import com.emenu.features.messaging.models.Message;
import com.emenu.features.messaging.repository.MessageRepository;
import com.emenu.features.messaging.specication.MessageSpecification;
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
public class PlatformOwnerServiceImpl implements PlatformOwnerService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final RoleRepository roleRepository;
    private final MessageRepository messageRepository;
    private final PlatformMapper platformMapper;
    private final BusinessMapper businessMapper;
    private final CustomerMapper customerMapper;
    private final MessageMapper messageMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    // Platform User Management
    @Override
    public PlatformUserResponse createPlatformUser(PlatformUserCreateRequest request) {
        log.info("Creating platform user: {}", request.getEmail());

        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = platformMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(UserType.PLATFORM_USER);

        // Set roles
        List<Role> roles = roleRepository.findByNameIn(request.getRoles());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("Platform user created successfully: {}", savedUser.getEmail());

        return platformMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<PlatformUserResponse> getAllPlatformUsers(UserFilterRequest filter) {
        filter.setUserType(UserType.PLATFORM_USER);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Specification<User> spec = UserSpecification.buildSpecification(filter);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<PlatformUserResponse> content = platformMapper.toResponseList(userPage.getContent());

        return PaginationResponse.<PlatformUserResponse>builder()
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
    public PlatformUserResponse getPlatformUserById(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Platform user not found"));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        return platformMapper.toResponse(user);
    }

    @Override
    public PlatformUserResponse updatePlatformUser(UUID id, PlatformUserUpdateRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Platform user not found"));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        platformMapper.updateEntity(request, user);

        if (request.getRoles() != null) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        log.info("Platform user updated successfully: {}", updatedUser.getEmail());

        return platformMapper.toResponse(updatedUser);
    }

    @Override
    public void deletePlatformUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Platform user not found"));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        user.softDelete();
        userRepository.save(user);
        log.info("Platform user deleted successfully: {}", user.getEmail());
    }

    // Business Management
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BusinessResponse> getAllBusinesses(BusinessFilterRequest filter) {
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Specification<Business> spec = BusinessSpecification.buildSpecification(filter);
        Page<Business> businessPage = businessRepository.findAll(spec, pageable);

        List<BusinessResponse> content = businessMapper.toResponseList(businessPage.getContent());

        return PaginationResponse.<BusinessResponse>builder()
                .content(content)
                .pageNo(businessPage.getNumber() + 1)
                .pageSize(businessPage.getSize())
                .totalElements(businessPage.getTotalElements())
                .totalPages(businessPage.getTotalPages())
                .first(businessPage.isFirst())
                .last(businessPage.isLast())
                .hasNext(businessPage.hasNext())
                .hasPrevious(businessPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        return businessMapper.toResponse(business);
    }

    @Override
    public BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        businessMapper.updateEntity(request, business);
        Business updatedBusiness = businessRepository.save(business);
        log.info("Business updated successfully: {}", updatedBusiness.getName());

        return businessMapper.toResponse(updatedBusiness);
    }

    @Override
    public void deleteBusiness(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.softDelete();
        businessRepository.save(business);
        log.info("Business deleted successfully: {}", business.getName());
    }

    @Override
    public void suspendBusiness(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setStatus(BusinessStatus.SUSPENDED);
        businessRepository.save(business);
        log.info("Business suspended successfully: {}", business.getName());

        // Send notification to business
        sendBusinessStatusMessage(business, "suspended");
    }

    @Override
    public void activateBusiness(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setStatus(BusinessStatus.ACTIVE);
        businessRepository.save(business);
        log.info("Business activated successfully: {}", business.getName());

        // Send notification to business
        sendBusinessStatusMessage(business, "activated");
    }

    // Customer Management
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CustomerResponse> getAllCustomers(UserFilterRequest filter) {
        filter.setUserType(UserType.CUSTOMER);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Specification<User> spec = UserSpecification.buildSpecification(filter);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<CustomerResponse> content = customerMapper.toResponseList(userPage.getContent());

        return PaginationResponse.<CustomerResponse>builder()
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
    public CustomerResponse getCustomerById(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        return customerMapper.toResponse(user);
    }

    @Override
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        customerMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);
        log.info("Customer updated successfully: {}", updatedUser.getEmail());

        return customerMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteCustomer(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        user.softDelete();
        userRepository.save(user);
        log.info("Customer deleted successfully: {}", user.getEmail());
    }

    // Platform Messaging
    @Override
    public void sendPlatformMessage(PlatformMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        log.info("Sending platform message from: {}", currentUser.getEmail());

        if (request.getSendToAll()) {
            sendMessageToAllUsers(currentUser, request);
        } else if (request.getUserTypes() != null && !request.getUserTypes().isEmpty()) {
            sendMessageToUserTypes(currentUser, request);
        } else if (request.getBusinessIds() != null && !request.getBusinessIds().isEmpty()) {
            sendMessageToBusinesses(currentUser, request);
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            sendMessageToSpecificUsers(currentUser, request);
        }

        log.info("Platform message sent successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageResponse> getAllMessages(MessageFilterRequest filter) {
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Specification<Message> spec = MessageSpecification.buildSpecification(filter);
        Page<Message> messagePage = messageRepository.findAll(spec, pageable);

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
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(UUID id) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        return messageMapper.toResponse(message);
    }

    @Override
    public void deleteMessage(UUID id) {
        Message message = messageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.softDelete();
        messageRepository.save(message);
        log.info("Message deleted successfully: {}", id);
    }

    // Platform Statistics
    @Override
    @Transactional(readOnly = true)
    public PlatformStatsResponse getPlatformStats() {
        PlatformStatsResponse stats = new PlatformStatsResponse();
        
        stats.setTotalUsers(userRepository.countByIsDeletedFalse());
        stats.setTotalBusinesses(businessRepository.countByIsDeletedFalse());
        stats.setTotalCustomers(userRepository.countByUserTypeAndIsDeletedFalse(UserType.CUSTOMER));
        stats.setActiveBusinesses(businessRepository.countByStatusAndIsDeletedFalse(BusinessStatus.ACTIVE));
        stats.setSuspendedBusinesses(businessRepository.countByStatusAndIsDeletedFalse(BusinessStatus.SUSPENDED));
        stats.setTotalMessages(messageRepository.countByIsDeletedFalse());
        stats.setUnreadMessages(messageRepository.countByStatusAndIsDeletedFalse(MessageStatus.SENT));

        return stats;
    }

    // System Management
    @Override
    public void lockUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.LOCKED);
        userRepository.save(user);
        log.info("User locked successfully: {}", user.getEmail());

        // Send notification
        sendUserStatusMessage(user, "locked");
    }

    @Override
    public void unlockUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("User unlocked successfully: {}", user.getEmail());

        // Send notification
        sendUserStatusMessage(user, "unlocked");
    }

    @Override
    public void resetUserPassword(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String tempPassword = "TempPass123!";
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getEmail());

        // Send notification with temporary password
        sendPasswordResetMessage(user, tempPassword);
    }

    // Helper methods
    private void sendMessageToAllUsers(User sender, PlatformMessageRequest request) {
        List<User> allUsers = userRepository.findByIsDeletedFalse();
        sendBulkMessage(sender, allUsers, request);
    }

    private void sendMessageToUserTypes(User sender, PlatformMessageRequest request) {
        List<User> users = userRepository.findByUserTypeInAndIsDeletedFalse(request.getUserTypes());
        sendBulkMessage(sender, users, request);
    }

    private void sendMessageToBusinesses(User sender, PlatformMessageRequest request) {
        List<User> users = userRepository.findByBusinessIdInAndIsDeletedFalse(request.getBusinessIds());
        sendBulkMessage(sender, users, request);
    }

    private void sendMessageToSpecificUsers(User sender, PlatformMessageRequest request) {
        List<User> users = userRepository.findByIdInAndIsDeletedFalse(request.getUserIds());
        sendBulkMessage(sender, users, request);
    }

    private void sendBulkMessage(User sender, List<User> recipients, PlatformMessageRequest request) {
        for (User recipient : recipients) {
            Message message = new Message();
            message.setSenderId(sender.getId());
            message.setSenderEmail(sender.getEmail());
            message.setSenderName(sender.getFullName());
            message.setRecipientId(recipient.getId());
            message.setRecipientEmail(recipient.getEmail());
            message.setRecipientName(recipient.getFullName());
            message.setSubject(request.getSubject());
            message.setContent(request.getContent());
            message.setMessageType(request.getMessageType());
            message.setPriority(request.getPriority());
            message.setBusinessId(recipient.getBusinessId());

            messageRepository.save(message);
        }
    }

    private void sendBusinessStatusMessage(Business business, String action) {
        // Find business users and send notification
        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(business.getId());
        
        for (User user : businessUsers) {
            Message message = new Message();
            message.setSenderId(null); // System message
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientId(user.getId());
            message.setRecipientEmail(user.getEmail());
            message.setRecipientName(user.getFullName());
            message.setSubject("Business Status Update");
            message.setContent(String.format("Your business '%s' has been %s.", business.getName(), action));
            message.setMessageType(MessageType.NOTIFICATION);
            message.setBusinessId(business.getId());

            messageRepository.save(message);
        }
    }

    private void sendUserStatusMessage(User user, String action) {
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
    }

    private void sendPasswordResetMessage(User user, String tempPassword) {
        Message message = new Message();
        message.setSenderId(null); // System message
        message.setSenderEmail("system@emenu-platform.com");
        message.setSenderName("E-Menu Platform");
        message.setRecipientId(user.getId());
        message.setRecipientEmail(user.getEmail());
        message.setRecipientName(user.getFullName());
        message.setSubject("Password Reset");
        message.setContent(String.format("Your password has been reset. Temporary password: %s", tempPassword));
        message.setMessageType(MessageType.NOTIFICATION);
        message.setBusinessId(user.getBusinessId());

        messageRepository.save(message);
    }
}