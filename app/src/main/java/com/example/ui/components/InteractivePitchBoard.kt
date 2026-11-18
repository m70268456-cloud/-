package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MatchParticipantEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.MatchParticipantDetail
import com.example.data.model.MatchWithParticipants
import com.example.data.model.PaymentStatus
import com.example.data.model.PitchFormat
import com.example.ui.theme.LocalMalabnaColors
import com.example.ui.util.playerInitials
import kotlin.math.roundToInt

data class PitchSlotSpec(
  val index: Int,
  val team: Int, // 0 = Top Team A, 1 = Bottom Team B
  val roleLabel: String,
  val xFraction: Float, // 0f..1f left to right
  val yFraction: Float, // 0f..1f top to bottom
)

fun buildPitchSlots(format: PitchFormat): List<PitchSlotSpec> {
  return when (format) {
    PitchFormat.FIVE_A_SIDE ->
      listOf(
        // Team A (Top half)
        PitchSlotSpec(0, 0, "حارس", 0.50f, 0.08f),
        PitchSlotSpec(1, 0, "دفاع", 0.28f, 0.21f),
        PitchSlotSpec(2, 0, "دفاع", 0.72f, 0.21f),
        PitchSlotSpec(3, 0, "وسط", 0.50f, 0.33f),
        PitchSlotSpec(4, 0, "هجوم", 0.50f, 0.43f),
        // Team B (Bottom half)
        PitchSlotSpec(5, 1, "هجوم", 0.50f, 0.57f),
        PitchSlotSpec(6, 1, "وسط", 0.50f, 0.67f),
        PitchSlotSpec(7, 1, "دفاع", 0.28f, 0.79f),
        PitchSlotSpec(8, 1, "دفاع", 0.72f, 0.79f),
        PitchSlotSpec(9, 1, "حارس", 0.50f, 0.92f),
      )
    PitchFormat.SEVEN_A_SIDE ->
      listOf(
        // Team A
        PitchSlotSpec(0, 0, "حارس", 0.50f, 0.07f),
        PitchSlotSpec(1, 0, "دفاع", 0.30f, 0.18f),
        PitchSlotSpec(2, 0, "دفاع", 0.70f, 0.18f),
        PitchSlotSpec(3, 0, "وسط", 0.20f, 0.31f),
        PitchSlotSpec(4, 0, "وسط", 0.50f, 0.31f),
        PitchSlotSpec(5, 0, "وسط", 0.80f, 0.31f),
        PitchSlotSpec(6, 0, "هجوم", 0.50f, 0.43f),
        // Team B
        PitchSlotSpec(7, 1, "هجوم", 0.50f, 0.57f),
        PitchSlotSpec(8, 1, "وسط", 0.20f, 0.69f),
        PitchSlotSpec(9, 1, "وسط", 0.50f, 0.69f),
        PitchSlotSpec(10, 1, "وسط", 0.80f, 0.69f),
        PitchSlotSpec(11, 1, "دفاع", 0.30f, 0.82f),
        PitchSlotSpec(12, 1, "دفاع", 0.70f, 0.82f),
        PitchSlotSpec(13, 1, "حارس", 0.50f, 0.93f),
      )
    PitchFormat.EIGHT_A_SIDE ->
      listOf(
        // Team A
        PitchSlotSpec(0, 0, "حارس", 0.50f, 0.07f),
        PitchSlotSpec(1, 0, "دفاع", 0.22f, 0.18f),
        PitchSlotSpec(2, 0, "دفاع", 0.50f, 0.18f),
        PitchSlotSpec(3, 0, "دفاع", 0.78f, 0.18f),
        PitchSlotSpec(4, 0, "وسط", 0.33f, 0.31f),
        PitchSlotSpec(5, 0, "وسط", 0.67f, 0.31f),
        PitchSlotSpec(6, 0, "هجوم", 0.35f, 0.43f),
        PitchSlotSpec(7, 0, "هجوم", 0.65f, 0.43f),
        // Team B
        PitchSlotSpec(8, 1, "هجوم", 0.35f, 0.57f),
        PitchSlotSpec(9, 1, "هجوم", 0.65f, 0.57f),
        PitchSlotSpec(10, 1, "وسط", 0.33f, 0.69f),
        PitchSlotSpec(11, 1, "وسط", 0.67f, 0.69f),
        PitchSlotSpec(12, 1, "دفاع", 0.22f, 0.82f),
        PitchSlotSpec(13, 1, "دفاع", 0.50f, 0.82f),
        PitchSlotSpec(14, 1, "دفاع", 0.78f, 0.82f),
        PitchSlotSpec(15, 1, "حارس", 0.50f, 0.93f),
      )
    PitchFormat.ELEVEN_A_SIDE ->
      listOf(
        // Team A (1-4-3-3)
        PitchSlotSpec(0, 0, "حارس", 0.50f, 0.06f),
        PitchSlotSpec(1, 0, "ظهير", 0.15f, 0.16f),
        PitchSlotSpec(2, 0, "دفاع", 0.38f, 0.16f),
        PitchSlotSpec(3, 0, "دفاع", 0.62f, 0.16f),
        PitchSlotSpec(4, 0, "ظهير", 0.85f, 0.16f),
        PitchSlotSpec(5, 0, "وسط", 0.25f, 0.29f),
        PitchSlotSpec(6, 0, "وسط", 0.50f, 0.29f),
        PitchSlotSpec(7, 0, "وسط", 0.75f, 0.29f),
        PitchSlotSpec(8, 0, "جناح", 0.22f, 0.42f),
        PitchSlotSpec(9, 0, "مهاجم", 0.50f, 0.42f),
        PitchSlotSpec(10, 0, "جناح", 0.78f, 0.42f),
        // Team B (1-4-3-3)
        PitchSlotSpec(11, 1, "جناح", 0.22f, 0.58f),
        PitchSlotSpec(12, 1, "مهاجم", 0.50f, 0.58f),
        PitchSlotSpec(13, 1, "جناح", 0.78f, 0.58f),
        PitchSlotSpec(14, 1, "وسط", 0.25f, 0.71f),
        PitchSlotSpec(15, 1, "وسط", 0.50f, 0.71f),
        PitchSlotSpec(16, 1, "وسط", 0.75f, 0.71f),
        PitchSlotSpec(17, 1, "ظهير", 0.15f, 0.84f),
        PitchSlotSpec(18, 1, "دفاع", 0.38f, 0.84f),
        PitchSlotSpec(19, 1, "دفاع", 0.62f, 0.84f),
        PitchSlotSpec(20, 1, "ظهير", 0.85f, 0.84f),
        PitchSlotSpec(21, 1, "حارس", 0.50f, 0.94f),
      )
  }
}

