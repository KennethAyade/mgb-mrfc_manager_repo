package com.mgb.mrfcmanager.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.mgb.mrfcmanager.R

class NotificationActivity : AppCompatActivity() {

    private lateinit var tvMarkAllRead: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var rvNotifications: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    private val allNotifications = mutableListOf<NotificationItem>()
    private val displayedNotifications = mutableListOf<NotificationItem>()
    private lateinit var notificationAdapter: NotificationAdapter

    data class NotificationItem(
        val id: Long,
        val title: String,
        val message: String,
        val time: String,
        val type: NotificationType,
        var isRead: Boolean = false
    )

    enum class NotificationType {
        MEETING,
        COMPLIANCE,
        ALERT,
        GENERAL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        setupToolbar()
        initializeViews()
        setupRecyclerView()
        setupTabs()
        loadDemoNotifications()
        filterNotifications(0)
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

        tvMarkAllRead.setOnClickListener {
            markAllAsRead()
        }
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

    private fun loadDemoNotifications() {
        // TODO: BACKEND - Fetch notifications from database
        allNotifications.addAll(
            listOf(
                NotificationItem(
                    1,
                    "Meeting Reminder",
                    "MRFC-1 meeting scheduled for tomorrow at 10:00 AM",
                    "2 hours ago",
                    NotificationType.MEETING,
                    false
                ),
                NotificationItem(
                    2,
                    "Compliance Alert",
                    "Proponent 3 has pending compliance report submission",
                    "5 hours ago",
                    NotificationType.ALERT,
                    false
                ),
                NotificationItem(
                    3,
                    "Document Uploaded",
                    "New MTF Report uploaded for Q2 2025",
                    "1 day ago",
                    NotificationType.GENERAL,
                    true
                ),
                NotificationItem(
                    4,
                    "Attendance Required",
                    "Please mark attendance for MRFC-2 meeting",
                    "1 day ago",
                    NotificationType.MEETING,
                    false
                ),
                NotificationItem(
                    5,
                    "Compliance Warning",
                    "Deadline approaching for CMVR submission",
                    "2 days ago",
                    NotificationType.ALERT,
                    false
                ),
                NotificationItem(
                    6,
                    "Meeting Minutes",
                    "Minutes from last MRFC meeting are now available",
                    "3 days ago",
                    NotificationType.GENERAL,
                    true
                ),
                NotificationItem(
                    7,
                    "New Proponent Added",
                    "Proponent 10 has been added to MRFC-1",
                    "4 days ago",
                    NotificationType.GENERAL,
                    true
                ),
                NotificationItem(
                    8,
                    "System Update",
                    "MRFC Manager system will undergo maintenance on Sunday",
                    "5 days ago",
                    NotificationType.GENERAL,
                    true
                )
            )
        )
    }

    private fun filterNotifications(tabPosition: Int) {
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
                displayedNotifications.addAll(allNotifications.filter { it.type == NotificationType.ALERT })
            }
        }

        notificationAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun markAsRead(notification: NotificationItem) {
        // TODO: BACKEND - Update read status in database
        notification.isRead = true
        notificationAdapter.notifyDataSetChanged()
    }

    private fun markAllAsRead() {
        // TODO: BACKEND - Update all notifications as read in database
        allNotifications.forEach { it.isRead = true }
        notificationAdapter.notifyDataSetChanged()
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
        private val notifications: List<NotificationItem>,
        private val onNotificationClick: (NotificationItem) -> Unit
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

            fun bind(notification: NotificationItem, onNotificationClick: (NotificationItem) -> Unit) {
                tvTitle.text = notification.title
                tvMessage.text = notification.message
                tvTime.text = notification.time

                // Set icon based on type
                val (iconRes, iconColor) = when (notification.type) {
                    NotificationType.MEETING -> R.drawable.ic_calendar to R.color.primary
                    NotificationType.COMPLIANCE -> R.drawable.ic_check to R.color.status_success
                    NotificationType.ALERT -> R.drawable.ic_warning to R.color.status_warning
                    NotificationType.GENERAL -> R.drawable.ic_notifications to R.color.primary
                }

                ivNotificationIcon.setImageResource(iconRes)
                ivNotificationIcon.setColorFilter(itemView.context.getColor(iconColor))
                cardIcon.setCardBackgroundColor(
                    itemView.context.getColor(
                        when (notification.type) {
                            NotificationType.ALERT -> R.color.status_warning
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
        }
    }
}
