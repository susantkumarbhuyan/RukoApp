package com.ruko.system.presentation

import androidx.lifecycle.ViewModel
import com.ruko.system.data.local.preference.PreferenceManager

class AppViewModel(
    val preferenceManager: PreferenceManager
) : ViewModel()