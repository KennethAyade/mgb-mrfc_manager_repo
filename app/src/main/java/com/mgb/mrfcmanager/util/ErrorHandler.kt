package com.mgb.mrfcmanager.util

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgb.mrfcmanager.R

/**
 * ================================================
 * APP-WIDE ERROR HANDLER
 * ================================================
 * Centralized error handling utility for consistent
 * error messages and dialogs across the entire app
 * 
 * Usage:
 * - ErrorHandler.showError(context, "Error message")
 * - ErrorHandler.showSuccess(context, "Success message")
 * - ErrorHandler.showWarning(context, "Warning message")
 */
object ErrorHandler {

    /**
     * Show error dialog with parsed user-friendly message
     */
    fun showError(
        context: Context,
        rawMessage: String,
        title: String? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        val userFriendlyMessage = parseErrorMessage(rawMessage)
        val errorTitle = title ?: getErrorTitle(rawMessage)
        
        MaterialAlertDialogBuilder(context)
            .setTitle(errorTitle)
            .setMessage(userFriendlyMessage)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show success dialog
     */
    fun showSuccess(
        context: Context,
        message: String,
        title: String = "Success",
        onDismiss: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_check)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .show()
    }

    /**
     * Show warning dialog
     */
    fun showWarning(
        context: Context,
        message: String,
        title: String = "Warning",
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                onConfirm?.invoke()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onCancel?.invoke()
            }
            .show()
    }

    /**
     * Show confirmation dialog
     */
    fun showConfirmation(
        context: Context,
        message: String,
        title: String = "Confirm",
        positiveText: String = "Yes",
        negativeText: String = "No",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_info)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                dialog.dismiss()
                onCancel?.invoke()
            }
            .show()
    }

    /**
     * Show simple toast message
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    /**
     * Show loading dialog
     */
    fun showLoading(
        context: Context,
        message: String = "Please wait..."
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setCancelable(false)
            .create()
            .apply { show() }
    }

    /**
     * Parse raw error message into user-friendly text
     */
    private fun parseErrorMessage(rawMessage: String): String {
        return when {
            // Authentication Errors
            rawMessage.contains("INVALID_CREDENTIALS", ignoreCase = true) ||
            rawMessage.contains("Invalid username or password", ignoreCase = true) -> {
                "The username or password you entered is incorrect.\n\n" +
                "Please check your credentials and try again."
            }
            rawMessage.contains("TOKEN_EXPIRED", ignoreCase = true) ||
            rawMessage.contains("token expired", ignoreCase = true) -> {
                "Your session has expired.\n\n" +
                "Please login again to continue."
            }
            rawMessage.contains("UNAUTHORIZED", ignoreCase = true) ||
            rawMessage.contains("401", ignoreCase = true) -> {
                "You are not authorized to perform this action.\n\n" +
                "Please login or contact your administrator."
            }
            
            // Account Status Errors
            rawMessage.contains("USER_INACTIVE", ignoreCase = true) ||
            rawMessage.contains("account is inactive", ignoreCase = true) -> {
                "Your account has been deactivated.\n\n" +
                "Please contact your administrator for assistance."
            }
            rawMessage.contains("ACCOUNT_LOCKED", ignoreCase = true) ||
            rawMessage.contains("account is locked", ignoreCase = true) -> {
                "Your account has been locked due to multiple failed login attempts.\n\n" +
                "Please contact your administrator to unlock it."
            }
            
            // Permission Errors
            rawMessage.contains("FORBIDDEN", ignoreCase = true) ||
            rawMessage.contains("403", ignoreCase = true) ||
            rawMessage.contains("ACCESS_DENIED", ignoreCase = true) -> {
                "You do not have permission to access this resource.\n\n" +
                "Contact your administrator if you believe this is an error."
            }
            
            // Network Errors
            rawMessage.contains("NETWORK", ignoreCase = true) ||
            rawMessage.contains("Unable to resolve host", ignoreCase = true) ||
            rawMessage.contains("timeout", ignoreCase = true) ||
            rawMessage.contains("Failed to connect", ignoreCase = true) -> {
                "Unable to connect to the server.\n\n" +
                "Please check your internet connection and try again."
            }
            
            // Server Errors
            rawMessage.contains("500") || 
            rawMessage.contains("SERVER_ERROR", ignoreCase = true) ||
            rawMessage.contains("Internal Server Error", ignoreCase = true) -> {
                "The server encountered an error.\n\n" +
                "Please try again later or contact support if the problem persists."
            }
            rawMessage.contains("502") || rawMessage.contains("Bad Gateway", ignoreCase = true) -> {
                "The server is temporarily unavailable.\n\n" +
                "Please try again in a few moments."
            }
            rawMessage.contains("503") || rawMessage.contains("Service Unavailable", ignoreCase = true) -> {
                "The service is temporarily unavailable.\n\n" +
                "Please try again later."
            }
            
            // Resource Errors
            rawMessage.contains("404") || 
            rawMessage.contains("NOT_FOUND", ignoreCase = true) -> {
                "The requested resource was not found.\n\n" +
                "It may have been deleted or is no longer available."
            }
            rawMessage.contains("ALREADY_EXISTS", ignoreCase = true) ||
            rawMessage.contains("duplicate", ignoreCase = true) ||
            rawMessage.contains("409", ignoreCase = true) -> {
                "This item already exists.\n\n" +
                "Please use a different name or update the existing item."
            }
            
            // Validation Errors
            rawMessage.contains("VALIDATION_ERROR", ignoreCase = true) ||
            rawMessage.contains("400", ignoreCase = true) ||
            rawMessage.contains("Bad Request", ignoreCase = true) -> {
                "The data you entered is invalid.\n\n" +
                "Please check your input and try again."
            }
            rawMessage.contains("REQUIRED_FIELD", ignoreCase = true) ||
            rawMessage.contains("required", ignoreCase = true) -> {
                "Please fill in all required fields."
            }
            
            // Data Errors
            rawMessage.contains("NO_DATA", ignoreCase = true) ||
            rawMessage.contains("empty", ignoreCase = true) -> {
                "No data available.\n\n" +
                "Please try refreshing or adding new items."
            }
            
            // File Upload Errors
            rawMessage.contains("FILE_TOO_LARGE", ignoreCase = true) ||
            rawMessage.contains("file size", ignoreCase = true) -> {
                "The file you selected is too large.\n\n" +
                "Please choose a smaller file and try again."
            }
            rawMessage.contains("INVALID_FILE_TYPE", ignoreCase = true) ||
            rawMessage.contains("file type", ignoreCase = true) -> {
                "The file type is not supported.\n\n" +
                "Please choose a valid file format."
            }
            
            // Generic handling
            else -> {
                // Try to clean up JSON and technical details
                val cleanMessage = rawMessage
                    .replace(Regex("\\{.*?\\}"), "") // Remove JSON objects
                    .replace(Regex("\".*?\":"), "") // Remove JSON keys
                    .replace(Regex("\\[.*?\\]"), "") // Remove arrays
                    .replace("success", "", ignoreCase = true)
                    .replace("false", "", ignoreCase = true)
                    .replace("error", "", ignoreCase = true)
                    .trim()
                    .replaceFirstChar { it.uppercase() }
                
                if (cleanMessage.isNotEmpty() && cleanMessage.length < 150) {
                    cleanMessage
                } else {
                    "An error occurred while processing your request.\n\n" +
                    "Please try again or contact support if the problem persists."
                }
            }
        }
    }

    /**
     * Get appropriate error title based on error type
     */
    private fun getErrorTitle(rawMessage: String): String {
        return when {
            rawMessage.contains("INVALID_CREDENTIALS", ignoreCase = true) -> "Invalid Credentials"
            rawMessage.contains("TOKEN_EXPIRED", ignoreCase = true) -> "Session Expired"
            rawMessage.contains("UNAUTHORIZED", ignoreCase = true) -> "Unauthorized"
            rawMessage.contains("USER_INACTIVE", ignoreCase = true) -> "Account Inactive"
            rawMessage.contains("ACCOUNT_LOCKED", ignoreCase = true) -> "Account Locked"
            rawMessage.contains("FORBIDDEN", ignoreCase = true) || 
            rawMessage.contains("403", ignoreCase = true) -> "Access Denied"
            rawMessage.contains("NETWORK", ignoreCase = true) -> "Connection Error"
            rawMessage.contains("500") || rawMessage.contains("SERVER_ERROR") -> "Server Error"
            rawMessage.contains("502") -> "Server Unavailable"
            rawMessage.contains("503") -> "Service Unavailable"
            rawMessage.contains("404") -> "Not Found"
            rawMessage.contains("ALREADY_EXISTS", ignoreCase = true) -> "Already Exists"
            rawMessage.contains("VALIDATION_ERROR", ignoreCase = true) -> "Validation Error"
            rawMessage.contains("FILE_TOO_LARGE", ignoreCase = true) -> "File Too Large"
            rawMessage.contains("INVALID_FILE_TYPE", ignoreCase = true) -> "Invalid File Type"
            else -> "Error"
        }
    }

    /**
     * Check if error is authentication related
     */
    fun isAuthError(rawMessage: String): Boolean {
        return rawMessage.contains("INVALID_CREDENTIALS", ignoreCase = true) ||
               rawMessage.contains("TOKEN_EXPIRED", ignoreCase = true) ||
               rawMessage.contains("UNAUTHORIZED", ignoreCase = true) ||
               rawMessage.contains("401", ignoreCase = true)
    }

    /**
     * Check if error is network related
     */
    fun isNetworkError(rawMessage: String): Boolean {
        return rawMessage.contains("NETWORK", ignoreCase = true) ||
               rawMessage.contains("Unable to resolve host", ignoreCase = true) ||
               rawMessage.contains("timeout", ignoreCase = true) ||
               rawMessage.contains("Failed to connect", ignoreCase = true)
    }
}

