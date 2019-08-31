package io.github.lee0701.lboard.dictbuilder

import java.io.*
import java.text.Normalizer

fun main() {

    val dirName = "../dict/"
    val outFileName = "../dict.bin"
    val dirFile = File(dirName)

    val dictionary = TrieDictionary()
    var count = 0

    dirFile.list().forEachIndexed { i, name ->
        val br = BufferedReader(InputStreamReader(FileInputStream(dirName + name)))

        val words: MutableList<Pair<String, Int>> = mutableListOf()
        var j = 0
        br.forEachLine { it.split('\t').let { words += it[0] to j++ } }
        val sum = words.sumBy { it.second }

        words.map { Normalizer.normalize(it.first, Normalizer.Form.NFD) to (j - it.second).toFloat() / j }
            .forEach { dictionary.insert(it.first, i, it.second) }

        count += words.size
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
