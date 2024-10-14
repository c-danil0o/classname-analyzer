package org.example.util

import javax.lang.model.SourceVersion

class NameProcessor(private val wordsMap: MutableMap<String, Int>) {
    private val wordRegex = Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])")

    private fun extractClassName(name: String): String {
        return name.substringAfterLast("/").removeSuffix(".java")
    }

    private fun iSValidJavaClassName(name: String): Boolean {
        return SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name)
    }

    fun processNames(names: List<File>): Unit {
        for (name in names) {
            val className = extractClassName(name.path)
            if (!iSValidJavaClassName(className)) return
            for (word in className.split(wordRegex.toPattern())) {
                if (word in wordsMap.keys) {
                    wordsMap[word] = wordsMap[word]!! + 1
                } else {
                    wordsMap[word] = 1
                }
            }
            
        }
    }
}
