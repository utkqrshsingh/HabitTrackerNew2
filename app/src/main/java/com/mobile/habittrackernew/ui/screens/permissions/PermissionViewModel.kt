// ui/screens/permissions/PermissionViewModel.kt
package com.mobile.habittrackernew.ui.screens.permissions

import android.content.Context
import androidx.lifecycle.ViewModel
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.services.PermissionHelper
import com.mobile.habittrackernew.services.PermissionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val permissionHelper = PermissionHelper(context)

    private val _permissionStatus = MutableStateFlow(PermissionStatus())
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        _permissionStatus.value = permissionHelper.checkAllPermissions()
    }

    fun areAllPermissionsGranted(): Boolean {
        return permissionHelper.checkAllPermissions().allGranted
    }
}