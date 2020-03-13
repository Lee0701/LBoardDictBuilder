package io.github.lee0701.lboard.dictbuilder

import org.openkoreantext.processor.OpenKoreanTextProcessorJava
import java.io.*
import java.text.Normalizer
import kotlin.math.log10

fun main() {

    val sourceFileName = "../source.txt"
    val dirName = "../dict/"
    val outFileName = "../dict.bin"

    buildDictionaries(sourceFileName, dirName)
    val dictionary = buildTrieDictionary(dirName)
    writeBinaryDictionary(dictionary, outFileName)
    testBinaryDictionary()
}

fun testBinaryDictionary() {

//    val baos = ByteArrayOutputStream(dictionary.count * 32)
//    TrieDictionary.write(dictionary.root.serialize(), baos)

//    val flatDictionary = FlatTrieDictionary(baos.toByteArray())
    val flatDictionary = FlatTrieDictionary(FileInputStream("../dict.bin").readBytes())

    while(true) {
        val line = readLine() ?: break
        val normalized = Normalizer.normalize(line, Normalizer.Form.NFD)
//        println(dictionary.search(normalized))
        println(flatDictionary.search(normalized))
    }

}

fun buildTrieDictionary(dirName: String): TrieDictionary {
    val dirFile = File(dirName)

    val dictionary = TrieDictionary()

    val totalWords = mutableListOf<PreInsertWord>()
    var maxFrequencies = mutableMapOf<Int, Int>()

    dirFile.list().forEachIndexed { i, name ->
        if(name.startsWith(".")) return@forEachIndexed
        val posCode = i + 1
        println("$posCode - $name")

        val br = BufferedReader(InputStreamReader(FileInputStream(dirName + name)))

        val currentWords: MutableList<Pair<String, Int>> = mutableListOf()
        br.forEachLine { it.split('\t').let { currentWords += it[0] to it[1].toInt() } }
        val max = currentWords.maxBy { it.second }?.second ?: 0

        currentWords.map { Normalizer.normalize(it.first, Normalizer.Form.NFD) to it.second }
            .forEach {
                totalWords += PreInsertWord(it.first, posCode, it.second)
            }

        maxFrequencies[posCode] = max
    }

    println("maxFrequencies: $maxFrequencies")

    val maxFrequency = maxFrequencies.values.max()!!

    totalWords.forEach {
        dictionary.insert(it.word, it.pos, (log10(it.frequency.toDouble()) / log10(maxFrequency.toDouble())).toFloat())
    }

    return dictionary
}

fun writeBinaryDictionary(dictionary: TrieDictionary, outFileName: String) {
    val out = FileOutputStream(outFileName)
    TrieDictionary.write(dictionary.root.serialize(), out)
}

fun buildDictionaries(sourceFileName: String, dirName: String, cutFrequency: Int = 10) {
    val dirFile = File(dirName)
    dirFile.mkdirs()

    val br = BufferedReader(InputStreamReader(FileInputStream(sourceFileName)))

    val dict = mutableMapOf<String, MutableMap<String, Int>>()

    var i = 0
    br.forEachLine { line ->
        if(i % 1000 == 0) println(i)
        if(line.isEmpty()) return@forEachLine
//        val normalized = OpenKoreanTextProcessorJava.normalize(line)
        val normalized = line
        val tokens = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(
            OpenKoreanTextProcessorJava.tokenize(normalized))
        tokens.forEach { token ->
            val pos = token.pos.name
            val text = token.text
            if(!dict.containsKey(pos)) dict[pos] = mutableMapOf()
            if(!dict[pos]!!.containsKey(text)) dict[pos]!![text] = 0
            dict[pos]!![text] = dict[pos]!![text]!! + 1
        }
        i++
    }

    dict.forEach { pos, words ->
        val sorted = words.toList().filter { it.second >= cutFrequency }.sortedByDescending { it.second }.toMap()
        val content = sorted.map { "${it.key}\t${it.value}" }.joinToString("\n")
        FileOutputStream(File(dirFile, "$pos")).write(content.toByteArray())
    }

}

data class PreInsertWord(
    val word: String,
    val pos: Int,
    val frequency: Int
)
