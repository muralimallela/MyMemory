package online.muralimallela.mymemory

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import online.muralimallela.mymemory.models.BoardSize
import online.muralimallela.mymemory.models.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {


    companion object{
        private const val MARIN_SIZE = 20
        private const val TAG = "MemoryBoardAdapter"
    }

    interface CardClickListener{
        fun onCardClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardViewWidth : Int = parent.width/boardSize.getWidth() - (2* MARIN_SIZE)
        val cardViewHeight : Int= parent.height/boardSize.getHeight() - (2* MARIN_SIZE)
        val cardSideLength :Int = min(cardViewWidth,cardViewHeight)
        val view : View = LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)
        val layoutParams : ViewGroup.MarginLayoutParams  =  view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.height = cardSideLength
        layoutParams.width = cardSideLength
        layoutParams.setMargins(MARIN_SIZE, MARIN_SIZE, MARIN_SIZE, MARIN_SIZE)
        return ViewHolder(view)
    }

    override fun getItemCount() = boardSize.numcards

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    };

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageBtn = itemView.findViewById<ImageButton>(R.id.imageButton)
        fun bind(position: Int) {
            val memoryCard : MemoryCard = cards[position]
            imageBtn.setImageResource(if(memoryCard.isFaceUp) memoryCard.identifier else R.drawable.ic_launcher_background)
            imageBtn.alpha = if(memoryCard.isMatched) .4f else 1.0f
            val colorStateList : ColorStateList? = if(memoryCard.isMatched) ContextCompat.getColorStateList(context,R.color.color_gray) else null
            ViewCompat.setBackgroundTintList(imageBtn,colorStateList)
            imageBtn.setOnClickListener{
                Log.i(TAG,"Clicked on position $position")
                cardClickListener.onCardClicked(position)
            }
        }
    }
}
