package com.nt118.joliecafe.ui.fragments.profile_bottom_sheet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.BuildConfig
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentProfileBottomSheetBinding
import com.nt118.joliecafe.firebase.firebasefirestore.FirebaseStorage
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafe.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.util.extenstions.setCustomBackground
import com.nt118.joliecafe.util.extenstions.setIcon
import com.nt118.joliecafe.viewmodels.profile_activity.ProfileActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@AndroidEntryPoint
class ProfileBottomSheetFragment(
    private val profileActivityViewModel: ProfileActivityViewModel
) : BottomSheetDialogFragment() {

    private var _binding: FragmentProfileBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    var userImageUri: MutableLiveData<Uri?> = MutableLiveData()

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseStorage = FirebaseStorage()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBottomSheetBinding.inflate(layoutInflater, container, false)

        observerUserUpdateDataResponse()

        updateBackOnlineStatus()

        updateNetworkStatus()

        handleButtonClicked()

        return binding.root
    }

    private fun observerUserUpdateDataResponse() {
        profileActivityViewModel.updateUserDataResponse.observe(this) { userInfos ->
            when (userInfos) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    showSnackBar(
                        message = "Update data success",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    profileActivityViewModel.saveIsUserDataChange(true)
                    this.dismiss()
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Update data failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                    profileActivityViewModel.saveIsUserDataChange(false)
                }
                else -> {}
            }
        }
    }

    private fun handleButtonClicked() {
        binding.btnDismiss.setOnClickListener {
            this.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }

        binding.btnChooseGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                firebaseStorage.chooseFile(getFile)
            } else {
                requestGetFilePermission()
            }
        }

        binding.btnTakeImage.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requestAllPermission()
            }
        }

        binding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun updateNetworkStatus() {
        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    profileActivityViewModel.networkStatus = status
                    profileActivityViewModel.showNetworkStatus()
                }
        }
    }

    private fun updateBackOnlineStatus() {
        profileActivityViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            profileActivityViewModel.backOnline = it
        }
    }

    fun updateUserData(thumbnail: String) {
        profileActivityViewModel.updateUserInfos(
            token = profileActivityViewModel.userToken,
            newUserData = mapOf(
                "thumbnail" to thumbnail
            )
        )
    }


    private fun requestGetFilePermission() {
        getFilePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private var getFilePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showSnackBar(
                    message = "Permission granted",
                    status = SNACK_BAR_STATUS_SUCCESS,
                    icon = R.drawable.ic_success
                )
                firebaseStorage.chooseFile(getFile)
            } else {
                showSnackBar(
                    message = "Oops, you just denied the permission for storage",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_sad
                )
            }
        }

    private val getFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Uri? = result.data?.data
                if (data != null) {
                    try {
                        val fileName = getNameFile(data, requireContext().applicationContext)

                        userImageUri.value = data

                        firebaseStorage.uploadFile(
                            file = data,
                            fileName = fileName,
                            root = (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
                            profileBottomSheetFragment = this
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        showSnackBar(
                            message = "Get image failed!",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    }
                }
            }
        }

    @SuppressLint("Range")
    fun getNameFile(uri: Uri, context: Context): String {
        var res: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            cursor.use { cur ->
                if (cur.moveToFirst()) {
                    res = cur.getString(cur.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (res == null) {
            res = uri.path!!.split('/').also {
                it[it.lastIndex]
            }.toString()
        }
        return res as String
    }

    private fun allPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager() && REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    requireContext(), it
                ) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    requireContext(), it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun requestAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getManageFile.launch(intent)
            getPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            getPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JolieCafe")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    showSnackBar(
                        message = "Photo capture failed",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture success: ${output.savedUri}"
                    userImageUri.value = output.savedUri

                    output.savedUri?.let {
                        firebaseStorage.uploadFile(
                            file = it,
                            fileName = "$name.jpg",
                            root = (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
                            profileBottomSheetFragment = this@ProfileBottomSheetFragment
                        )
                    }

                    setLayoutForAction(false)
                    showSnackBar(
                        message = "Photo capture success",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun startCamera() {
        setLayoutForAction(true)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setLayoutForAction(isTakeImage: Boolean) {
        if (isTakeImage) {
            TransitionManager.beginDelayedTransition(
                binding.profileImageBottomSheetAction,
                AutoTransition()
            )
            binding.imagePreview.visibility = View.VISIBLE
            println(binding.textView2.marginTop)
            binding.imagePreview.layoutParams.height = (
                    Resources.getSystem().displayMetrics.heightPixels -
                            binding.textView2.height -
                            binding.textView2.marginTop * 2
                    )
            binding.actionLayout.visibility = View.GONE
        } else {
            TransitionManager.beginDelayedTransition(
                binding.profileImageBottomSheetAction,
                AutoTransition()
            )
            binding.actionLayout.visibility = View.VISIBLE
            binding.imagePreview.visibility = View.GONE
        }
    }

    private var getPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            println(isGranted)
            if (!isGranted.containsValue(false)) {
                showSnackBar(
                    message = "Permission granted",
                    status = SNACK_BAR_STATUS_SUCCESS,
                    icon = R.drawable.ic_success
                )
                startCamera()
            } else {
                showSnackBar(
                    message = "Oops, you just denied the permission",
                    status = SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_sad
                )
            }
        }


    private val getManageFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                // enter you code here
            }
        }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = requireContext().getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(requireContext(), snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(requireContext(), snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(requireContext().getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }

    companion object {
        val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)

        @RequiresApi(Build.VERSION_CODES.R)
        val intent =
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)

        private const val TAG = "JolieCafe"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && Environment.isExternalStorageLegacy()) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}