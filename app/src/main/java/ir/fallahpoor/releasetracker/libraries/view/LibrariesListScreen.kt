package ir.fallahpoor.releasetracker.libraries.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import ir.fallahpoor.releasetracker.R
import ir.fallahpoor.releasetracker.common.DefaultSnackbar
import ir.fallahpoor.releasetracker.common.NightModeManager
import ir.fallahpoor.releasetracker.common.SPACE_NORMAL
import ir.fallahpoor.releasetracker.common.Screen
import ir.fallahpoor.releasetracker.data.entity.Library
import ir.fallahpoor.releasetracker.libraries.view.dialogs.NightModeScreen
import ir.fallahpoor.releasetracker.libraries.view.states.LibrariesListState
import ir.fallahpoor.releasetracker.libraries.view.states.LibraryDeleteState
import ir.fallahpoor.releasetracker.libraries.viewmodel.LibrariesViewModel
import ir.fallahpoor.releasetracker.theme.ReleaseTrackerTheme

@ExperimentalFoundationApi
@Composable
fun LibrariesListScreen(
    navController: NavController,
    nightModeManager: NightModeManager,
    librariesViewModel: LibrariesViewModel
) {

    librariesViewModel.getLibraries()

    val nightMode by librariesViewModel.nightMode.observeAsState()
    val isNightModeOn = when (nightMode) {
        NightModeManager.Mode.OFF.name -> false
        NightModeManager.Mode.ON.name -> true
        else -> isSystemInDarkTheme()
    }
    val scaffoldState = rememberScaffoldState()

    ReleaseTrackerTheme(darkTheme = isNightModeOn) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.app_name))
                    },
                    actions = {
                        ActionButtons(nightModeManager)
                    }
                )
            },
            scaffoldState = scaffoldState,
            snackbarHost = {
                scaffoldState.snackbarHostState
            }
        ) {
            LibrariesListContent(librariesViewModel, navController, scaffoldState)
        }
    }

}

@Composable
private fun ActionButtons(nightModeManager: NightModeManager) {

    IconButton(onClick = { /*showSortDialog()*/ }) {
        Icon(
            imageVector = Icons.Filled.Sort,
            contentDescription = stringResource(R.string.sort)
        )
    }

    IconButton(onClick = { /*TODO*/ }) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(R.string.search)
        )
    }

    val nightModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    if (nightModeSupported) {
        var showDropdownMenu by remember { mutableStateOf(false) }
        val showNightModeDialog = remember { mutableStateOf(false) }
        IconButton(onClick = { showDropdownMenu = !showDropdownMenu }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options)
            )
        }
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false })
        {
            DropdownMenuItem(
                onClick = {
                    showDropdownMenu = false
                    showNightModeDialog.value = true
                }
            ) {
                Text(text = stringResource(R.string.night_mode))
            }
        }
        if (showNightModeDialog.value) {
            NightModeDialog(showNightModeDialog, nightModeManager)
        }
    }

}

//private fun showSortDialog() {
//    val dialogDialog = SortDialog()
//    dialogDialog.setListener(SortListener())
//    showDialogFragment(dialogDialog, SortDialog.TAG)
//}

//private class SortListener : SortDialog.SortListener {
//
//    override fun orderSelected(order: SortDialog.Order) {
//        if (order != currentOrder) {
//            currentOrder = order
//            librariesViewModel.getLibraries(getSortingOrder(order))
//        }
//    }
//
//    private fun getSortingOrder(order: SortDialog.Order) = when (order) {
//        SortDialog.Order.A_TO_Z -> LibrariesViewModel.Order.A_TO_Z
//        SortDialog.Order.Z_TO_A -> LibrariesViewModel.Order.Z_TO_A
//        SortDialog.Order.PINNED_FIRST -> LibrariesViewModel.Order.PINNED_FIRST
//    }
//
//}

@Composable
private fun NightModeDialog(showDialog: MutableState<Boolean>, nightModeManager: NightModeManager) {
    var nightMode: NightModeManager.Mode? = null
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Text(text = stringResource(R.string.select_night_mode))
        },
        text = {
            NightModeScreen(nightModeManager) {
                nightMode = it
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nightMode?.let {
                        nightModeManager.setNightMode(it)
                    }
                    showDialog.value = false
                }
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@ExperimentalFoundationApi
@Composable
private fun LibrariesListContent(
    librariesViewModel: LibrariesViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState
) {

    val librariesListState: LibrariesListState by librariesViewModel.librariesListState.observeAsState(
        LibrariesListState.Fresh
    )
    val lastUpdateCheckState by librariesViewModel.lastUpdateCheckState.observeAsState("N/A")
    val libraryDeleteState by librariesViewModel.deleteState.observeAsState(LibraryDeleteState.Fresh)

    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        LastUpdateCheckText(lastUpdateCheckState)
        when (librariesListState) {
            is LibrariesListState.Loading -> {
                CircularProgressIndicator()
            }
            is LibrariesListState.LibrariesLoaded -> {
                val libraries: List<Library> =
                    (librariesListState as LibrariesListState.LibrariesLoaded).libraries
                LibrariesList(
                    navController = navController,
                    scaffoldState = scaffoldState,
                    libraries = libraries,
                    libraryDeleteState = libraryDeleteState,
                    clickListener = { library: Library ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(library.url)
                        }
//                        startActivity(intent)
                    },
                    longClickListener = { library: Library ->
//                        showDeleteConfirmationDialog(library)
                    },
                    pinClickListener = { library: Library, pin: Boolean ->
                        librariesViewModel.setPinned(library, pin)
                    })
            }
            LibrariesListState.Fresh -> {
            }
        }
    }

}

