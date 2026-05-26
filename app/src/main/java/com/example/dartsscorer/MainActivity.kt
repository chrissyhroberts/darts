package com.example.dartsscorer

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { DartsScorerApp() } }
    }
}

enum class FinishRule { STRAIGHT_OUT, DOUBLE_OUT }

data class Player(val name: String, val score: Int)

data class DartHit(
    val segment: Int?,
    val multiplier: Int,
    val label: String,
    val score: Int,
    val isDoubleFinish: Boolean = false
)

data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val currentTurn: List<DartHit> = emptyList(),
    val startingScore: Int = 501,
    val finishRule: FinishRule = FinishRule.DOUBLE_OUT,
    val message: String = "",
    val selectedSlotIndex: Int? = null
)

private val boardOrder = listOf(20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5)

@Composable
fun DartsScorerApp() {
    val context = LocalContext.current
    var gameState by remember { mutableStateOf<GameState?>(null) }
    var savedState by remember { mutableStateOf(loadGameState(context)) }

    LaunchedEffect(gameState) {
        gameState?.let {
            saveGameState(context, it)
            savedState = it
        }
    }

    if (gameState == null) {
        SetupScreen(
            savedState = savedState,
            onResume = { savedState?.let { gameState = it.copy(message = "Game restored.") } },
            onClearSaved = {
                clearSavedGame(context)
                savedState = null
            },
            onStart = { playerNames, startingScore, finishRule ->
                gameState = GameState(
                    players = playerNames.map { Player(it, startingScore) },
                    startingScore = startingScore,
                    finishRule = finishRule
                )
            }
        )
    } else {
        GameScreen(
            state = gameState!!,
            onStateChange = { gameState = it },
            onExitToDashboard = { gameState = null }
        )
    }
}

