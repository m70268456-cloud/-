package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MatchParticipantEntity
import com.example.data.model.PaymentLedgerItem
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.RecordPaymentBottomSheet
import com.example.ui.components.StatusPill
import com.example.ui.components.paymentMethodIcon
import com.example.ui.theme.LocalMalabnaColors
import com.example.ui.util.formatArabicDate
import com.example.ui.util.formatCurrency

private enum class LedgerTabFilter(val label: String, val status: PaymentStatus) {
  UNPAID("غير مدفوع (هيدفع)", PaymentStatus.UNPAID),
  PAID("مدفوع", PaymentStatus.PAID),
  EXEMPT("مش هيدفع (معفي)", PaymentStatus.EXEMPT),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentsScreen(
  ledgerItems: List<PaymentLedgerItem>,
  onRecordPayment: (
    participant: MatchParticipantEntity,
    status: PaymentStatus,
    method: PaymentMethod?,
    amountPaid: Double,
    customAmount: Double?,
    note: String,
  ) -> Unit,
  onNavigateToBookings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedFilter by remember { mutableStateOf(LedgerTabFilter.UNPAID) }
  var editingPaymentItem by remember { mutableStateOf<PaymentLedgerItem?>(null) }

  val totalCollected =
    remember(ledgerItems) {
      ledgerItems
        .filter { it.participant.paymentStatus == PaymentStatus.PAID }
        .sumOf { it.participant.amountPaid }
    }
  val totalRemaining =
    remember(ledgerItems) {
      ledgerItems
        .filter { it.participant.paymentStatus == PaymentStatus.UNPAID }
        .sumOf { it.suggestedShare }
    }
  val methodTotals =
    remember(ledgerItems) {
      PaymentMethod.entries.mapNotNull { method ->
        val sum =
          ledgerItems
            .filter {
              it.participant.paymentStatus == PaymentStatus.PAID &&
                it.participant.paymentMethod == method
            }
            .sumOf { it.participant.amountPaid }
        if (sum > 0.0) method to sum else null
      }
    }

  val filteredItems =
    remember(ledgerItems, selectedFilter) {
      ledgerItems.filter { it.participant.paymentStatus == selectedFilter.status }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (ledgerItems.isEmpty()) {
        item(key = "empty_payments") {
          EmptyStateCard(
            title = "سجل المدفوعات فاضي",
            subtitle =
              "أنشئ حجز كورة وأضف اللاعبين لتبدأ بتتبع مين دفع (كاش، محفظة، بطاقة، إنستاباي) ومين لسه هيدفع.",
            buttonText = "عرض الحجوزات",
            onAction = onNavigateToBookings,
            testTag = "empty_payments_cta",
          )
        }
      } else {
        item(key = "payments_overview_card") {
          val statusColors = LocalMalabnaColors.current
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.AccountBalanceWallet,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                  text = "ملخص الحسابات والمستحقات",
                  style = MaterialTheme.typography.titleLarge,
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Surface(
                  modifier = Modifier.weight(1f),
                  shape = MaterialTheme.shapes.medium,
                  color = statusColors.goingBg.copy(alpha = 0.7f),
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                      text = "إجمالي المحصّل",
                      style = MaterialTheme.typography.labelMedium,
                      color = statusColors.goingText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = formatCurrency(totalCollected),
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.Bold,
                      color = statusColors.goingText,
                    )
                  }
                }
                Surface(
                  modifier = Modifier.weight(1f),
                  shape = MaterialTheme.shapes.medium,
                  color = statusColors.pendingBg.copy(alpha = 0.7f),
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                      text = "متبقي للتحصيل",
                      style = MaterialTheme.typography.labelMedium,
                      color = statusColors.pendingText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = formatCurrency(totalRemaining),
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.Bold,
                      color = statusColors.pendingText,
                    )
                  }
                }
              }

              if (methodTotals.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = "توزيع التحصيل حسب طريقة الدفع",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    methodTotals.forEach { (method, sum) ->
                      StatusPill(
                        label = "${method.shortLabelAr}: ${formatCurrency(sum)}",
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        bgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                        icon = paymentMethodIcon(method),
                      )
                    }
                  }
                }
              }
            }
          }
        }

        item(key = "payments_filter_row") {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            LedgerTabFilter.entries.forEach { tab ->
              val count = ledgerItems.count { it.participant.paymentStatus == tab.status }
              FilterChip(
                selected = selectedFilter == tab,
                onClick = { selectedFilter = tab },
                label = { Text("${tab.label} ($count)") },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  ),
                modifier = Modifier.testTag("payments_tab_${tab.name.lowercase()}"),
              )
            }
          }
        }

        if (filteredItems.isEmpty()) {
          item(key = "empty_tab_state") {
            Surface(
              shape = MaterialTheme.shapes.large,
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(36.dp),
                )
                Text(
                  text =
                    when (selectedFilter) {
                      LedgerTabFilter.UNPAID -> "ممتاز! مفيش أي مبالغ متأخرة"
                      LedgerTabFilter.PAID -> "لم يتم تسجيل مدفوعات بعد"
                      LedgerTabFilter.EXEMPT -> "لا يوجد لاعبين معفيين"
                    },
                  style = MaterialTheme.typography.titleMedium,
                )
              }
            }
          }
        } else {
          items(filteredItems, key = { it.participant.id }) { item ->
            LedgerItemCard(
              item = item,
              onManagePayment = { editingPaymentItem = item },
            )
          }
        }
      }
    }
  }

  val activeItem = editingPaymentItem
  if (activeItem != null) {
    RecordPaymentBottomSheet(
      playerName = activeItem.player.name,
      matchTitle = activeItem.match.title,
      participant = activeItem.participant,
      suggestedShare = activeItem.suggestedShare,
      onDismiss = { editingPaymentItem = null },
      onSavePayment = { status, method, amountPaid, customAmount, note ->
        onRecordPayment(activeItem.participant, status, method, amountPaid, customAmount, note)
        editingPaymentItem = null
      },
    )
  }
}

