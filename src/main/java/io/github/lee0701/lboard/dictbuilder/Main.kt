package io.github.lee0701.lboard.dictbuilder

import java.io.*
import java.text.Normalizer
import kotlin.math.log10
import kotlin.math.log2

fun main() {

    val dirName = "../dict/"
    val outFileName = "../dict.bin"
    val dirFile = File(dirName)

    val dictionary = TrieDictionary()
    var count = 0

    val totalWords = mutableListOf<PreInsertWord>()
    var maxFrequency = 0

    dirFile.list().forEachIndexed { i, name ->
        if(name.startsWith(".")) return@forEachIndexed
        println("${i+1} - $name")

        val br = BufferedReader(InputStreamReader(FileInputStream(dirName + name)))

        val currentWords: MutableList<Pair<String, Int>> = mutableListOf()
        br.forEachLine { it.split('\t').let { currentWords += it[0] to it[1].toInt() } }
        val max = currentWords.maxBy { it.second }?.second ?: 0

        currentWords.map { Normalizer.normalize(it.first, Normalizer.Form.NFD) to it.second }
            .forEach {
                totalWords += PreInsertWord(it.first, i+1, it.second)
            }

        count += currentWords.size
        if(max > maxFrequency) maxFrequency = max
    }

    println("max: $maxFrequency")

    totalWords.forEach {
        dictionary.insert(it.word, it.pos, (it.frequency.toDouble() / maxFrequency).toFloat())
    }

    val baos = ByteArrayOutputStream(count * 32)
    TrieDictionary.write(dictionary.root.serialize(), baos)

    val out = FileOutputStream(outFileName)
    TrieDictionary.write(dictionary.root.serialize(), out)

    val flatDictionary = FlatTrieDictionary(FileInputStream(outFileName).readBytes())

    while(true) {
        val line = readLine() ?: break
        val normalized = Normalizer.normalize(line, Normalizer.Form.NFD)
        println(dictionary.search(normalized))
        println(flatDictionary.search(normalized))
    }

}

data class PreInsertWord(
    val word: String,
    val pos: Int,
    val frequency: Int
)
