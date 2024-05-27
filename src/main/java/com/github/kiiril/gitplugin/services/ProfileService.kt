package com.github.kiiril.gitplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.log.VcsUser
import git4idea.GitCommit
import git4idea.GitUserRegistry
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
class ProfileService(private val project: Project, private val cs: CoroutineScope) {
    private fun getCommitsAsync(user: String? = null): Deferred<List<GitCommit>> = cs.async {
        val repository = GitRepositoryManager.getInstance(project).repositories.firstOrNull()
        return@async try {
            val args = if (user != null) arrayOf("--author=$user") else emptyArray()
            GitHistoryUtils.history(project, repository?.root ?: return@async emptyList(), *args)
        } catch (e: VcsException) {
            println("Problem with running git: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getCommitsForTable(): Array<Array<String?>> {
        return getCommitsForTableAsync().await()
    }

    fun getCommitsForTableJava(): CompletableFuture<Array<Array<String?>>> {
        val future = CompletableFuture<Array<Array<String?>>>()
        cs.launch {
            try {
                val result = getCommitsForTable()
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    private fun getCommitsForTableAsync(): Deferred<Array<Array<String?>>> = cs.async {
        val commits = getCommitsAsync().await()
        val size = commits.size
        val commitsData = Array(size) { arrayOfNulls<String>(size) }
        for (i in 0 until size) {
            val commit = commits[i]
            val instant = Instant.ofEpochMilli(commit.timestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
            val formattedDateTime = localDateTime.format(formatter)
            commitsData[i] = arrayOf(formattedDateTime, commit.fullMessage, commit.author.name)
        }
        return@async commitsData
    }

    private suspend fun getUserAndEmail(): Array<String> {
        return getUserAndEmailAsync().await()
    }

    private fun getUserAndEmailAsync(): Deferred<Array<String>> = cs.async {
        val user = GitRepositoryManager.getInstance(project).repositories.firstOrNull()?.root?.let {
            GitUserRegistry.getInstance(project).getUser(it)
        }
        return@async arrayOf(user?.name ?: "", user?.email ?: "")
    }

    fun getUserAndEmailJava(): CompletableFuture<Array<String>> {
        val future = CompletableFuture<Array<String>>()
        cs.launch {
            try {
                val result = getUserAndEmail()
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    private suspend fun getTechnologies(): Map<String, Double> {
        return getTechnologiesAsync().await()
    }
    private fun getTechnologiesAsync(): Deferred<Map<String, Double>> = cs.async {
        val technologyCounts: MutableMap<String, Int> = HashMap()
        traverseProjectAsync(technologyCounts).await()

        // Calculate the total number of files
        val totalFiles = technologyCounts.values.sum()

        // Calculate the percentages for each technology
        val technologyPercentages: MutableMap<String, Double> = HashMap()
        for ((technology, count) in technologyCounts) {
            val percentage = count.toDouble() / totalFiles * 100
            technologyPercentages[technology] = percentage
        }
        return@async technologyPercentages
    }

    fun getTechnologiesJava(): CompletableFuture<Map<String, Double>> {
        val future = CompletableFuture<Map<String, Double>>()
        cs.launch {
            try {
                val result = getTechnologies()
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    private fun traverseProjectAsync(technologyCounts: MutableMap<String, Int>) = cs.async {
        val rootDirs = ProjectRootManager.getInstance(project).contentSourceRoots
        rootDirs.forEach { rootDir ->
            traverseDirectoryAsync(rootDir, technologyCounts).await()
        }
    }

    private fun CoroutineScope.traverseDirectoryAsync(dir: VirtualFile, technologyCounts: MutableMap<String, Int>): Deferred<Unit> = async {
        val childJobs = dir.children.map { file ->
            async {
                if (file.isDirectory) {
                    traverseDirectoryAsync(file, technologyCounts).await()
                } else {
                    val extension = file.extension
                    if (extension != null) {
                        val technology = getTechnologyFromExtension(extension)
                        technologyCounts.merge(technology, 1) { a: Int?, b: Int? -> (a ?: 0) + (b ?: 0) }
                    }
                }
            }
        }
        childJobs.awaitAll()
    }

    private fun getTechnologyFromExtension(extension: String): String {
        return when (extension) {
            "java" -> "Java"
            "py" -> "Python"
            "js" -> "JavaScript"
            "css" -> "CSS"
            "kt" -> "Kotlin"
            "html" -> "HTML"
            else -> "Other"
        }
    }

    private suspend fun getProjectLibraries(): Array<String?>
    {
        return getProjectLibrariesAsync().await()
    }

    private fun getProjectLibrariesAsync(): Deferred<Array<String?>> = cs.async {
        val libraries = LibraryTablesRegistrar.getInstance().getLibraryTable(project).libraries
        val libs = arrayOfNulls<String>(libraries.size)
        for (i in libs.indices) {
            libs[i] = libraries[i].name
        }
        return@async libs
    }

    fun getProjectLibrariesJava(): CompletableFuture<Array<String?>> {
        val future = CompletableFuture<Array<String?>>()
        cs.launch {
            try {
                val result = getProjectLibraries()
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    private fun getCurrentUser(): VcsUser? {
        val repository = GitRepositoryManager.getInstance(project).repositories.firstOrNull()
        return if (repository != null) {
            GitUserRegistry.getInstance(project).getUser(repository.root)
        } else {
            null
        }
    }
    fun getNumberOfCommitsByUserJava(): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        cs.launch {
            try {
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    val commitMessages = getCommitsAsync(currentUser.name).await()
                    future.complete(commitMessages.size)
                } else {
                    future.complete(0)
                }
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }
}