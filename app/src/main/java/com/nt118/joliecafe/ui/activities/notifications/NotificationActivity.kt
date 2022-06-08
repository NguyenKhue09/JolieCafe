package com.nt118.joliecafe.ui.activities.notifications

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.NotificationAdapter
import com.nt118.joliecafe.databinding.ActivityNotificationBinding
import com.nt118.joliecafe.models.Notification
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.listNotificationTab
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.NotificationComparator
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.notification_activity.NotificationActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {
    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!

    private val notificationActivityViewModel: NotificationActivityViewModel by viewModels()

    private lateinit var notificationItemAdapter: NotificationAdapter
    private lateinit var selectedTab: String
    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        initNotificationAdapter()
        configProductRecyclerView()
        addNotificationTabText()
        initProductAdapterData()
        setNotificationAdapterDataWhenTabChange()
        onNotificationTabSelected()
        handleNotificationPagingAdapterState()
    }

    private fun handleNotificationPagingAdapterState() {
        notificationItemAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.notificationCircularProgressIndicator.visibility = View.VISIBLE
            } else {
                binding.notificationCircularProgressIndicator.visibility = View.GONE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    if (notificationActivityViewModel.networkStatus)
                        showSnackBar(
                            message = "Can't load notification data!",
                            status = Constants.SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                }
            }
        }
    }


    private fun onNotificationTabSelected() {
        binding.notificationTabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                println(binding.notificationTabLayout.selectedTabPosition)
                if (tab != null) {
                    notificationActivityViewModel.setTabSelected(tab = tab.text.toString())
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }


    private fun setNotificationAdapterDataWhenTabChange() {
        notificationActivityViewModel.tabSelected.observe(this) { tab ->
            println(tab)
            if (notificationActivityViewModel.networkStatus) {
                lifecycleScope.launchWhenStarted {
                    notificationActivityViewModel.getAllNotificationsForUser()
                        .collectLatest { data ->
                            selectedTab = tab
                            submitNotificationAdapterData(data = data)
                        }
                }
            }
        }
    }

    private suspend fun submitNotificationAdapterData(data: PagingData<Notification>) {
        notificationItemAdapter.submitData(data)
    }

    private fun initProductAdapterData() {
        val tabIndex = listNotificationTab.indexOf(notificationActivityViewModel.tabSelected.value)
        binding.notificationTabLayout.selectTab(
            binding.notificationTabLayout.getTabAt(tabIndex),
            true
        )
    }

    private fun initNotificationAdapter() {
        val diffUtil = NotificationComparator
        notificationItemAdapter = NotificationAdapter(diffUtil)
    }


    private fun configProductRecyclerView() {
        val recyclerViewNotificationAdapter = binding.notificationsRv
        recyclerViewNotificationAdapter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewNotificationAdapter.adapter = notificationItemAdapter
    }

    private fun addNotificationTabText() {
        listNotificationTab.forEach {
            binding.notificationTabLayout.addTab(binding.notificationTabLayout.newTab().apply {
                tag = it
                text = it
            })
        }
    }


    private fun observerNetworkMessage() {
        notificationActivityViewModel.networkMessage.observe(this) { message ->
            if (!notificationActivityViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (notificationActivityViewModel.networkStatus) {
                if (notificationActivityViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun updateBackOnlineStatus() {
        notificationActivityViewModel.readBackOnline.asLiveData().observe(this) { status ->
            notificationActivityViewModel.backOnline = status
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                notificationActivityViewModel.networkStatus = status
                notificationActivityViewModel.showNetworkStatus()
                backOnlineRecallNotifications()
            }
    }

    private fun backOnlineRecallNotifications() {
        lifecycleScope.launchWhenStarted {
            if (notificationActivityViewModel.backOnline) {
                println(binding.notificationTabLayout.selectedTabPosition)
                notificationActivityViewModel.getAllNotificationsForUser(
                ).collectLatest { data ->
                    selectedTab = listNotificationTab[binding.notificationTabLayout.selectedTabPosition]
                    submitNotificationAdapterData(data = data)
                }
            }

        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, icon, null)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(this, snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(this, snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.snackbar_normal_custom_bg,
                    null
                )!!
            )

        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}