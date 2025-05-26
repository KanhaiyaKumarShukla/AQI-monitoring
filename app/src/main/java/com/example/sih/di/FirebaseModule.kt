package com.example.sih.di

import android.content.Context
import android.location.Geocoder
import com.example.sih.repository.StationRepositoryImpl
import com.example.sih.repository.StationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder = Geocoder(context)

    @Provides
    @Singleton
    fun provideStationRepository(
        firestore: FirebaseFirestore,
        geocoder: Geocoder
    ): StationRepository = StationRepositoryImpl(firestore, geocoder)

}