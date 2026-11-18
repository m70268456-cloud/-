package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.MatchEntity
import com.example.data.local.MatchParticipantEntity
import com.example.data.local.PlayerEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.MatchParticipantDetail
import com.example.data.model.MatchWithParticipants
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.notifications.BookingReminderHelper
import com.example.ui.components.InteractivePitchBoard
import com.example.ui.components.MatchEditorBottomSheet
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.RecordPaymentBottomSheet
import com.example.ui.components.StatusPill
import com.example.ui.components.paymentMethodIcon
import com.example.ui.theme.LocalMalabnaColors
import com.example.ui.util.buildMatchShareText
import com.example.ui.util.formatArabicDate
import com.example.ui.util.formatCurrency

private enum class ParticipantListFilter(val label: String) {
  ALL("الكل"),
  GOING("جاي"),
  PENDING("لم يرد"),
  NOT_GOING("مش جاي"),
  PAID("دفع"),
  UNPAID("مدفعش"),
  EXEMPT("مش هيدفع"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MatchDetailScreen(
  matchWrap: MatchWithParticipants?,
  allSquadPlayers: List<PlayerEntity>,
  onBack: () -> Unit,
  onUpdateMatch: (MatchEntity) -> Unit,
  onUpdateReminder: (MatchEntity, Int?) -> Unit,
  onToggleCompleted: (Long, Boolean) -> Unit,
  onDeleteMatch: (Long) -> Unit,
  onAddPlayersToMatch: (Long, List<Long>) -> Unit,
  onAddNewPlayerToMatch: (Long, String, String, String) -> Unit,
  onRemovePlayerFromMatch: (Long, Long) -> Unit,
  onUpdateAttendance: (MatchParticipantEntity, AttendanceStatus) -> Unit,
  onRecordPayment: (
    participant: MatchParticipantEntity,
    status: PaymentStatus,
    method: PaymentMethod?,
    amountPaid: Double,
    customAmount: Double?,
    note: String,
  ) -> Unit,
  onAssignPlayerToPitchSlot: (Long, MatchParticipantEntity, Int?) -> Unit,
  onAutoAssignPitchSlots: (Long, List<MatchParticipantEntity>, Int) -> Unit,
  onSaveTeamRatings: (Long, List<Triple<MatchParticipantEntity, Int?, String>>, Boolean) -> Unit,
) {
  BackHandler(onBack = onBack)

  if (matchWrap == null) {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("تفاصيل الحجز") },
          navigationIcon = {
            IconButton(onClick = onBack) {
              Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
            }
          },
        )
      }
    ) { padding ->
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text("جاري تحميل بيانات الحجز...")
      }
    }
    return
  }

  val context = LocalContext.current
  val match = matchWrap.match
  val statusColors = LocalMalabnaColors.current

  var selectedSectionTab by remember { mutableIntStateOf(0) }
  var participantFilter by remember { mutableStateOf(ParticipantListFilter.ALL) }
  var showEditMatchSheet by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showAddPlayersSheet by remember { mutableStateOf(false) }
  var showSmsReminderSheet by remember { mutableStateOf(false) }
  var showTeamRatingSheet by remember { mutableStateOf(false) }
  var paymentTarget by remember { mutableStateOf<MatchParticipantDetail?>(null) }

  val notificationPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        BookingReminderHelper.showBookingNotification(
          context = context,
          matchId = match.id,
          title = match.title,
          pitchName = match.pitchName,
          timeLabel = match.timeLabel,
          dateMillis = match.dateMillis,
        )
        Toast.makeText(context, "تم تفعيل الإشعارات وإرسال تنبيه تجريبي", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, "برجاء السماح بالإشعارات من إعدادات الجهاز", Toast.LENGTH_SHORT).show()
      }
    }

  val filteredParticipants =
    remember(matchWrap.participants, participantFilter) {
      when (participantFilter) {
        ParticipantListFilter.ALL -> matchWrap.participants
        ParticipantListFilter.GOING ->
          matchWrap.participants.filter { it.participant.attendanceStatus == AttendanceStatus.GOING }
        ParticipantListFilter.PENDING ->
          matchWrap.participants.filter { it.participant.attendanceStatus == AttendanceStatus.PENDING }
        ParticipantListFilter.NOT_GOING ->
          matchWrap.participants.filter { it.participant.attendanceStatus == AttendanceStatus.NOT_GOING }
        ParticipantListFilter.PAID ->
          matchWrap.participants.filter { it.participant.paymentStatus == PaymentStatus.PAID }
        ParticipantListFilter.UNPAID ->
          matchWrap.participants.filter {
            it.participant.paymentStatus == PaymentStatus.UNPAID &&
              it.participant.attendanceStatus != AttendanceStatus.NOT_GOING
          }
        ParticipantListFilter.EXEMPT ->
          matchWrap.participants.filter { it.participant.paymentStatus == PaymentStatus.EXEMPT }
      }
    }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = match.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
            Icon(
              Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = stringResource(R.string.back_button_desc),
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              val shareText = buildMatchShareText(matchWrap)
              val intent =
                Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, shareText)
                }
              context.startActivity(Intent.createChooser(intent, "مشاركة ملخص الحجز"))
            },
            modifier = Modifier.testTag("share_match_button"),
          ) {
            Icon(
              Icons.Rounded.Share,
              contentDescription = stringResource(R.string.share_summary_desc),
            )
          }
          IconButton(onClick = { showEditMatchSheet = true }) {
            Icon(Icons.Rounded.Edit, contentDescription = "تعديل الحجز")
          }
          IconButton(onClick = { showDeleteDialog = true }) {
            Icon(
              Icons.Rounded.Delete,
              contentDescription = "حذف الحجز",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
          ),
      )
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Match Header Summary Card
        item(key = "header_summary") {
          Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.LocationOn,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp),
                    )
                    Text(
                      text =
                        if (match.location.isNotBlank()) "${match.pitchName} • ${match.location}"
                        else match.pitchName,
                      style = MaterialTheme.typography.titleMedium,
                    )
                  }
                  Text(
                    text = "${formatArabicDate(match.dateMillis)} • ${match.timeLabel} (${match.format.labelAr})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                FilterChip(
                  selected = match.isCompleted,
                  onClick = { onToggleCompleted(match.id, !match.isCompleted) },
                  label = { Text(if (match.isCompleted) "مكتمل" else "قادم") },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Rounded.TaskAlt,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                  },
                )
              }

              // Attendance & Financial KPI Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                MetricTile(
                  label = "جايين",
                  value = "${matchWrap.goingCount}/${match.targetPlayers}",
                  fg = statusColors.goingText,
                  bg = statusColors.goingBg.copy(alpha = 0.7f),
                  modifier = Modifier.weight(1f),
                )
                MetricTile(
                  label = "سعر الملعب",
                  value = formatCurrency(match.totalCost),
                  fg = MaterialTheme.colorScheme.onSurface,
                  bg = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.weight(1f),
                )
                MetricTile(
                  label = "حصة الفرد",
                  value = formatCurrency(matchWrap.perPlayerShare),
                  fg = MaterialTheme.colorScheme.primary,
                  bg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                  modifier = Modifier.weight(1f),
                )
              }

              if (match.totalCost > 0.0) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(
                      text = "المحصّل: ${formatCurrency(matchWrap.totalCollected)}",
                      style = MaterialTheme.typography.labelLarge,
                      color = statusColors.goingText,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = "المتبقي: ${formatCurrency(matchWrap.remainingAmount)}",
                      style = MaterialTheme.typography.labelLarge,
                      color =
                        if (matchWrap.remainingAmount > 0.0) statusColors.pendingText
                        else statusColors.goingText,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                  LinearProgressIndicator(
                    progress = { matchWrap.collectionProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                  )
                }
              }

              if (match.notes.isNotBlank()) {
                Text(
                  text = "📝 ${match.notes}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              if (matchWrap.participants.isNotEmpty()) {
                val ratedDetails = matchWrap.participants.filter { it.participant.rating != null }
                val ratedCount = ratedDetails.size
                val matchStar = ratedDetails.maxByOrNull { it.participant.rating ?: 0 }

                if (matchStar != null) {
                  Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp),
                      )
                      Text(
                        text =
                          "نجم الماتش: ${matchStar.player.name} (${matchStar.participant.rating}/10)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                }

                FilledTonalButton(
                  onClick = { showTeamRatingSheet = true },
                  modifier =
                    Modifier.fillMaxWidth().height(48.dp).testTag("open_team_rating_button"),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text =
                      if (ratedCount > 0) "تعديل تقييم الفريق ($ratedCount/${matchWrap.participants.size})"
                      else "تقييم الفريق بعد الماتش (القائد)",
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }

        // Section Tabs: Roster/Payments | Interactive Pitch | Reminders & Alerts
        item(key = "section_tabs") {
          SecondaryTabRow(
            selectedTabIndex = selectedSectionTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(MaterialTheme.shapes.medium),
          ) {
            Tab(
              selected = selectedSectionTab == 0,
              onClick = { selectedSectionTab = 0 },
              text = { Text("الحضور والدفع", maxLines = 1) },
              icon = { Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) },
              modifier = Modifier.testTag("detail_tab_roster"),
            )
            Tab(
              selected = selectedSectionTab == 1,
              onClick = { selectedSectionTab = 1 },
              text = { Text("تشكيلة الملعب", maxLines = 1) },
              icon = { Icon(Icons.Rounded.Stadium, contentDescription = null, modifier = Modifier.size(18.dp)) },
              modifier = Modifier.testTag("detail_tab_pitch"),
            )
            Tab(
              selected = selectedSectionTab == 2,
              onClick = { selectedSectionTab = 2 },
              text = { Text("التنبيهات", maxLines = 1) },
              icon = {
                Icon(
                  Icons.Rounded.NotificationsActive,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
              },
              modifier = Modifier.testTag("detail_tab_alerts"),
            )
          }
        }

        when (selectedSectionTab) {
          0 -> {
            item(key = "roster_actions") {
              var inlinePlayerName by remember { mutableStateOf("") }
              val squadRemainingCount =
                remember(allSquadPlayers, matchWrap.participants) {
                  val currentIds = matchWrap.participants.map { it.player.id }.toSet()
                  allSquadPlayers.count { it.id !in currentIds }
                }

              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Quick Inline Add Player Bar (1-line frictionless entry)
                Surface(
                  shape = MaterialTheme.shapes.large,
                  color = MaterialTheme.colorScheme.surface,
                  tonalElevation = 1.dp,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    OutlinedTextField(
                      value = inlinePlayerName,
                      onValueChange = { inlinePlayerName = it },
                      placeholder = { Text("اكتب اسم لاعب لإضافته فوراً...") },
                      singleLine = true,
                      modifier = Modifier.weight(1f).testTag("inline_add_player_input"),
                      shape = MaterialTheme.shapes.medium,
                    )
                    Button(
                      onClick = {
                        onAddNewPlayerToMatch(match.id, inlinePlayerName, "", "لاعب")
                        inlinePlayerName = ""
                      },
                      enabled = inlinePlayerName.isNotBlank(),
                      modifier = Modifier.height(52.dp).testTag("inline_add_player_button"),
                      shape = MaterialTheme.shapes.medium,
                    ) {
                      Icon(Icons.Rounded.Add, contentDescription = "إضافة لاعب")
                    }
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "اللاعبين (${matchWrap.participants.size})",
                    style = MaterialTheme.typography.titleMedium,
                  )
                  FilledTonalButton(
                    onClick = { showAddPlayersSheet = true },
                    modifier = Modifier.testTag("add_players_to_match_button"),
                  ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      if (squadRemainingCount > 0) "من الشلة ($squadRemainingCount)"
                      else "إضافة بتفاصيل",
                    )
                  }
                }

                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  ParticipantListFilter.entries.forEach { filter ->
                    FilterChip(
                      selected = participantFilter == filter,
                      onClick = { participantFilter = filter },
                      label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                      colors =
                        FilterChipDefaults.filterChipColors(
                          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                  }
                }
              }
            }

            if (filteredParticipants.isEmpty()) {
              item(key = "empty_participants") {
                Surface(
                  shape = MaterialTheme.shapes.large,
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                ) {
                  Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.SportsSoccer,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(38.dp),
                    )
                    Text(
                      text =
                        if (matchWrap.participants.isEmpty()) "لم تتم إضافة لاعبين لهذا الحجز بعد"
                        else "لا يوجد لاعبين مطابقين لهذا التصنيف",
                      style = MaterialTheme.typography.titleMedium,
                    )
                    if (matchWrap.participants.isEmpty()) {
                      FilledTonalButton(onClick = { showAddPlayersSheet = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة لاعبين من الشلة أو لاعب جديد")
                      }
                    }
                  }
                }
              }
            } else {
              items(filteredParticipants, key = { it.participant.id }) { detail ->
                MatchParticipantCard(
                  detail = detail,
                  onAttendanceChange = { newStatus ->
                    onUpdateAttendance(detail.participant, newStatus)
                  },
                  onQuickPayCash = {
                    onRecordPayment(
                      detail.participant,
                      PaymentStatus.PAID,
                      PaymentMethod.CASH,
                      detail.effectiveShare,
                      null,
                      "",
                    )
                  },
                  onManagePayment = { paymentTarget = detail },
                  onRemove = { onRemovePlayerFromMatch(match.id, detail.player.id) },
                )
              }
            }
          }

          1 -> {
            item(key = "interactive_pitch_board") {
              InteractivePitchBoard(
                matchWrap = matchWrap,
                onAssignPlayerToSlot = { participant, slotIndex ->
                  onAssignPlayerToPitchSlot(match.id, participant, slotIndex)
                },
                onAutoAssign = { totalSlots ->
                  onAutoAssignPitchSlots(
                    match.id,
                    matchWrap.participants.map { it.participant },
                    totalSlots,
                  )
                },
                onUpdateAttendance = onUpdateAttendance,
                onOpenPaymentSheet = { detail -> paymentTarget = detail },
                onOpenAddPlayers = { showAddPlayersSheet = true },
              )
            }
          }

          2 -> {
            item(key = "notifications_and_sms_panel") {
              RemindersAndAlertsSection(
                matchWrap = matchWrap,
                onSelectReminderMinutes = { mins ->
                  if (mins != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !BookingReminderHelper.hasNotificationPermission(context)
                  ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  }
                  onUpdateReminder(match, mins)
                },
                onSendTestNotification = {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !BookingReminderHelper.hasNotificationPermission(context)
                  ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  } else {
                    BookingReminderHelper.showBookingNotification(
                      context = context,
                      matchId = match.id,
                      title = match.title,
                      pitchName = match.pitchName,
                      timeLabel = match.timeLabel,
                      dateMillis = match.dateMillis,
                    )
                    Toast.makeText(context, "تم إرسال إشعار التذكير الآن", Toast.LENGTH_SHORT).show()
                  }
                },
                onOpenSmsSheet = { showSmsReminderSheet = true },
                onShareSummary = {
                  val intent =
                    Intent(Intent.ACTION_SEND).apply {
                      type = "text/plain"
                      putExtra(Intent.EXTRA_TEXT, buildMatchShareText(matchWrap))
                    }
                  context.startActivity(Intent.createChooser(intent, "مشاركة ملخص الحجز"))
                },
              )
            }
          }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
      }
    }
  }

  val activePaymentDetail = paymentTarget
  if (activePaymentDetail != null) {
    RecordPaymentBottomSheet(
      playerName = activePaymentDetail.player.name,
      matchTitle = match.title,
      participant = activePaymentDetail.participant,
      suggestedShare = activePaymentDetail.effectiveShare,
      onDismiss = { paymentTarget = null },
      onSavePayment = { status, method, amountPaid, customAmount, note ->
        onRecordPayment(
          activePaymentDetail.participant,
          status,
          method,
          amountPaid,
          customAmount,
          note,
        )
        paymentTarget = null
      },
    )
  }

  if (showEditMatchSheet) {
    MatchEditorBottomSheet(
      existingMatch = match,
      onDismiss = { showEditMatchSheet = false },
      onSave = { title, pitch, loc, date, time, format, target, cost, notes, _ ->
        onUpdateMatch(
          match.copy(
            title = title.trim(),
            pitchName = pitch.trim(),
            location = loc.trim(),
            dateMillis = date,
            timeLabel = time.trim(),
            format = format,
            targetPlayers = target,
            totalCost = cost,
            notes = notes.trim(),
          )
        )
        showEditMatchSheet = false
      },
    )
  }

  if (showAddPlayersSheet) {
    AddPlayersToMatchBottomSheet(
      alreadyInMatchIds = matchWrap.participants.map { it.player.id }.toSet(),
      allSquadPlayers = allSquadPlayers,
      onDismiss = { showAddPlayersSheet = false },
      onAddExisting = { selectedIds ->
        onAddPlayersToMatch(match.id, selectedIds)
        showAddPlayersSheet = false
      },
      onAddNewPlayer = { name, phone, position ->
        onAddNewPlayerToMatch(match.id, name, phone, position)
      },
    )
  }

  if (showSmsReminderSheet) {
    SmsReminderBottomSheet(
      matchWrap = matchWrap,
      onDismiss = { showSmsReminderSheet = false },
      onSendSms = { phones ->
        val smsIntent = BookingReminderHelper.buildSmsIntent(phones, match)
        try {
          context.startActivity(smsIntent)
          showSmsReminderSheet = false
        } catch (_: Exception) {
          Toast.makeText(context, "لا يوجد تطبيق رسائل SMS متاح على هذا الجهاز", Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("حذف الحجز") },
      text = { Text("هل أنت متأكد من حذف حجز \"${match.title}\" وجميع بيانات الحضور والمدفوعات الخاصة به؟") },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteDialog = false
            onDeleteMatch(match.id)
          }
        ) {
          Text("حذف", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء") }
      },
    )
  }

  if (showTeamRatingSheet) {
    TeamRatingBottomSheet(
      matchWrap = matchWrap,
      onDismiss = { showTeamRatingSheet = false },
      onSaveRatings = { ratings, markCompleted ->
        onSaveTeamRatings(match.id, ratings, markCompleted)
        showTeamRatingSheet = false
        Toast.makeText(context, "تم حفظ تقييمات الفريق بنجاح", Toast.LENGTH_SHORT).show()
      },
    )
  }
}

