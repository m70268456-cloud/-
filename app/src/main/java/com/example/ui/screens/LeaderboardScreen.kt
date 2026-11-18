package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.PlayerRatingLeaderboardEntry
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PlayerAvatar
import com.example.ui.components.StatusPill
import com.example.ui.theme.LocalMalabnaColors
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private enum class LeaderboardSortMode(val label: String) {
  TOTAL_POINTS("الأعلى مجموعاً (نقاط الشهر)"),
  AVERAGE_SCORE("الأعلى متوسطاً (/10)"),
}

private val avgFormatter = DecimalFormat("0.0", DecimalFormatSymbols(Locale.ENGLISH))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeaderboardScreen(
  monthlyEntries: List<PlayerRatingLeaderboardEntry>,
  allTimeEntries: List<PlayerRatingLeaderboardEntry>,
  onNavigateToBookings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showLast30Days by remember { mutableStateOf(true) }
  var sortMode by remember { mutableStateOf(LeaderboardSortMode.TOTAL_POINTS) }

  val baseList = if (showLast30Days) monthlyEntries else allTimeEntries
  val rankedEntries =
    remember(baseList, sortMode) {
      when (sortMode) {
        LeaderboardSortMode.TOTAL_POINTS ->
          baseList.sortedWith(
            compareByDescending<PlayerRatingLeaderboardEntry> { it.totalRatingPoints }
              .thenByDescending { it.averageRating }
          )
        LeaderboardSortMode.AVERAGE_SCORE ->
          baseList.sortedWith(
            compareByDescending<PlayerRatingLeaderboardEntry> { it.averageRating }
              .thenByDescending { it.totalRatingPoints }
          )
      }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item(key = "leaderboard_filters") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            FilterChip(
              selected = showLast30Days,
              onClick = { showLast30Days = true },
              label = { Text("آخر 30 يوم (نجم الشهر)") },
              leadingIcon = {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
              modifier = Modifier.testTag("leaderboard_filter_month"),
            )
            FilterChip(
              selected = !showLast30Days,
              onClick = { showLast30Days = false },
              label = { Text("كل التقييمات") },
              modifier = Modifier.testTag("leaderboard_filter_all"),
            )
          }

          if (baseList.isNotEmpty()) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              LeaderboardSortMode.entries.forEach { mode ->
                FilterChip(
                  selected = sortMode == mode,
                  onClick = { sortMode = mode },
                  label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) },
                )
              }
            }
          }
        }
      }

      if (rankedEntries.isEmpty()) {
        item(key = "empty_leaderboard") {
          EmptyStateCard(
            title =
              if (showLast30Days) "مفيش تقييمات متسجلة آخر 30 يوم"
              else "مفيش تقييمات متسجلة لسه",
            subtitle =
              "بعد كل ماتش، يقدر قائد الفريق يفتح الحجز ويضغط على \"تقييم الفريق بعد الماتش\" عشان يظهر نجم الشهر هنا!",
            buttonText = "الذهاب للحجوزات",
            onAction = onNavigateToBookings,
            testTag = "empty_leaderboard_cta",
          )
        }
      } else {
        item(key = "top_champion_card") {
          TopMonthlyChampionCard(
            champion = rankedEntries.first(),
            isMonthly = showLast30Days,
          )
        }

        if (rankedEntries.size > 1) {
          item(key = "podium_header") {
            Text(
              text = "ترتيب اللاعبين (${rankedEntries.size})",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.padding(top = 4.dp),
            )
          }
        }

        itemsIndexed(rankedEntries, key = { _, item -> item.player.id }) { index, entry ->
          LeaderboardPlayerRow(
            rank = index + 1,
            entry = entry,
            sortMode = sortMode,
          )
        }
      }
    }
  }
}

