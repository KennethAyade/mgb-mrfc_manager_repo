package com.mgb.mrfcmanager.ui.base

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mgb.mrfcmanager.util.ErrorHandler

/**
 * ================================================
 * BASE ACTIVITY
 * ================================================
 * Base class for all activities with common error
 * handling and utility functions
 * 
 * All activities should extend this class to get
 * consistent error handling across the app
 */
abstract class BaseActivity : AppCompatActivity() {

    private var loadingDialog: AlertDialog? = null

    /**
     * Show error dialog with user-friendly message
     */
    protected fun showError(
        message: String,
        title: String? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        dismissLoading()
        ErrorHandler.showError(this, message, title, onDismiss)
    }

    /**
     * Show success dialog
     */
    protected fun showSuccess(
        message: String,
        title: String = "Success",
        onDismiss: (() -> Unit)? = null
    ) {
        dismissLoading()
        ErrorHandler.showSuccess(this, message, title, onDismiss)
    }

    /**
     * Show warning dialog
     */
    protected fun showWarning(
        message: String,
        title: String = "Warning",
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        dismissLoading()
        ErrorHandler.showWarning(this, message, title, onConfirm, onCancel)
    }

    /**
     * Show confirmation dialog
     */
    protected fun showConfirmation(
        message: String,
        title: String = "Confirm",
        positiveText: String = "Yes",
        negativeText: String = "No",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        ErrorHandler.showConfirmation(this, message, title, positiveText, negativeText, onConfirm, onCancel)
    }

    /**
     * Show simple toast message
     */
    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        ErrorHandler.showToast(this, message, duration)
    }

    /**
     * Show loading dialog
     */
    protected fun showLoading(message: String = "Please wait...") {
        dismissLoading()
        loadingDialog = ErrorHandler.showLoading(this, message)
    }

    /**
     * Dismiss loading dialog
     */
    protected fun dismissLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    /**
     * Handle API error response
     */
    protected fun handleApiError(error: String) {
        dismissLoading()
        
        // Check if it's an auth error - might need to logout
        if (ErrorHandler.isAuthError(error)) {
            showError(error) {
                // TODO: Navigate to login if needed
                // logout()
            }
        } else {
            showError(error)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
    }
}