@Composable
private fun MetricTile(
  label: String,
  value: String,
  fg: androidx.compose.ui.graphics.Color,
  bg: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = bg,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.85f))
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun MatchParticipantCard(
  detail: MatchParticipantDetail,
  onAttendanceChange: (AttendanceStatus) -> Unit,
  onQuickPayCash: () -> Unit,
  onManagePayment: () -> Unit,
  onRemove: () -> Unit,
) {
  val p = detail.participant
  val player = detail.player
  val statusColors = LocalMalabnaColors.current

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("participant_card_${p.id}"),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        PlayerAvatar(
          name = player.name,
          colorIndex = player.avatarColorIndex,
          size = 44.dp,
          photoUri = player.photoUri,
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = player.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          val slotBadge =
            if (p.pitchSlotIndex != null) " • مسكن بالملعب" else ""
          Text(
            text = "${player.position}$slotBadge",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (p.rating != null) {
          StatusPill(
            label = "${p.rating}/10",
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            bgColor = MaterialTheme.colorScheme.secondaryContainer,
            icon = Icons.Rounded.Star,
          )
        }
        IconButton(onClick = onRemove) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "إزالة ${player.name} من الحجز",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      // 1-Tap Attendance Segmented Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        listOf(
          Triple(AttendanceStatus.GOING, Icons.Rounded.CheckCircle, statusColors.goingText to statusColors.goingBg),
          Triple(AttendanceStatus.PENDING, Icons.Rounded.Schedule, statusColors.pendingText to statusColors.pendingBg),
          Triple(AttendanceStatus.NOT_GOING, Icons.Rounded.Close, statusColors.notGoingText to statusColors.notGoingBg),
        ).forEach { (status, icon, pair) ->
          val (fg, bg) = pair
          val selected = p.attendanceStatus == status
          Surface(
            modifier =
              Modifier.weight(1f)
                .height(44.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { onAttendanceChange(status) }
                .testTag("attendance_${p.id}_${status.name.lowercase()}"),
            shape = MaterialTheme.shapes.small,
            color = if (selected) bg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border =
              BorderStroke(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) fg else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
              ),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = status.labelAr,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              )
            }
          }
        }
      }

      // 1-Tap Quick Payment Controls
      if (p.paymentStatus == PaymentStatus.UNPAID) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = onQuickPayCash,
            modifier = Modifier.weight(1.2f).height(44.dp).testTag("quick_pay_cash_${p.id}"),
            shape = MaterialTheme.shapes.medium,
          ) {
            Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text =
                if (detail.effectiveShare > 0.0) "دفع كاش (${formatCurrency(detail.effectiveShare)})"
                else "دفع كاش",
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          OutlinedButton(
            onClick = onManagePayment,
            modifier = Modifier.weight(1f).height(44.dp).testTag("payment_strip_${p.id}"),
            shape = MaterialTheme.shapes.medium,
          ) {
            Text(
              text = "محفظة / بطاقة / إعفاء",
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      } else {
        Surface(
          modifier =
            Modifier.fillMaxWidth()
              .clip(MaterialTheme.shapes.medium)
              .clickable(onClick = onManagePayment)
              .testTag("payment_strip_${p.id}"),
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (p.paymentStatus == PaymentStatus.PAID) {
              val method = p.paymentMethod ?: PaymentMethod.CASH
              StatusPill(
                label = "دفع • ${method.shortLabelAr} • ${formatCurrency(p.amountPaid)}",
                textColor = statusColors.goingText,
                bgColor = statusColors.goingBg,
                icon = paymentMethodIcon(method),
              )
            } else {
              StatusPill(
                label = "مش هيدفع (معفي)",
                textColor = statusColors.exemptText,
                bgColor = statusColors.exemptBg,
              )
            }
            Text(
              text = "تغيير / تعديل",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RemindersAndAlertsSection(
  matchWrap: MatchWithParticipants,
  onSelectReminderMinutes: (Int?) -> Unit,
  onSendTestNotification: () -> Unit,
  onOpenSmsSheet: () -> Unit,
  onShareSummary: () -> Unit,
) {
  val match = matchWrap.match
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Surface(
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.NotificationsActive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "إشعارات التذكير التلقائية",
            style = MaterialTheme.typography.titleMedium,
          )
        }
        Text(
          text = "اختر موعد إرسال إشعار الهاتف لتذكيرك بموعد الماتش ومراجعة تأكيدات الحضور:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf(
            60 to "قبل بساعة",
            120 to "قبل بساعتين",
            1440 to "قبل بيوم",
            null to "إيقاف التذكير",
          ).forEach { (mins, label) ->
            FilterChip(
              selected = match.reminderMinutesBefore == mins,
              onClick = { onSelectReminderMinutes(mins) },
              label = { Text(label) },
            )
          }
        }
        FilledTonalButton(
          onClick = onSendTestNotification,
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("send_test_notification_button"),
        ) {
          Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("إرسال إشعار تذكير الآن")
        }
      }
    }

    Surface(
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Sms,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
          )
          Text(
            text = "تذكير اللاعبين برسائل SMS أو واتساب",
            style = MaterialTheme.typography.titleMedium,
          )
        }
        Text(
          text = "أرسل رسالة تذكير جاهزة للاعبين المسجلين (خاصة اللي لسه مردوش) لتأكيد الحضور قبل الماتش.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onOpenSmsSheet,
            modifier = Modifier.weight(1f).height(48.dp).testTag("open_sms_sheet_button"),
          ) {
            Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("رسالة SMS")
          }
          OutlinedButton(
            onClick = onShareSummary,
            modifier = Modifier.weight(1f).height(48.dp),
          ) {
            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("مشاركة القائمة")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlayersToMatchBottomSheet(
  alreadyInMatchIds: Set<Long>,
  allSquadPlayers: List<PlayerEntity>,
  onDismiss: () -> Unit,
  onAddExisting: (List<Long>) -> Unit,
  onAddNewPlayer: (name: String, phone: String, position: String) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val available = remember(allSquadPlayers, alreadyInMatchIds) {
    allSquadPlayers.filter { it.id !in alreadyInMatchIds }
  }
  var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
  var newName by remember { mutableStateOf("") }
  var newPhone by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "إضافة لاعبين للحجز",
        style = MaterialTheme.typography.headlineSmall,
      )

      // Quick Add New Player Inline
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text("إضافة لاعب جديد سريعاً (ويُحفظ في الشلة)", style = MaterialTheme.typography.titleSmall)
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("اسم اللاعب") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("quick_add_player_name"),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = newPhone,
              onValueChange = { newPhone = it },
              label = { Text("الموبايل (اختياري)") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            Button(
              onClick = {
                onAddNewPlayer(newName, newPhone, "لاعب")
                newName = ""
                newPhone = ""
              },
              enabled = newName.isNotBlank(),
              modifier = Modifier.height(54.dp).testTag("quick_add_player_confirm"),
            ) {
              Text("إضافة")
            }
          }
        }
      }

      if (available.isNotEmpty()) {
        HorizontalDivider()
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("اختر من الشلة (${available.size})", style = MaterialTheme.typography.titleSmall)
          TextButton(
            onClick = {
              selectedIds =
                if (selectedIds.size == available.size) emptySet()
                else available.map { it.id }.toSet()
            }
          ) {
            Text(if (selectedIds.size == available.size) "إلغاء الكل" else "تحديد الكل")
          }
        }

        LazyColumn(
          modifier = Modifier.fillMaxWidth().height(210.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          items(available, key = { it.id }) { player ->
            val checked = player.id in selectedIds
            Surface(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(MaterialTheme.shapes.medium)
                  .clickable {
                    selectedIds = if (checked) selectedIds - player.id else selectedIds + player.id
                  },
              shape = MaterialTheme.shapes.medium,
              color =
                if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Checkbox(
                  checked = checked,
                  onCheckedChange = {
                    selectedIds = if (checked) selectedIds - player.id else selectedIds + player.id
                  },
                )
                PlayerAvatar(
                  name = player.name,
                  colorIndex = player.avatarColorIndex,
                  size = 34.dp,
                  photoUri = player.photoUri,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = player.name, style = MaterialTheme.typography.bodyLarge)
              }
            }
          }
        }

        Button(
          onClick = { onAddExisting(selectedIds.toList()) },
          enabled = selectedIds.isNotEmpty(),
          modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
          Text("إضافة المحددين (${selectedIds.size})")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmsReminderBottomSheet(
  matchWrap: MatchWithParticipants,
  onDismiss: () -> Unit,
  onSendSms: (List<String>) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val playersWithPhones =
    remember(matchWrap.participants) {
      matchWrap.participants.filter { it.player.phone.isNotBlank() }
    }
  var selectedPhones by remember(playersWithPhones) {
    mutableStateOf(
      playersWithPhones
        .filter { it.participant.attendanceStatus != AttendanceStatus.NOT_GOING }
        .map { it.player.phone }
        .toSet()
    )
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "إرسال رسالة SMS لتأكيد الحضور",
        style = MaterialTheme.typography.headlineSmall,
      )
      if (playersWithPhones.isEmpty()) {
        Text(
          text = "لا يوجد أرقام هواتف مسجلة للاعبين في هذا الحجز. يمكنك إضافة أرقامهم من تبويب \"الشلة\" أو فتح تطبيق الرسائل مباشرة.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
          onClick = { onSendSms(emptyList()) },
          modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
          Text("فتح تطبيق الرسائل بالرسالة الجاهزة")
        }
      } else {
        Text(
          text = "اختر اللاعبين لإرسال رسالة التذكير:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
          modifier = Modifier.fillMaxWidth().height(220.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          items(playersWithPhones, key = { it.player.id }) { detail ->
            val phone = detail.player.phone
            val checked = phone in selectedPhones
            Surface(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(MaterialTheme.shapes.medium)
                  .clickable {
                    selectedPhones = if (checked) selectedPhones - phone else selectedPhones + phone
                  },
              shape = MaterialTheme.shapes.medium,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Checkbox(
                  checked = checked,
                  onCheckedChange = {
                    selectedPhones = if (checked) selectedPhones - phone else selectedPhones + phone
                  },
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(detail.player.name, style = MaterialTheme.typography.titleSmall)
                  Text(
                    text = "$phone • ${detail.participant.attendanceStatus.labelAr}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
        Button(
          onClick = { onSendSms(selectedPhones.toList()) },
          enabled = selectedPhones.isNotEmpty(),
          modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
          Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("إرسال SMS (${selectedPhones.size})")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TeamRatingBottomSheet(
  matchWrap: MatchWithParticipants,
  onDismiss: () -> Unit,
  onSaveRatings: (List<Triple<MatchParticipantEntity, Int?, String>>, Boolean) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val match = matchWrap.match

  val ratingMap = remember(match.id, matchWrap.participants) {
    mutableStateMapOf<Long, Int?>().apply {
      matchWrap.participants.forEach { put(it.participant.id, it.participant.rating) }
    }
  }
  val noteMap = remember(match.id, matchWrap.participants) {
    mutableStateMapOf<Long, String>().apply {
      matchWrap.participants.forEach { put(it.participant.id, it.participant.ratingNote) }
    }
  }

  var showAllPlayers by remember(match.id) {
    mutableStateOf(
      matchWrap.participants.none { it.participant.attendanceStatus == AttendanceStatus.GOING }
    )
  }
  var markCompleted by remember(match.id, match.isCompleted) { mutableStateOf(true) }
  var expandedNoteId by remember { mutableStateOf<Long?>(null) }

  val visibleParticipants =
    remember(matchWrap.participants, showAllPlayers) {
      if (showAllPlayers) {
        matchWrap.participants
      } else {
        matchWrap.participants.filter {
          it.participant.attendanceStatus == AttendanceStatus.GOING || it.participant.rating != null
        }
      }
    }

  val topRatedDetail =
    matchWrap.participants
      .filter { (ratingMap[it.participant.id] ?: 0) > 0 }
      .maxByOrNull { ratingMap[it.participant.id] ?: 0 }
  val ratedCount = matchWrap.participants.count { ratingMap[it.participant.id] != null }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "تقييم قائد الفريق بعد الماتش",
            style = MaterialTheme.typography.headlineSmall,
          )
          Text(
            text = "قيّم أداء كل لاعب من 1 إلى 10 عشان يظهر نجم الشهر",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (matchWrap.participants.any { it.participant.attendanceStatus != AttendanceStatus.GOING }) {
          FilterChip(
            selected = showAllPlayers,
            onClick = { showAllPlayers = !showAllPlayers },
            label = { Text(if (showAllPlayers) "الكل" else "الحاضرون") },
          )
        }
      }

      if (topRatedDetail != null) {
        val topScore = ratingMap[topRatedDetail.participant.id] ?: 0
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.EmojiEvents,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
              text = "الأعلى تقييماً في الماتش: ${topRatedDetail.player.name} ($topScore/10)",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(visibleParticipants, key = { it.participant.id }) { detail ->
          val pid = detail.participant.id
          val currentRating = ratingMap[pid]
          val currentNote = noteMap[pid] ?: ""
          val noteOpen = expandedNoteId == pid || currentNote.isNotBlank()

          Surface(
            modifier = Modifier.fillMaxWidth().testTag("rating_player_row_$pid"),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            border =
              BorderStroke(
                width = 1.dp,
                color =
                  if (currentRating != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
              ),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                PlayerAvatar(
                  name = detail.player.name,
                  colorIndex = detail.player.avatarColorIndex,
                  size = 38.dp,
                  photoUri = detail.player.photoUri,
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = detail.player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = detail.player.position,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                // Stepper (- / score / +) with accessible 48dp touch targets
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  IconButton(
                    onClick = {
                      ratingMap[pid] =
                        when {
                          currentRating == null -> 5
                          currentRating <= 1 -> null
                          else -> currentRating - 1
                        }
                    },
                    modifier = Modifier.size(48.dp).testTag("rating_minus_$pid"),
                  ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "تقليل التقييم")
                  }
                  Surface(
                    shape = CircleShape,
                    color =
                      if (currentRating != null) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.surfaceVariant,
                  ) {
                    Text(
                      text = if (currentRating != null) "$currentRating/10" else "--",
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color =
                        if (currentRating != null) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                  }
                  IconButton(
                    onClick = {
                      ratingMap[pid] =
                        when {
                          currentRating == null -> 7
                          currentRating < 10 -> currentRating + 1
                          else -> 10
                        }
                    },
                    modifier = Modifier.size(48.dp).testTag("rating_plus_$pid"),
                  ) {
                    Icon(Icons.Rounded.Add, contentDescription = "زيادة التقييم")
                  }
                }
              }

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                listOf(6, 7, 8, 9, 10).forEach { score ->
                  val selected = currentRating == score
                  FilterChip(
                    selected = selected,
                    onClick = { ratingMap[pid] = if (selected) null else score },
                    label = { Text("$score") },
                    modifier = Modifier.testTag("rating_chip_${pid}_$score"),
                  )
                }
                FilterChip(
                  selected = noteOpen,
                  onClick = {
                    expandedNoteId = if (expandedNoteId == pid) null else pid
                  },
                  label = { Text(if (currentNote.isBlank()) "+ ملاحظة" else "تعديل الملاحظة") },
                )
              }

              if (noteOpen) {
                OutlinedTextField(
                  value = currentNote,
                  onValueChange = { noteMap[pid] = it },
                  label = { Text("ملاحظة القائد (اختياري)") },
                  placeholder = { Text("مثال: هاتريك / أفضل مدافع") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "تحديد الحجز كمكتمل",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
        )
        Switch(
          checked = markCompleted,
          onCheckedChange = { markCompleted = it },
          modifier = Modifier.testTag("rating_mark_completed_switch"),
        )
      }

      Button(
        onClick = {
          val payload =
            matchWrap.participants.map { detail ->
              val pid = detail.participant.id
              Triple(detail.participant, ratingMap[pid], noteMap[pid] ?: "")
            }
          onSaveRatings(payload, markCompleted)
        },
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_team_ratings_button"),
      ) {
        Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("حفظ التقييمات ($ratedCount)")
      }
    }
  }
}
