package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.MessageType;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.auth.dto.request.CustomerCreateRequest;
import com.emenu.features.auth.dto.request.CustomerMessageRequest;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.auth.mapper.CustomerMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.CustomerService;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageRepository messageRepository;
    private final CustomerMapper customerMapper;
    private final MessageMapper messageMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    // Customer Management
    @Override
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        log.info("Creating customer: {}", request.getEmail());

        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setUserType(UserType.CUSTOMER);

        // Set customer role
        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
        customer.setRoles(List.of(customerRole));

        User savedCustomer = userRepository.save(customer);
        log.info("Customer created successfully: {}", savedCustomer.getEmail());

        // Send welcome message
        sendWelcomeMessage(savedCustomer);

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CustomerResponse> getCustomers(int pageNo, int pageSize, String search) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<User> customerPage;
        if (StringUtils.hasText(search)) {
            customerPage = userRepository.findBySearchAndIsDeletedFalse(search, pageable);
        } else {
            customerPage = userRepository.findByUserTypeAndIsDeletedFalse(UserType.CUSTOMER, pageable);
        }

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
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        User customer = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!customer.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        CustomerResponse response = customerMapper.toResponse(customer);
        
        // Add statistics
        response.setTotalMessages((int) messageRepository.countByRecipientIdAndIsDeletedFalse(id));
        response.setUnreadMessages((int) messageRepository.countUnreadByRecipientIdAndIsDeletedFalse(id));
        
        return response;
    }

    @Override
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        User customer = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!customer.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        customerMapper.updateEntity(request, customer);
        User updatedCustomer = userRepository.save(customer);
        
        log.info("Customer updated successfully: {}", updatedCustomer.getEmail());
        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    public void deleteCustomer(UUID id) {
        User customer = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!customer.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        customer.softDelete();
        userRepository.save(customer);
        
        log.info("Customer deleted successfully: {}", customer.getEmail());
    }

    @Override
    public void activateCustomer(UUID id) {
        User customer = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        customer.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(customer);
        
        log.info("Customer activated successfully: {}", customer.getEmail());
        sendStatusChangeMessage(customer, "activated");
    }

    @Override
    public void deactivateCustomer(UUID id) {
        User customer = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        customer.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(customer);
        
        log.info("Customer deactivated successfully: {}", customer.getEmail());
        sendStatusChangeMessage(customer, "deactivated");
    }

    // Customer Messaging
    @Override
    public void sendMessageToCustomer(UUID customerId, CustomerMessageRequest request) {
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
        message.setBusinessId(currentUser.getBusinessId());

        messageRepository.save(message);
        log.info("Message sent to customer: {}", customer.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageResponse> getCustomerMessages(UUID customerId, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Message> messagePage = messageRepository.findByRecipientIdAndIsDeletedFalse(customerId, pageable);
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

    // Customer Self-Service
    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCurrentCustomerProfile() {
        User currentUser = securityUtils.getCurrentUser();
        
        if (!currentUser.isCustomer()) {
            throw new ValidationException("Current user is not a customer");
        }

        return getCustomerById(currentUser.getId());
    }

    @Override
    public CustomerResponse updateCurrentCustomerProfile(CustomerUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        if (!currentUser.isCustomer()) {
            throw new ValidationException("Current user is not a customer");
        }

        return updateCustomer(currentUser.getId(), request);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<MessageResponse> getCurrentCustomerMessages(int pageNo, int pageSize) {
        User currentUser = securityUtils.getCurrentUser();
        return getCustomerMessages(currentUser.getId(), pageNo, pageSize);
    }

    @Override
    public void sendMessageFromCustomer(CustomerMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        User recipient = null;
        if (request.getRecipientId() != null) {
            recipient = userRepository.findByIdAndIsDeletedFalse(request.getRecipientId())
                    .orElseThrow(() -> new UserNotFoundException("Recipient not found"));
        } else {
            // Send to platform support (simplified)
            recipient = userRepository.findByEmailAndIsDeletedFalse("support@emenu-platform.com")
                    .orElse(null);
        }

        if (recipient == null) {
            throw new ValidationException("No recipient specified or support not available");
        }

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
        message.setBusinessId(recipient.getBusinessId());

        messageRepository.save(message);
        log.info("Message sent from customer: {} to: {}", currentUser.getEmail(), recipient.getEmail());
    }

    // Helper methods
    private void sendWelcomeMessage(User customer) {
        try {
            Message welcomeMessage = new Message();
            welcomeMessage.setSenderId(null); // System message
            welcomeMessage.setSenderEmail("system@emenu-platform.com");
            welcomeMessage.setSenderName("E-Menu Platform");
            welcomeMessage.setRecipientId(customer.getId());
            welcomeMessage.setRecipientEmail(customer.getEmail());
            welcomeMessage.setRecipientName(customer.getFullName());
            welcomeMessage.setSubject("Welcome to E-Menu Platform!");
            welcomeMessage.setContent(String.format(
                    "Hello %s,\n\nWelcome to E-Menu Platform! We're excited to have you as a customer.\n\n" +
                    "You can now browse restaurants and place orders through our platform.\n\n" +
                    "If you have any questions, feel free to contact us.\n\n" +
                    "Best regards,\nE-Menu Platform Team",
                    customer.getFullName()
            ));
            welcomeMessage.setMessageType(MessageType.WELCOME);

            messageRepository.save(welcomeMessage);
        } catch (Exception e) {
            log.error("Failed to send welcome message to: {}", customer.getEmail(), e);
        }
    }

    private void sendStatusChangeMessage(User customer, String action) {
        try {
            Message message = new Message();
            message.setSenderId(null); // System message
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientId(customer.getId());
            message.setRecipientEmail(customer.getEmail());
            message.setRecipientName(customer.getFullName());
            message.setSubject("Account Status Update");
            message.setContent(String.format("Your customer account has been %s.", action));
            message.setMessageType(MessageType.NOTIFICATION);

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send status change message to: {}", customer.getEmail(), e);
        }
    }
}