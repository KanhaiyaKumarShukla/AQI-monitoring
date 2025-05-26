package com.example.sih.di


import com.example.sih.repository.AqiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import com.example.sih.util.LocationService

@Module
@InstallIn(ViewModelComponent::class)
object AqiModule {

    @Provides
    fun provideAqiRepository(firestore: FirebaseFirestore): AqiRepository =
        AqiRepository(firestore)

    @Provides
    @Singleton
    fun provideLocationService(@ApplicationContext context: Context): LocationService =
        LocationService(context)

    @Provides
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

}
