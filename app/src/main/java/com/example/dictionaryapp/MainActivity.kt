package com.example.dictionaryapp

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var statusText: TextView
    private lateinit var progress: ProgressBar
    private lateinit var resultCard: LinearLayout
    private lateinit var recentText: TextView
    private var audioUrl: String? = null
    private var player: MediaPlayer? = null
    private val prefs by lazy { getSharedPreferences("wordly_history", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchInput = findViewById(R.id.searchInput)
        statusText = findViewById(R.id.statusText)
        progress = findViewById(R.id.progress)
        resultCard = findViewById(R.id.resultCard)
        recentText = findViewById(R.id.recentText)
        refreshHistory()

        findViewById<Button>(R.id.searchButton).setOnClickListener { searchWord() }
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { searchWord(); true } else false
        }
        findViewById<TextView>(R.id.historyButton).setOnClickListener { showHistoryDialog() }
        findViewById<Button>(R.id.audioButton).setOnClickListener { playPronunciation() }
        recentText.setOnClickListener { showHistoryDialog() }
    }

    private fun searchWord() {
        val word = searchInput.text.toString().trim().lowercase()
        if (word.isBlank()) {
            searchInput.error = "Enter a word first"
            return
        }
        progress.visibility = View.VISIBLE
        resultCard.visibility = View.GONE
        statusText.text = "Looking up “$word”…"
        lifecycleScope.launch {
            try {
                val entries = withContext(Dispatchers.IO) { DictionaryApi.service.lookup(word) }
                val entry = entries.firstOrNull()
                if (entry == null) {
                    statusText.text = "No definition found. Check the spelling and try again."
                } else {
                    showEntry(entry)
                    saveHistory(entry.word ?: word)
                    statusText.text = ""
                }
            } catch (e: Exception) {
                statusText.text = if (e.message?.contains("404") == true)
                    "Word not found. Try another spelling."
                else "Couldn't connect. Check your internet and try again."
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun showEntry(entry: DictionaryEntry) {
        val firstMeaning = entry.meanings?.firstOrNull()
        val firstDefinition = firstMeaning?.definitions?.firstOrNull()
        findViewById<TextView>(R.id.wordText).text = entry.word ?: "Word"
        findViewById<TextView>(R.id.phoneticText).text =
            entry.phonetic ?: entry.phonetics?.firstNotNullOfOrNull { it.text }.orEmpty()
        findViewById<TextView>(R.id.partText).text = firstMeaning?.partOfSpeech ?: "Meaning"
        findViewById<TextView>(R.id.meaningText).text =
            firstDefinition?.definition ?: "No definition available."
        findViewById<TextView>(R.id.exampleText).text =
            firstDefinition?.example?.let { "“$it”" } ?: ""
        val synonyms = (firstDefinition?.synonyms.orEmpty() + firstMeaning?.synonyms.orEmpty()).distinct().take(8)
        findViewById<TextView>(R.id.synonymText).text =
            if (synonyms.isNotEmpty()) "Synonyms  ·  ${synonyms.joinToString(", ")}" else "Synonyms  ·  Not available"
        audioUrl = entry.phonetics?.firstOrNull { !it.audio.isNullOrBlank() }?.audio
            ?: entry.phonetics?.firstNotNullOfOrNull { it.audio }
        findViewById<Button>(R.id.audioButton).isEnabled = !audioUrl.isNullOrBlank()
        resultCard.visibility = View.VISIBLE
    }

    private fun playPronunciation() {
        val url = audioUrl
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "Audio is not available for this word.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { it.start() }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@MainActivity, "Unable to play audio.", Toast.LENGTH_SHORT).show()
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to play pronunciation.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveHistory(word: String) {
        val old = prefs.getString("words", "").orEmpty().split("|").filter { it.isNotBlank() }
        val updated = (listOf(word) + old.filterNot { it.equals(word, true) }).take(12)
        prefs.edit().putString("words", updated.joinToString("|")).apply()
        refreshHistory()
    }

    private fun refreshHistory() {
        val words = prefs.getString("words", "").orEmpty().split("|").filter { it.isNotBlank() }
        recentText.text = if (words.isEmpty()) "Your searched words will appear here."
        else words.joinToString("     ·     ")
    }

    private fun showHistoryDialog() {
        val words = prefs.getString("words", "").orEmpty().split("|").filter { it.isNotBlank() }
        if (words.isEmpty()) {
            Toast.makeText(this, "No search history yet.", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Recent searches")
            .setItems(words.toTypedArray()) { _, which ->
                searchInput.setText(words[which])
                searchWord()
            }
            .setNegativeButton("Close", null)
            .setNeutralButton("Clear history") { _, _ ->
                prefs.edit().remove("words").apply()
                refreshHistory()
            }.show()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
