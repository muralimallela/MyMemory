package online.muralimallela.mymemory

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import online.muralimallela.mymemory.models.BoardSize
import online.muralimallela.mymemory.models.MemoryGame

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
        private const val GITHUB_URL = "https://github.com/muralimallela"
    }

    private lateinit var clRoot : ConstraintLayout
    private lateinit var rvBoard : RecyclerView
    private lateinit var TextVeiwMoves : TextView
    private lateinit var TextVeiwPairs : TextView
    private var boardSize = BoardSize.MEDIUM

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter




    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        window.statusBarColor = ContextCompat.getColor(this,R.color.status_bar_color)
//        window.navigationBarColor = ContextCompat.getColor(this,R.color.status_bar_color)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true
        }

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        TextVeiwMoves = findViewById(R.id.textViewMoves)
        TextVeiwPairs = findViewById(R.id.textViewPairs)

        setUpBoard()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mi_refresh -> {
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?",null,View.OnClickListener {
                        setUpBoard()
                    })
                } else
                    setUpBoard()
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.about -> {
                showAboutDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val aboutView = LayoutInflater.from(this).inflate(R.layout.about_view,null)
        val githubBtn : Button = aboutView.findViewById(R.id.githubBtn)
        githubBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
            startActivity(intent)
        }
        showAlertDialog("About",aboutView) { }
    }

    @SuppressLint("MissingInflatedId")
    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size",boardSizeView) {
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                R.id.rbHard -> BoardSize.HARD
                else -> BoardSize.HARD
            }
            setUpBoard()
        }
    }

    private fun showAlertDialog(title :String, view : View?, positiveClickListener : View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){ _, _->
                positiveClickListener.onClick(null)
            }.show()
    }

    @SuppressLint("SetTextI18n")
    private fun setUpBoard() {
        when(boardSize){
            BoardSize.EASY -> {
                TextVeiwMoves.text = "Easy: 4 x 2"
                TextVeiwPairs.text = "Pairs : 0 / 4"
            }
            BoardSize.MEDIUM -> {
                TextVeiwMoves.text = "Medium: 6 x 3"
                TextVeiwPairs.text = "Pairs : 0 / 9"
            }
            BoardSize.HARD -> {
                TextVeiwMoves.text = "Hard: 6 x 4"
                TextVeiwPairs.text = "Pairs : 0 / 24"
            }
        }
        memoryGame = MemoryGame(boardSize)
        TextVeiwPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
        adapter = MemoryBoardAdapter(this,boardSize,memoryGame.cards,object : MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())

    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun updateGameWithFlip(position: Int) {
        if(memoryGame.haveWonGame()){

            val snackbar:Snackbar =Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG)
                .setTextColor(Color.WHITE)
                .setBackgroundTint(Color.parseColor("#548af7"))

            snackbar.show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            val snackbar:Snackbar = Snackbar.make(clRoot, "Invalid Move!",Snackbar.LENGTH_SHORT)
                .setTextColor(Color.WHITE)
                .setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#c94f4f")))
            snackbar.show()
            return
        }
        if(memoryGame.flipCard(position)){
            Log.i(TAG,"Found match! num pairs found : ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full),

            ) as Int
            TextVeiwPairs.setTextColor(color)
            TextVeiwPairs.text = "Pairs ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){
                val snackbar:Snackbar =Snackbar.make(clRoot, "You won! Congratulations", Snackbar.LENGTH_LONG)
                    .setTextColor(Color.WHITE)
                    .setBackgroundTint(Color.parseColor("#57965c"))
                snackbar.show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW,Color.RED,Color.MAGENTA,Color.BLUE,Color.GREEN,Color.CYAN)).stream(5000)

            }
        }
        TextVeiwMoves.text = "Moves : ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}