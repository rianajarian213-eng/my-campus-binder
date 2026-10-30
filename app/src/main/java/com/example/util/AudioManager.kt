package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var recordingJob: Job? = null
    private var currentOutputFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _waveAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    val waveAmplitudes: StateFlow<List<Float>> = _waveAmplitudes.asStateFlow()

    fun startRecording(courseId: Long, meetingNo: Int): File? {
        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val filename = "lecture_c${courseId}_m${meetingNo}_${System.currentTimeMillis()}.m4a"
        val outputFile = File(recordingsDir, filename)
        currentOutputFile = outputFile

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _isPaused.value = false
            _elapsedSeconds.value = 0
            _waveAmplitudes.value = emptyList()

            // Coroutine loop for elapsed time & waveform polling
            recordingJob = CoroutineScope(Dispatchers.Default).launch {
                var tickCounter = 0
                val ampList = mutableListOf<Float>()
                while (isActive && _isRecording.value) {
                    if (!_isPaused.value) {
                        delay(100L)
                        tickCounter++
                        if (tickCounter % 10 == 0) {
                            _elapsedSeconds.value += 1
                        }

                        val maxAmp = try {
                            mediaRecorder?.maxAmplitude ?: 0
                        } catch (e: Exception) {
                            0
                        }
                        val normalized = (maxAmp.toFloat() / 25000f).coerceIn(0.08f, 1.0f)
                        ampList.add(normalized)
                        if (ampList.size > 40) {
                            ampList.removeAt(0)
                        }
                        _waveAmplitudes.value = ampList.toList()
                    } else {
                        delay(200L)
                    }
                }
            }

            return outputFile
        } catch (e: IOException) {
            Log.e("AudioRecorderManager", "Failed to start recording", e)
            release()
            return null
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Unexpected recording error", e)
            release()
            return null
        }
    }

    fun pauseRecording() {
        if (_isRecording.value && !_isPaused.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.pause()
                    _isPaused.value = true
                }
            } catch (e: Exception) {
                Log.e("AudioRecorderManager", "Error pausing recorder", e)
            }
        }
    }

    fun resumeRecording() {
        if (_isRecording.value && _isPaused.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mediaRecorder?.resume()
                    _isPaused.value = false
                }
            } catch (e: Exception) {
                Log.e("AudioRecorderManager", "Error resuming recorder", e)
            }
        }
    }

    fun stopRecording(): File? {
        val file = currentOutputFile
        try {
            if (_isRecording.value) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error stopping recorder", e)
        } finally {
            release()
        }
        return file
    }

    fun release() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        _isRecording.value = false
        _isPaused.value = false
    }
}

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _currentPlayingPath = MutableStateFlow<String?>(null)
    val currentPlayingPath: StateFlow<String?> = _currentPlayingPath.asStateFlow()

    fun play(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("AudioPlayerManager", "Audio file does not exist: $filePath")
            return
        }

        if (_currentPlayingPath.value == filePath && mediaPlayer != null) {
            if (!_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressPolling()
            }
            return
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setSpeedInternal(_playbackSpeed.value)
                start()
            }
            mediaPlayer = player
            _currentPlayingPath.value = filePath
            _duration.value = player.duration / 1000
            _isPlaying.value = true

            player.setOnCompletionListener {
                _isPlaying.value = false
                _currentPosition.value = 0
                progressJob?.cancel()
            }

            startProgressPolling()
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to play audio file", e)
            stop()
        }
    }

    fun pause() {
        try {
            if (_isPlaying.value) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                progressJob?.cancel()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error pausing player", e)
        }
    }

    fun seekTo(seconds: Int) {
        try {
            mediaPlayer?.seekTo(seconds * 1000)
            _currentPosition.value = seconds
        } catch (_: Exception) {}
    }

    fun seekForward(deltaSeconds: Int = 10) {
        val newPos = (_currentPosition.value + deltaSeconds).coerceAtMost(_duration.value)
        seekTo(newPos)
    }

    fun seekBackward(deltaSeconds: Int = 10) {
        val newPos = (_currentPosition.value - deltaSeconds).coerceAtLeast(0)
        seekTo(newPos)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        setSpeedInternal(speed)
    }

    private fun setSpeedInternal(speed: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed)
                    ?: android.media.PlaybackParams().setSpeed(speed)
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to set playback speed", e)
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _currentPosition.value = 0
        _duration.value = 0
        _currentPlayingPath.value = null
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && _isPlaying.value) {
                val pos = try {
                    (mediaPlayer?.currentPosition ?: 0) / 1000
                } catch (e: Exception) {
                    0
                }
                _currentPosition.value = pos
                delay(300L)
            }
        }
    }

    fun release() {
        stop()
    }
}