@Composable
fun SetupScreen(
    savedState: GameState?,
    onResume: () -> Unit,
    onClearSaved: () -> Unit,
    onStart: (List<String>, Int, FinishRule) -> Unit
) {
    var playerCount by remember { mutableStateOf("2") }
    var playerNames by remember { mutableStateOf(listOf("Player 1", "Player 2")) }
    var startingScore by remember { mutableStateOf(501) }
    var finishRule by remember { mutableStateOf(FinishRule.DOUBLE_OUT) }

    fun syncPlayerNames(count: Int) {
        val safeCount = count.coerceIn(1, 8)
        playerNames = List(safeCount) { index -> playerNames.getOrNull(index) ?: "Player ${index + 1}" }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Darts Scorer", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        if (savedState != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val player = savedState.players[savedState.currentPlayerIndex]
                Text("Saved game", fontWeight = FontWeight.Bold)
                Text("${player.name} to throw. Remaining: ${player.score}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onResume, modifier = Modifier.weight(1f)) { Text("Resume") }
                    OutlinedButton(onClick = onClearSaved, modifier = Modifier.weight(1f)) { Text("Clear") }
                }
            }
        }

        OutlinedTextField(
            value = playerCount,
            onValueChange = {
                playerCount = it.filter { char -> char.isDigit() }.take(1)
                syncPlayerNames(playerCount.toIntOrNull() ?: 1)
            },
            label = { Text("Number of players") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
            itemsIndexed(playerNames) { index, name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName -> playerNames = playerNames.toMutableList().also { it[index] = newName } },
                    label = { Text("Player ${index + 1} name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text("Starting score", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(selected = startingScore == 501, onClick = { startingScore = 501 }, label = { Text("501") })
            FilterChip(selected = startingScore == 301, onClick = { startingScore = 301 }, label = { Text("301") })
        }

        Text("Finish rule", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(selected = finishRule == FinishRule.DOUBLE_OUT, onClick = { finishRule = FinishRule.DOUBLE_OUT }, label = { Text("Double out") })
            FilterChip(selected = finishRule == FinishRule.STRAIGHT_OUT, onClick = { finishRule = FinishRule.STRAIGHT_OUT }, label = { Text("Straight out") })
        }

        Button(
            onClick = { onStart(playerNames.mapIndexed { i, n -> n.trim().ifEmpty { "Player ${i + 1}" } }, startingScore, finishRule) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start game") }
    }
}

@Composable
fun GameScreen(state: GameState, onStateChange: (GameState) -> Unit, onExitToDashboard: () -> Unit) {
    val currentPlayer = state.players[state.currentPlayerIndex]
    val turnScore = state.currentTurn.sumOf { it.score }
    val provisionalRemaining = currentPlayer.score - turnScore
    val checkout = checkoutSuggestion(provisionalRemaining, state.finishRule)
    val provisionalBust = provisionalRemaining < 0 ||
        (state.finishRule == FinishRule.DOUBLE_OUT && provisionalRemaining == 1) ||
        (state.finishRule == FinishRule.DOUBLE_OUT && provisionalRemaining == 0 && state.currentTurn.lastOrNull()?.isDoubleFinish != true)
    val statusText = when {
        state.currentTurn.isNotEmpty() && provisionalBust -> "Would bust"
        checkout != null -> "Out: $checkout"
        state.currentTurn.isNotEmpty() -> "After darts: $provisionalRemaining"
        else -> " "
    }
    val statusColor = when {
        state.currentTurn.isNotEmpty() && provisionalBust -> MaterialTheme.colorScheme.error
        checkout != null -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(currentPlayer.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Remaining: ${currentPlayer.score}", fontSize = 24.sp)
                Box(modifier = Modifier.fillMaxWidth().height(26.dp), contentAlignment = Alignment.CenterStart) {
                    Text(statusText, fontSize = 18.sp, color = statusColor, maxLines = 1)
                }
            }
            Button(onClick = onExitToDashboard) { Text("Exit") }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    if (state.message.isNotBlank()) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = state.message.ifBlank { " " }, fontWeight = FontWeight.Bold, maxLines = 1)
        }

        PlayerScores(state)
        DartSlots(
            turn = state.currentTurn,
            selectedSlotIndex = state.selectedSlotIndex,
            onSlotSelected = { slot ->
                val selectable = slot < state.currentTurn.size || slot == state.currentTurn.size
                if (selectable) onStateChange(state.copy(selectedSlotIndex = slot, message = "Tap board to replace Dart ${slot + 1}"))
            }
        )
        Text("Turn score: $turnScore   Provisional remaining: $provisionalRemaining", fontSize = 18.sp)

        DartsBoard(hits = state.currentTurn, enabled = state.currentTurn.size < 3 || state.selectedSlotIndex != null) { hit ->
            onStateChange(applyHitToTurn(state, hit))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onStateChange(applyHitToTurn(state, DartHit(null, 0, "Miss", 0))) },
                enabled = state.currentTurn.size < 3 || state.selectedSlotIndex != null,
                modifier = Modifier.weight(1f)
            ) { Text("Miss") }

            OutlinedButton(
                onClick = { onStateChange(state.copy(currentTurn = state.currentTurn.dropLast(1), message = "", selectedSlotIndex = null)) },
                enabled = state.currentTurn.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) { Text("Undo") }

            Button(
                onClick = { onStateChange(commitTurn(state.copy(selectedSlotIndex = null))) },
                enabled = state.currentTurn.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) { Text("Confirm") }
        }
    }
}