@Composable
fun InteractivePitchBoard(
  matchWrap: MatchWithParticipants,
  onAssignPlayerToSlot: (MatchParticipantEntity, Int?) -> Unit,
  onAutoAssign: (Int) -> Unit,
  onUpdateAttendance: (MatchParticipantEntity, AttendanceStatus) -> Unit,
  onOpenPaymentSheet: (MatchParticipantDetail) -> Unit,
  onOpenAddPlayers: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val slots = remember(matchWrap.match.format) { buildPitchSlots(matchWrap.match.format) }
  val slotAssignments =
    remember(matchWrap.participants) {
      matchWrap.participants.mapNotNull { detail ->
        detail.participant.pitchSlotIndex?.let { idx -> idx to detail }
      }.toMap()
    }
  var activeSlot by remember { mutableStateOf<PitchSlotSpec?>(null) }

  val statusColors = LocalMalabnaColors.current

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Controls & Legend Bar
    Surface(
      shape = MaterialTheme.shapes.medium,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "تشكيلة الملعب (${matchWrap.match.format.labelAr})",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "اضغط على أي مركز لتسكين أو تبديل لاعب",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          FilledTonalButton(
            onClick = { onAutoAssign(slots.size) },
            modifier = Modifier.testTag("auto_assign_pitch_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.AutoFixHigh,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("توزيع تلقائي", style = MaterialTheme.typography.labelMedium)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          LegendDot(color = statusColors.goingText, label = "جاي")
          LegendDot(color = statusColors.pendingText, label = "لم يرد")
          LegendDot(color = statusColors.notGoingText, label = "مش جاي")
          Spacer(modifier = Modifier.weight(1f))
          Text(
            text = "${slotAssignments.size}/${slots.size} مركز مشغول",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    // Visual Football Pitch Canvas + Interactive Nodes
    // Force LTR inside pitch coordinate space so left/right wing positions stay symmetrical
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp,
        border = BorderStroke(2.dp, Color(0xFF1B5E3F)),
      ) {
        val aspectRatio =
          when (matchWrap.match.format) {
            PitchFormat.FIVE_A_SIDE -> 0.72f
            PitchFormat.SEVEN_A_SIDE -> 0.68f
            PitchFormat.EIGHT_A_SIDE -> 0.66f
            PitchFormat.ELEVEN_A_SIDE -> 0.62f
          }

        BoxWithConstraints(
          modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatio).testTag("interactive_pitch_canvas")
        ) {
          val widthPx = constraints.maxWidth.toFloat()
          val heightPx = constraints.maxHeight.toFloat()

          // Realistic mowed turf & pitch lines Canvas
          Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeCount = 10
            val stripeHeight = size.height / stripeCount
            for (i in 0 until stripeCount) {
              drawRect(
                color = if (i % 2 == 0) Color(0xFF0E4D31) else Color(0xFF135B3B),
                topLeft = Offset(0f, i * stripeHeight),
                size = Size(size.width, stripeHeight + 1f),
              )
            }

            val lineColor = Color.White.copy(alpha = 0.55f)
            val strokeWidth = 2.dp.toPx()
            val pad = 12.dp.toPx()
            val pitchW = size.width - pad * 2
            val pitchH = size.height - pad * 2

            // Outer boundary
            drawRect(
              color = lineColor,
              topLeft = Offset(pad, pad),
              size = Size(pitchW, pitchH),
              style = Stroke(width = strokeWidth),
            )

            // Halfway line
            drawLine(
              color = lineColor,
              start = Offset(pad, size.height / 2f),
              end = Offset(size.width - pad, size.height / 2f),
              strokeWidth = strokeWidth,
            )

            // Center circle & spot
            drawCircle(
              color = lineColor,
              radius = pitchW * 0.16f,
              center = Offset(size.width / 2f, size.height / 2f),
              style = Stroke(width = strokeWidth),
            )
            drawCircle(
              color = lineColor,
              radius = 3.5.dp.toPx(),
              center = Offset(size.width / 2f, size.height / 2f),
            )

            // Top Penalty Area & Goal Box
            val boxW = pitchW * 0.52f
            val boxH = pitchH * 0.14f
            drawRect(
              color = lineColor,
              topLeft = Offset((size.width - boxW) / 2f, pad),
              size = Size(boxW, boxH),
              style = Stroke(width = strokeWidth),
            )
            val goalBoxW = pitchW * 0.26f
            val goalBoxH = pitchH * 0.055f
            drawRect(
              color = lineColor,
              topLeft = Offset((size.width - goalBoxW) / 2f, pad),
              size = Size(goalBoxW, goalBoxH),
              style = Stroke(width = strokeWidth),
            )

            // Bottom Penalty Area & Goal Box
            drawRect(
              color = lineColor,
              topLeft = Offset((size.width - boxW) / 2f, size.height - pad - boxH),
              size = Size(boxW, boxH),
              style = Stroke(width = strokeWidth),
            )
            drawRect(
              color = lineColor,
              topLeft = Offset((size.width - goalBoxW) / 2f, size.height - pad - goalBoxH),
              size = Size(goalBoxW, goalBoxH),
              style = Stroke(width = strokeWidth),
            )

            // Subtle corner arcs
            val cornerR = 12.dp.toPx()
            drawArc(
              color = lineColor,
              startAngle = 0f,
              sweepAngle = 90f,
              useCenter = false,
              topLeft = Offset(pad - cornerR, pad - cornerR),
              size = Size(cornerR * 2, cornerR * 2),
              style = Stroke(width = strokeWidth),
            )
            drawArc(
              color = lineColor,
              startAngle = 90f,
              sweepAngle = 90f,
              useCenter = false,
              topLeft = Offset(size.width - pad - cornerR, pad - cornerR),
              size = Size(cornerR * 2, cornerR * 2),
              style = Stroke(width = strokeWidth),
            )
            drawArc(
              color = lineColor,
              startAngle = 270f,
              sweepAngle = 90f,
              useCenter = false,
              topLeft = Offset(pad - cornerR, size.height - pad - cornerR),
              size = Size(cornerR * 2, cornerR * 2),
              style = Stroke(width = strokeWidth),
            )
            drawArc(
              color = lineColor,
              startAngle = 180f,
              sweepAngle = 90f,
              useCenter = false,
              topLeft = Offset(size.width - pad - cornerR, size.height - pad - cornerR),
              size = Size(cornerR * 2, cornerR * 2),
              style = Stroke(width = strokeWidth),
            )
          }

          // Team labels on pitch corners
          Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.45f),
          ) {
            Text(
              text = "الفريق الأول",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
          Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.45f),
          ) {
            Text(
              text = "الفريق الثاني",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFFDE68A),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }

          // Interactive Player Spot Nodes
          val nodeWidthDp = 64.dp
          val nodeHeightDp = 58.dp
          slots.forEach { slot ->
            val assigned = slotAssignments[slot.index]
            val density = androidx.compose.ui.platform.LocalDensity.current
            val nodeWidthPx = with(density) { nodeWidthDp.toPx() }
            val nodeHeightPx = with(density) { nodeHeightDp.toPx() }
            val offsetX = (slot.xFraction * widthPx - nodeWidthPx / 2f).roundToInt()
            val offsetY = (slot.yFraction * heightPx - nodeHeightPx / 2f).roundToInt()

            PitchSpotNode(
              slot = slot,
              assigned = assigned,
              onClick = { activeSlot = slot },
              modifier =
                Modifier
                  .offset { IntOffset(offsetX, offsetY) }
                  .size(width = nodeWidthDp, height = nodeHeightDp)
                  .testTag("pitch_slot_${slot.index}"),
            )
          }
        }
      }
    }
  }

  // Interactive Slot Assignment & Quick Actions Bottom Sheet
  val currentSlot = activeSlot
  if (currentSlot != null) {
    val currentAssigned = slotAssignments[currentSlot.index]
    SlotAssignmentBottomSheet(
      slot = currentSlot,
      assignedDetail = currentAssigned,
      allParticipants = matchWrap.participants,
      onDismiss = { activeSlot = null },
      onSelectPlayerForSlot = { detail ->
        onAssignPlayerToSlot(detail.participant, currentSlot.index)
        activeSlot = null
      },
      onClearSlot = {
        if (currentAssigned != null) {
          onAssignPlayerToSlot(currentAssigned.participant, null)
        }
        activeSlot = null
      },
      onUpdateAttendance = { status ->
        if (currentAssigned != null) {
          onUpdateAttendance(currentAssigned.participant, status)
        }
      },
      onOpenPayment = {
        if (currentAssigned != null) {
          activeSlot = null
          onOpenPaymentSheet(currentAssigned)
        }
      },
      onAddNewPlayers = {
        activeSlot = null
        onOpenAddPlayers()
      },
    )
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun PitchSpotNode(
  slot: PitchSlotSpec,
  assigned: MatchParticipantDetail?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val statusColors = LocalMalabnaColors.current
  val teamTint = if (slot.team == 0) Color.White else Color(0xFFFDE68A)

  Column(
    modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    if (assigned == null) {
      // Empty Spot Circle
      Box(
        modifier =
          Modifier.size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.5.dp, teamTint.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Add,
          contentDescription = "تسكين لاعب في ${slot.roleLabel}",
          tint = teamTint,
          modifier = Modifier.size(18.dp),
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = slot.roleLabel,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = Color.White.copy(alpha = 0.85f),
        maxLines = 1,
      )
    } else {
      val attendanceRing =
        when (assigned.participant.attendanceStatus) {
          AttendanceStatus.GOING -> Color(0xFF34D399)
          AttendanceStatus.PENDING -> Color(0xFFFBBF24)
          AttendanceStatus.NOT_GOING -> Color(0xFFF87171)
        }
      val attendanceIcon =
        when (assigned.participant.attendanceStatus) {
          AttendanceStatus.GOING -> Icons.Rounded.Check
          AttendanceStatus.PENDING -> Icons.Rounded.Schedule
          AttendanceStatus.NOT_GOING -> Icons.Rounded.Close
        }
      val jerseyBg =
        if (slot.team == 0) Color(0xFFECFDF5) else Color(0xFF1F2937)
      val jerseyText =
        if (slot.team == 0) Color(0xFF064E3B) else Color(0xFFFDE68A)

      Box(contentAlignment = Alignment.Center) {
        Box(
          modifier =
            Modifier.size(38.dp)
              .clip(CircleShape)
              .background(jerseyBg)
              .border(2.5.dp, attendanceRing, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          if (!assigned.player.photoUri.isNullOrBlank()) {
            AsyncImage(
              model = assigned.player.photoUri,
              contentDescription = assigned.player.name,
              modifier = Modifier.size(38.dp).clip(CircleShape),
              contentScale = ContentScale.Crop,
            )
          } else {
            Text(
              text = playerInitials(assigned.player.name),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = jerseyText,
            )
          }
        }

        // Attendance Corner Indicator
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .offset(x = 2.dp, y = (-2).dp)
              .size(15.dp)
              .clip(CircleShape)
              .background(attendanceRing)
              .border(1.dp, Color(0xFF0B2E1E), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = attendanceIcon,
            contentDescription = null,
            tint = Color(0xFF081C13),
            modifier = Modifier.size(10.dp),
          )
        }

        // Payment Dot Indicator
        val payColor =
          when (assigned.participant.paymentStatus) {
            PaymentStatus.PAID -> Color(0xFF10B981)
            PaymentStatus.UNPAID -> Color(0xFFF59E0B)
            PaymentStatus.EXEMPT -> Color(0xFF94A3B8)
          }
        Box(
          modifier =
            Modifier.align(Alignment.BottomStart)
              .offset(x = (-1).dp, y = 1.dp)
              .size(11.dp)
              .clip(CircleShape)
              .background(payColor)
              .border(1.dp, Color.White, CircleShape),
        )
      }

      Spacer(modifier = Modifier.height(2.dp))
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.Black.copy(alpha = 0.68f),
      ) {
        Text(
          text = assigned.player.name.substringBefore(" "),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotAssignmentBottomSheet(
  slot: PitchSlotSpec,
  assignedDetail: MatchParticipantDetail?,
  allParticipants: List<MatchParticipantDetail>,
  onDismiss: () -> Unit,
  onSelectPlayerForSlot: (MatchParticipantDetail) -> Unit,
  onClearSlot: () -> Unit,
  onUpdateAttendance: (AttendanceStatus) -> Unit,
  onOpenPayment: () -> Unit,
  onAddNewPlayers: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val statusColors = LocalMalabnaColors.current
  val teamName = if (slot.team == 0) "الفريق الأول" else "الفريق الثاني"

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
        text = "مركز ${slot.roleLabel} • $teamName",
        style = MaterialTheme.typography.headlineSmall,
      )

      if (assignedDetail != null) {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              PlayerAvatar(
                name = assignedDetail.player.name,
                colorIndex = assignedDetail.player.avatarColorIndex,
                size = 40.dp,
                photoUri = assignedDetail.player.photoUri,
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = assignedDetail.player.name,
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = "اللاعب الحالي في هذا المركز",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            // Quick Attendance Switcher
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              AttendanceStatus.entries.forEach { status ->
                val selected = assignedDetail.participant.attendanceStatus == status
                val (fg, bg) =
                  when (status) {
                    AttendanceStatus.GOING -> statusColors.goingText to statusColors.goingBg
                    AttendanceStatus.PENDING -> statusColors.pendingText to statusColors.pendingBg
                    AttendanceStatus.NOT_GOING ->
                      statusColors.notGoingText to statusColors.notGoingBg
                  }
                Surface(
                  modifier =
                    Modifier.weight(1f)
                      .height(42.dp)
                      .clip(MaterialTheme.shapes.small)
                      .clickable { onUpdateAttendance(status) },
                  shape = MaterialTheme.shapes.small,
                  color = if (selected) bg else MaterialTheme.colorScheme.surface,
                  border =
                    BorderStroke(
                      1.dp,
                      if (selected) fg else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    ),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = status.labelAr,
                      style = MaterialTheme.typography.labelMedium,
                      color = if (selected) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                  }
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              FilledTonalButton(
                onClick = onOpenPayment,
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حالة الدفع")
              }
              OutlinedButton(
                onClick = onClearSlot,
                modifier = Modifier.weight(1f),
              ) {
                Icon(
                  Icons.Rounded.PersonRemove,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("إخلاء المركز")
              }
            }
          }
        }
        HorizontalDivider()
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "اختر لاعب لتسكينه هنا",
          style = MaterialTheme.typography.titleSmall,
        )
        OutlinedButton(onClick = onAddNewPlayers) {
          Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("إضافة لاعبين")
        }
      }

      if (allParticipants.isEmpty()) {
        Text(
          text = "لا يوجد لاعبين مضافين لهذا الحجز بعد. اضغط على إضافة لاعبين.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(vertical = 12.dp),
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().height(260.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(allParticipants, key = { it.participant.id }) { detail ->
            val isHere = detail.participant.pitchSlotIndex == slot.index
            val (attFg, attBg) =
              when (detail.participant.attendanceStatus) {
                AttendanceStatus.GOING -> statusColors.goingText to statusColors.goingBg
                AttendanceStatus.PENDING -> statusColors.pendingText to statusColors.pendingBg
                AttendanceStatus.NOT_GOING -> statusColors.notGoingText to statusColors.notGoingBg
              }
            Surface(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(MaterialTheme.shapes.medium)
                  .clickable { onSelectPlayerForSlot(detail) },
              shape = MaterialTheme.shapes.medium,
              color =
                if (isHere) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border =
                BorderStroke(
                  1.dp,
                  if (isHere) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                ),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
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
                    style = MaterialTheme.typography.titleSmall,
                  )
                  Text(
                    text =
                      when {
                        isHere -> "في هذا المركز حالياً"
                        detail.participant.pitchSlotIndex != null -> "مسكن في مركز آخر (سيتم نقله)"
                        else -> "غير مسكن في التشكيلة"
                      },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                StatusPill(
                  label = detail.participant.attendanceStatus.labelAr,
                  textColor = attFg,
                  bgColor = attBg,
                )
              }
            }
          }
        }
      }
    }
  }
}