@Composable
private fun TopMonthlyChampionCard(
  champion: PlayerRatingLeaderboardEntry,
  isMonthly: Boolean,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("monthly_champion_card"),
    shape = MaterialTheme.shapes.large,
    shadowElevation = 4.dp,
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors = listOf(Color(0xFF0A4D36), Color(0xFF062C1F), Color(0xFF1A2E05))
            )
          )
          .padding(20.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(50),
          color = Color(0xFFFFC13B).copy(alpha = 0.2f),
          border = BorderStroke(1.dp, Color(0xFFFFC13B)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.EmojiEvents,
              contentDescription = null,
              tint = Color(0xFFFFC13B),
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = if (isMonthly) "أعلى تقييمات الشهر 🏆" else "نجم التقييمات العام 🏆",
              style = MaterialTheme.typography.labelLarge,
              color = Color(0xFFFDE68A),
              fontWeight = FontWeight.Bold,
            )
          }
        }

        Box(
          modifier =
            Modifier.size(84.dp)
              .clip(CircleShape)
              .border(3.dp, Color(0xFFFFC13B), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          PlayerAvatar(
            name = champion.player.name,
            colorIndex = champion.player.avatarColorIndex,
            size = 78.dp,
            photoUri = champion.player.photoUri,
          )
        }

        Text(
          text = champion.player.name,
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          ChampionStatPill(
            label = "مجموع النقاط",
            value = "${champion.totalRatingPoints} نقطة",
          )
          ChampionStatPill(
            label = "المتوسط",
            value = "${avgFormatter.format(champion.averageRating)} / 10",
          )
          ChampionStatPill(
            label = "المباريات",
            value = "${champion.ratedMatchesCount}",
          )
        }
      }
    }
  }
}

@Composable
private fun ChampionStatPill(label: String, value: String) {
  Surface(
    shape = MaterialTheme.shapes.medium,
    color = Color.White.copy(alpha = 0.12f),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFD1FAE5),
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        color = Color(0xFFFFC13B),
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
private fun LeaderboardPlayerRow(
  rank: Int,
  entry: PlayerRatingLeaderboardEntry,
  sortMode: LeaderboardSortMode,
) {
  val statusColors = LocalMalabnaColors.current
  val rankColor =
    when (rank) {
      1 -> Color(0xFFD97706)
      2 -> Color(0xFF64748B)
      3 -> Color(0xFFB45309)
      else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

  Surface(
    modifier = Modifier.fillMaxWidth().testTag("leaderboard_row_$rank"),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = if (rank <= 3) 2.dp else 1.dp,
    border =
      BorderStroke(
        width = if (rank == 1) 1.5.dp else 1.dp,
        color =
          if (rank == 1) Color(0xFFF59E0B).copy(alpha = 0.6f)
          else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
      ),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier =
          Modifier.size(34.dp)
            .clip(CircleShape)
            .background(rankColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        if (rank <= 3) {
          Icon(
            imageVector = Icons.Rounded.MilitaryTech,
            contentDescription = "المركز $rank",
            tint = rankColor,
            modifier = Modifier.size(20.dp),
          )
        } else {
          Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = rankColor,
          )
        }
      }

      PlayerAvatar(
        name = entry.player.name,
        colorIndex = entry.player.avatarColorIndex,
        size = 44.dp,
        photoUri = entry.player.photoUri,
      )

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = entry.player.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "تقييم ${entry.ratedMatchesCount} ماتش • أعلى تقييم: ${entry.highestRating}/10",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        StatusPill(
          label =
            if (sortMode == LeaderboardSortMode.TOTAL_POINTS) "${entry.totalRatingPoints} نقطة"
            else "${avgFormatter.format(entry.averageRating)} / 10",
          textColor = statusColors.goingText,
          bgColor = statusColors.goingBg,
          icon = Icons.Rounded.Star,
        )
        Text(
          text =
            if (sortMode == LeaderboardSortMode.TOTAL_POINTS) "متوسط ${avgFormatter.format(entry.averageRating)}/10"
            else "${entry.totalRatingPoints} نقطة",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
