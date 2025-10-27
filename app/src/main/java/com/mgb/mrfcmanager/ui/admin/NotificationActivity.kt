package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.mgb.mrfcmanager.MRFCManagerApp
import com.mgb.mrfcmanager.R
import com.mgb.mrfcmanager.data.remote.RetrofitClient
import com.mgb.mrfcmanager.data.remote.api.NotificationApiService
import com.mgb.mrfcmanager.data.remote.dto.NotificationDto
import com.mgb.mrfcmanager.data.repository.NotificationRepository
import com.mgb.mrfcmanager.viewmodel.NotificationListState
import com.mgb.mrfcmanager.viewmodel.NotificationViewModel
import com.mgb.mrfcmanager.viewmodel.NotificationViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationActivity : AppCompatActivity() {

    private lateinit var tvMarkAllRead: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var rvNotifications: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private val allNotifications = mutableListOf<NotificationDto>()
    private val displayedNotifications = mutableListOf<NotificationDto>()
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var viewModel: NotificationViewModel

    private var userId: Long = 0L
    private var currentTabPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Get userId from TokenManager or intent
        userId = intent.getLongExtra("USER_ID", 0L)

        setupToolbar()
        initializeViews()
        setupViewModel()
        setupRecyclerView()
        setupTabs()
        setupSwipeRefresh()
        observeNotificationState()
        loadNotifications()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead)
        tabLayout = findViewById(R.id.tabLayout)
        rvNotifications = findViewById(R.id.rvNotifications)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        progressBar = findViewById(R.id.progressBar)
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh = findViewById(R.id.swipeRefresh)

        tvMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
    }

    private fun setupViewModel() {
        // Use singleton TokenManager to prevent DataStore conflicts
        val tokenManager = MRFCManagerApp.getTokenManager()
        val retrofit = RetrofitClient.getInstance(tokenManager)
        val notificationApiService = retrofit.create(NotificationApiService::class.java)
        val notificationRepository = NotificationRepository(notificationApiService)
        val factory = NotificationViewModelFactory(notificationRepository)
        viewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]

        // Get userId from TokenManager if not provided
        if (userId == 0L) {
            userId = tokenManager.getUserId() ?: 0L
        }
    }

    private fun setupSwipeRefresh() {
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.setOnRefreshListener {
        //     loadNotifications()
        // }
    }

    private fun observeNotificationState() {
        viewModel.notificationListState.observe(this) { state ->
            when (state) {
                is NotificationListState.Loading -> {
                    showLoading(true)
                }
                is NotificationListState.Success -> {
                    showLoading(false)
                    allNotifications.clear()
                    allNotifications.addAll(state.data)
                    filterNotifications(currentTabPosition)
                }
                is NotificationListState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is NotificationListState.Idle -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // TODO: Add swipeRefresh to layout XML
        // swipeRefresh.isRefreshing = false
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(displayedNotifications) { notification ->
            markAsRead(notification)
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = notificationAdapter
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    filterNotifications(position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadNotifications() {
        if (userId == 0L) {
            showError("User ID is required")
            return
        }

        // Load notifications based on current tab
        when (currentTabPosition) {
            0 -> {
                // All notifications
                viewModel.loadNotifications(userId = userId)
            }
            1 -> {
                // Unread only
                viewModel.loadUnreadNotifications(userId = userId)
            }
            2 -> {
                // All notifications (will filter for alerts in filterNotifications)
                viewModel.loadNotifications(userId = userId)
            }
        }
    }

    private fun filterNotifications(tabPosition: Int) {
        currentTabPosition = tabPosition
        displayedNotifications.clear()

        when (tabPosition) {
            0 -> {
                // All notifications
                displayedNotifications.addAll(allNotifications)
            }
            1 -> {
                // Unread only
                displayedNotifications.addAll(allNotifications.filter { !it.isRead })
            }
            2 -> {
                // Alerts only
                displayedNotifications.addAll(allNotifications.filter {
                    it.notificationType.equals("ALERT", ignoreCase = true)
                })
            }
        }

        notificationAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun markAsRead(notification: NotificationDto) {
        lifecycleScope.launch {
            when (val result = viewModel.markAsRead(notification.id)) {
                is com.mgb.mrfcmanager.data.repository.Result.Success -> {
                    // Update local list
                    val index = allNotifications.indexOfFirst { it.id == notification.id }
                    if (index != -1) {
                        allNotifications[index] = result.data
                        filterNotifications(currentTabPosition)
                    }
                }
                is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                    showError("Failed to mark as read: ${result.message}")
                }
                else -> {}
            }
        }
    }

    private fun markAllAsRead() {
        lifecycleScope.launch {
            var hasError = false
            displayedNotifications.forEach { notification ->
                if (!notification.isRead) {
                    when (viewModel.markAsRead(notification.id)) {
                        is com.mgb.mrfcmanager.data.repository.Result.Error -> {
                            hasError = true
                        }
                        else -> {}
                    }
                }
            }

            if (hasError) {
                showError("Some notifications could not be marked as read")
            } else {
                Toast.makeText(this@NotificationActivity, "All notifications marked as read", Toast.LENGTH_SHORT).show()
            }

            // Reload notifications
            loadNotifications()
        }
    }

    private fun updateEmptyState() {
        if (displayedNotifications.isEmpty()) {
            rvNotifications.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvNotifications.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    // Adapter for notifications
    class NotificationAdapter(
        private val notifications: List<NotificationDto>,
        private val onNotificationClick: (NotificationDto) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(notifications[position], onNotificationClick)
        }

        override fun getItemCount() = notifications.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardIcon: MaterialCardView = itemView.findViewById(R.id.cardIcon)
            private val ivNotificationIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
            private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
            private val viewUnreadIndicator: View = itemView.findViewById(R.id.viewUnreadIndicator)

            fun bind(notification: NotificationDto, onNotificationClick: (NotificationDto) -> Unit) {
                tvTitle.text = notification.title
                tvMessage.text = notification.message
                tvTime.text = formatTimestamp(notification.createdAt)

                // Set icon based on type
                val (iconRes, iconColor) = when (notification.notificationType.uppercase()) {
                    "MEETING" -> R.drawable.ic_calendar to R.color.primary
                    "COMPLIANCE" -> R.drawable.ic_check to R.color.status_success
                    "ALERT", "DEADLINE" -> R.drawable.ic_warning to R.color.status_warning
                    "DOCUMENT" -> R.drawable.ic_document to R.color.primary
                    else -> R.drawable.ic_notifications to R.color.primary
                }

                ivNotificationIcon.setImageResource(iconRes)
                ivNotificationIcon.setColorFilter(itemView.context.getColor(iconColor))
                cardIcon.setCardBackgroundColor(
                    itemView.context.getColor(
                        when (notification.notificationType.uppercase()) {
                            "ALERT", "DEADLINE" -> R.color.status_warning
                            else -> R.color.background
                        }
                    )
                )

                // Show unread indicator
                viewUnreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

                // Make read notifications slightly transparent
                itemView.alpha = if (notification.isRead) 0.7f else 1.0f

                itemView.setOnClickListener {
                    onNotificationClick(notification)
                }
            }

            private fun formatTimestamp(timestamp: String): String {
                return try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val date = sdf.parse(timestamp) ?: return timestamp

                    val now = Date()
                    val diffInMillis = now.time - date.time

                    when {
                        diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                        diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                            "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
                        }
                        diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                            val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                            "$hours ${if (hours == 1L) "hour" else "hours"} ago"
                        }
                        diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
                            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                            "$days ${if (days == 1L) "day" else "days"} ago"
                        }
                        else -> {
                            val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            displayFormat.format(date)
                        }
                    }
                } catch (e: Exception) {
                    timestamp
                }
            }
        }
    }
}
