package neth.iecal.trease.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.models.TimerStatus
import neth.iecal.trease.utils.TreeStatsLodger


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

    var viewModelCoroutineScope = CoroutineScope(Dispatchers.Default)
    val treeStatsLodger = TreeStatsLodger()
    fun adjustTime(amount: Long) {
        if (_timerStatus.value == TimerStatus.Idle) {
            val newMinutes = (_selectedMinutes.value + amount).coerceIn(1, 120)
            _selectedMinutes.value = newMinutes
            _remainingSeconds.value = newMinutes * 60L
            _progress.value = 1f
        }
    }

    fun selectTree(treeId:String){
        _timerStatus.value = TimerStatus.Idle
        selectedTree.value = treeId
    }
    fun setTimeStatus(status:TimerStatus){
        _timerStatus.value = status
    }
    fun toggleIsTreeSelectionVisible() {
        _isTreeSelectionVisible.value = !_isTreeSelectionVisible.value
    }
    fun toggleTimer() {
        if (_timerStatus.value != TimerStatus.Running) {
            // start a new timer only if not running
            startTimer()
        } else{
            // stop the timer as user quit
            _timerStatus.value = TimerStatus.HAS_QUIT
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
            stopTimer()
            _timerStatus.value = TimerStatus.HAS_WON
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        viewModelCoroutineScope.launch {
            treeStatsLodger.appendStats(
                FocusStats(
                    remainingSeconds.value,
                    selectedTree.value,
                    timerStatus.value == TimerStatus.HAS_QUIT
                )
            )
        }


    }
    fun cleanTimerSession(){
        _timerStatus.value = if(_timerStatus.value == TimerStatus.HAS_QUIT)
            TimerStatus.POST_QUIT else TimerStatus.POST_WIN
        _remainingSeconds.value = _selectedMinutes.value * 60L
        _progress.value = 1f
    }

    fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        val minutesStr = m.toString().padStart(2, '0')
        val secondsStr = s.toString().padStart(2, '0')
        return "$minutesStr:$secondsStr"
    }
}