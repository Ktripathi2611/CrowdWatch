package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportCategory
import com.example.ui.theme.CrowdWatchColors
import com.example.ui.viewmodel.ProximityFilter
import com.example.ui.viewmodel.VerificationStatusFilter
import kotlinx.coroutines.launch

/**
 * Modern Search and Filter UI component providing multi-criteria refinement:
 * - Proximity (Distance radius from live device GPS)
 * - Report Type (Incident Category)
 * - Verification Status (All, Verified Only, Active/Developing, Resolved, Disputed)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentSearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: ReportCategory?,
    onCategorySelected: (ReportCategory?) -> Unit,
    selectedProximity: ProximityFilter,
    onProximitySelected: (ProximityFilter) -> Unit,
    selectedVerification: VerificationStatusFilter,
    onVerificationSelected: (VerificationStatusFilter) -> Unit,
    onResetFilters: () -> Unit,
    totalMatchingCount: Int,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val activeFiltersCount = (if (selectedCategory != null) 1 else 0) +
            (if (selectedProximity != ProximityFilter.ANY) 1 else 0) +
            (if (selectedVerification != VerificationStatusFilter.ALL) 1 else 0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Input Bar with Filter Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Search incidents or locations...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrowdWatchColors.TextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CrowdWatchColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.testTag("search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = CrowdWatchColors.TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CrowdWatchColors.Surface,
                    unfocusedContainerColor = CrowdWatchColors.Surface,
                    focusedBorderColor = CrowdWatchColors.PrimaryBlue,
                    unfocusedBorderColor = CrowdWatchColors.Border,
                    focusedTextColor = CrowdWatchColors.TextPrimary,
                    unfocusedTextColor = CrowdWatchColors.TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("incident_search_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Filter button with badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (activeFiltersCount > 0) CrowdWatchColors.PrimaryBlueBg else CrowdWatchColors.Surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeFiltersCount > 0) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.Border
                ),
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showFilterSheet = true }
                    .testTag("open_filter_sheet_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BadgedBox(
                        badge = {
                            if (activeFiltersCount > 0) {
                                Badge(
                                    containerColor = CrowdWatchColors.PrimaryBlue,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "$activeFiltersCount",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Open Filters",
                            tint = if (activeFiltersCount > 0) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Quick Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Filter Trigger Chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (activeFiltersCount > 0) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.Surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeFiltersCount > 0) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.Border
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showFilterSheet = true }
                    .testTag("filter_chips_trigger")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (activeFiltersCount > 0) Color.White else CrowdWatchColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeFiltersCount > 0) "Filters ($activeFiltersCount)" else "Filters",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (activeFiltersCount > 0) Color.White else CrowdWatchColors.TextPrimary
                    )
                }
            }

            // Proximity Quick Chip
            QuickFilterChip(
                label = if (selectedProximity == ProximityFilter.ANY) "Proximity: All" else "Within ${selectedProximity.label}",
                isActive = selectedProximity != ProximityFilter.ANY,
                onClick = { showFilterSheet = true },
                testTag = "quick_filter_proximity"
            )

            // Report Type Quick Chip
            QuickFilterChip(
                label = selectedCategory?.displayName ?: "All Types",
                isActive = selectedCategory != null,
                onClick = { showFilterSheet = true },
                testTag = "quick_filter_type"
            )

            // Verification Status Quick Chip
            QuickFilterChip(
                label = selectedVerification.label,
                isActive = selectedVerification != VerificationStatusFilter.ALL,
                onClick = { showFilterSheet = true },
                testTag = "quick_filter_status"
            )

            // Clear All Filters Chip (if active)
            if (activeFiltersCount > 0 || searchQuery.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CrowdWatchColors.Emergency.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrowdWatchColors.Emergency.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onResetFilters() }
                        .testTag("quick_filter_reset")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reset Filters",
                            tint = CrowdWatchColors.Emergency,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CrowdWatchColors.Emergency
                        )
                    }
                }
            }
        }

        // Active filter pills preview bar
        AnimatedVisibility(
            visible = activeFiltersCount > 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active:",
                    style = MaterialTheme.typography.labelSmall,
                    color = CrowdWatchColors.TextTertiary,
                    modifier = Modifier.padding(end = 2.dp)
                )

                if (selectedProximity != ProximityFilter.ANY) {
                    ActiveFilterTag(
                        text = "Dist: ${selectedProximity.label}",
                        onDismiss = { onProximitySelected(ProximityFilter.ANY) }
                    )
                }

                if (selectedCategory != null) {
                    ActiveFilterTag(
                        text = selectedCategory.displayName,
                        onDismiss = { onCategorySelected(null) }
                    )
                }

                if (selectedVerification != VerificationStatusFilter.ALL) {
                    ActiveFilterTag(
                        text = selectedVerification.label,
                        onDismiss = { onVerificationSelected(VerificationStatusFilter.ALL) }
                    )
                }
            }
        }
    }

    // Comprehensive Filter Modal Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = CrowdWatchColors.Background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            IncidentFilterBottomSheetContent(
                selectedProximity = selectedProximity,
                onProximitySelected = onProximitySelected,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                selectedVerification = selectedVerification,
                onVerificationSelected = onVerificationSelected,
                onResetFilters = onResetFilters,
                matchingCount = totalMatchingCount,
                onApply = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showFilterSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) CrowdWatchColors.PrimaryBlueBg else CrowdWatchColors.Surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.Border
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isActive) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isActive) CrowdWatchColors.PrimaryBlue else CrowdWatchColors.TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ActiveFilterTag(
    text: String,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CrowdWatchColors.PrimaryBlueBg,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CrowdWatchColors.PrimaryBlue.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = CrowdWatchColors.PrimaryBlue
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                tint = CrowdWatchColors.PrimaryBlue,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidentFilterBottomSheetContent(
    selectedProximity: ProximityFilter,
    onProximitySelected: (ProximityFilter) -> Unit,
    selectedCategory: ReportCategory?,
    onCategorySelected: (ReportCategory?) -> Unit,
    selectedVerification: VerificationStatusFilter,
    onVerificationSelected: (VerificationStatusFilter) -> Unit,
    onResetFilters: () -> Unit,
    matchingCount: Int,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Filter Incident Reports",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CrowdWatchColors.TextPrimary
                )
                Text(
                    text = "Refine reports by proximity, type, and verification",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrowdWatchColors.TextSecondary
                )
            }

            TextButton(
                onClick = onResetFilters,
                modifier = Modifier.testTag("filter_sheet_reset_button")
            ) {
                Text(
                    text = "Reset All",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = CrowdWatchColors.Emergency
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: PROXIMITY
        FilterSectionHeader(
            icon = Icons.Default.NearMe,
            title = "Proximity from Live Location",
            subtitle = "Refine reports by physical distance from your device's GPS."
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProximityFilter.values().forEach { prox ->
                val isSelected = selectedProximity == prox
                FilterChip(
                    selected = isSelected,
                    onClick = { onProximitySelected(prox) },
                    label = {
                        Text(
                            text = prox.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrowdWatchColors.PrimaryBlueBg,
                        selectedLabelColor = CrowdWatchColors.PrimaryBlue,
                        selectedLeadingIconColor = CrowdWatchColors.PrimaryBlue,
                        containerColor = CrowdWatchColors.Surface,
                        labelColor = CrowdWatchColors.TextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = CrowdWatchColors.Border,
                        selectedBorderColor = CrowdWatchColors.PrimaryBlue
                    ),
                    modifier = Modifier.testTag("proximity_chip_${prox.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: REPORT TYPE / CATEGORY
        FilterSectionHeader(
            icon = Icons.Default.FilterList,
            title = "Report Type & Category",
            subtitle = "Select a specific domain of crowd-reported incident."
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // "All Types" Chip
            val isAllSelected = selectedCategory == null
            FilterChip(
                selected = isAllSelected,
                onClick = { onCategorySelected(null) },
                label = {
                    Text(
                        text = "All Categories",
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (isAllSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrowdWatchColors.PrimaryBlueBg,
                    selectedLabelColor = CrowdWatchColors.PrimaryBlue,
                    selectedLeadingIconColor = CrowdWatchColors.PrimaryBlue,
                    containerColor = CrowdWatchColors.Surface,
                    labelColor = CrowdWatchColors.TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isAllSelected,
                    borderColor = CrowdWatchColors.Border,
                    selectedBorderColor = CrowdWatchColors.PrimaryBlue
                ),
                modifier = Modifier.testTag("category_chip_all")
            )

            ReportCategory.values().forEach { cat ->
                val isSelected = selectedCategory == cat
                val catColor = getCategoryColor(cat)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(if (isSelected) null else cat) },
                    label = {
                        Text(
                            text = cat.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = null,
                            tint = if (isSelected) catColor else CrowdWatchColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = catColor.copy(alpha = 0.15f),
                        selectedLabelColor = catColor,
                        selectedLeadingIconColor = catColor,
                        containerColor = CrowdWatchColors.Surface,
                        labelColor = CrowdWatchColors.TextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = CrowdWatchColors.Border,
                        selectedBorderColor = catColor
                    ),
                    modifier = Modifier.testTag("category_chip_${cat.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 3: VERIFICATION STATUS
        FilterSectionHeader(
            icon = Icons.Default.Verified,
            title = "Verification Status",
            subtitle = "Filter reports by confidence thresholds and community verification."
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            VerificationStatusFilter.values().forEach { status ->
                val isSelected = selectedVerification == status
                FilterChip(
                    selected = isSelected,
                    onClick = { onVerificationSelected(status) },
                    label = {
                        Text(
                            text = when (status) {
                                VerificationStatusFilter.ALL -> "All Statuses"
                                VerificationStatusFilter.VERIFIED_ONLY -> "Verified Only (High Confidence)"
                                VerificationStatusFilter.ACTIVE_DEVELOPING -> "Active & Developing"
                                VerificationStatusFilter.RESOLVED -> "Resolved Hazards"
                                VerificationStatusFilter.DISPUTED -> "Disputed / Contested"
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrowdWatchColors.PrimaryBlueBg,
                        selectedLabelColor = CrowdWatchColors.PrimaryBlue,
                        selectedLeadingIconColor = CrowdWatchColors.PrimaryBlue,
                        containerColor = CrowdWatchColors.Surface,
                        labelColor = CrowdWatchColors.TextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = CrowdWatchColors.Border,
                        selectedBorderColor = CrowdWatchColors.PrimaryBlue
                    ),
                    modifier = Modifier.testTag("status_chip_${status.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Apply Action Button
        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("apply_filter_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrowdWatchColors.PrimaryBlue)
        ) {
            Text(
                text = "Show $matchingCount Matching Reports",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun FilterSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CrowdWatchColors.PrimaryBlueBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CrowdWatchColors.PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = CrowdWatchColors.TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = CrowdWatchColors.TextSecondary
            )
        }
    }
}
