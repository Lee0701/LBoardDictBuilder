package io.github.lee0701.lboard.dictbuilder

interface Dictionary {

    fun search(word: String): List<Word>
    fun searchPrefix(prefix: String, length: Int): List<Word>
    fun searchSequence(seq: List<Int>, layout: Map<Int, List<Int>>): List<Word>

}
