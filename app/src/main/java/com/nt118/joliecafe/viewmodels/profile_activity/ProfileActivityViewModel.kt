package com.nt118.joliecafe.viewmodels.profile_activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileActivityViewModel @Inject constructor(
    application: Application,
): AndroidViewModel(application) {

    var isChangePassword = false

}