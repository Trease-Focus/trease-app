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
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import neth.iecal.trease.api.TreeRepository
import neth.iecal.trease.getCacheManager
import neth.iecal.trease.isBlockerRunning
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.models.TimerStatus
import neth.iecal.trease.models.TreeData
import neth.iecal.trease.onForceStopFocus
import neth.iecal.trease.utils.CoinManager
import neth.iecal.trease.utils.FocusStateManager
import neth.iecal.trease.utils.TreeStatsLodger
import neth.iecal.trease.utils.randomBiased
import kotlin.math.log2


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

    private val _isWitheredTreeSelectionVisible : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isWitheredTreeSelectionVisible = _isWitheredTreeSelectionVisible.asStateFlow()


    val coinManager = CoinManager()
    val coins = MutableStateFlow(0)
    val selectedWitheredTree = MutableStateFlow<FocusStats?>(null)
    var selectedTree : MutableStateFlow<TreeData> = MutableStateFlow(TreeData(
        id = "tree",
        name = "tree",
        description = "tree",
        creator = "nethical",
        donate = "tree",
        variants = 4,
        basePrice = 0,
        isGrowable = true
    ))

    var viewModelCoroutineScope = CoroutineScope(Dispatchers.Default)
    val treeStatsLodger = TreeStatsLodger()

    val _currentTreeSeedVariant = MutableStateFlow(0)
    val currentTreeSeedVariant : StateFlow<Int> = _currentTreeSeedVariant.asStateFlow()

    var treeList : MutableStateFlow<List<TreeData>> = MutableStateFlow(emptyList())

    var _snackBarMessage : MutableStateFlow<String?> = MutableStateFlow(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()
    // caches the state if tree is growing or no,so if accidentally system kills the app, user can restart from last progress
    val focusStateManager = FocusStateManager()
    init {
        viewModelCoroutineScope.launch {
            fetchTreesFromCdn()
            coins.value = coinManager.reloadCoins()
            reloadTreeList()
            checkNotOverWhileDoze()

        }
    }

    suspend fun checkNotOverWhileDoze(){
        val runDuration = focusStateManager.getRunOverAt()
        if(runDuration!=null){
            val isNotOver = runDuration > Clock.System.now().epochSeconds
            if(isNotOver){
                val treeInfo = focusStateManager.getRunningTree()!!
                val originalDuration = treeInfo.duration
                treeInfo.duration = (runDuration - Clock.System.now().epochSeconds)
                selectWitheredTree(treeInfo)
                if(isBlockerRunning()){
                    startTimer()
                    _timerStatus.value = TimerStatus.Running
                }else{
                    showSnackbar("There was a system error that stopped the service. Fortunately you can still grow the same tree without losing progress by pressing start.")
                }
                _selectedMinutes.value = originalDuration/60
            }
        }
    }

    suspend fun reloadTreeList(){
        val cacheManager = getCacheManager()
        cacheManager.readFile("tree.json")?.let {
            treeList.value = Json.decodeFromString(it)
        }
    }
    fun adjustTime(amount: Long) {
        if (_timerStatus.value == TimerStatus.Idle) {
            val newMinutes = (_selectedMinutes.value + amount).coerceIn(1, 120)
            _selectedMinutes.value = newMinutes
            _remainingSeconds.value = newMinutes * 60L
            _progress.value = 1f
        }
    }

    fun  selectWitheredTree(focusStats: FocusStats){
        selectedWitheredTree.value = focusStats

        _currentTreeSeedVariant.value = focusStats.treeSeed

        _timerStatus.value = TimerStatus.Idle
        selectedTree.value = treeList.value.filter { it.id == focusStats.treeId }.first()

        val newMinutes = focusStats.duration / 60
        _selectedMinutes.value = newMinutes
        _remainingSeconds.value = focusStats.duration
        _progress.value = 1f
    }
    fun selectTree(treeId: TreeData){
        selectedWitheredTree.value = null
        _timerStatus.value = TimerStatus.Idle
        selectedTree.value = treeId
    }
    fun setTimeStatus(status:TimerStatus){
        _timerStatus.value = status
    }
    fun showSnackbar(msg:String){
        _snackBarMessage.value = msg
    }
    fun hideSnackbar(){
        _snackBarMessage.value = null
    }
    fun toggleIsTreeSelectionVisible() {
        _isTreeSelectionVisible.value = !_isTreeSelectionVisible.value
    }
    fun toggleIsWitheredTreeSelectionVisible() {
        _isWitheredTreeSelectionVisible.value = !_isWitheredTreeSelectionVisible.value
    }
    fun toggleTimer() {
        if (_timerStatus.value != TimerStatus.Running) {
            // start a new timer only if not running
            startTimer()
        } else{
            // stop the timer as user quit
            _timerStatus.value = TimerStatus.HAS_QUIT
            stopTimer()
            onForceStopFocus()
        }
    }

    private fun startTimer() {
        if(selectedWitheredTree.value != null){
            reSelectSeed()
        }
        _timerStatus.value = TimerStatus.Running
        // We use the current remaining seconds as the "start line" for progress
        val initialSecondsAtStart = _remainingSeconds.value
        timerJob = viewModelScope.launch {
            focusStateManager.setRunning(Clock.System.now().epochSeconds + _remainingSeconds.value)
            focusStateManager.setRunningTree(FocusStats(
                selectedMinutes.value*60,
                selectedTree.value.id,
                false,
                treeSeed = currentTreeSeedVariant.value,
            ))
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
                _progress.value = _remainingSeconds.value.toFloat() / initialSecondsAtStart.toFloat()
            }
            stopTimer()
            _timerStatus.value = TimerStatus.HAS_WON
        }
    }

    private fun reSelectSeed(){
        viewModelCoroutineScope.launch {
            _currentTreeSeedVariant.value = randomBiased(selectedTree.value?.variants ?: 1,
                calculateBias(selectedMinutes.value))
            println("Seed ${currentTreeSeedVariant.value}")
        }
    }
     suspend fun fetchTreesFromCdn():List<TreeData>{
        val trees = TreeRepository.fetchTrees().filter { it.id != "weathered" }
        val cacheManager = getCacheManager()
        cacheManager.saveFile("tree.json", Json.encodeToString(trees))
        return trees
    }
    private suspend fun calculateBias(
        currentFocusDurationInMins: Long
    ): Double {
        val stats = treeStatsLodger.getTodayStats()

        if (stats.isEmpty()) return 1.0

        val totalSessions = stats.size
        val successfulSessions = stats.count { !it.isFailed }
        val failedSessions = totalSessions - successfulSessions

        val focusBias = log2(currentFocusDurationInMins + 1.0)
            .coerceAtMost(4.0) // ~64 mins max effect

        val successRatio = successfulSessions.toDouble() / totalSessions

        val failurePenalty = failedSessions * 0.3

        val rawBias = 1.0 - (
                (focusBias * 0.15) +
                        (successRatio * 0.4) -
                        (failurePenalty * 0.1)
                )

        // 1.0 → uniform
        // 0.3 → very strong bias toward high values
        return rawBias.coerceIn(0.3, 1.0)
    }


    private fun stopTimer() {
        timerJob?.cancel()
        viewModelCoroutineScope.launch {
            treeStatsLodger.injectStats(
                if(selectedWitheredTree.value!=null){
                    // As selected tree is a focus stat object too, we just update the value of isFailed and inject it back.
                    selectedWitheredTree.value!!.copy(
                        isFailed = timerStatus.value == TimerStatus.HAS_QUIT
                    )
                }else{
                    FocusStats(
                        duration = selectedMinutes.value*60,
                        treeId = selectedTree.value.id ?: "tree",
                        // also btw all withered trees are actually listed from the saved focus stat list on disk.
                        isFailed = timerStatus.value == TimerStatus.HAS_QUIT,
                        treeSeed = currentTreeSeedVariant.value,
                    )
                }
            )
            selectedWitheredTree.value = null
            if(timerStatus.value != TimerStatus.HAS_QUIT){
                coinManager.addCoins(
                    calculateRewardedCoin()
                )
                coins.value = coinManager.reloadCoins()
            }
            focusStateManager.setRunning(-1)
        }

    }
    suspend fun useCoins(value:Int){
        coins.value -= value
        coinManager.removeCoins(value)
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

    fun calculateRarity(): Boolean = currentTreeSeedVariant.value==selectedTree.value.variants

    fun calculateRewardedCoin(): Int {
        val multiplier = if(calculateRarity()) 2 else 1
        return selectedMinutes.value.toInt() * multiplier
    }
}