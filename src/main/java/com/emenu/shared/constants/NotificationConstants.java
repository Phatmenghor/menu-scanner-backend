package com.emenu.shared.constants;

public class NotificationConstants {
    
    // Notification Types
    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String BUSINESS_CREATED = "BUSINESS_CREATED";
    public static final String PRODUCT_CREATED = "PRODUCT_CREATED";
    public static final String TELEGRAM_LINKED = "TELEGRAM_LINKED";
    public static final String WELCOME_MESSAGE = "WELCOME_MESSAGE";
    
    // Telegram Notification Recipients
    public static final String RECIPIENT_PLATFORM_USERS = "PLATFORM_USERS";
    public static final String RECIPIENT_BUSINESS_OWNERS = "BUSINESS_OWNERS";
    public static final String RECIPIENT_CUSTOMER = "CUSTOMER";
    public static final String RECIPIENT_ALL = "ALL";
    
    // Telegram Bot Commands
    public static final String COMMAND_START = "/start";
    public static final String COMMAND_HELP = "/help";
    public static final String COMMAND_REGISTER = "/register";
    public static final String COMMAND_LOGIN = "/login";
    public static final String COMMAND_PROFILE = "/profile";
    public static final String COMMAND_BUSINESSES = "/businesses";
    
    // Telegram Messages
    public static final String WELCOME_TELEGRAM_USER = """
            🇰🇭 Welcome to Cambodia E-Menu Platform! 🇰🇭
            
            🎉 Your Telegram account has been successfully linked!
            
            ✨ What you can do:
            • 📱 Login with Telegram
            • 🔔 Receive notifications
            • 🏪 Manage your business (if owner)
            • 🍽️ Browse menus (if customer)
            
            🚀 Get started: /help
            """;
            
    public static final String CUSTOMER_REGISTRATION_SUCCESS = """
            🎉 Registration Successful! 🎉
            
            👤 Welcome to Cambodia E-Menu Platform!
            🔐 You can now login with:
            • Telegram (this account)
            • Username/Password
            
            📱 Start exploring restaurants and menus!
            """;
}