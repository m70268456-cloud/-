package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.MatchParticipantEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.ui.theme.AvatarColors
import com.example.ui.theme.LocalMalabnaColors
import com.example.ui.util.formatCurrency
import com.example.ui.util.formatNumber
import com.example.ui.util.playerInitials

@Composable
fun PlayerAvatar(
  name: String,
  colorIndex: Int,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  photoUri: String? = null,
) {
  val bgColor = AvatarColors[colorIndex.mod(AvatarColors.size)]
  Box(
    modifier = modifier.size(size).clip(CircleShape).background(bgColor.copy(alpha = 0.18f)),
    contentAlignment = Alignment.Center,
  ) {
    if (!photoUri.isNullOrBlank()) {
      AsyncImage(
        model = photoUri,
        contentDescription = "صورة $name",
        modifier = Modifier.size(size).clip(CircleShape),
        contentScale = ContentScale.Crop,
      )
    } else {
      Text(
        text = playerInitials(name),
        style = MaterialTheme.typography.titleSmall,
        color = bgColor,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
fun StatusPill(
  label: String,
  textColor: Color,
  bgColor: Color,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = bgColor,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(14.dp),
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

fun paymentMethodIcon(method: PaymentMethod): ImageVector {
  return when (method) {
    PaymentMethod.CASH -> Icons.Rounded.Payments
    PaymentMethod.MOBILE_PAYMENT -> Icons.Rounded.PhoneAndroid
    PaymentMethod.CREDIT_CARD -> Icons.Rounded.CreditCard
    PaymentMethod.INSTAPAY -> Icons.Rounded.AccountBalanceWallet
    PaymentMethod.BANK_TRANSFER -> Icons.Rounded.AccountBalance
  }
}

@Composable
fun EmptyStateCard(
  title: String,
  subtitle: String,
  buttonText: String,
  onAction: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "empty_state_cta",
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_empty_pitch),
        contentDescription = stringResource(R.string.empty_pitch_desc),
        modifier = Modifier.fillMaxWidth().height(170.dp).clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop,
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(20.dp))
      Button(
        onClick = onAction,
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag(testTag),
        shape = MaterialTheme.shapes.medium,
      ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = buttonText, style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordPaymentBottomSheet(
  playerName: String,
  matchTitle: String,
  participant: MatchParticipantEntity,
  suggestedShare: Double,
  onDismiss: () -> Unit,
  onSavePayment: (
    status: PaymentStatus,
    method: PaymentMethod?,
    amountPaid: Double,
    customAmount: Double?,
    note: String,
  ) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val statusColors = LocalMalabnaColors.current

  var selectedStatus by remember(participant) {
    mutableStateOf(
      if (participant.paymentStatus == PaymentStatus.UNPAID) PaymentStatus.PAID
      else participant.paymentStatus
    )
  }
  var selectedMethod by remember(participant) {
    mutableStateOf(participant.paymentMethod ?: PaymentMethod.CASH)
  }
  val defaultAmount =
    when {
      participant.amountPaid > 0.0 -> participant.amountPaid
      participant.customAmount != null && participant.customAmount > 0.0 -> participant.customAmount
      else -> suggestedShare
    }
  var amountText by remember(participant) {
    mutableStateOf(if (defaultAmount > 0.0) formatNumber(defaultAmount).replace(",", "") else "")
  }
  var noteText by remember(participant) { mutableStateOf(participant.paymentNote) }

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
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "إدارة الدفع • $playerName",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "$matchTitle • الحصة المقترحة: ${formatCurrency(suggestedShare)}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "حالة الدفع",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          PaymentStatusChipOption(
            label = "مدفوع",
            icon = Icons.Rounded.CheckCircle,
            selected = selectedStatus == PaymentStatus.PAID,
            activeColor = statusColors.goingText,
            activeBg = statusColors.goingBg,
            onClick = { selectedStatus = PaymentStatus.PAID },
            modifier = Modifier.weight(1f).testTag("payment_status_paid_chip"),
          )
          PaymentStatusChipOption(
            label = "غير مدفوع",
            icon = Icons.Rounded.Schedule,
            selected = selectedStatus == PaymentStatus.UNPAID,
            activeColor = statusColors.pendingText,
            activeBg = statusColors.pendingBg,
            onClick = { selectedStatus = PaymentStatus.UNPAID },
            modifier = Modifier.weight(1f).testTag("payment_status_unpaid_chip"),
          )
          PaymentStatusChipOption(
            label = "مش هيدفع",
            icon = Icons.Rounded.Block,
            selected = selectedStatus == PaymentStatus.EXEMPT,
            activeColor = statusColors.exemptText,
            activeBg = statusColors.exemptBg,
            onClick = { selectedStatus = PaymentStatus.EXEMPT },
            modifier = Modifier.weight(1f).testTag("payment_status_exempt_chip"),
          )
        }
      }

      if (selectedStatus == PaymentStatus.PAID) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "المبلغ المدفوع (ج.م)",
              style = MaterialTheme.typography.titleSmall,
            )
            if (suggestedShare > 0.0) {
              TextButton(
                onClick = { amountText = formatNumber(suggestedShare).replace(",", "") }
              ) {
                Text("الحصة كاملة (${formatNumber(suggestedShare)})")
              }
            }
          }
          OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                amountText = input
              }
            },
            placeholder = { Text("مثلاً: 100") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
            shape = MaterialTheme.shapes.medium,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "طريقة الدفع",
            style = MaterialTheme.typography.titleSmall,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            PaymentMethod.entries.forEach { method ->
              val isSelected = selectedMethod == method
              FilterChip(
                selected = isSelected,
                onClick = { selectedMethod = method },
                label = { Text(method.labelAr) },
                leadingIcon = {
                  Icon(
                    imageVector = paymentMethodIcon(method),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                  ),
                modifier = Modifier.testTag("payment_method_${method.name.lowercase()}"),
              )
            }
          }
        }
      }

      OutlinedTextField(
        value = noteText,
        onValueChange = { noteText = it },
        label = { Text("ملاحظة الدفع (اختياري)") },
        placeholder = { Text("مثلاً: رقم مرجعي، محفظة فودافون، دفع كاش في الملعب") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("payment_note_input"),
        shape = MaterialTheme.shapes.medium,
      )

      Button(
        onClick = {
          val parsedAmount = amountText.toDoubleOrNull() ?: suggestedShare
          val customOverride =
            if (
              selectedStatus == PaymentStatus.PAID &&
                suggestedShare > 0.0 &&
                kotlin.math.abs(parsedAmount - suggestedShare) > 0.5
            ) {
              parsedAmount
            } else {
              null
            }
          onSavePayment(
            selectedStatus,
            if (selectedStatus == PaymentStatus.PAID) selectedMethod else null,
            if (selectedStatus == PaymentStatus.PAID) parsedAmount else 0.0,
            customOverride,
            noteText,
          )
        },
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_payment_button"),
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(
          text =
            when (selectedStatus) {
              PaymentStatus.PAID -> "تأكيد الدفع (${selectedMethod.shortLabelAr})"
              PaymentStatus.UNPAID -> "تعيين كغير مدفوع"
              PaymentStatus.EXEMPT -> "تعيين كمعفي (مش هيدفع)"
            },
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}

@Composable
private fun PaymentStatusChipOption(
  label: String,
  icon: ImageVector,
  selected: Boolean,
  activeColor: Color,
  activeBg: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .height(50.dp),
    shape = MaterialTheme.shapes.medium,
    color = if (selected) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border =
      BorderStroke(
        width = if (selected) 1.5.dp else 1.dp,
        color = if (selected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
      ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
