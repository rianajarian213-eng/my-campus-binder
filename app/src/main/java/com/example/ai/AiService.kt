package com.example.ai

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.SummaryLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class ProcessedLecturePackage(
    val title: String,
    val fullTranscript: String,
    val structuredNotes: String,
    val summaryText: String,
    val definitions: String,
    val keyPoints: String,
    val lecturerExamples: String,
    val conclusion: String,
    val studyRecommendations: String
)

sealed interface AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>
    data class Error(val message: String) : AiResult<Nothing>
    data class NotConfigured(val message: String) : AiResult<Nothing>
}

class AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun isAiConfigured(): Boolean {
        val key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun processOneRecordingToFullPackage(
        audioFile: File?,
        courseName: String,
        lecturerName: String,
        meetingNo: Int,
        topicHint: String = ""
    ): AiResult<ProcessedLecturePackage> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AiResult.NotConfigured(
                "Layanan AI belum terhubung. Tambahkan GEMINI_API_KEY di Secrets Panel AI Studio agar AI dapat mentranskripsikan audio perkuliahan secara otomatis."
            )
        }

        try {
            val audioBytes = audioFile?.takeIf { it.exists() }?.readBytes()
            val prompt = """
            Anda adalah asisten akademik cerdas untuk aplikasi MY CAMPUS BINDER.
            Mata Kuliah: $courseName
            Dosen: $lecturerName
            Pertemuan: ke-$meetingNo
            Topik: ${if (topicHint.isNotBlank()) topicHint else "Perkuliahan $courseName"}

            TUGAS UTAMA: PROSES SATU REKAMAN MENJADI SATU PAKET CATATAN LENGKAP
            Transkripsikan rekaman audio dosen ini (atau analisis penjelasan materi kuliah ini), lalu hasilkan paket lengkap dengan format JSON yang valid.
            Gunakan Bahasa Indonesia akademik yang formal, jelas, dan baku.
            Jangan mengarang hal yang tidak disebutkan dosen. Jika ada bagian audio yang tidak jelas atau terpotong, tandai dengan [Audio kurang jelas].

            Struktur output JSON yang WAJIB dihasilkan:
            {
              "title": "Judul Materi Kuliah yang Tepat",
              "fullTranscript": "Transkrip lengkap penjelasan dosen dengan paragraf terpisah dan tanda baca akurat...",
              "structuredNotes": "Catatan kuliah terstruktur menggunakan poin dan sub-poin rapi...",
              "summaryText": "Rangkuman menyeluruh materi perkuliahan...",
              "definitions": "Daftar definisi dan istilah penting yang dijelaskan dosen...",
              "keyPoints": "Poin-poin inti pembahasan utama...",
              "lecturerExamples": "Contoh kasus nyata atau analogi yang disampaikan dosen...",
              "conclusion": "Kesimpulan pembelajaran pertemuan ini...",
              "studyRecommendations": "Hal-hal penting yang harus dipelajari dan dipersiapkan mahasiswa untuk ujian/tugas..."
            }
            Balas HANYA dengan JSON murni tanpa markdown fence.
            """.trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Text prompt
            partsArray.put(JSONObject().put("text", prompt))

            // Audio data if available and within size limits (< 15MB base64)
            if (audioBytes != null && audioBytes.isNotEmpty() && audioBytes.size < 15 * 1024 * 1024) {
                val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                val inlineData = JSONObject().apply {
                    put("mimeType", "audio/mp4")
                    put("data", base64Audio)
                }
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            }
            requestJson.put("generationConfig", genConfig)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("AiService", "API error: ${response.code} - $responseBody")
                return@withContext AiResult.Error("Gagal menghubungi layanan AI (Kode ${response.code}). Pastikan kuota dan API key valid.")
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext AiResult.Error("AI tidak memberikan respon. Coba ulangi kembali.")
            }

            val firstCand = candidates.getJSONObject(0)
            val contentRes = firstCand.optJSONObject("content")
            val partsRes = contentRes?.optJSONArray("parts")
            val rawText = partsRes?.optJSONObject(0)?.optString("text") ?: ""

            val cleanJsonStr = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJsonStr)

            val pkg = ProcessedLecturePackage(
                title = parsed.optString("title", "Catatan Pertemuan $meetingNo: $courseName"),
                fullTranscript = parsed.optString("fullTranscript", "Transkrip tidak tersedia"),
                structuredNotes = parsed.optString("structuredNotes", ""),
                summaryText = parsed.optString("summaryText", ""),
                definitions = parsed.optString("definitions", ""),
                keyPoints = parsed.optString("keyPoints", ""),
                lecturerExamples = parsed.optString("lecturerExamples", ""),
                conclusion = parsed.optString("conclusion", ""),
                studyRecommendations = parsed.optString("studyRecommendations", "")
            )

            AiResult.Success(pkg)
        } catch (e: Exception) {
            Log.e("AiService", "Processing failed", e)
            AiResult.Error("Pemrosesan gagal: ${e.localizedMessage ?: "Terjadi kesalahan jaringan"}")
        }
    }

    suspend fun generateStructuredSummary(
        sourceText: String,
        courseName: String,
        length: SummaryLength
    ): AiResult<ProcessedLecturePackage> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AiResult.NotConfigured(
                "Layanan AI belum terhubung. Konfigurasikan GEMINI_API_KEY di Secrets Panel untuk merangkum catatan otomatis."
            )
        }

        try {
            val prompt = """
            Anda adalah asisten AI akademik di MY CAMPUS BINDER untuk mata kuliah "$courseName".
            Tugas: Rangkum materi kuliah berikut dengan tingkat kedalaman "${length.label}".
            Instruksi panjang: ${length.promptInstruction}

            Struktur resume yang WAJIB diikuti:
            1. Judul materi
            2. Pengertian atau definisi
            3. Poin-poin pembahasan utama
            4. Penjelasan setiap poin
            5. Contoh yang diberikan dosen
            6. Istilah penting
            7. Kesimpulan
            8. Hal-hal penting untuk dipelajari

            Ketentuan:
            - Gunakan bahasa Indonesia baku dan akademis.
            - Jangan mengarang fakta.
            - Hasilkan format JSON valid berikut:
            {
              "title": "Judul Materi",
              "fullTranscript": "",
              "structuredNotes": "Catatan Terstruktur Rinci",
              "summaryText": "Ringkasan Materi",
              "definitions": "Daftar Istilah & Definisi",
              "keyPoints": "Poin Inti",
              "lecturerExamples": "Contoh dari Dosen",
              "conclusion": "Kesimpulan",
              "studyRecommendations": "Materi yang Perlu Dipelajari"
            }

            Teks Materi Sumber:
            $sourceText
            """.trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("temperature", 0.3)
                put("responseMimeType", "application/json")
            }
            requestJson.put("generationConfig", genConfig)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext AiResult.Error("Gagal menghubungi AI (Kode ${response.code}). Periksa API Key Anda.")
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates") ?: return@withContext AiResult.Error("Tidak ada respon dari AI.")
            val rawText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

            val cleanJsonStr = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJsonStr)

            val pkg = ProcessedLecturePackage(
                title = parsed.optString("title", "Resume Kuliah $courseName"),
                fullTranscript = "",
                structuredNotes = parsed.optString("structuredNotes", ""),
                summaryText = parsed.optString("summaryText", ""),
                definitions = parsed.optString("definitions", ""),
                keyPoints = parsed.optString("keyPoints", ""),
                lecturerExamples = parsed.optString("lecturerExamples", ""),
                conclusion = parsed.optString("conclusion", ""),
                studyRecommendations = parsed.optString("studyRecommendations", "")
            )

            AiResult.Success(pkg)
        } catch (e: Exception) {
            Log.e("AiService", "Summary failed", e)
            AiResult.Error("Gagal membuat resume: ${e.localizedMessage ?: "Terjadi kesalahan"}")
        }
    }

    suspend fun askStudyAssistant(
        question: String,
        contextNotes: String
    ): AiResult<String> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Throwable) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AiResult.NotConfigured(
                "Layanan AI belum terhubung. Konfigurasikan GEMINI_API_KEY di Secrets Panel untuk menggunakan fitur Tanya AI."
            )
        }

        try {
            val prompt = """
            Anda adalah Tutor & Asisten Belajar Cerdas di MY CAMPUS BINDER.
            Bantu mahasiswa menjawab pertanyaan kuliah ini secara ramah, terstruktur, dan akademis.

            Konteks Catatan Kuliah Mahasiswa:
            ${if (contextNotes.isNotBlank()) contextNotes.take(3000) else "Tidak ada catatan spesifik yang dilampirkan."}

            Pertanyaan Mahasiswa:
            $question
            """.trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext AiResult.Error("Gagal menghubungi AI (Kode ${response.code}).")
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates") ?: return@withContext AiResult.Error("Tidak ada respon.")
            val rawText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")

            AiResult.Success(rawText)
        } catch (e: Exception) {
            AiResult.Error("Tanya AI gagal: ${e.localizedMessage ?: "Terjadi kesalahan"}")
        }
    }
}
