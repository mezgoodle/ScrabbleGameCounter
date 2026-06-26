package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

// Premium Color Palette for Boardgame Atmosphere
val PrimaryAmber = Color(0xFFE2B13C)       // Classic Scrabble gold/yellow accent
val ActiveGold = Color(0xFFFFD54F)         // Glistening Active highlight
val ForestBgCenter = Color(0xFF132819)     // Central Deep Forest Green Felt
val ForestBgEdge = Color(0xFF0C140E)       // Dark ambient shadow borders
val CardGraphite = Color(0xFF1B1E1C)       // High-contrast deep carbon slate cards
val IvoryTile = Color(0xFFF9F6EA)          // Tactile warm ivory letter tile face
val WalnutDark = Color(0xFF2E1B10)         // Rich dark letter ink / engraving wood shade

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ScrabbleTrackerApp()
            }
        }
    }
}

/**
 * Kept to avoid breaking the visual regression tests which expect a Greeting composable
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun ScrabbleTrackerApp() {
    val model: ScrabbleGameViewModel = viewModel()
    val state by model.gameState.collectAsStateWithLifecycle()

    // Render global background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(ForestBgCenter, ForestBgEdge)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Crossfade(targetState = state, label = "screen_navigation") { gameStateState ->
            when {
                gameStateState.isSetupActive -> {
                    GameSetupScreen(
                        onStartGame = { players -> model.startGame(players) }
                    )
                }
                gameStateState.isGameOver -> {
                    GameOverScreen(
                        players = gameStateState.players,
                        history = gameStateState.history,
                        onNewGame = { model.resetGame() }
                    )
                }
                else -> {
                    ScoreboardScreen(
                        gameState = gameStateState,
                        onAddTurn = { word, points, desc, base -> model.addTurn(word, points, desc, base) },
                        onUndoTurn = { model.undoLastTurn() },
                        onEditTurn = { word, points, desc, base -> model.editLastTurn(word, points, desc, base) },
                        onEndGame = { model.endGame() },
                        onResetGame = { model.resetGame() },
                        onSetPlayerActive = { index -> model.setActivePlayer(index) }
                    )
                }
            }
        }
    }
}

/**
 * 1. GAME SETUP SCREEN
 */