@Composable
fun PlayerScores(state: GameState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        state.players.forEachIndexed { index, player ->
            val isCurrent = index == state.currentPlayerIndex
            Column(
                modifier = Modifier.weight(1f).background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(player.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(player.score.toString(), fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun DartSlots(turn: List<DartHit>, selectedSlotIndex: Int?, onSlotSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(3) { index ->
            val label = turn.getOrNull(index)?.label ?: "Dart ${index + 1}"
            val isSelected = selectedSlotIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSlotSelected(index) }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DartsBoard(hits: List<DartHit>, enabled: Boolean, onHit: (DartHit) -> Unit) {
    var boardSizePx by remember { mutableStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { boardSizePx = minOf(it.width, it.height).toFloat() }
                .pointerInput(enabled, hits.size, boardSizePx) {
                    detectTapGestures { offset ->
                        if (enabled && boardSizePx > 0f) {
                            onHit(pointToDartHit(offset, boardSizePx))
                        }
                    }
                }
        ) {
            drawDartBoard(hits)
        }
    }
}

fun DrawScope.drawDartBoard(hits: List<DartHit>) {
    val centre = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f * 0.96f
    val doubleInner = radius * 0.86f
    val trebleOuter = radius * 0.58f
    val trebleInner = radius * 0.49f
    val outerBull = radius * 0.12f
    val innerBull = radius * 0.055f

    drawCircle(Color(0xFF202020), radius, centre)

    for (i in 0 until 20) {
        val startAngle = -99f + i * 18f
        val segmentColor = if (i % 2 == 0) Color(0xFFE8E1D1) else Color(0xFF1F1F1F)
        val ringColor = if (i % 2 == 0) Color(0xFFC0392B) else Color(0xFF1F8A4C)
        drawArc(color = segmentColor, startAngle = startAngle, sweepAngle = 18f, useCenter = true, topLeft = Offset(centre.x - doubleInner, centre.y - doubleInner), size = androidx.compose.ui.geometry.Size(doubleInner * 2, doubleInner * 2))
        drawArcRing(ringColor, centre, (trebleInner + trebleOuter) / 2f, trebleOuter - trebleInner, startAngle, 18f)
        drawArcRing(ringColor, centre, (doubleInner + radius) / 2f, radius - doubleInner, startAngle, 18f)
    }

    drawCircle(Color(0xFF1F8A4C), outerBull, centre)
    drawCircle(Color(0xFFC0392B), innerBull, centre)

    listOf(radius, doubleInner, trebleOuter, trebleInner, outerBull, innerBull).forEach {
        drawCircle(Color.Black, it, centre, style = Stroke(2f))
    }

    for (i in 0 until 20) {
        val angle = Math.toRadians((-90 + i * 18).toDouble())
        val labelRadius = radius * 0.74f
        val number = boardOrder[i]
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = if (i % 2 == 0) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                textSize = 32f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            drawText(number.toString(), centre.x + cos(angle).toFloat() * labelRadius, centre.y + sin(angle).toFloat() * labelRadius + 10f, paint)
        }
    }

    hits.forEachIndexed { index, hit ->
        if (hit.label != "Miss") {
            val pos = labelPosition(hit, centre, radius)
            drawCircle(Color.Yellow, radius * 0.045f, pos)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                drawText((index + 1).toString(), pos.x, pos.y + 9f, paint)
            }
        }
    }
}

fun DrawScope.drawArcRing(color: Color, centre: Offset, radius: Float, width: Float, startAngle: Float, sweep: Float) {
    drawArc(color = color, startAngle = startAngle, sweepAngle = sweep, useCenter = false, topLeft = Offset(centre.x - radius, centre.y - radius), size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2), style = Stroke(width))
}

fun labelPosition(hit: DartHit, centre: Offset, radius: Float): Offset {
    if (hit.label == "Bull") return centre
    if (hit.label == "Outer Bull") return Offset(centre.x, centre.y - radius * 0.09f)
    val segment = hit.segment ?: return centre
    val index = boardOrder.indexOf(segment).coerceAtLeast(0)
    val angle = Math.toRadians((-90 + index * 18).toDouble())
    val r = when (hit.multiplier) { 3 -> radius * 0.535f; 2 -> radius * 0.93f; else -> radius * 0.72f }
    return Offset(centre.x + cos(angle).toFloat() * r, centre.y + sin(angle).toFloat() * r)
}

fun pointToDartHit(position: Offset, boardSize: Float): DartHit {
    val centre = Offset(boardSize / 2f, boardSize / 2f)
    val dx = position.x - centre.x
    val dy = position.y - centre.y
    val distance = sqrt(dx * dx + dy * dy)
    val radius = boardSize / 2f * 0.96f
    val doubleInner = radius * 0.86f
    val trebleOuter = radius * 0.58f
    val trebleInner = radius * 0.49f
    val outerBull = radius * 0.12f
    val innerBull = radius * 0.055f

    if (distance > radius) return DartHit(null, 0, "Miss", 0)
    if (distance <= innerBull) return DartHit(25, 2, "Bull", 50, isDoubleFinish = true)
    if (distance <= outerBull) return DartHit(25, 1, "Outer Bull", 25)

    val rawAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
    val adjusted = ((rawAngle + 90 + 9 + 360) % 360)
    val index = floor(adjusted / 18).toInt().coerceIn(0, 19)
    val segment = boardOrder[index]
    val multiplier = when {
        distance in trebleInner..trebleOuter -> 3
        distance in doubleInner..radius -> 2
        else -> 1
    }
    val prefix = when (multiplier) { 3 -> "T"; 2 -> "D"; else -> "S" }
    return DartHit(segment, multiplier, "$prefix$segment", segment * multiplier, isDoubleFinish = multiplier == 2)
}

fun applyHitToTurn(state: GameState, hit: DartHit): GameState {
    val selected = state.selectedSlotIndex
    val updatedTurn = when {
        selected != null && selected < state.currentTurn.size -> {
            state.currentTurn.toMutableList().also { it[selected] = hit }
        }
        selected != null && selected == state.currentTurn.size && state.currentTurn.size < 3 -> {
            state.currentTurn + hit
        }
        state.currentTurn.size < 3 -> {
            state.currentTurn + hit
        }
        else -> state.currentTurn
    }

    return state.copy(
        currentTurn = updatedTurn,
        selectedSlotIndex = null,
        message = if (updatedTurn.size == 3) "Check darts, then confirm." else ""
    )
}

fun commitTurn(state: GameState): GameState {
    val current = state.players[state.currentPlayerIndex]
    val turnScore = state.currentTurn.sumOf { it.score }
    val remaining = current.score - turnScore
    val lastDart = state.currentTurn.lastOrNull()
    val isBust = remaining < 0 ||
        (state.finishRule == FinishRule.DOUBLE_OUT && remaining == 1) ||
        (state.finishRule == FinishRule.DOUBLE_OUT && remaining == 0 && lastDart?.isDoubleFinish != true)
    val hasWon = remaining == 0 && !isBust
    val updatedPlayers = if (!isBust) state.players.toMutableList().also { it[state.currentPlayerIndex] = current.copy(score = remaining) } else state.players
    val nextIndex = if (hasWon) state.currentPlayerIndex else (state.currentPlayerIndex + 1) % state.players.size
    val message = when {
        hasWon -> "${current.name} wins!"
        isBust -> "Bust. ${current.name} stays on ${current.score}."
        else -> "${current.name} scored $turnScore."
    }
    return state.copy(players = updatedPlayers, currentPlayerIndex = nextIndex, currentTurn = emptyList(), message = message, selectedSlotIndex = null)
}

fun checkoutSuggestion(score: Int, finishRule: FinishRule): String? {
    if (score <= 0 || score > 180) return null
    if (finishRule == FinishRule.DOUBLE_OUT && score > 170) return null
    val darts = possibleDarts()

    if (finishRule == FinishRule.STRAIGHT_OUT) {
        for (a in darts) if (a.score == score) return a.label
        for (a in darts) for (b in darts) if (a.score + b.score == score) return "${a.label} ${b.label}"
        for (a in darts) for (b in darts) for (c in darts) if (a.score + b.score + c.score == score) return "${a.label} ${b.label} ${c.label}"
        return null
    }

    val finishingDarts = darts.filter { it.isDoubleFinish }
    for (a in finishingDarts) if (a.score == score) return a.label
    for (a in darts) for (b in finishingDarts) if (a.score + b.score == score) return "${a.label} ${b.label}"
    for (a in darts) for (b in darts) for (c in finishingDarts) if (a.score + b.score + c.score == score) return "${a.label} ${b.label} ${c.label}"
    return null
}

fun possibleDarts(): List<DartHit> {
    val darts = mutableListOf<DartHit>()
    for (n in 1..20) {
        darts += DartHit(n, 1, "S$n", n)
        darts += DartHit(n, 2, "D$n", n * 2, isDoubleFinish = true)
        darts += DartHit(n, 3, "T$n", n * 3)
    }
    darts += DartHit(25, 1, "Outer Bull", 25)
    darts += DartHit(25, 2, "Bull", 50, isDoubleFinish = true)
    return darts.sortedWith(compareByDescending<DartHit> { it.score }.thenBy { if (it.label.startsWith("T")) 0 else if (it.label.startsWith("D")) 1 else 2 })
}


private const val PREFS_NAME = "darts_scorer_prefs"
private const val KEY_GAME_STATE = "game_state_json"

fun saveGameState(context: Context, state: GameState) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_GAME_STATE, gameStateToJson(state).toString())
        .apply()
}

