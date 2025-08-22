package com.emenu.features.notification.constants;

import com.emenu.features.notification.dto.response.PlatformUserCreationNotificationDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TelegramMessages {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===== CUSTOMER WELCOME MESSAGES =====

    public static String buildCustomerWelcomeMessage(String displayName, String userIdentifier, LocalDateTime registeredAt) {
        return String.format("""
                🎉 <b>Welcome to Cambodia E-Menu Platform!</b> 🇰🇭
                
                👋 <b>Hello %s!</b>
                
                ✅ <b>Your account is ready:</b>
                🆔 Username: <code>%s</code>
                📅 Registered: %s
                
                🚀 <b>What you can do now:</b>
                • 🍽️ Browse restaurant menus
                • 🛒 Place orders online
                • 🔔 Get order notifications
                • 📱 Use Telegram for quick access
                
                💡 <b>Tips:</b>
                • Use /help to see all commands
                • Your orders will be sent here automatically
                • Enjoy exploring Cambodia's restaurants!
                
                <i>Welcome to the future of dining in Cambodia! 🎊</i>
                """, displayName, userIdentifier, FORMATTER.format(registeredAt));
    }

    public static String buildTelegramLinkSuccessMessage(String displayName, String userIdentifier, String userType, LocalDateTime linkedAt) {
        return String.format("""
                🔗 <b>Telegram Account Linked Successfully!</b>
                
                👤 <b>User:</b> %s (%s)
                🏷️ <b>Type:</b> %s
                📅 <b>Linked:</b> %s
                
                ✨ <b>New Features Unlocked:</b>
                • 🔐 Login with Telegram
                • 🔔 Real-time notifications
                • ⚡ Quick actions via bot commands
                • 📲 Mobile-friendly access
                
                🎯 <b>Try these commands:</b>
                /help - See all available commands
                /profile - View your profile
                
                <i>Enjoy the enhanced experience! 🚀</i>
                """, displayName, userIdentifier, userType, FORMATTER.format(linkedAt));
    }

    // ===== ADMIN NOTIFICATION MESSAGES =====

    public static String buildCustomerRegistrationAdminMessage(String userIdentifier, String fullName, String email, 
                                                               String phoneNumber, String socialProvider, 
                                                               boolean hasTelegram, LocalDateTime registeredAt) {
        return String.format("""
                👤 <b>New Customer Registered!</b>
                
                🆔 <b>User ID:</b> <code>%s</code>
                👤 <b>Name:</b> %s
                📧 <b>Email:</b> %s
                📞 <b>Phone:</b> %s
                📱 <b>Provider:</b> %s
                🔗 <b>Telegram:</b> %s
                📅 <b>Date:</b> %s
                
                <b>Platform Stats Updated! 📊</b>
                """, 
                userIdentifier, 
                fullName != null ? fullName : "Not provided",
                email != null ? email : "Not provided",
                phoneNumber != null ? phoneNumber : "Not provided",
                socialProvider,
                hasTelegram ? "✅ Linked" : "❌ Not linked",
                FORMATTER.format(registeredAt));
    }

    public static String buildTelegramLinkAdminMessage(String userIdentifier, String fullName, String userType, 
                                                       String telegramUsername, Long telegramUserId, LocalDateTime linkedAt) {
        return String.format("""
                🔗 <b>Telegram Account Linked!</b>
                
                👤 <b>User:</b> %s (%s)
                🏷️ <b>Type:</b> %s
                📱 <b>Telegram:</b> %s
                🆔 <b>Telegram ID:</b> <code>%s</code>
                📅 <b>Linked:</b> %s
                
                <i>User can now receive notifications and use Telegram features.</i>
                """, 
                fullName != null ? fullName : userIdentifier, 
                userIdentifier,
                userType,
                telegramUsername != null ? "@" + telegramUsername : "No username",
                telegramUserId,
                FORMATTER.format(linkedAt));
    }

    // ===== BUSINESS REGISTRATION MESSAGES =====

    public static String buildBusinessRegistrationMessage(String businessName, String ownerName, String ownerUserIdentifier, 
                                                          String subdomain, LocalDateTime registeredAt) {
        return String.format("""
                🏪 <b>New Business Registered!</b>
                
                🏢 <b>Business:</b> %s
                👤 <b>Owner:</b> %s (%s)
                🌐 <b>Website:</b> %s.menu.com
                📅 <b>Date:</b> %s
                
                🎉 <b>Cambodia's restaurant ecosystem is growing!</b>
                """, 
                businessName, 
                ownerName, 
                ownerUserIdentifier, 
                subdomain, 
                FORMATTER.format(registeredAt));
    }

    public static String buildPlatformUserCreationMessage(PlatformUserCreationNotificationDto dto) {
        return String.format("""
                🔧 <b>New Platform User Created!</b>
                
                👤 <b>User:</b> %s (%s)
                📧 <b>Email:</b> %s
                📞 <b>Phone:</b> %s
                🏷️ <b>Roles:</b> %s
                📱 <b>Status:</b> %s
                💼 <b>Position:</b> %s
                👨‍💼 <b>Created by:</b> %s (%s)
                📅 <b>Date:</b> %s
                
                <i>Platform team expansion! 🚀</i>
                """,
                dto.getFullName() != null ? dto.getFullName() : dto.getUserIdentifier(),
                dto.getUserIdentifier(),
                dto.getEmail() != null ? dto.getEmail() : "Not provided",
                dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "Not provided",
                dto.getRoles(),
                dto.getAccountStatus(),
                dto.getPosition() != null ? dto.getPosition() : "Not specified",
                dto.getCreatedByFullName() != null ? dto.getCreatedByFullName() : dto.getCreatedByUserIdentifier(),
                dto.getCreatedByUserIdentifier(),
                FORMATTER.format(dto.getCreatedAt())
        );
    }


    // ===== ERROR & INFO MESSAGES =====

    public static String buildErrorMessage(String title, String details, String action) {
        return String.format("""
                ❌ <b>%s</b>
                
                📝 <b>Details:</b> %s
                
                💡 <b>What to do:</b> %s
                
                <i>Contact support if this issue persists.</i>
                """, title, details, action);
    }

    public static String buildInfoMessage(String title, String content) {
        return String.format("""
                ℹ️ <b>%s</b>
                
                %s
                """, title, content);
    }

    // ===== BOT COMMAND MESSAGES =====

    public static final String HELP_MESSAGE = """
            🆘 <b>Cambodia E-Menu Platform Help</b>
            
            🚀 <b>Getting Started:</b>
            /start - Welcome & introduction
            /register - Create new account
            /login - Access existing account
            
            👤 <b>Account Management:</b>
            /profile - View your profile
            /notifications - Manage notifications
            /link - Link Telegram to existing account
            
            🏪 <b>Business Users:</b>
            /business - Business information
            /menu - Manage your menu
            /orders - View orders
            
            🍽️ <b>Customers:</b>
            /restaurants - Browse restaurants
            /myorders - Your order history
            
            ⚙️ <b>Other Commands:</b>
            /help - Show this help message
            /cancel - Cancel current operation
            /status - Platform status
            
            💬 <b>Contact Support:</b>
            Email: support@cambodia-emenu.com
            
            <i>🇰🇭 Powering Cambodia's digital dining experience!</i>
            """;

    public static final String START_MESSAGE = """
            🇰🇭 <b>Welcome to Cambodia E-Menu Platform!</b> 🇰🇭
            
            🎉 <b>Digital Menu Revolution for Cambodia!</b>
            
            📱 <b>What is this platform?</b>
            • 🏪 Complete restaurant management system
            • 📋 Digital menus for customers
            • 🛒 Online ordering system
            • 💰 Affordable SaaS solution for Cambodia
            
            👥 <b>Who can use it?</b>
            • 🏢 Restaurant owners & managers
            • 👨‍🍳 Restaurant staff
            • 🍽️ Customers who love good food
            
            ✨ <b>Why use Telegram?</b>
            • ⚡ Instant notifications
            • 🔐 Quick & secure login
            • 📱 Mobile-first experience
            • 🚀 No app installation needed
            
            🎯 <b>Get Started:</b>
            /register - Create your account
            /login - Access existing account
            /help - See all commands
            
            <i>Ready to revolutionize dining in Cambodia? Let's go! 🚀</i>
            """;

    public static final String REGISTRATION_SUCCESS = """
            ✅ <b>Registration Successful!</b>
            
            🎊 <b>Welcome to Cambodia E-Menu Platform!</b>
            
            👤 <b>Your account is ready:</b>
            • 🔐 You can now login with Telegram
            • 🔔 You'll receive notifications here
            • 📱 Full platform access enabled
            
            🚀 <b>Next Steps:</b>
            /profile - Complete your profile
            /help - Explore all features
            
            <i>Welcome aboard! 🎉</i>
            """;

    public static final String LOGIN_SUCCESS = """
            🔐 <b>Login Successful!</b>
            
            👋 <b>Welcome back!</b>
            
            ✅ <b>You're now connected:</b>
            • 🔔 Notifications enabled
            • 📱 Full platform access
            • ⚡ Telegram features unlocked
            
            🎯 <b>Quick Actions:</b>
            /profile - View your profile
            /help - See available commands
            
            <i>Great to have you back! 🚀</i>
            """;

    // ===== NOTIFICATION TYPES =====

    public static String buildOrderNotification(String customerName, String businessName, String orderDetails, String total) {
        return String.format("""
                🛒 <b>New Order Received!</b>
                
                👤 <b>Customer:</b> %s
                🏪 <b>Business:</b> %s
                
                📋 <b>Order Details:</b>
                %s
                
                💰 <b>Total:</b> $%s
                
                <i>Please confirm or reject this order.</i>
                """, customerName, businessName, orderDetails, total);
    }

    public static String buildOrderStatusUpdate(String orderStatus, String businessName, String orderDetails) {
        return String.format("""
                📦 <b>Order Status Update</b>
                
                🏪 <b>From:</b> %s
                📊 <b>Status:</b> %s
                
                📋 <b>Your Order:</b>
                %s
                
                <i>Thank you for choosing us!</i>
                """, businessName, orderStatus, orderDetails);
    }

    public static String buildSubscriptionExpiredNotification(String businessName, int daysUntilExpiry) {
        return String.format("""
                ⚠️ <b>Subscription Expiring Soon!</b>
                
                🏪 <b>Business:</b> %s
                ⏰ <b>Days Remaining:</b> %d
                
                📋 <b>Action Required:</b>
                • Review your subscription
                • Renew to continue service
                • Contact support if needed
                
                💡 Don't lose access to your digital menu!
                """, businessName, daysUntilExpiry);
    }

    // ===== UTILITY METHODS =====

    public static String formatDateTime(LocalDateTime dateTime) {
        return FORMATTER.format(dateTime);
    }

    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    public static String bold(String text) {
        return "<b>" + escape(text) + "</b>";
    }

    public static String italic(String text) {
        return "<i>" + escape(text) + "</i>";
    }

    public static String code(String text) {
        return "<code>" + escape(text) + "</code>";
    }
}