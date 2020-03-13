package io.github.lee0701.lboard.dictbuilder

import java.io.DataOutputStream
import java.io.OutputStream

class TrieDictionary {

    val root = Node()
    var count: Int = 0
        private set

    data class Node(
        val words: MutableList<Word> = mutableListOf(),
        val children: MutableMap<Char, Node> = mutableMapOf()
    ) {
        fun addChild(key: Char, node: Node) {
            children[key] = node
        }
        fun addWord(word: Word) {
            words += word
        }
        fun serialize(): List<Node> {
            return children.flatMap { it.value.serialize() } + this
        }
    }

    fun insert(word: String, pos: Int, frequency: Float) {
        var p = root
        word.forEach { c ->
            val n = p.children[c] ?: Node()
            p.addChild(c, n)
            p = n
        }
        p.addWord(Word(word, pos, frequency))
        count += 1
    }

    fun search(word: String): List<Word> {
        var p = root
        word.forEach { c ->
            p = p.children[c] ?: return listOf()
        }
        return p.words
    }

    companion object {
        fun write(list: List<Node>, out: OutputStream) {
            val dos = DataOutputStream(out)

            val addressMap: MutableMap<Node, Int> = mutableMapOf()
            var address: Int = 0

            list.forEach { node ->
                addressMap += node to address

                dos.writeByte(node.words.size)
                address += 1
                node.words.forEach { word ->
                    dos.writeByte(word.pos)
                    dos.writeFloat(word.frequency)
                    address += 5
                }
                dos.writeByte(node.children.size)
                address += 1
                node.children.forEach { c, n ->
                    dos.writeShort(c.toInt())
                    dos.writeInt(addressMap[n] ?: 0)
                    address += 6
                }
            }
            dos.writeInt(addressMap[list.last()] ?: 0)

        }
    }

}
