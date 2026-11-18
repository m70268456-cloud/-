package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.PlayerEntity
import com.example.data.model.PlayerWithStats
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.StatusPill
import com.example.ui.theme.LocalMalabnaColors
import com.example.ui.util.PhotoStorageHelper
import com.example.ui.util.formatCurrency
import kotlinx.coroutines.launch

@Composable
fun SquadScreen(
  playersWithStats: List<PlayerWithStats>,
  onAddPlayer: (name: String, phone: String, position: String, photoUri: String?) -> Unit,
  onUpdatePlayer: (PlayerEntity) -> Unit,
  onDeletePlayer: (Long) -> Unit,
  showAddSheetTrigger: Boolean,
  onDismissAddSheetTrigger: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by remember { mutableStateOf("") }
  var editingPlayer by remember { mutableStateOf<PlayerEntity?>(null) }
  var deletingPlayer by remember { mutableStateOf<PlayerEntity?>(null) }

  val filteredPlayers =
    remember(playersWithStats, searchQuery) {
      val q = searchQuery.trim()
      if (q.isEmpty()) playersWithStats
      else {
        playersWithStats.filter {
          it.player.name.contains(q, ignoreCase = true) ||
            it.player.position.contains(q, ignoreCase = true) ||
            it.player.phone.contains(q)
        }
      }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (playersWithStats.isEmpty()) {
        item(key = "empty_squad") {
          EmptyStateCard(
            title = "قائمة الشلة فاضية",
            subtitle =
              "أضف أسماء صحابك اللي بتلعب معاهم دايماً وأرقامهم عشان تضيفهم لأي حجز كورة بضغطة واحدة.",
            buttonText = "إضافة أول لاعب",
            onAction = { editingPlayer = PlayerEntity(name = "") },
            testTag = "empty_add_player_button",
          )
        }
      } else {
        item(key = "squad_search") {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث بالاسم أو المركز...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("squad_search_input"),
            shape = MaterialTheme.shapes.large,
          )
        }

        items(filteredPlayers, key = { it.player.id }) { stats ->
          SquadPlayerCard(
            stats = stats,
            onEdit = { editingPlayer = stats.player },
            onDelete = { deletingPlayer = stats.player },
          )
        }
      }
    }
  }

  if (showAddSheetTrigger) {
    PlayerEditorBottomSheet(
      existingPlayer = null,
      onDismiss = onDismissAddSheetTrigger,
      onSave = { name, phone, position, photoUri ->
        onAddPlayer(name, phone, position, photoUri)
        onDismissAddSheetTrigger()
      },
    )
  }

  val playerToEdit = editingPlayer
  if (playerToEdit != null) {
    val isNew = playerToEdit.id == 0L
    PlayerEditorBottomSheet(
      existingPlayer = if (isNew) null else playerToEdit,
      onDismiss = { editingPlayer = null },
      onSave = { name, phone, position, photoUri ->
        if (isNew) {
          onAddPlayer(name, phone, position, photoUri)
        } else {
          onUpdatePlayer(
            playerToEdit.copy(
              name = name.trim(),
              phone = phone.trim(),
              position = position.ifBlank { "لاعب" },
              photoUri = photoUri,
            )
          )
        }
        editingPlayer = null
      },
    )
  }

  val playerToDelete = deletingPlayer
  if (playerToDelete != null) {
    AlertDialog(
      onDismissRequest = { deletingPlayer = null },
      title = { Text("حذف اللاعب") },
      text = { Text("هل أنت متأكد من حذف ${playerToDelete.name} من قائمة الشلة؟") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeletePlayer(playerToDelete.id)
            deletingPlayer = null
          }
        ) {
          Text("حذف", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingPlayer = null }) { Text("إلغاء") }
      },
    )
  }
}

@Composable
private fun SquadPlayerCard(
  stats: PlayerWithStats,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val player = stats.player
  val statusColors = LocalMalabnaColors.current

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("squad_player_card_${player.id}"),
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        PlayerAvatar(
          name = player.name,
          colorIndex = player.avatarColorIndex,
          size = 48.dp,
          photoUri = player.photoUri,
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = player.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = player.position,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
            )
            if (player.phone.isNotBlank()) {
              Text(
                text = "• ${player.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
        IconButton(onClick = onEdit) {
          Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "تعديل ${player.name}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = "حذف ${player.name}",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        StatusPill(
          label = "حضر: ${stats.goingCount}",
          textColor = statusColors.goingText,
          bgColor = statusColors.goingBg,
        )
        StatusPill(
          label = "غاب: ${stats.notGoingCount}",
          textColor = statusColors.notGoingText,
          bgColor = statusColors.notGoingBg,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (stats.totalOwed > 0.0) {
          StatusPill(
            label = "عليه: ${formatCurrency(stats.totalOwed)}",
            textColor = statusColors.pendingText,
            bgColor = statusColors.pendingBg,
          )
        } else if (stats.totalPaid > 0.0) {
          StatusPill(
            label = "دفع: ${formatCurrency(stats.totalPaid)}",
            textColor = statusColors.goingText,
            bgColor = statusColors.goingBg,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerEditorBottomSheet(
  existingPlayer: PlayerEntity?,
  onDismiss: () -> Unit,
  onSave: (name: String, phone: String, position: String, photoUri: String?) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var name by remember { mutableStateOf(existingPlayer?.name ?: "") }
  var phone by remember { mutableStateOf(existingPlayer?.phone ?: "") }
  var position by remember { mutableStateOf(existingPlayer?.position ?: "وسط") }
  var photoUri by remember { mutableStateOf(existingPlayer?.photoUri) }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        scope.launch {
          val saved = PhotoStorageHelper.saveProfilePhoto(context, uri)
          if (saved != null) photoUri = saved
        }
      }
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
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        text = if (existingPlayer == null) "إضافة لاعب جديد للشلة" else "تعديل بيانات اللاعب",
        style = MaterialTheme.typography.headlineSmall,
      )

      // Zero-permission Photo Picker Profile Picture Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Box(
          modifier =
            Modifier.size(72.dp)
              .clip(CircleShape)
              .clickable {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }
              .testTag("pick_player_photo_button"),
          contentAlignment = Alignment.Center,
        ) {
          PlayerAvatar(
            name = name.ifBlank { "ل" },
            colorIndex = existingPlayer?.avatarColorIndex ?: 0,
            size = 72.dp,
            photoUri = photoUri,
          )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          TextButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
          ) {
            Icon(Icons.Rounded.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (photoUri == null) "اختر صورة شخصية" else "تغيير الصورة")
          }
          if (photoUri != null) {
            TextButton(onClick = { photoUri = null }) {
              Text("إزالة الصورة", color = MaterialTheme.colorScheme.error)
            }
          }
        }
      }

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("اسم اللاعب *") },
        placeholder = { Text("مثلاً: أحمد مصطفى") },
        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("player_name_input"),
        shape = MaterialTheme.shapes.medium,
      )
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("رقم الموبايل (اختياري للتذكير SMS)") },
        placeholder = { Text("010xxxxxxxx") },
        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("player_phone_input"),
        shape = MaterialTheme.shapes.medium,
      )
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "المركز المفضل", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("حارس", "دفاع", "وسط", "هجوم", "جوكر").forEach { pos ->
            FilterChip(
              selected = position == pos,
              onClick = { position = pos },
              label = { Text(pos) },
            )
          }
        }
      }
      Button(
        onClick = { onSave(name, phone, position, photoUri) },
        enabled = name.isNotBlank(),
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_player_button"),
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(if (existingPlayer == null) "إضافة للشلة" else "حفظ التعديلات")
      }
    }
  }
}
