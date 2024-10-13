package org.example

import kotlinx.coroutines.runBlocking
import org.example.services.GitHubAPIService

fun main() = runBlocking {
    val githubToken = System.getenv("GITHUB_TOKEN")
    println(githubToken)
    val apiService = GitHubAPIService(githubToken)
    
    val repositories = apiService.fetchRepositories("Java", 1, 3)
    for (repo in repositories) {
        println(apiService.fetchJavaFiles(repo))
    }
}