fun loadGameState(context: Context): GameState? {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_GAME_STATE, null) ?: return null
    return runCatching { jsonToGameState(JSONObject(raw)) }.getOrNull()
}

fun clearSavedGame(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_GAME_STATE)
        .apply()
}

fun gameStateToJson(state: GameState): JSONObject {
    return JSONObject().apply {
        put("players", JSONArray().apply {
            state.players.forEach { player ->
                put(JSONObject().apply {
                    put("name", player.name)
                    put("score", player.score)
                })
            }
        })
        put("currentPlayerIndex", state.currentPlayerIndex)
        put("currentTurn", JSONArray().apply {
            state.currentTurn.forEach { hit ->
                put(JSONObject().apply {
                    put("segment", hit.segment ?: JSONObject.NULL)
                    put("multiplier", hit.multiplier)
                    put("label", hit.label)
                    put("score", hit.score)
                    put("isDoubleFinish", hit.isDoubleFinish)
                })
            }
        })
        put("startingScore", state.startingScore)
        put("finishRule", state.finishRule.name)
        put("message", state.message)
        put("selectedSlotIndex", state.selectedSlotIndex ?: JSONObject.NULL)
    }
}

fun jsonToGameState(json: JSONObject): GameState {
    val playersJson = json.getJSONArray("players")
    val players = List(playersJson.length()) { index ->
        val player = playersJson.getJSONObject(index)
        Player(player.getString("name"), player.getInt("score"))
    }

    val turnJson = json.optJSONArray("currentTurn") ?: JSONArray()
    val currentTurn = List(turnJson.length()) { index ->
        val hit = turnJson.getJSONObject(index)
        DartHit(
            segment = if (hit.isNull("segment")) null else hit.getInt("segment"),
            multiplier = hit.getInt("multiplier"),
            label = hit.getString("label"),
            score = hit.getInt("score"),
            isDoubleFinish = hit.optBoolean("isDoubleFinish", false)
        )
    }

    return GameState(
        players = players,
        currentPlayerIndex = json.optInt("currentPlayerIndex", 0).coerceIn(0, players.lastIndex.coerceAtLeast(0)),
        currentTurn = currentTurn,
        startingScore = json.optInt("startingScore", 501),
        finishRule = runCatching { FinishRule.valueOf(json.optString("finishRule", FinishRule.DOUBLE_OUT.name)) }.getOrDefault(FinishRule.DOUBLE_OUT),
        message = json.optString("message", ""),
        selectedSlotIndex = if (json.isNull("selectedSlotIndex")) null else json.optInt("selectedSlotIndex")
    )
}
