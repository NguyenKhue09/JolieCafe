package com.nt118.joliecafe.ui.activities.order_history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RatingBar
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.nt118.joliecafe.R
import com.nt118.joliecafe.adapter.OrderHistoryAdapter
import com.nt118.joliecafe.databinding.ActivityOrderHistoryBinding
import com.nt118.joliecafe.models.BillReviewBody
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.OrderHistoryComparator
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.order_history.OrderHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class OrderHistoryActivity : AppCompatActivity() {

    private var _binding: ActivityOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var networkListener: NetworkListener
    private val orderHistoryViewModel: OrderHistoryViewModel by viewModels()
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter

    lateinit var orderHistoryClickedList: LiveData<MutableList<String>>

    private lateinit var customAlertDialogView : View
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderHistoryClickedList = orderHistoryViewModel.orderHistoryClickedList

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()
        observerGetUserBillsResponse()

        setupActionBar()
        setUpOrderHistoryRecyclerView()

        getUserBills()
        handlePagingAdapterState()

        initMaterialAlertDialogBuilder()
    }

    private fun initMaterialAlertDialogBuilder() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background)
    }

    private fun initUpdateDialogView() {
        customAlertDialogView = layoutInflater.inflate(R.layout.update_bill_status_dialog_layout, null, false)
    }

    private fun launchUpdateAlertDialog(billId: String, productIds: List<String>) {

        observerReviewBill()

        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setPositiveButton("Review") { dialog, _ ->
                val star = customAlertDialogView.findViewById<RatingBar>(R.id.bill_rating).rating
                val comment = customAlertDialogView.findViewById<TextInputEditText>(R.id.et_bill_comment).text.toString()

                val billReviewBody = BillReviewBody(billId = billId, rating = star, content = comment, productIds = productIds)
                println("billReviewBody: $billReviewBody")
                orderHistoryViewModel.reviewBills(billReviewBody)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun observerReviewBill() {
        orderHistoryViewModel.reviewBillResponse.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.orderHistoryCircularProgressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.NullDataSuccess -> {
                    binding.orderHistoryCircularProgressIndicator.visibility = View.GONE
                    //Toast.makeText(requireContext(), "Remove favorite product successful", Toast.LENGTH_SHORT).show()
                    showSnackBar(
                        message = "Review bill successful",
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    orderHistoryAdapter.refresh()
                }
                is ApiResult.Error -> {
                    binding.orderHistoryCircularProgressIndicator.visibility = View.GONE
                    //Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    showSnackBar(
                        message = "Review bill failed!",
                        status = Constants.SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }

    }

    fun addNewOrderToClickList(id: String) {
        orderHistoryViewModel.addNewOrderToClickedList(id)
    }

    private fun updateBackOnlineStatus() {
        orderHistoryViewModel.readBackOnline.asLiveData().observe(this) {
            orderHistoryViewModel.backOnline = it
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                orderHistoryViewModel.networkStatus = status
                orderHistoryViewModel.showNetworkStatus()
                if(orderHistoryViewModel.backOnline) {
                    getUserBills()
                }
            }
    }

    private fun getUserBills() {
        orderHistoryViewModel.getUserBills()
    }

    private fun observerNetworkMessage() {
        orderHistoryViewModel.networkMessage.observe(this) { message ->
            if (!orderHistoryViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (orderHistoryViewModel.networkStatus) {
                if (orderHistoryViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun observerGetUserBillsResponse() {
        lifecycleScope.launchWhenStarted {
            orderHistoryViewModel.orderHistory.collectLatest { data ->
                println(data)
                orderHistoryAdapter.submitData(data)
            }
        }
    }

    private fun handlePagingAdapterState() {
        orderHistoryAdapter.addLoadStateListener {
                loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.orderHistoryCircularProgressIndicator.visibility = View.VISIBLE
            } else {
                binding.orderHistoryCircularProgressIndicator.visibility = View.INVISIBLE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    if (orderHistoryViewModel.networkStatus)
                        showSnackBar(
                            message = "Get bills failed!",
                            status = Constants.SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                }
            }
        }
    }

    private fun setUpOrderHistoryRecyclerView() {
        val orderHistoryRecyclerView = binding.rvOrderHistory
        val difUtil = OrderHistoryComparator
        orderHistoryAdapter = OrderHistoryAdapter(orderHistoryActivity = this,diffUtil = difUtil, onBillReviewClicked = { billId, productIds ->
            initUpdateDialogView()
            launchUpdateAlertDialog(billId, productIds)
        })
        orderHistoryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        orderHistoryRecyclerView.adapter = orderHistoryAdapter
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarOrderHistoryActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarOrderHistoryActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = getDrawable(icon)

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
            .setCustomBackground(getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        orderHistoryAdapter.removeOrderListIdObserver()
    }
}