package com.ruko.system.di

import com.ruko.system.data.repository.QueueRepositoryImpl
import com.ruko.system.domain.repository.QueueRepository
import com.ruko.system.presentation.AppViewModel
import com.ruko.system.presentation.screens.owner.OwnerHomeViewModel
import com.ruko.system.presentation.screens.owner.profile.OwnerProfileViewModel
import com.ruko.system.presentation.screens.owner.queue.OwnerQueueViewModel
import com.ruko.system.presentation.screens.owner.queue.history.QueueHistoryViewModel
import com.ruko.system.presentation.screens.owner.queue.userqueue.UserQueueViewModel
import com.ruko.system.presentation.screens.signup.AuthViewModel
import com.ruko.system.presentation.screens.user.UserHomeViewModel
import com.ruko.system.presentation.screens.user.current_queue.CurrentQueueViewModel
import com.ruko.system.presentation.screens.user.profile.UserProfileViewModel
import com.ruko.system.presentation.screens.user.queue.JoinQViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val koinModules = module {
    viewModel { AppViewModel(get()) }
    viewModel { OwnerProfileViewModel(get()) }
    viewModel { OwnerHomeViewModel(get(), get()) }
    viewModel { UserQueueViewModel(get()) }
    viewModel { OwnerQueueViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { UserHomeViewModel(get(), get()) }
    viewModel { UserProfileViewModel(get()) }
    viewModel { JoinQViewModel(get(), get()) }
    viewModel { CurrentQueueViewModel(get(), get()) }
    viewModel { QueueHistoryViewModel() }
    single<QueueRepository> {
        QueueRepositoryImpl(
            Firebase.auth,
            Firebase.database,
            Firebase.firestore,
            get()
        )
    }
}