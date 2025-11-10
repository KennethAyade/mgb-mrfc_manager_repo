package com.mgb.mrfcmanager.ui.base

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.ui.admin.AdminDashboardActivity
import com.mgb.mrfcmanager.ui.user.UserDashboardActivity
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

    /**
     * Setup floating home button
     * Call this method from your activity after setContentView()
     * 
     * The FAB should already exist in your layout with id: fabHome
     * 
     * Example usage in your activity:
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     super.onCreate(savedInstanceState)
     *     setContentView(R.layout.your_activity)
     *     setupHomeFab() // Add this line
     * }
     */
    protected fun setupHomeFab() {
        val fabHome = findViewById<FloatingActionButton>(R.id.fabHome)
        fabHome?.let { fab ->
            // Hide on dashboard activities (you're already home)
            if (this is AdminDashboardActivity || this is UserDashboardActivity) {
                fab.visibility = View.GONE
                return
            }
            
            fab.setOnClickListener {
                navigateToHome()
            }
        }
    }

    /**
     * Navigate to the appropriate home screen based on user role
     */
    private fun navigateToHome() {
        val tokenManager = MRFCManagerApp.getTokenManager()
        val userRole = tokenManager.getUserRole()
        
        val intent = when (userRole) {
            "ADMIN", "SUPER_ADMIN" -> Intent(this, AdminDashboardActivity::class.java)
            "USER" -> Intent(this, UserDashboardActivity::class.java)
            else -> Intent(this, AdminDashboardActivity::class.java)
        }
        
        // Clear activity stack and start fresh
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}



