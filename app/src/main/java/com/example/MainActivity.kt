package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.PaymentStatus
import com.example.notifications.BookingReminderHelper
import com.example.ui.components.MatchEditorBottomSheet
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MatchDetailScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.SquadScreen
import com.example.ui.theme.MalabnaTheme
import com.example.ui.viewmodel.MalabnaViewModel

private object AppRoutes {
  const val BOOKINGS = "bookings"
  const val PAYMENTS = "payments"
  const val SQUAD = "squad"
  const val LEADERBOARD = "leaderboard"
  const val MATCH_DETAIL = "match_detail/{matchId}"

  fun matchDetail(matchId: Long) = "match_detail/$matchId"
}

private data class BottomTabItem(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val mainTabs =
  listOf(
    BottomTabItem(
      route = AppRoutes.BOOKINGS,
      labelRes = R.string.tab_bookings,
      selectedIcon = Icons.Rounded.SportsSoccer,
      unselectedIcon = Icons.Outlined.SportsSoccer,
      testTag = "nav_tab_bookings",
    ),
    BottomTabItem(
      route = AppRoutes.PAYMENTS,
      labelRes = R.string.tab_payments,
      selectedIcon = Icons.Rounded.AccountBalanceWallet,
      unselectedIcon = Icons.Outlined.AccountBalanceWallet,
      testTag = "nav_tab_payments",
    ),
    BottomTabItem(
      route = AppRoutes.SQUAD,
      labelRes = R.string.tab_squad,
      selectedIcon = Icons.Rounded.Groups,
      unselectedIcon = Icons.Outlined.Groups,
      testTag = "nav_tab_squad",
    ),
    BottomTabItem(
      route = AppRoutes.LEADERBOARD,
      labelRes = R.string.tab_ratings,
      selectedIcon = Icons.Rounded.EmojiEvents,
      unselectedIcon = Icons.Outlined.EmojiEvents,
      testTag = "nav_tab_ratings",
    ),
  )

class MainActivity : ComponentActivity() {
  private var pendingMatchId by mutableStateOf<Long?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    BookingReminderHelper.ensureNotificationChannel(this)
    extractMatchDeepLink(intent)

    setContent {
      val systemDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(systemDark) }

      MalabnaTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          MalabnaApp(
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
            pendingMatchId = pendingMatchId,
            onConsumedPendingMatch = { pendingMatchId = null },
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    extractMatchDeepLink(intent)
  }

  private fun extractMatchDeepLink(intent: Intent?) {
    val id = intent?.getLongExtra(BookingReminderHelper.EXTRA_MATCH_ID, -1L) ?: -1L
    if (id > 0L) {
      pendingMatchId = id
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MalabnaApp(
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  pendingMatchId: Long?,
  onConsumedPendingMatch: () -> Unit,
  viewModel: MalabnaViewModel = viewModel(),
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val matches by viewModel.matches.collectAsStateWithLifecycle()
  val playersWithStats by viewModel.playersWithStats.collectAsStateWithLifecycle()
  val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle()
  val paymentLedger by viewModel.paymentLedger.collectAsStateWithLifecycle()
  val monthlyLeaderboard by viewModel.monthlyLeaderboard.collectAsStateWithLifecycle()
  val allTimeLeaderboard by viewModel.allTimeLeaderboard.collectAsStateWithLifecycle()

  val unpaidBadgeCount =
    remember(paymentLedger) {
      paymentLedger.count { it.participant.paymentStatus == PaymentStatus.UNPAID }
    }

  var showCreateMatchSheet by remember { mutableStateOf(false) }
  var showAddSquadPlayerSheet by remember { mutableStateOf(false) }

  LaunchedEffect(pendingMatchId) {
    if (pendingMatchId != null && pendingMatchId > 0L) {
      navController.navigate(AppRoutes.matchDetail(pendingMatchId)) {
        launchSingleTop = true
      }
      onConsumedPendingMatch()
    }
  }

  val isTopLevelRoute =
    currentRoute == AppRoutes.BOOKINGS ||
      currentRoute == AppRoutes.PAYMENTS ||
      currentRoute == AppRoutes.SQUAD ||
      currentRoute == AppRoutes.LEADERBOARD

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useNavigationRail && isTopLevelRoute) {
        NavigationRail(modifier = Modifier.fillMaxHeight()) {
          Spacer(modifier = Modifier.weight(0.2f))
          mainTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
              selected = selected,
              onClick = {
                navController.navigate(tab.route) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = {
                TabIconWithBadge(
                  tab = tab,
                  selected = selected,
                  unpaidCount = unpaidBadgeCount,
                )
              },
              label = { Text(stringResource(tab.labelRes)) },
              modifier = Modifier.testTag(tab.testTag),
            )
          }
          Spacer(modifier = Modifier.weight(1f))
        }
      }

      Scaffold(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
          if (isTopLevelRoute) {
            TopAppBar(
              title = {
                Row {
                  Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text =
                      when (currentRoute) {
                        AppRoutes.BOOKINGS -> "• الحجوزات"
                        AppRoutes.PAYMENTS -> "• الحسابات"
                        AppRoutes.SQUAD -> "• الشلة"
                        AppRoutes.LEADERBOARD -> "• نجم الشهر"
                        else -> ""
                      },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              },
              actions = {
                IconButton(
                  onClick = onToggleTheme,
                  modifier = Modifier.testTag("toggle_theme_button"),
                ) {
                  Icon(
                    imageVector =
                      if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription =
                      if (isDarkTheme) "تفعيل الوضع النهاري" else "تفعيل الوضع الليلي",
                    tint = MaterialTheme.colorScheme.onSurface,
                  )
                }
              },
              colors =
                TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.background,
                ),
            )
          }
        },
        bottomBar = {
          if (!useNavigationRail && isTopLevelRoute) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 4.dp,
            ) {
              mainTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationBarItem(
                  selected = selected,
                  onClick = {
                    navController.navigate(tab.route) {
                      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = {
                    TabIconWithBadge(
                      tab = tab,
                      selected = selected,
                      unpaidCount = unpaidBadgeCount,
                    )
                  },
                  label = { Text(stringResource(tab.labelRes)) },
                  modifier = Modifier.testTag(tab.testTag),
                )
              }
            }
          }
        },
        floatingActionButton = {
          when (currentRoute) {
            AppRoutes.BOOKINGS ->
              ExtendedFloatingActionButton(
                onClick = { showCreateMatchSheet = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_booking)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_new_booking"),
              )
            AppRoutes.SQUAD ->
              ExtendedFloatingActionButton(
                onClick = { showAddSquadPlayerSheet = true },
                icon = { Icon(Icons.Rounded.PersonAdd, contentDescription = null) },
                text = { Text(stringResource(R.string.add_player)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_player"),
              )
          }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
      ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          NavHost(
            navController = navController,
            startDestination = AppRoutes.BOOKINGS,
          ) {
            composable(AppRoutes.BOOKINGS) {
              BookingsScreen(
                matches = matches,
                onMatchClick = { matchId ->
                  navController.navigate(AppRoutes.matchDetail(matchId))
                },
                onCreateMatchClick = { showCreateMatchSheet = true },
              )
            }

            composable(AppRoutes.PAYMENTS) {
              PaymentsScreen(
                ledgerItems = paymentLedger,
                onRecordPayment = { participant, status, method, amountPaid, customAmount, note ->
                  viewModel.recordPayment(
                    participant = participant,
                    status = status,
                    method = method,
                    amountPaid = amountPaid,
                    customAmount = customAmount,
                    note = note,
                  )
                },
                onNavigateToBookings = {
                  navController.navigate(AppRoutes.BOOKINGS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                  }
                },
              )
            }

            composable(AppRoutes.SQUAD) {
              SquadScreen(
                playersWithStats = playersWithStats,
                onAddPlayer = { name, phone, position, photoUri ->
                  viewModel.addPlayer(name, phone, position, photoUri)
                },
                onUpdatePlayer = { player -> viewModel.updatePlayer(player) },
                onDeletePlayer = { playerId -> viewModel.deletePlayer(playerId) },
                showAddSheetTrigger = showAddSquadPlayerSheet,
                onDismissAddSheetTrigger = { showAddSquadPlayerSheet = false },
              )
            }

            composable(AppRoutes.LEADERBOARD) {
              LeaderboardScreen(
                monthlyEntries = monthlyLeaderboard,
                allTimeEntries = allTimeLeaderboard,
                onNavigateToBookings = {
                  navController.navigate(AppRoutes.BOOKINGS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                  }
                },
              )
            }

            composable(
              route = AppRoutes.MATCH_DETAIL,
              arguments = listOf(navArgument("matchId") { type = NavType.LongType }),
            ) { backStackEntry ->
              val matchId = backStackEntry.arguments?.getLong("matchId") ?: -1L
              val matchWrap by
                viewModel
                  .observeMatch(matchId)
                  .collectAsStateWithLifecycle(initialValue = null)

              MatchDetailScreen(
                matchWrap = matchWrap,
                allSquadPlayers = allPlayers,
                onBack = { navController.popBackStack() },
                onUpdateMatch = { updated -> viewModel.updateMatch(updated) },
                onUpdateReminder = { m, mins -> viewModel.updateMatchReminder(m, mins) },
                onToggleCompleted = { id, completed -> viewModel.setMatchCompleted(id, completed) },
                onDeleteMatch = { id ->
                  viewModel.deleteMatch(id) { navController.popBackStack() }
                },
                onAddPlayersToMatch = { id, ids -> viewModel.addPlayersToMatch(id, ids) },
                onAddNewPlayerToMatch = { id, name, phone, pos ->
                  viewModel.addNewPlayerToMatch(id, name, phone, pos)
                },
                onRemovePlayerFromMatch = { id, playerId ->
                  viewModel.removePlayerFromMatch(id, playerId)
                },
                onUpdateAttendance = { participant, newStatus ->
                  viewModel.updateAttendance(participant, newStatus)
                },
                onRecordPayment = { participant, status, method, amountPaid, customAmount, note ->
                  viewModel.recordPayment(
                    participant = participant,
                    status = status,
                    method = method,
                    amountPaid = amountPaid,
                    customAmount = customAmount,
                    note = note,
                  )
                },
                onAssignPlayerToPitchSlot = { id, participant, slot ->
                  viewModel.assignPlayerToPitchSlot(id, participant, slot)
                },
                onAutoAssignPitchSlots = { id, participants, totalSlots ->
                  viewModel.autoAssignPitchSlots(id, participants, totalSlots)
                },
                onSaveTeamRatings = { id, ratings, markCompleted ->
                  viewModel.saveTeamRatings(id, ratings, markCompleted)
                },
              )
            }
          }
        }
      }
    }
  }

  if (showCreateMatchSheet) {
    MatchEditorBottomSheet(
      existingMatch = null,
      availablePlayers = allPlayers,
      onDismiss = { showCreateMatchSheet = false },
      onSave = { title, pitch, loc, date, time, format, target, cost, notes, selectedIds ->
        showCreateMatchSheet = false
        viewModel.createMatch(
          title = title,
          pitchName = pitch,
          location = loc,
          dateMillis = date,
          timeLabel = time,
          format = format,
          targetPlayers = target,
          totalCost = cost,
          notes = notes,
          initialPlayerIds = selectedIds,
        ) { newId ->
          navController.navigate(AppRoutes.matchDetail(newId))
        }
      },
    )
  }
}

@Composable
private fun TabIconWithBadge(
  tab: BottomTabItem,
  selected: Boolean,
  unpaidCount: Int,
) {
  val iconVector = if (selected) tab.selectedIcon else tab.unselectedIcon
  if (tab.route == AppRoutes.PAYMENTS && unpaidCount > 0) {
    BadgedBox(
      badge = {
        Badge { Text(unpaidCount.toString()) }
      }
    ) {
      Icon(imageVector = iconVector, contentDescription = null)
    }
  } else {
    Icon(imageVector = iconVector, contentDescription = null)
  }
}
