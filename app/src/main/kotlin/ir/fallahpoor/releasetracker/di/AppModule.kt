package ir.fallahpoor.releasetracker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

import androidx.room.Room
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.fallahpoor.releasetracker.BuildConfig
import ir.fallahpoor.releasetracker.data.database.DatabaseContract
import ir.fallahpoor.releasetracker.data.database.LibraryDao
import ir.fallahpoor.releasetracker.data.database.LibraryDatabase
import ir.fallahpoor.releasetracker.data.repository.LibraryRepository
import ir.fallahpoor.releasetracker.data.repository.LibraryRepositoryImpl
import ir.fallahpoor.releasetracker.data.utils.storage.LocalStorage
import ir.fallahpoor.releasetracker.data.utils.storage.Storage
import ir.fallahpoor.releasetracker.data.webservice.GithubWebservice
import ir.fallahpoor.releasetracker.data.webservice.WebserviceFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    fun provideRxkPrefs(sharedPreferences: SharedPreferences): RxkPrefs =
        rxkPrefs(sharedPreferences)

    @Provides
    @Singleton
    fun provideLibraryDatabase(context: Context): LibraryDatabase =
        Room.databaseBuilder(context, LibraryDatabase::class.java, DatabaseContract.DATABASE_NAME)
            .createFromAsset("database/libraries.db")
            .build()

    @Provides
    fun provideLibraryRepository(libraryRepositoryImpl: LibraryRepositoryImpl): LibraryRepository {
        return libraryRepositoryImpl
    }

    @Provides
    fun provideGithubWebservice(webserviceFactory: WebserviceFactory): GithubWebservice {
        return webserviceFactory.createGithubService(
            serviceClass = GithubWebservice::class.java,
            isDebugBuild = BuildConfig.DEBUG
        )
    }

    @Provides
    fun provideLibraryDao(libraryDatabase: LibraryDatabase): LibraryDao {
        return libraryDatabase.libraryDao()
    }

    @Provides
    fun provideStorage(localStorage: LocalStorage): Storage {
        return localStorage
    }

}