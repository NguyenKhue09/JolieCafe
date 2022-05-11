package com.nt118.joliecafe.ui.fragments.profile_bottom_sheet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nt118.joliecafe.BuildConfig
import com.nt118.joliecafe.R
import com.nt118.joliecafe.databinding.FragmentProfileBottomSheetBinding
import com.nt118.joliecafe.firebase.firebasefirestore.FirebaseStorage
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.NetworkListener
import com.nt118.joliecafe.viewmodels.profile_activity.ProfileActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

@AndroidEntryPoint
class ProfileBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentProfileBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val profileActivityViewModel: ProfileActivityViewModel by viewModels()

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBottomSheetBinding.inflate(layoutInflater, container, false)

        profileActivityViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) {
            profileActivityViewModel.backOnline = it
        }

        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    profileActivityViewModel.networkStatus = status
                    profileActivityViewModel.showNetworkStatus()
                }
        }

        lifecycleScope.launchWhenStarted {
            profileActivityViewModel.readUserToken.collectLatest { token ->
                profileActivityViewModel.userToken = token
            }
        }


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

        profileActivityViewModel.updateUserDataResponse.observe(this) { userInfos ->
            when (userInfos) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    Toast.makeText(requireContext(), "Update data success", Toast.LENGTH_SHORT)
                        .show()
                    profileActivityViewModel.saveIsUserDataChange(true)
                    this.dismiss()
                }
                is ApiResult.Error -> {
                    Toast.makeText(requireContext(), "Update data failed!", Toast.LENGTH_SHORT)
                        .show()
                    profileActivityViewModel.saveIsUserDataChange(false)
                }
                else -> {}
            }
        }

        return binding.root
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
                Toast.makeText(
                    context,
                    "Permission granted",
                    Toast.LENGTH_LONG
                ).show()
                firebaseStorage.chooseFile(getFile)
            } else {
                Toast.makeText(
                    context,
                    "Oops, you just denied the permission for storage, You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
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
                        Toast.makeText(
                            context,
                            "Get image failed!",
                            Toast.LENGTH_LONG
                        ).show()
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
                    requireContext(), it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    requireContext(), it) == PackageManager.PERMISSION_GRANTED
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

    private fun takePhoto() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    //it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private var getPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            println(isGranted)
            if (!isGranted.containsValue(false)) {
                Toast.makeText(
                    context,
                    "Permissions granted",
                    Toast.LENGTH_LONG
                ).show()
                startCamera()
            } else {
                Toast.makeText(
                    context,
                    "Oops, you just denied the permission, You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    private val getManageFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Environment.isExternalStorageManager()) {
                // enter you code here
            }
        }

    companion object {
        val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
        @RequiresApi(Build.VERSION_CODES.R)
        val intent   =
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