@Composable
private fun LedgerItemCard(
  item: PaymentLedgerItem,
  onManagePayment: () -> Unit,
) {
  val statusColors = LocalMalabnaColors.current
  val p = item.participant

  Surface(
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      PlayerAvatar(
        name = item.player.name,
        colorIndex = item.player.avatarColorIndex,
        size = 44.dp,
        photoUri = item.player.photoUri,
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
          text = item.player.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "${item.match.title} • ${formatArabicDate(item.match.dateMillis)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          when (p.paymentStatus) {
            PaymentStatus.PAID -> {
              val method = p.paymentMethod ?: PaymentMethod.CASH
              StatusPill(
                label = "${method.shortLabelAr} • ${formatCurrency(p.amountPaid)}",
                textColor = statusColors.goingText,
                bgColor = statusColors.goingBg,
                icon = paymentMethodIcon(method),
              )
            }
            PaymentStatus.UNPAID -> {
              StatusPill(
                label = "المطلوب: ${formatCurrency(item.suggestedShare)}",
                textColor = statusColors.pendingText,
                bgColor = statusColors.pendingBg,
              )
            }
            PaymentStatus.EXEMPT -> {
              StatusPill(
                label = "معفي / مش هيدفع",
                textColor = statusColors.exemptText,
                bgColor = statusColors.exemptBg,
              )
            }
          }
        }
      }

      if (p.paymentStatus == PaymentStatus.UNPAID) {
        Button(
          onClick = onManagePayment,
          modifier = Modifier.testTag("ledger_pay_${p.id}"),
        ) {
          Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("دفع")
        }
      } else {
        OutlinedButton(
          onClick = onManagePayment,
          modifier = Modifier.testTag("ledger_edit_${p.id}"),
        ) {
          Text("تعديل")
        }
      }
    }
  }
}