//private fun showDeleteConfirmationDialog(library: Library) {
//    val dialogFragment = DeleteLibraryDialog()
//    dialogFragment.setListener {
//        librariesViewModel.deleteLibrary(library.name)
//    }
//    showDialogFragment(dialogFragment, DeleteLibraryDialog.TAG)
//}

@Composable
private fun LastUpdateCheckText(lastUpdateCheck: String) {
    Text(
        text = stringResource(R.string.last_check_for_updates, lastUpdateCheck),
        modifier = Modifier.padding(SPACE_NORMAL.dp)
    )
}

@ExperimentalFoundationApi
@Composable
private fun LibrariesList(
    navController: NavController,
    scaffoldState: ScaffoldState,
    libraries: List<Library>,
    libraryDeleteState: LibraryDeleteState,
    clickListener: (Library) -> Unit,
    longClickListener: (Library) -> Unit,
    pinClickListener: (Library, Boolean) -> Unit
) {
    if (libraries.isEmpty()) {
        NoLibrariesText()
    } else {
        if (libraryDeleteState is LibraryDeleteState.InProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Box(contentAlignment = Alignment.BottomStart, modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                itemsIndexed(libraries) { index: Int, library: Library ->
                    LibraryItem(
                        library = library,
                        clickListener = clickListener,
                        longClickListener = longClickListener,
                        pinClickListener = pinClickListener
                    )
                    if (index != libraries.lastIndex) {
                        Divider()
                    }
                }
            }
            AddLibraryButton(navController)
//            when (libraryDeleteState) {
//                is LibraryDeleteState.Error -> {
//                    lifecycleScope.launch {
//                        scaffoldState.snackbarHostState.showSnackbar(
//                            message = libraryDeleteState.message
//                        )
//                    }
//                }
//                is LibraryDeleteState.Deleted -> {
//                    val message = stringResource(R.string.library_deleted)
//                    lifecycleScope.launch {
//                        scaffoldState.snackbarHostState.showSnackbar(
//                            message = message
//                        )
//                    }
//                }
//            }
            DefaultSnackbar(
                snackbarHostState = scaffoldState.snackbarHostState
            )
        }
    }
}

@Composable
private fun NoLibrariesText() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.no_libraries),
            modifier = Modifier.padding(SPACE_NORMAL.dp)
        )
    }
}

@ExperimentalFoundationApi
@Composable
private fun LibraryItem(
    library: Library,
    clickListener: (Library) -> Unit,
    longClickListener: (Library) -> Unit,
    pinClickListener: (Library, Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    clickListener.invoke(library)
                },
                onLongClick = {
                    longClickListener.invoke(library)
                }
            )
            .padding(
                end = SPACE_NORMAL.dp,
                top = SPACE_NORMAL.dp,
                bottom = SPACE_NORMAL.dp
            )
    ) {
        PinToggleButton(library, pinClickListener)
        Column(modifier = Modifier.weight(1f)) {
            LibraryNameText(library)
            LibraryUrlText(library)
        }
        Text(text = library.version)
    }
}

@Composable
private fun PinToggleButton(library: Library, onCheckedChange: (Library, Boolean) -> Unit) {
    IconToggleButton(
        checked = library.isPinned(),
        onCheckedChange = {
            onCheckedChange.invoke(library, it)
        }
    ) {
        val pinImage = if (library.isPinned()) Icons.Filled.PushPin else Icons.Outlined.PushPin
        Icon(
            imageVector = pinImage,
            tint = MaterialTheme.colors.secondary,
            contentDescription = stringResource(R.string.pin_library)
        )
    }
}

@Composable
private fun LibraryNameText(library: Library) {
    EllipsisText(text = library.name, style = MaterialTheme.typography.body1)
}

@Composable
private fun LibraryUrlText(library: Library) {
    EllipsisText(
        text = library.url,
        style = MaterialTheme.typography.body2,
        modifier = Modifier.padding(end = SPACE_NORMAL.dp)
    )
}

@Composable
private fun EllipsisText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = style,
        modifier = modifier
    )
}

@Composable
private fun AddLibraryButton(navController: NavController) {
    FloatingActionButton(
        onClick = {
            navController.navigate(Screen.AddLibrary.route)
        },
        modifier = Modifier.padding(SPACE_NORMAL.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.add_library)
        )
    }
}