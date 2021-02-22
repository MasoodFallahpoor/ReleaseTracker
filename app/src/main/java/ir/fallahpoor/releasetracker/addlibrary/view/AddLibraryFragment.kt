package ir.fallahpoor.releasetracker.addlibrary.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.fallahpoor.releasetracker.R
import ir.fallahpoor.releasetracker.addlibrary.viewmodel.AddLibraryViewModel
import ir.fallahpoor.releasetracker.common.DefaultSnackbar
import ir.fallahpoor.releasetracker.common.NightModeManager
import ir.fallahpoor.releasetracker.common.SPACE_NORMAL
import ir.fallahpoor.releasetracker.common.SPACE_SMALL
import ir.fallahpoor.releasetracker.theme.ReleaseTrackerTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalMaterialApi
class AddLibraryFragment : Fragment() {

    @Inject
    lateinit var nightModeManager: NightModeManager
    private val addLibraryViewModel: AddLibraryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            AddLibraryScreen()
        }
    }

    @Composable
    private fun AddLibraryScreen() {
        ReleaseTrackerTheme(darkTheme = nightModeManager.isDarkTheme()) {
            val scaffoldState = rememberScaffoldState()
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = stringResource(R.string.add_library))
                        },
                        navigationIcon = {
                            BackButton()
                        }
                    )
                },
                scaffoldState = scaffoldState,
                snackbarHost = {
                    scaffoldState.snackbarHostState
                }
            ) {
                AddLibraryContent(scaffoldState)
            }
        }
    }

    @Composable
    private fun BackButton() {
        IconButton(
            onClick = {
                findNavController().popBackStack()
            }
        ) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
        }
    }

    @Composable
    private fun AddLibraryContent(scafoldState: ScaffoldState) {

        val state: State by addLibraryViewModel.state.observeAsState(State.Fresh)

        Column(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            if (state is State.Loading) {
                ProgressIndicator()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(SPACE_NORMAL.dp)
            ) {
                LibraryNameTextField(state)
                if (state is State.EmptyLibraryName) {
                    ErrorText(R.string.library_name_empty)
                }
                Spacer(modifier = Modifier.height(SPACE_SMALL.dp))
                LibraryUrlTextField(state)
                if (state is State.EmptyLibraryUrl) {
                    ErrorText(R.string.library_url_empty)
                } else if (state is State.InvalidLibraryUrl) {
                    ErrorText(R.string.library_url_invalid)
                }
            }
            AddLibraryButton(state)
            if (state is State.Error) {
                val errorMessage = (state as State.Error).message
                lifecycleScope.launch {
                    scafoldState.snackbarHostState.showSnackbar(message = errorMessage)
                }
            }
            if (state is State.LibraryAdded) {
                val message = stringResource(R.string.library_added)
                lifecycleScope.launch {
                    scafoldState.snackbarHostState.showSnackbar(message = message)
                }
            }
            DefaultSnackbar(
                snackbarHostState = scafoldState.snackbarHostState
            )
        }
    }

    @Composable
    private fun ProgressIndicator() {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        )
    }

    @Composable
    private fun LibraryNameTextField(state: State) {
        TextFieldWithHint(
            text = addLibraryViewModel.libraryName,
            hint = stringResource(R.string.library_name),
            imeAction = ImeAction.Next,
            onTextChange = { addLibraryViewModel.libraryName = it },
            modifier = Modifier.fillMaxWidth(),
            isErrorValue = state is State.EmptyLibraryName
        )
    }

    @Composable
    private fun LibraryUrlTextField(state: State) {
        TextFieldWithHint(
            text = addLibraryViewModel.libraryUrl,
            hint = stringResource(R.string.library_url),
            onTextChange = { addLibraryViewModel.libraryUrl = it },
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth(),
            isErrorValue = state is State.EmptyLibraryUrl || state is State.InvalidLibraryUrl
        )
    }

    @Composable
    private fun ErrorText(@StringRes textRes: Int) {
        Text(text = stringResource(textRes), style = TextStyle(color = MaterialTheme.colors.error))
    }

    @Composable
    private fun AddLibraryButton(state: State) {
        Button(
            modifier = Modifier.padding(SPACE_NORMAL.dp),
            onClick = { addLibraryViewModel.addLibrary() },
            enabled = state !is State.Loading
        ) {
            Text(
                text = stringResource(R.string.add_library),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun TextFieldWithHint(
        text: String,
        hint: String,
        onTextChange: (String) -> Unit,
        imeAction: ImeAction,
        isErrorValue: Boolean,
        modifier: Modifier
    ) {
        OutlinedTextField(
            value = text,
            label = {
                Text(text = hint)
            },
            onValueChange = onTextChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = imeAction
            ),
            singleLine = true,
            isErrorValue = isErrorValue,
            modifier = modifier
        )
    }

    @Preview(name = "Add Library Screen")
    @Composable
    fun AddLibraryScreenPreview() {
        AddLibraryScreen()
    }

}