package neth.iecal.trease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerStatus { Idle, Running }

class HomeScreenViewModel : ViewModel() {
    private var timerJob: Job? = null

    private val _selectedMinutes = MutableStateFlow(25L)
    val selectedMinutes: StateFlow<Long> = _selectedMinutes.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25L * 60L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _timerStatus = MutableStateFlow(TimerStatus.Idle)
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isTreeSelectionVisible : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isTreeSelectionVisible = _isTreeSelectionVisible.asStateFlow()

    var selectedTree = MutableStateFlow("tree")

    fun adjustTime(amount: Long) {
        if (_timerStatus.value == TimerStatus.Idle) {
            val newMinutes = (_selectedMinutes.value + amount).coerceIn(1, 120)
            _selectedMinutes.value = newMinutes
            _remainingSeconds.value = newMinutes * 60L
            _progress.value = 1f
        }
    }

    fun toggleIsTreeSelectionVisible() {
        _isTreeSelectionVisible.value = !_isTreeSelectionVisible.value
    }
    fun toggleTimer() {
        if (_timerStatus.value == TimerStatus.Idle) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        _timerStatus.value = TimerStatus.Running
        // We use the current remaining seconds as the "start line" for progress
        val initialSecondsAtStart = _remainingSeconds.value

        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                _progress.value = _remainingSeconds.value.toFloat() / initialSecondsAtStart.toFloat()
            }
            _timerStatus.value = TimerStatus.Idle
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _timerStatus.value = TimerStatus.Idle
        _remainingSeconds.value = _selectedMinutes.value * 60L
        _progress.value = 1f
    }

    // FIXED: Clean Kotlin formatting without using String.format factory
    fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        val minutesStr = m.toString().padStart(2, '0')
        val secondsStr = s.toString().padStart(2, '0')
        return "$minutesStr:$secondsStr"
    }
}