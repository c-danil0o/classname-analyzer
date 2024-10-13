package org.example.services

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.util.File
import org.example.util.Repository
import org.example.util.SearchResult
import org.example.util.Tree

class GitHubAPIService(private val githubToken: String) {
    
    private val client = HttpClient(CIO){
        install(ContentNegotiation){
            json(Json{
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Auth){
            bearer{
                loadTokens{
                    BearerTokens(githubToken, "")
                }
            }
        }
    }
    
    suspend fun fetchRepositories(language: String, page: Int, resultsPerPage:Int): List<Repository> {
        val url = "https://api.github.com/search/repositories?q=language:$language&sort=stars&order=desc&page=$page&per_page=$resultsPerPage" // max search results per page ==  100
        val response: HttpResponse = client.get(url)
        return response.body<SearchResult>().items
    }
    
    suspend fun fetchJavaFiles(repository: Repository): List<File>{
        val url = repository.trees_url.replace("{/sha}","/"+repository.default_branch).plus("?recursive=1")
        val response: HttpResponse = client.get(url)
        var files: Tree = response.body<Tree>()
        return files.tree.filter{it.path.endsWith(".java")}
    }
    
}