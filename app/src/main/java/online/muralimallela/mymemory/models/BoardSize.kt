package online.muralimallela.mymemory.models

enum class BoardSize(val numcards : Int) {
    EASY (numcards = 8),
    MEDIUM (numcards = 18),
    HARD (numcards = 24);

    fun getWidth() : Int{
        return when (this){
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }
    fun getHeight() : Int{
        return numcards/getWidth()
    }
    fun getNumPairs() : Int{
        return numcards/2
    }
}