@Composable
fun GameSetupScreen(onStartGame: (List<String>) -> Unit) {
    var playerCount by remember { mutableIntStateOf(2) }
    val playerNames = remember { mutableStateListOf("Player 1", "Player 2", "", "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            // Stylized Tiled Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                "СКРАБЛ".forEach { char ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(IvoryTile)
                            .border(1.5.dp, PrimaryAmber, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = WalnutDark
                            )
                        )
                        val points = ScrabbleRules.getLetterBaseValue(char)
                        if (points > 0) {
                            Text(
                                text = points.toString(),
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = WalnutDark
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 1.dp, end = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ТАБЛО РАХУНКУ",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W900,
                    letterSpacing = 4.sp,
                    color = PrimaryAmber
                )
            )
            Text(
                text = "Мінімалістичний настільний помічник",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Setup Main Configuration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardGraphite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Кількість гравців",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Robust Player Incrementor Tab
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { if (playerCount > 2) playerCount-- },
                        modifier = Modifier.size(44.dp).testTag("decrement_players_btn")
                    ) {
                        Text(
                            text = "−",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (playerCount > 2) PrimaryAmber else Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }

                    Text(
                        text = "Гравців: $playerCount",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = { if (playerCount < 4) playerCount++ },
                        modifier = Modifier.size(44.dp).testTag("increment_players_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Players",
                            tint = if (playerCount < 4) PrimaryAmber else Color.White.copy(alpha = 0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Імена гравців",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryAmber
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List name fields matching the count
                for (i in 0 until playerCount) {
                    OutlinedTextField(
                        value = playerNames[i],
                        onValueChange = { playerNames[i] = it },
                        placeholder = { Text("Гравець ${i + 1}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("player_name_input_$i"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PrimaryAmber,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.Transparent,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.3f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryAmber.copy(alpha = 0.7f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = if (i == playerCount - 1) ImeAction.Done else ImeAction.Next
                        )
                    )
                }
            }
        }

        // Start Game CTA Button
        Button(
            onClick = {
                val list = mutableListOf<String>()
                for (i in 0 until playerCount) {
                    val input = playerNames[i].trim()
                    list.add(input.ifEmpty { "Гравець ${i + 1}" })
                }
                onStartGame(list)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_game_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryAmber,
                contentColor = WalnutDark
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ПОЧАТИ ГРУ",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

/**
 * 2. MAIN SCOREBOARD SCREEN
 */
@Composable
fun ScoreboardScreen(
    gameState: ScrabbleGameState,
    onAddTurn: (String, Int, String, Int) -> Unit,
    onUndoTurn: () -> Unit,
    onEditTurn: (String, Int, String, Int) -> Unit,
    onEndGame: () -> Unit,
    onResetGame: () -> Unit,
    onSetPlayerActive: (Int) -> Unit
) {
    var showAddWordDialog by remember { mutableStateOf(false) }
    var editIndexToLoad by remember { mutableStateOf<Int?>(null) } // Set to index if loading edit

    val activePlayer = gameState.players.getOrNull(gameState.activePlayerIndex)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Screen Header Admin controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Quit Reset
                TextButton(
                    onClick = onResetGame,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Match")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Скинути")
                }

                // Scrabble branding badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "СКРАБЛ",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber,
                            letterSpacing = 3.sp
                        )
                    )
                }

                // Finish End Match Button
                Button(
                    onClick = onEndGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828), // deep red
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("end_game_btn")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Finish Match", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Завершити", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scoreboard Grid: Large display of names and current sum points
            Text(
                text = "Рахунок",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryAmber,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Players Dashboard Flex Grid Flow
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                gameState.players.forEachIndexed { index, player ->
                    val isActive = index == gameState.activePlayerIndex

                    val cardBorder = if (isActive) {
                        BorderStroke(2.dp, ActiveGold)
                    } else {
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    }

                    val cardElevation = if (isActive) 8.dp else 2.dp
                    val cardBg = if (isActive) Color(0xFF242C26) else CardGraphite

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_card_$index")
                            .clickable { onSetPlayerActive(index) },
                        shape = RoundedCornerShape(16.dp),
                        border = cardBorder,
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = player.name,
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isActive) ActiveGold else Color.White
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(ActiveGold, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ХІД",
                                                style = TextStyle(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WalnutDark
                                                )
                                            )
                                        }
                                    }
                                }

                                // Quick standing indicator
                                val gameRank = gameState.players
                                    .sortedByDescending { it.score }
                                    .indexOfFirst { it.id == player.id } + 1
                                
                                val rankLabel = when (gameRank) {
                                    1 -> "👑 1 місце"
                                    2 -> "🥈 2 місце"
                                    3 -> "🥉 3 місце"
                                    else -> "🏅 4 місце"
                                }

                                Text(
                                    text = rankLabel,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Huge Score display so everyone at table sees
                            Text(
                                text = "${player.score}",
                                style = TextStyle(
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.W900,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isActive) ActiveGold else Color.White
                                ),
                                modifier = Modifier.testTag("player_score_$index")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History Log Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Turn Logs & History",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryAmber,
                        letterSpacing = 1.sp
                    )
                )

                if (gameState.history.isNotEmpty()) {
                    TextButton(
                        onClick = onUndoTurn,
                        modifier = Modifier.testTag("undo_last_btn"),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF8A80))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo Last Turn", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Custom Display for Empty History State
            if (gameState.history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "No Entries Yet",
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ще немає слів",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Натисніть \"ДОДАТИ СЛОВО\", щоб почати грати.",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                // Scrollable History Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("history_list")
                    ) {
                        val recentHistory = gameState.history.takeLast(5)
                        itemsIndexed(recentHistory) { logIndex, log ->
                            val isLast = logIndex == recentHistory.size - 1
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${log.playerName} зіграв(-ла)",
                                            style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = log.word,
                                                style = TextStyle(
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = PrimaryAmber
                                                )
                                            )
                                            if (log.description.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = 8.dp)
                                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = log.description,
                                                        style = TextStyle(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White.copy(alpha = 0.7f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "+${log.finalScore}",
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF66BB6A) // lovely green
                                            )
                                        )

                                        // Edit controller IF last entry
                                        if (isLast) {
                                            IconButton(
                                                onClick = { editIndexToLoad = logIndex },
                                                modifier = Modifier
                                                    .padding(start = 8.dp)
                                                    .size(36.dp)
                                                    .testTag("edit_last_turn_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Редагувати хід",
                                                    tint = Color.White.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Big CTA to add words for the turn
            if (activePlayer != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showAddWordDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("add_word_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActiveGold,
                            contentColor = WalnutDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ДОДАТИ СЛОВО ДЛЯ ${activePlayer.name.uppercase()}",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { onAddTurn("", 0, "Пропуск ходу / Обмін літер", 0) },
                        modifier = Modifier.fillMaxWidth().testTag("skip_turn_btn"),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.5f))
                    ) {
                        Text(text = "Пропустити хід", fontSize = 12.sp)
                    }
                }
            }
        }

        // TURN ADDITION DIALOG
        if (showAddWordDialog) {
            WordEntryCalculatorDialog(
                playerName = activePlayer?.name ?: "Гравець",
                onDismiss = { showAddWordDialog = false },
                onConfirm = { word, points, desc, base ->
                    onAddTurn(word, points, desc, base)
                    showAddWordDialog = false
                }
            )
        }

        // EDIT LAST TURN DIALOG
        if (editIndexToLoad != null) {
            val lastLog = gameState.history.lastOrNull()
            if (lastLog != null) {
                WordEntryCalculatorDialog(
                    playerName = lastLog.playerName,
                    initialWord = lastLog.word.replace("(Пропустив)", ""),
                    initialIsEdit = true,
                    onDismiss = { editIndexToLoad = null },
                    onConfirm = { word, points, desc, base ->
                        onEditTurn(word, points, desc, base)
                        editIndexToLoad = null
                    }
                )
            } else {
                editIndexToLoad = null
            }
        }
    }
}

