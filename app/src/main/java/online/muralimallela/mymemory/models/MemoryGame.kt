package online.muralimallela.mymemory.models

import online.muralimallela.mymemory.utils.DEFAULT_ICONS

class MemoryGame (private val boardSize: BoardSize ){
    val cards : List<MemoryCard>
    private var indexOfSingleSelectedCard : Int? = null
    var numPairsFound = 0
    private var numCardFlips = 0

    init {
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomizedImages :List<Int> = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int) : Boolean {
        numCardFlips++
        val card : MemoryCard = cards[position]
        var foundMatch = false
        if(indexOfSingleSelectedCard == null){
            restoreCards()
            indexOfSingleSelectedCard = position
        }else{
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!,position)
            indexOfSingleSelectedCard = null
        }

        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if(cards[position1].identifier != cards[position2].identifier){
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true

        numPairsFound++
        return true
    }

    private fun restoreCards() {
        for (card: MemoryCard in cards){
            if(!card.isMatched){
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return  numCardFlips / 2
    }
}
