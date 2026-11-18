package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MatchWithParticipants
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.LocalMalabnaColors
import com.example.ui.util.formatArabicDate
import com.example.ui.util.formatCurrency

private enum class BookingFilter(val label: String) {
  ALL("الكل"),
  UPCOMING("القادمة"),
  COMPLETED("المكتملة"),
}

@Composable
fun BookingsScreen(
  matches: List<MatchWithParticipants>,
  onMatchClick: (Long) -> Unit,
  onCreateMatchClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedFilter by remember { mutableStateOf(BookingFilter.ALL) }

  val upcomingMatch = remember(matches) { matches.firstOrNull { !it.match.isCompleted } }
  val filteredMatches =
    remember(matches, selectedFilter) {
      when (selectedFilter) {
        BookingFilter.ALL -> matches
        BookingFilter.UPCOMING -> matches.filter { !it.match.isCompleted }
        BookingFilter.COMPLETED -> matches.filter { it.match.isCompleted }
      }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      if (upcomingMatch != null) {
        item(key = "hero_upcoming") {
          UpcomingHeroBanner(
            matchWrap = upcomingMatch,
            onClick = { onMatchClick(upcomingMatch.match.id) },
          )
        }
      }

      if (matches.isEmpty()) {
        item(key = "empty_bookings") {
          EmptyStateCard(
            title = "هيا بنا! مفيش حجوزات مسجلة حالياً",
            subtitle =
              "ابدأ بتنظيم أول حجز ملعب، وزّع اللاعبين على التشكيل، وتابع مين جاي ومين مش جاي ومين دفع بسهولة!",
            buttonText = "نظّم أول حجز كورة",
            onAction = onCreateMatchClick,
            testTag = "empty_create_booking_button",
          )
        }
      } else {
        item(key = "filter_chips") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            BookingFilter.entries.forEach { filter ->
              FilterChip(
                selected = selectedFilter == filter,
                onClick = { selectedFilter = filter },
                label = { Text(filter.label) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  ),
                modifier = Modifier.testTag("filter_${filter.name.lowercase()}"),
              )
            }
          }
        }

        if (filteredMatches.isEmpty()) {
          item(key = "empty_filtered") {
            Surface(
              shape = MaterialTheme.shapes.large,
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.SportsSoccer,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(40.dp),
                )
                Text(
                  text = "لا توجد حجوزات في هذا التصنيف",
                  style = MaterialTheme.typography.titleMedium,
                )
              }
            }
          }
        } else {
          items(filteredMatches, key = { it.match.id }) { matchWrap ->
            MatchBookingCard(
              matchWrap = matchWrap,
              onClick = { onMatchClick(matchWrap.match.id) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun UpcomingHeroBanner(
  matchWrap: MatchWithParticipants,
  onClick: () -> Unit,
) {
  val match = matchWrap.match
  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.large)
        .clickable(onClick = onClick)
        .testTag("upcoming_hero_card"),
    shape = MaterialTheme.shapes.large,
    shadowElevation = 4.dp,
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(210.dp)) {
      Image(
        painter = painterResource(id = R.drawable.img_pitch_hero),
        contentDescription = stringResource(R.string.hero_banner_desc),
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors =
                  listOf(
                    Color(0x66061A11),
                    Color(0xCC061A11),
                    Color(0xF204120B),
                  )
              )
            )
      )

      Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF10B981).copy(alpha = 0.92f),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.SportsSoccer,
                contentDescription = null,
                tint = Color(0xFF022C22),
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = "الحجز القادم • ${match.format.labelAr}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF022C22),
                fontWeight = FontWeight.Bold,
              )
            }
          }

          if (match.reminderMinutesBefore != null) {
            Surface(
              shape = RoundedCornerShape(50),
              color = Color.White.copy(alpha = 0.16f),
            ) {
              Icon(
                imageVector = Icons.Rounded.NotificationsActive,
                contentDescription = "تذكير مفعل",
                tint = Color(0xFFFDE68A),
                modifier = Modifier.padding(6.dp).size(16.dp),
              )
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = match.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Color(0xFF6EE7B7),
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = match.pitchName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD1FAE5),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Text(
              text = "• ${formatArabicDate(match.dateMillis)} (${match.timeLabel})",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "الحضور: ${matchWrap.goingCount}/${match.targetPlayers} جاي",
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFF6EE7B7),
              fontWeight = FontWeight.Bold,
            )
            Text(
              text =
                if (match.totalCost > 0.0) {
                  "التحصيل: ${formatCurrency(matchWrap.totalCollected)} / ${formatCurrency(match.totalCost)}"
                } else {
                  "اضغط لإدارة التشكيل والدفع"
                },
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFFFDE68A),
              fontWeight = FontWeight.SemiBold,
            )
          }
          if (match.totalCost > 0.0) {
            LinearProgressIndicator(
              progress = { matchWrap.collectionProgress },
              modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
              color = Color(0xFF34D399),
              trackColor = Color.White.copy(alpha = 0.2f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MatchBookingCard(
  matchWrap: MatchWithParticipants,
  onClick: () -> Unit,
) {
  val match = matchWrap.match
  val statusColors = LocalMalabnaColors.current

  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.large)
        .clickable(onClick = onClick)
        .testTag("match_card_${match.id}"),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    border =
      BorderStroke(
        width = 1.dp,
        color =
          if (match.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
          else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
      ),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text(
            text = match.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.LocationOn,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text =
                if (match.location.isNotBlank()) "${match.pitchName} • ${match.location}"
                else match.pitchName,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        StatusPill(
          label = if (match.isCompleted) "مكتمل" else match.format.labelAr,
          textColor =
            if (match.isCompleted) statusColors.exemptText
            else MaterialTheme.colorScheme.onPrimaryContainer,
          bgColor =
            if (match.isCompleted) statusColors.exemptBg
            else MaterialTheme.colorScheme.primaryContainer,
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.CalendarMonth,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = "${formatArabicDate(match.dateMillis)} • ${match.timeLabel}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      // Attendance Breakdown Pills
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        StatusPill(
          label = "جاي: ${matchWrap.goingCount}",
          textColor = statusColors.goingText,
          bgColor = statusColors.goingBg,
          icon = Icons.Rounded.CheckCircle,
        )
        StatusPill(
          label = "لم يرد: ${matchWrap.pendingCount}",
          textColor = statusColors.pendingText,
          bgColor = statusColors.pendingBg,
          icon = Icons.Rounded.Schedule,
        )
        StatusPill(
          label = "مش جاي: ${matchWrap.notGoingCount}",
          textColor = statusColors.notGoingText,
          bgColor = statusColors.notGoingBg,
          icon = Icons.Rounded.Close,
        )
      }

      AnimatedVisibility(visible = match.totalCost > 0.0) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "دفع ${matchWrap.paidCount} • متبقي ${matchWrap.unpaidCount}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text(
              text = "${formatCurrency(matchWrap.totalCollected)} / ${formatCurrency(match.totalCost)}",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          LinearProgressIndicator(
            progress = { matchWrap.collectionProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
          )
        }
      }
    }
  }
}
