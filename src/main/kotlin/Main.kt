package org.example

import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.example.services.GitHubAPIService
import org.example.util.NameProcessor
import java.io.File
import java.util.concurrent.ConcurrentHashMap

fun main() = runBlocking {
    val numberOfPages: Int = 10 // one page contains 100 repositories
    val pagesPerCoroutine: Int = 2
    val numberOfCoroutines: Int = (numberOfPages + pagesPerCoroutine - 1) / pagesPerCoroutine

    val githubToken = System.getenv("GITHUB_TOKEN")
    val apiService = GitHubAPIService(githubToken)

    var wordStatistics: MutableMap<String, Int> = mutableMapOf<String, Int>()
    val deferredResults = mutableListOf<Deferred<Map<String, Int>>>()

    val pageMap = divideRange(1, numberOfPages, numberOfCoroutines)
    coroutineScope {
        for (i in 0..numberOfCoroutines - 1) {
            val deferred = async(Dispatchers.Default) {
                val localWordMap = mutableMapOf<String, Int>()
                val nameProcessor = NameProcessor(localWordMap)
                for (page in pageMap[i].first..pageMap[i].second) {
                    val repositories = apiService.fetchRepositories("Java", page, 100)
                    for (repo in repositories) {
                        nameProcessor.processNames(apiService.fetchJavaFiles(repo))
                    }
                }
                return@async localWordMap
            }
            deferredResults.add(deferred)
        }
    }
    
    // Reducing results from map phase 
    deferredResults.awaitAll().forEach { localMap -> 
        for ((word, count) in localMap) {
            wordStatistics.merge(word, count, Int::plus)
        }
    }
    // Filter out classname lengths in words
    val classNameLength = wordStatistics.filterKeys { key -> isNumericToX(key)}
    
    val wordStatisticsFiltered = wordStatistics.minus(classNameLength.keys)
    // Sort by value and take only top 1000 
    val mostCommonWords = wordStatisticsFiltered.toList().sortedByDescending { it.second }.take(1000).toMap()
    
    // Write to file
    val wordsFile = File("most_common_words.txt")
    wordsFile.printWriter().use { out ->
        mostCommonWords.forEach {
            (key, value) -> out.println("$key: $value")
        }
    }

    val lengthFile = File("classname_length.txt")
    lengthFile.printWriter().use { out ->
        classNameLength.forEach {
                (key, value) -> out.println("$key: $value")
        }
    }

}

// Divide a range into N ranges
fun divideRange(start: Int, end: Int, N: Int): List<Pair<Int, Int>> {
    val totalSize = end - start + 1
    val baseSize = totalSize / N
    val remainder = totalSize % N

    val ranges = mutableListOf<Pair<Int, Int>>()
    var currentStart = start

    for (i in 0 until N) {
        val currentEnd = currentStart + baseSize + if (i < remainder) 1 else 0 - 1
        ranges.add(Pair(currentStart, currentEnd))
        currentStart = currentEnd + 1
    }

    return ranges
}


fun isNumericToX(toCheck: String): Boolean {
    return toCheck.toDoubleOrNull() != null
}
