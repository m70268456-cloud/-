package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.MatchEntity
import com.example.data.local.PlayerEntity
import com.example.data.model.PitchFormat
import com.example.ui.util.formatArabicDate
import com.example.ui.util.formatCurrency
import com.example.ui.util.formatNumber
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MatchEditorBottomSheet(
  existingMatch: MatchEntity? = null,
  availablePlayers: List<PlayerEntity> = emptyList(),
  onDismiss: () -> Unit,
  onSave: (
    title: String,
    pitchName: String,
    location: String,
    dateMillis: Long,
    timeLabel: String,
    format: PitchFormat,
    targetPlayers: Int,
    totalCost: Double,
    notes: String,
    selectedPlayerIds: List<Long>,
  ) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val todayMillis = remember { System.currentTimeMillis() }
  val tomorrowMillis = remember { todayMillis + 86_400_000L }
  val fridayMillis = remember {
    val cal = Calendar.getInstance()
    val daysUntilFriday = (Calendar.FRIDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
    cal.add(Calendar.DAY_OF_YEAR, if (daysUntilFriday == 0) 7 else daysUntilFriday)
    cal.timeInMillis
  }

  var pitchName by remember { mutableStateOf(existingMatch?.pitchName ?: "") }
  var title by remember { mutableStateOf(existingMatch?.title ?: "") }
  var location by remember { mutableStateOf(existingMatch?.location ?: "") }
  var dateMillis by remember { mutableStateOf(existingMatch?.dateMillis ?: todayMillis) }
  var selectedDateTag by remember { mutableStateOf(if (existingMatch == null) 0 else 3) }
  var timeLabel by remember { mutableStateOf(existingMatch?.timeLabel ?: "9:00 م - 10:00 م") }
  var selectedFormat by remember {
    mutableStateOf(existingMatch?.format ?: PitchFormat.FIVE_A_SIDE)
  }
  var targetPlayersText by remember {
    mutableStateOf(
      (existingMatch?.targetPlayers ?: PitchFormat.FIVE_A_SIDE.defaultTargetPlayers).toString()
    )
  }
  var totalCostText by remember {
    mutableStateOf(
      if (existingMatch != null && existingMatch.totalCost > 0.0) {
        formatNumber(existingMatch.totalCost).replace(",", "")
      } else {
        ""
      }
    )
  }
  var notes by remember { mutableStateOf(existingMatch?.notes ?: "") }
  var showMoreOptions by remember {
    mutableStateOf(existingMatch != null && (existingMatch.location.isNotBlank() || existingMatch.notes.isNotBlank()))
  }
  var selectedPlayerIds by remember {
    mutableStateOf(if (existingMatch == null) availablePlayers.map { it.id }.toSet() else emptySet())
  }
  var showDatePicker by remember { mutableStateOf(false) }

  val targetCount = targetPlayersText.toIntOrNull()?.coerceAtLeast(2) ?: 10
  val totalCost = totalCostText.toDoubleOrNull() ?: 0.0
  val expectedShare = if (totalCost > 0.0) totalCost / targetCount else 0.0

  // Allow saving as long as either pitchName OR title is entered!
  val effectivePitch = pitchName.trim().ifBlank { title.trim() }
  val effectiveTitle = title.trim().ifBlank { "حجز $effectivePitch" }
  val canSave = effectivePitch.isNotBlank()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = if (existingMatch == null) "حجز كورة سريع ⚽" else "تعديل بيانات الحجز",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )

      OutlinedTextField(
        value = pitchName,
        onValueChange = { pitchName = it },
        label = { Text("اسم الملعب *") },
        placeholder = { Text("مثلاً: ملعب النيل، ملاعب الواحة") },
        leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("match_pitch_input"),
        shape = MaterialTheme.shapes.medium,
      )

      // Quick Date Chips (No need to open calendar unless user wants a custom date)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "اليوم والساعة", style = MaterialTheme.typography.titleSmall)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          FilterChip(
            selected = selectedDateTag == 0,
            onClick = {
              selectedDateTag = 0
              dateMillis = todayMillis
            },
            label = { Text("النهارده") },
          )
          FilterChip(
            selected = selectedDateTag == 1,
            onClick = {
              selectedDateTag = 1
              dateMillis = tomorrowMillis
            },
            label = { Text("بكرة") },
          )
          FilterChip(
            selected = selectedDateTag == 2,
            onClick = {
              selectedDateTag = 2
              dateMillis = fridayMillis
            },
            label = { Text("الجمعة") },
          )
          FilterChip(
            selected = selectedDateTag == 3,
            onClick = { showDatePicker = true },
            label = { Text(if (selectedDateTag == 3) formatArabicDate(dateMillis) else "تاريخ آخر") },
            leadingIcon = {
              Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            modifier = Modifier.testTag("match_date_button"),
          )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("8:00 م", "9:00 م", "10:00 م", "11:00 م").forEach { preset ->
            val range = "$preset - ${preset.substringBefore(":").toIntOrNull()?.plus(1) ?: 10}:00 م"
            FilterChip(
              selected = timeLabel.startsWith(preset),
              onClick = { timeLabel = range },
              label = { Text(preset) },
              leadingIcon = {
                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
              },
            )
          }
        }
      }

      // Format chips
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "نوع الحجز", style = MaterialTheme.typography.titleSmall)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PitchFormat.entries.forEach { format ->
            FilterChip(
              selected = selectedFormat == format,
              onClick = {
                selectedFormat = format
                targetPlayersText = format.defaultTargetPlayers.toString()
              },
              label = { Text(format.labelAr) },
            )
          }
        }
      }

      // Quick Cost Input + Presets
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = totalCostText,
            onValueChange = { input ->
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) totalCostText = input
            },
            label = { Text("سعر الملعب (ج.م)") },
            placeholder = { Text("1000") },
            leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1.2f).testTag("match_cost_input"),
            shape = MaterialTheme.shapes.medium,
          )
          OutlinedTextField(
            value = targetPlayersText,
            onValueChange = { input ->
              if (input.isEmpty() || input.all { it.isDigit() }) targetPlayersText = input
            },
            label = { Text("العدد") },
            leadingIcon = { Icon(Icons.Rounded.Groups, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(0.8f).testTag("match_target_players_input"),
            shape = MaterialTheme.shapes.medium,
          )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("500", "700", "800", "1000", "1200").forEach { costPreset ->
            FilterChip(
              selected = totalCostText == costPreset,
              onClick = { totalCostText = costPreset },
              label = { Text("$costPreset ج.م", style = MaterialTheme.typography.labelSmall) },
            )
          }
        }
      }

      if (expectedShare > 0.0) {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "حصة الفرد المتوقعة ($targetCount لاعب)",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
              text = formatCurrency(expectedShare),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }

      if (existingMatch == null && availablePlayers.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "إضافة الشلة للحجز (${selectedPlayerIds.size}/${availablePlayers.size})",
              style = MaterialTheme.typography.titleSmall,
            )
            TextButton(
              onClick = {
                selectedPlayerIds =
                  if (selectedPlayerIds.size == availablePlayers.size) emptySet()
                  else availablePlayers.map { it.id }.toSet()
              }
            ) {
              Text(if (selectedPlayerIds.size == availablePlayers.size) "إلغاء الكل" else "تحديد الكل")
            }
          }
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            availablePlayers.forEach { player ->
              val selected = player.id in selectedPlayerIds
              FilterChip(
                selected = selected,
                onClick = {
                  selectedPlayerIds =
                    if (selected) selectedPlayerIds - player.id else selectedPlayerIds + player.id
                },
                label = { Text(player.name) },
                leadingIcon = {
                  if (selected) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                  } else {
                    PlayerAvatar(
                      name = player.name,
                      colorIndex = player.avatarColorIndex,
                      size = 20.dp,
                      photoUri = player.photoUri,
                    )
                  }
                },
              )
            }
          }
        }
      }

      // Collapsible optional details so the form stays clean and fast
      TextButton(
        onClick = { showMoreOptions = !showMoreOptions },
        modifier = Modifier.align(Alignment.Start),
      ) {
        Icon(
          imageVector = if (showMoreOptions) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
          contentDescription = null,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (showMoreOptions) "إخفاء التفاصيل الإضافية" else "إضافة اسم مخصص أو عنوان وملاحظات")
      }

      AnimatedVisibility(visible = showMoreOptions) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("اسم مخصص للحجز (اختياري)") },
            placeholder = { Text("مثلاً: ماتش الخميس") },
            leadingIcon = { Icon(Icons.Rounded.SportsSoccer, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("match_title_input"),
            shape = MaterialTheme.shapes.medium,
          )
          OutlinedTextField(
            value = timeLabel,
            onValueChange = { timeLabel = it },
            label = { Text("توقيت مخصص") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("match_time_input"),
            shape = MaterialTheme.shapes.medium,
          )
          OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("المنطقة / العنوان (اختياري)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("match_location_input"),
            shape = MaterialTheme.shapes.medium,
          )
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("ملاحظات (اختياري)") },
            modifier = Modifier.fillMaxWidth().testTag("match_notes_input"),
            shape = MaterialTheme.shapes.medium,
          )
        }
      }

      Button(
        onClick = {
          onSave(
            effectiveTitle,
            effectivePitch,
            location,
            dateMillis,
            timeLabel.ifBlank { "9:00 م" },
            selectedFormat,
            targetCount,
            totalCost,
            notes,
            selectedPlayerIds.toList(),
          )
        },
        enabled = canSave,
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_match_button"),
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(
          text = if (existingMatch == null) "تأكيد الحجز الآن" else "حفظ التعديلات",
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let {
              dateMillis = it
              selectedDateTag = 3
            }
            showDatePicker = false
          }
        ) {
          Text("اختيار")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }
}
