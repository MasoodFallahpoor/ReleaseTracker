package ir.fallahpoor.releasetracker.libraries.view.states

import ir.fallahpoor.releasetracker.data.entity.Library

sealed class LibrariesListState {

    object Fresh : LibrariesListState()
    object Loading : LibrariesListState()
    class LibrariesLoaded(val libraries: List<Library>) : LibrariesListState()

}