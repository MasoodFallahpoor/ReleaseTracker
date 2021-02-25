package ir.fallahpoor.releasetracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.fallahpoor.releasetracker.addlibrary.view.AddLibraryScreen
import ir.fallahpoor.releasetracker.addlibrary.viewmodel.AddLibraryViewModel
import ir.fallahpoor.releasetracker.common.NightModeManager
import ir.fallahpoor.releasetracker.common.Screen
import ir.fallahpoor.releasetracker.data.utils.LocalStorage
import ir.fallahpoor.releasetracker.libraries.view.LibrariesListScreen
import ir.fallahpoor.releasetracker.libraries.viewmodel.LibrariesViewModel
import javax.inject.Inject

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var nightModeManager: NightModeManager

    @Inject
    lateinit var localStorage: LocalStorage
    private val librariesViewModel: LibrariesViewModel by viewModels()
    private val addLibraryViewModel: AddLibraryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()

            NavHost(navController, startDestination = Screen.LibrariesList.route) {
                composable(Screen.LibrariesList.route) {
                    LibrariesListScreen(
                        librariesViewModel = librariesViewModel,
                        nightModeManager = nightModeManager,
                        localStorage = localStorage,
                        navController = navController
                    )
                }
                composable(Screen.AddLibrary.route) {
                    AddLibraryScreen(
                        addLibraryViewModel = addLibraryViewModel,
                        navController = navController,
                        isDarkTheme = nightModeManager.isDarkTheme()
                    )
                }
            }

        }

    }

}