/**
 * 3. WORD ENTRY & SCORE CALCULATOR
 */
@Composable
fun WordEntryCalculatorDialog(
    playerName: String,
    initialWord: String = "",
    initialIsEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (word: String, points: Int, description: String, baseScore: Int) -> Unit
) {
    var rawWordInput by remember { mutableStateOf(initialWord) }
    val cleanWord = remember(rawWordInput) {
        rawWordInput.trim().uppercase().filter { 
            it.isLetter() || it == '\'' || it == '’' 
        }.replace('’', '\'')
    }

    // List of multipliers for each letter in the word. Auto sync sized matching standard flow
    var letterMultipliers by remember { mutableStateOf(listOf<LetterMultiplier>()) }

    LaunchedEffect(cleanWord) {
        if (letterMultipliers.size < cleanWord.length) {
            letterMultipliers = letterMultipliers + List(cleanWord.length - letterMultipliers.size) { LetterMultiplier.NORMAL }
        } else if (letterMultipliers.size > cleanWord.length) {
            letterMultipliers = letterMultipliers.take(cleanWord.length)
        }
    }

    // Word multiplier toggles
    var wordMultiplier by remember { mutableStateOf(WordMultiplier.NONE) }
    var bingoApplied by remember { mutableStateOf(cleanWord.length == 7) }

    // Auto-apply bingo details if word length is exactly 7
    LaunchedEffect(cleanWord.length) {
        bingoApplied = cleanWord.length == 7
    }

    // Score Calculations
    val calculatedBaseLetterScore = remember(cleanWord, letterMultipliers) {
        var baseSum = 0
        for (i in cleanWord.indices) {
            val baseCharVal = ScrabbleRules.getLetterBaseValue(cleanWord[i])
            val multiplier = letterMultipliers.getOrNull(i) ?: LetterMultiplier.NORMAL
            baseSum += baseCharVal * multiplier.value
        }
        baseSum
    }

    val finalCalculatedScore = remember(calculatedBaseLetterScore, wordMultiplier, bingoApplied) {
        val wordMultiplyValue = wordMultiplier.value
        val scoreBeforeBingo = calculatedBaseLetterScore * wordMultiplyValue
        val bingoBonus = if (bingoApplied) 50 else 0
        scoreBeforeBingo + bingoBonus
    }

    val dynamicDescriptionLabel = remember(letterMultipliers, wordMultiplier, bingoApplied, cleanWord) {
        val descParts = mutableListOf<String>()
        
        // Letter mods
        var hasLetterMods = false
        val dls = letterMultipliers.count { it == LetterMultiplier.DOUBLE_LETTER }
        val tls = letterMultipliers.count { it == LetterMultiplier.TRIPLE_LETTER }
        val blanks = letterMultipliers.count { it == LetterMultiplier.BLANK }
        
        if (dls > 0) descParts.add("${dls}x DL")
        if (tls > 0) descParts.add("${tls}x TL")
        if (blanks > 0) descParts.add("${blanks}x BL")

        // Word mods
        if (wordMultiplier != WordMultiplier.NONE) {
            descParts.add(wordMultiplier.label)
        }

        // Bingo
        if (bingoApplied) {
            descParts.add("BINGO (+50)")
        }

        if (descParts.isEmpty() && cleanWord.isNotEmpty()) {
            "Звичайне слово"
        } else {
            descParts.joinToString(", ")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardGraphite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .testTag("word_entry_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Diagonal header titles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialIsEdit) "Редагувати хід" else "Калькулятор очок",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                Text(
                    text = "Гравець: $playerName",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Enter Word Text Input
                OutlinedTextField(
                    value = rawWordInput,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() || it.isWhitespace() || it == '\'' || it == '’' }) {
                            rawWordInput = input
                        }
                    },
                    label = { Text("Зігране слово") },
                    placeholder = { Text("Напр., ЯБЛУКО") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_word_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryAmber,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    trailingIcon = {
                        if (rawWordInput.isNotEmpty()) {
                            IconButton(onClick = { rawWordInput = "" }) {
                                Icon(Icons.Default.Clear, "Очистити", tint = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Tile Render matrix
                if (cleanWord.isNotEmpty()) {
                    Text(
                        text = "Натисніть плитку, щоб застосувати множник (2х Б / 3х Б / ПУСТА):",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Horizontal wrap scroll for letters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        cleanWord.forEachIndexed { i, char ->
                            val currentMult = letterMultipliers.getOrNull(i) ?: LetterMultiplier.NORMAL
                            ScrabbleTile(
                                char = char,
                                multiplier = currentMult,
                                onClick = {
                                    // Cycle MULTIPLIERS on click
                                    val nextMult = when (currentMult) {
                                        LetterMultiplier.NORMAL -> LetterMultiplier.DOUBLE_LETTER
                                        LetterMultiplier.DOUBLE_LETTER -> LetterMultiplier.TRIPLE_LETTER
                                        LetterMultiplier.TRIPLE_LETTER -> LetterMultiplier.BLANK
                                        LetterMultiplier.BLANK -> LetterMultiplier.NORMAL
                                    }
                                    val newList = letterMultipliers.toMutableList()
                                    if (i < newList.size) {
                                        newList[i] = nextMult
                                        letterMultipliers = newList
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // Empty list state helper
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Очікування букв...",
                            style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Word Multipliers selection chips
                Text(
                    text = "Множники слова",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryAmber
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(WordMultiplier.NONE, WordMultiplier.DOUBLE_WORD, WordMultiplier.TRIPLE_WORD).forEach { option ->
                        val selected = wordMultiplier == option
                        val containerBg = if (selected) PrimaryAmber else Color.White.copy(alpha = 0.05f)
                        val textClr = if (selected) WalnutDark else Color.White

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(containerBg)
                                .border(1.dp, if (selected) PrimaryAmber else Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable { wordMultiplier = option }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (option) {
                                    WordMultiplier.NONE -> "1x Звичайн."
                                    WordMultiplier.DOUBLE_WORD -> "2x 2С"
                                    WordMultiplier.TRIPLE_WORD -> "3x 3С"
                                },
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textClr
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bingo Toggle segment
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "7-буквений БІНГО (+50)",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = "Використав усі 7 плиток за один хід",
                            style = TextStyle(fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                        )
                    }

                    Switch(
                        checked = bingoApplied,
                        onCheckedChange = { bingoApplied = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WalnutDark,
                            checkedTrackColor = PrimaryAmber,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scoring Preview display cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ЗАГАЛОМ ОЧОК ОТРИМАНО",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                        )

                        Text(
                            text = "$finalCalculatedScore ОЧОК",
                            style = TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.W900,
                                fontFamily = FontFamily.Monospace,
                                color = ActiveGold
                            )
                        )

                        // Math representation string builder
                        val mathFormulaLabel = buildString {
                            if (cleanWord.isEmpty()) {
                                append("0 базових очок")
                            } else {
                                append("Базові плитки ($calculatedBaseLetterScore)")
                                if (wordMultiplier != WordMultiplier.NONE) {
                                    append(" x ${wordMultiplier.value} (${wordMultiplier.label})")
                                }
                                if (bingoApplied) {
                                    append(" + 50 (Бінго)")
                                }
                                append(" = $finalCalculatedScore")
                            }
                        }

                        Text(
                            text = mathFormulaLabel,
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dialog interactive choices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Secondary Pass Turn button (Real rule - users pass to score 0)
                    if (!initialIsEdit) {
                        OutlinedButton(
                            onClick = {
                                onConfirm("", 0, "Пропустив", 0)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("dialog_pass_turn_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                        ) {
                            Text("Пропустити", fontSize = 13.sp)
                        }
                    }

                    // Primary confirm button
                    Button(
                        onClick = {
                            if (cleanWord.isNotEmpty()) {
                                onConfirm(cleanWord, finalCalculatedScore, dynamicDescriptionLabel, calculatedBaseLetterScore)
                            } else {
                                onConfirm("", 0, "Пропустив", 0)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("dialog_confirm_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryAmber,
                            contentColor = WalnutDark
                        )
                    ) {
                        Text(
                            text = if (cleanWord.isEmpty()) "ПРОПУСТИВ (0 ОЧОК)" else "ПІДТВЕРДИТИ СЛОВО",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Representation of a Physical Wood / Ivory Tile
 */
@Composable
fun ScrabbleTile(
    char: Char,
    multiplier: LetterMultiplier = LetterMultiplier.NORMAL,
    onClick: (() -> Unit)? = null
) {
    val baseValue = ScrabbleRules.getLetterBaseValue(char)
    val multiplierLabel = when (multiplier) {
        LetterMultiplier.DOUBLE_LETTER -> "2Б"
        LetterMultiplier.TRIPLE_LETTER -> "3Б"
        LetterMultiplier.BLANK -> "ПУС"
        else -> ""
    }

    val multiplierColor = when (multiplier) {
        LetterMultiplier.DOUBLE_LETTER -> Color(0xFF1976D2)  // Classy sapphire blue
        LetterMultiplier.TRIPLE_LETTER -> Color(0xFFD32F2F)  // Crimson red
        LetterMultiplier.BLANK -> Color(0xFF616161)          // Slate gray
        else -> Color.Transparent
    }

    // Touch targets must be at least 48dp
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(54.dp, 58.dp)
            .clip(RoundedCornerShape(8.dp))
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(if (multiplier == LetterMultiplier.BLANK) Color(0xFFECEFF1) else IvoryTile)
            .border(
                2.dp,
                if (multiplierColor != Color.Transparent) multiplierColor else Color(0xFFDAD0BC),
                RoundedCornerShape(8.dp)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Core symbol
        Text(
            text = if (multiplier == LetterMultiplier.BLANK) "?" else char.toString(),
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = WalnutDark
            ),
            modifier = Modifier.align(Alignment.Center)
        )

        // Subscript base digit
        if (multiplier != LetterMultiplier.BLANK && baseValue > 0) {
            Text(
                text = baseValue.toString(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = WalnutDark
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 2.dp, end = 4.dp)
            )
        }

        // Mini banner sticker for the multiplier
        if (multiplierLabel.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(multiplierColor, RoundedCornerShape(bottomEnd = 4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = multiplierLabel,
                    style = TextStyle(
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

/**
 * 4. GAME OVER SCREEN WITH TROPHY & CELEBRATION
 */
@Composable
fun GameOverScreen(
    players: List<Player>,
    history: List<PlayHistoryEntry>,
    onNewGame: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onNewGame)

    // Sort final placements
    val rankings = remember(players) {
        players.sortedByDescending { it.score }
    }
    val winner = rankings.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        // Particles Custom Canvas Confetti
        ConfettiEffect()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Trophy Logo Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                // Gold Icon Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFFF8E1))
                        .border(3.dp, PrimaryAmber, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Trophy/Winner icon",
                        tint = PrimaryAmber,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ПЕРЕМОГА!",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.W900,
                        letterSpacing = 6.sp,
                        color = PrimaryAmber
                    )
                )

                if (winner != null) {
                    Text(
                        text = "${winner.name} виграє матч!".uppercase(),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // High-fidelity Ranking Podiums / List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardGraphite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Фінальний рейтинг",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    rankings.forEachIndexed { rank, player ->
                        val isWinner = rank == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isWinner) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isWinner) PrimaryAmber else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Position emblem
                                Text(
                                    text = when (rank) {
                                        0 -> "🥇"
                                        1 -> "🥈"
                                        2 -> "🥉"
                                        else -> "🏅"
                                    },
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )

                                Text(
                                    text = player.name,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isWinner) ActiveGold else Color.White
                                    )
                                )
                            }

                            Text(
                                text = "${player.score} очок",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWinner) ActiveGold else Color.White
                                )
                            )
                        }
                        if (rank < rankings.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Quick Match Audit Statistics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ЗАГАЛОМ РАУНДІВ",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        val totalRounds = if (players.isNotEmpty()) history.size / players.size else 0
                        Text(
                            text = "${maxOf(1, totalRounds)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ЗІГРАНО СЛІВ",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        val wordsPlayed = history.count { it.word != "(Пропустив)" }
                        Text(
                            text = "$wordsPlayed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "СЕРЕДНІЙ БАЛ",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        val avgScore = if (history.isNotEmpty()) history.map { it.finalScore }.average().toInt() else 0
                        Text(
                            text = "$avgScore оч",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber
                        )
                    }
                }
            }

            // Start New Game CTA button
            Button(
                onClick = onNewGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("new_game_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAmber,
                    contentColor = WalnutDark
                )
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ПОЧАТИ НОВУ ГРУ",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

/**
 * Custom particle drawing canvas for victory confetti falling effects
 */
@Composable
fun ConfettiEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")

    // Running duration time parameter (0 to 1000f) to animate gravity fall positions
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(
            Color(0xFFE2B13C), // gold
            Color(0xFF2E7D32), // green
            Color(0xFF1565C0), // blue
            Color(0xFFC62828), // red
            Color(0xFFAD1457), // violet
            Color(0xFFEF6C00)  // orange
        )

        val totalConfetti = 40
        for (i in 0 until totalConfetti) {
            // Seed horizontal layout coordinates
            val rootOffsetX = (i * 1234) % size.width
            val fallVelocity = 80f + ((i * 53) % 180f)
            val verticalY = (time * fallVelocity * 0.1f) % size.height

            // Introduce a subtle pendulum wind swing
            val windOffset = kotlin.math.sin((time * 0.05f) + i) * 15f
            val x = (rootOffsetX + windOffset).coerceIn(0f, size.width)

            // Randomize aspect sizes and angles
            val itemSize = 10f + ((i * 17) % 20f)
            val color = colors[i % colors.size]

            drawRoundRect(
                color = color,
                topLeft = Offset(x, verticalY),
                size = Size(itemSize, itemSize * 0.6f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

// Simple Composable wrapper Helper for scroll state
@Composable
fun rememberScrollState(): ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}
