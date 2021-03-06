package ir.fallahpoor.releasetracker.data.repository

import ir.fallahpoor.releasetracker.data.database.LibraryDao
import ir.fallahpoor.releasetracker.data.entity.Library
import ir.fallahpoor.releasetracker.data.entity.LibraryVersion
import ir.fallahpoor.releasetracker.data.utils.SortOrder
import ir.fallahpoor.releasetracker.data.utils.storage.Storage
import ir.fallahpoor.releasetracker.data.webservice.GithubWebservice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LibraryRepositoryImpl
@Inject constructor(
    private val storage: Storage,
    private val libraryDao: LibraryDao,
    private val githubWebservice: GithubWebservice
) : LibraryRepository {

    companion object {
        private const val GITHUB_BASE_URL = "https://github.com/"
    }

    override suspend fun addLibrary(
        libraryName: String,
        libraryUrl: String,
        libraryVersion: String
    ) {
        libraryDao.insert(Library(libraryName, libraryUrl, libraryVersion))
    }

    override suspend fun updateLibrary(library: Library) {
        libraryDao.update(library)
    }

    override suspend fun getLibrary(libraryName: String): Library? {
        return libraryDao.get(libraryName)
    }

    override fun getLibraries(
        sortOrder: SortOrder,
        searchTerm: String
    ): Flow<List<Library>> = when (sortOrder) {
        SortOrder.A_TO_Z -> libraryDao.getAllSortedByNameAscending(searchTerm)
        SortOrder.Z_TO_A -> libraryDao.getAllSortedByNameDescending(searchTerm)
        SortOrder.PINNED_FIRST -> libraryDao.getAllSortedByPinnedFirst(searchTerm)
    }

    override suspend fun getLibraries(): List<Library> = libraryDao.getAll()

    override suspend fun deleteLibrary(library: Library) {
        libraryDao.delete(library.name)
    }

    override suspend fun getLibraryVersion(libraryName: String, libraryUrl: String): String {

        val libraryPath = libraryUrl.removePrefix(GITHUB_BASE_URL)
        val libraryOwner = libraryPath.substring(0 until libraryPath.indexOf("/"))
        val libraryRepo = libraryPath.substring(libraryPath.indexOf("/") + 1)

        val libraryVersion: LibraryVersion =
            githubWebservice.getLatestVersion(libraryOwner, libraryRepo)

        return getRefinedLibraryVersion(libraryName, libraryVersion)

    }

    private fun getRefinedLibraryVersion(
        libraryName: String,
        libraryVersion: LibraryVersion
    ): String {
        return if (libraryVersion.name.isNotBlank()) {
            getRefinedLibraryVersion(libraryName, libraryVersion.name)
        } else {
            getRefinedLibraryVersion(libraryName, libraryVersion.tagName)
        }
    }

    /**
     * Sometimes the given version may contain irrelevant words/letters. Some examples are
     * 'Dagger 2.9.0' or 'v2.1.0'. This method removes such words/letters from the given
     * version.
     */
    private fun getRefinedLibraryVersion(libraryName: String, version: String): String =
        version
            .replace(libraryName, "", ignoreCase = true) // Remove the library name
            .replace("version", "", ignoreCase = true) // Remove the word "version"
            .replace("release", "", ignoreCase = true) // Remove the word "release"
            .replace("v", "", ignoreCase = true) // Remove the letter 'v'
            .replace("r", "", ignoreCase = true) // Remove the letter 'r'
            .trim()

    override suspend fun pinLibrary(library: Library, pinned: Boolean) {
        val newLibrary = library.copy(pinned = if (pinned) 1 else 0)
        libraryDao.update(newLibrary)
    }

    override fun getLastUpdateCheck(): Flow<String> =
        storage.getLastUpdateCheck()

    override fun setLastUpdateCheck(date: String) {
        storage.setLastUpdateCheck(date)
    }

}
