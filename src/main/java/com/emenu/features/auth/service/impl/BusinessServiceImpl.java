package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.request.BusinessMessageRequest;
import com.emenu.features.auth.dto.request.BusinessStaffCreateRequest;
import com.emenu.features.auth.dto.request.CustomerMessageRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.BusinessStaffResponse;
import com.emenu.features.auth.dto.response.BusinessStatsResponse;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.BusinessStaffUpdateRequest;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.mapper.BusinessMapper;
import com.emenu.features.auth.mapper.CustomerMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageRepository messageRepository;
    private final BusinessMapper businessMapper;
    private final CustomerMapper customerMapper;
    private final MessageMapper messageMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    // Business Management
    @Override
    public BusinessResponse createBusiness(BusinessCreateRequest request) {
        log.info("Creating business: {}", request.getName());

        if (request.getEmail() != null && businessRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Business email already exists");
        }

        Business business = businessMapper.toEntity(request);
        Business savedBusiness = businessRepository.save(business);

        log.info("Business created successfully: {}", savedBusiness.getName());
        return businessMapper.toResponse(savedBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(UUID id) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        BusinessResponse response = businessMapper.toResponse(business);

        // Add statistics
        response.setTotalStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(id));
        response.setHasActiveSubscription(true); // Simplified for now

        return response;
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
    @Transactional(readOnly = true)
    public BusinessStatsResponse getBusinessStats(UUID id) {
        BusinessStatsResponse stats = new BusinessStatsResponse();

        stats.setTotalStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(id));
        stats.setActiveStaff((int) userRepository.countByBusinessIdAndIsDeletedFalse(id)); // Simplified
        stats.setTotalMessages((int) messageRepository.countByBusinessIdAndIsDeletedFalse(id));
        stats.setUnreadMessages((int) messageRepository.countUnreadByBusinessIdAndIsDeletedFalse(id));
        stats.setCurrentPlan("FREE"); // Simplified
        stats.setSubscriptionActive(true);

        return stats;
    }

    // Staff Management
    @Override
    public BusinessStaffResponse createStaff(UUID businessId, BusinessStaffCreateRequest request) {
        log.info("Creating staff for business: {}", businessId);

        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User staff = new User();
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setPosition(request.getPosition());
        staff.setAddress(request.getAddress());
        staff.setNotes(request.getNotes());
        staff.setUserType(UserType.BUSINESS_USER);
        staff.setAccountStatus(AccountStatus.ACTIVE); // ✅ FIXED - Added missing account status
        staff.setBusinessId(businessId);

        // Set role - use default if not provided
        RoleEnum roleEnum = request.getRole() != null ? request.getRole() : RoleEnum.BUSINESS_STAFF;
        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum));
        staff.setRoles(List.of(role));

        User savedStaff = userRepository.save(staff);
        log.info("Staff created successfully: {}", savedStaff.getEmail());

        return businessMapper.toStaffResponse(savedStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BusinessStaffResponse> getBusinessStaff(UUID businessId, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<User> staffPage = userRepository.findByBusinessIdAndIsDeletedFalse(businessId, pageable);
        List<BusinessStaffResponse> content = businessMapper.toStaffResponseList(staffPage.getContent());

        return PaginationResponse.<BusinessStaffResponse>builder()
                .content(content)
                .pageNo(staffPage.getNumber() + 1)
                .pageSize(staffPage.getSize())
                .totalElements(staffPage.getTotalElements())
                .totalPages(staffPage.getTotalPages())
                .first(staffPage.isFirst())
                .last(staffPage.isLast())
                .hasNext(staffPage.hasNext())
                .hasPrevious(staffPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessStaffResponse getStaffById(UUID staffId) {
        User staff = userRepository.findByIdAndIsDeletedFalse(staffId)
                .orElseThrow(() -> new UserNotFoundException("Staff not found"));

        if (!staff.isBusinessUser()) {
            throw new ValidationException("User is not a business staff member");
        }

        return businessMapper.toStaffResponse(staff);
    }

    @Override
    public BusinessStaffResponse updateStaff(UUID staffId, BusinessStaffUpdateRequest request) {
        User staff = userRepository.findByIdAndIsDeletedFalse(staffId)
                .orElseThrow(() -> new UserNotFoundException("Staff not found"));

        if (!staff.isBusinessUser()) {
            throw new ValidationException("User is not a business staff member");
        }

        // Update fields
        if (request.getFirstName() != null) staff.setFirstName(request.getFirstName());
        if (request.getLastName() != null) staff.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) staff.setPhoneNumber(request.getPhoneNumber());
        if (request.getPosition() != null) staff.setPosition(request.getPosition());
        if (request.getAddress() != null) staff.setAddress(request.getAddress());
        if (request.getNotes() != null) staff.setNotes(request.getNotes());
        if (request.getAccountStatus() != null) staff.setAccountStatus(request.getAccountStatus());

        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            staff.setRoles(List.of(role));
        }

        User updatedStaff = userRepository.save(staff);
        log.info("Staff updated successfully: {}", updatedStaff.getEmail());

        return businessMapper.toStaffResponse(updatedStaff);
    }

    @Override
    public void deleteStaff(UUID staffId) {
        User staff = userRepository.findByIdAndIsDeletedFalse(staffId)
                .orElseThrow(() -> new UserNotFoundException("Staff not found"));

        if (!staff.isBusinessUser()) {
            throw new ValidationException("User is not a business staff member");
        }

        staff.softDelete();
        userRepository.save(staff);

        log.info("Staff deleted successfully: {}", staff.getEmail());
    }

    @Override
    public void activateStaff(UUID staffId) {
        User staff = userRepository.findByIdAndIsDeletedFalse(staffId)
                .orElseThrow(() -> new UserNotFoundException("Staff not found"));

        staff.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(staff);

        log.info("Staff activated successfully: {}", staff.getEmail());
    }

    @Override
    public void deactivateStaff(UUID staffId) {
        User staff = userRepository.findByIdAndIsDeletedFalse(staffId)
                .orElseThrow(() -> new UserNotFoundException("Staff not found"));

        staff.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(staff);

        log.info("Staff deactivated successfully: {}", staff.getEmail());
    }

    // Business Messaging
    @Override
    public void sendBusinessMessage(UUID businessId, BusinessMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        if (Boolean.TRUE.equals(request.getSendToAllStaff())) {
            sendMessageToAllStaff(currentUser, businessId, request);
        } else if (request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
            sendMessageToSpecificStaff(currentUser, request.getStaffIds(), request);
        } else if (Boolean.TRUE.equals(request.getSendToAllCustomers())) {
            sendMessageToAllCustomers(currentUser, request);
        } else if (request.getCustomerIds() != null && !request.getCustomerIds().isEmpty()) {
            sendMessageToSpecificCustomers(currentUser, request.getCustomerIds(), request);
        }

        log.info("Business message sent successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageResponse> getBusinessMessages(UUID businessId, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findByBusinessIdAndIsDeletedFalse(businessId, pageable);
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
    public PaginationResponse<MessageResponse> getUnreadMessages(UUID businessId, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findUnreadByBusinessIdAndIsDeletedFalse(businessId, pageable);
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

    // Customer Management for Business
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CustomerResponse> getBusinessCustomers(UUID businessId, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        // For now, get all customers (in real implementation, filter by business relationship)
        Page<User> customerPage = userRepository.findByUserTypeAndIsDeletedFalse(UserType.CUSTOMER, pageable);
        List<CustomerResponse> content = customerMapper.toResponseList(customerPage.getContent());

        return PaginationResponse.<CustomerResponse>builder()
                .content(content)
                .pageNo(customerPage.getNumber() + 1)
                .pageSize(customerPage.getSize())
                .totalElements(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .first(customerPage.isFirst())
                .last(customerPage.isLast())
                .hasNext(customerPage.hasNext())
                .hasPrevious(customerPage.hasPrevious())
                .build();
    }

    @Override
    public void sendMessageToCustomer(UUID businessId, UUID customerId, CustomerMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        User customer = userRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        Message message = new Message();
        message.setSenderId(currentUser.getId());
        message.setSenderEmail(currentUser.getEmail());
        message.setSenderName(currentUser.getFullName());
        message.setRecipientId(customer.getId());
        message.setRecipientEmail(customer.getEmail());
        message.setRecipientName(customer.getFullName());
        message.setSubject(request.getSubject());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setPriority(request.getPriority());
        message.setBusinessId(businessId);

        messageRepository.save(message);
        log.info("Message sent to customer: {}", customer.getEmail());
    }

    // Helper methods
    private void sendMessageToAllStaff(User sender, UUID businessId, BusinessMessageRequest request) {
        List<User> staff = userRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        sendBulkMessageToUsers(sender, staff, request, businessId);
    }

    private void sendMessageToSpecificStaff(User sender, List<UUID> staffIds, BusinessMessageRequest request) {
        List<User> staff = userRepository.findByIdInAndIsDeletedFalse(staffIds);
        sendBulkMessageToUsers(sender, staff, request, sender.getBusinessId());
    }

    private void sendMessageToAllCustomers(User sender, BusinessMessageRequest request) {
        List<User> customers = userRepository.findByUserTypeInAndIsDeletedFalse(List.of(UserType.CUSTOMER));
        sendBulkMessageToUsers(sender, customers, request, sender.getBusinessId());
    }

    private void sendMessageToSpecificCustomers(User sender, List<UUID> customerIds, BusinessMessageRequest request) {
        List<User> customers = userRepository.findByIdInAndIsDeletedFalse(customerIds);
        sendBulkMessageToUsers(sender, customers, request, sender.getBusinessId());
    }

    private void sendBulkMessageToUsers(User sender, List<User> recipients, BusinessMessageRequest request, UUID businessId) {
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
            message.setBusinessId(businessId);

            messageRepository.save(message);
        }
    }
}