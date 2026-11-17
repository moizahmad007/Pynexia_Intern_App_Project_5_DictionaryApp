package com.example.dictionaryapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class DictionaryEntry(
    val word: String?,
    val phonetic: String?,
    val phonetics: List<Phonetic>?,
    val meanings: List<Meaning>?
)
data class Phonetic(val text: String?, val audio: String?)
data class Meaning(val partOfSpeech: String?, val definitions: List<Definition>?, val synonyms: List<String>?)
data class Definition(val definition: String?, val example: String?, val synonyms: List<String>?)

interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    suspend fun lookup(@Path("word") word: String): List<DictionaryEntry>

    companion object {
        val service: DictionaryApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DictionaryApi::class.java)
        }
    }
}
