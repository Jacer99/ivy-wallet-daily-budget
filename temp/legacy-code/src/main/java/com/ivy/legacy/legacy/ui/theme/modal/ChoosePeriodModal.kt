package com.ivy.wallet.ui.theme.modal

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.IntervalType
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.api.LocalTimeProvider
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.data.model.FromToTimeRange
import com.ivy.legacy.data.model.LastNTimeRange
import com.ivy.legacy.data.model.Month
import com.ivy.legacy.data.model.Month.Companion.fromMonthValue
import com.ivy.legacy.data.model.Month.Companion.monthsList
import com.ivy.legacy.data.model.TimePeriod
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.addKeyboardListener
import com.ivy.legacy.utils.dateNowUTC
import com.ivy.legacy.utils.formatDateOnlyWithYear
import com.ivy.legacy.utils.onScreenStart
import com.ivy.ui.R
import com.ivy.ui.component.specularBorder
import com.ivy.wallet.ui.theme.Gradient
import com.ivy.wallet.ui.theme.GradientIvy
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.CircleButtonFilled
import com.ivy.wallet.ui.theme.components.IntervalPickerRow
import com.ivy.wallet.ui.theme.components.IvyDividerLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@Deprecated("Old design system. Use `:ivy-design` and Material3")
data class ChoosePeriodModalData(
    val id: UUID = UUID.randomUUID(),
    val period: TimePeriod
)

@SuppressLint("ComposeModifierMissing")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Suppress("ParameterNaming")
@Composable
fun BoxWithConstraintsScope.ChoosePeriodModal(
    modal: ChoosePeriodModalData?,
    dismiss: () -> Unit,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    var period by remember(modal) {
        mutableStateOf(modal?.period)
    }

    val ivyContext = ivyWalletCtx()
    val modalScrollState = rememberScrollState()

    IvyModal(
        id = modal?.id,
        visible = modal != null,
        dismiss = dismiss,
        scrollState = modalScrollState,
        PrimaryAction = {
            ModalSet(
                enabled = period != null && period!!.isValid()
            ) {
                if (period != null) {
                    ivyContext.updateSelectedPeriodInMemory(period!!)
                    dismiss()
                    onPeriodSelected(period!!)
                }
            }
        }
    ) {
        Spacer(Modifier.height(24.dp))

        ChooseMonth(
            selectedMonthYear = period?.month?.let {
                MonthYear(month = it, year = period?.year ?: dateNowUTC().year)
            }
        ) {
            period = TimePeriod(
                month = it.month,
                year = it.year
            )
        }

        Spacer(Modifier.height(24.dp))

        IvyDividerLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        FromToRange(
            timeRange = period?.fromToRange
        ) {
            period = TimePeriod(
                fromToRange = it
            )
        }

        Spacer(Modifier.height(24.dp))

        LastNPeriod(
            modalScrollState = modalScrollState,
            lastNTimeRange = period?.lastNRange,
        ) {
            period = TimePeriod(
                lastNRange = it
            )
        }

        Spacer(Modifier.height(24.dp))

        AllTime(
            timeRange = period?.fromToRange
        ) {
            period = TimePeriod(
                fromToRange = it
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
@Suppress("ParameterNaming")
private fun ColumnScope.ChooseMonth(
    selectedMonthYear: MonthYear?,
    onSelected: (MonthYear) -> Unit,
) {
    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = stringResource(R.string.choose_month),
        style = TextStyle(
            fontSize = 15.sp,
            color = if (selectedMonthYear != null) UI.colors.pureInverse else UI.colors.mediumInverse,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(16.dp))

    val currentYear = dateNowUTC().year
    val months = remember(currentYear) {
        monthsList()
            .map {
                MonthYear(month = it, year = currentYear - 1)
            }
            .plus(
                monthsList().map { MonthYear(month = it, year = currentYear) }
            )
            .plus(
                monthsList().map { MonthYear(month = it, year = currentYear + 1) }
            )
    }

    val state = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    onScreenStart {
        if (selectedMonthYear != null) {
            val selectedMonthIndex = months.indexOf(selectedMonthYear)
            if (selectedMonthIndex != -1) {
                coroutineScope.launch {
                    state.scrollToItem(selectedMonthIndex)
                }
            }
        } else {
            val currentMonthYear = MonthYear(
                month = fromMonthValue(dateNowUTC().monthValue),
                year = currentYear
            )
            val currentMonthIndex = months.indexOf(currentMonthYear)
            if (currentMonthIndex != -1) {
                coroutineScope.launch {
                    state.scrollToItem(currentMonthIndex)
                }
            }
        }
    }

    LazyRow(
        state = state,
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Spacer(Modifier.width(20.dp))
        }

        items(items = months) { monthYear ->
            MonthButton(
                selected = monthYear == selectedMonthYear,
                text = monthYear.forDisplay(currentYear = currentYear)
            ) {
                onSelected(monthYear)
            }

            Spacer(Modifier.width(10.dp))
        }
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
data class MonthYear(
    val month: Month,
    val year: Int
) {
    fun forDisplay(
        currentYear: Int
    ): String {
        return if (year != currentYear) {
            "${month.name}, $year"
        } else {
            month.name
        }
    }
}

@Composable
private fun MonthButton(
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isDark = isAppInDarkTheme()
    val pillShape = remember { CircleShape }
    val pureInverse = UI.colors.pureInverse

    val fill = remember(selected, isDark) {
        if (selected) {
            if (isDark) Color(0xFF7C4DFF).copy(alpha = 0.35f) else Color(0xFF9E4622).copy(alpha = 0.25f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f)
        }
    }
    val borderCol = remember(selected, isDark) {
        if (selected) {
            if (isDark) Color(0xFF00F2FE).copy(alpha = 0.50f) else Color(0xFFC2623A).copy(alpha = 0.50f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f)
        }
    }
    val textColor = remember(selected, isDark, pureInverse) {
        if (selected) {
            if (isDark) Color(0xFF67E8F9) else Color(0xFF9E4622)
        } else {
            pureInverse
        }
    }

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(fill, pillShape)
            .border(0.5.dp, borderCol, pillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        )
    }
}

@Composable
@Suppress("ParameterNaming")
private fun ColumnScope.FromToRange(
    timeRange: FromToTimeRange?,
    onSelected: (FromToTimeRange?) -> Unit,
) {
    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = stringResource(R.string.or_custom_range),
        style = TextStyle(
            fontSize = 15.sp,
            color = if (timeRange != null) UI.colors.pureInverse else UI.colors.mediumInverse,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(16.dp))

    val converter = LocalTimeConverter.current
    IntervalFromToDate(
        border = IntervalBorder.FROM,
        dateTime = with(converter) { timeRange?.from?.toLocalDateTime() },
        otherEndDateTime = with(converter) { timeRange?.to?.toLocalDateTime() }
    ) { from ->
        onSelected(
            if (from == null && timeRange?.to == null) {
                null
            } else {
                timeRange?.copy(
                    from = with(converter) { from?.toUTC() }
                ) ?: FromToTimeRange(
                    from = with(converter) { from?.toUTC() },
                    to = null
                )
            }
        )
    }

    Spacer(Modifier.height(12.dp))

    IntervalFromToDate(
        border = IntervalBorder.TO,
        dateTime = with(converter) { timeRange?.to?.toLocalDateTime() },
        otherEndDateTime = with(converter) { timeRange?.from?.toLocalDateTime() },
    ) { to ->
        onSelected(
            if (timeRange?.from == null && to == null) {
                null
            } else {
                timeRange?.copy(
                    to = with(converter) { to?.plusDays(1)?.minusNanos(1)?.toUTC() }
                ) ?: FromToTimeRange(
                    from = null,
                    to = with(converter) { to?.toUTC() }
                )
            }
        )
    }
}

@Composable
@Suppress("ParameterNaming")
private fun IntervalFromToDate(
    border: IntervalBorder,
    dateTime: LocalDateTime?,
    otherEndDateTime: LocalDateTime?,
    onSelected: (LocalDateTime?) -> Unit
) {
    val isDark = isAppInDarkTheme()
    val ivyContext = ivyWalletCtx()
    val pillShape = remember { CircleShape }

    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(pillShape)
            .background(
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f),
                pillShape
            )
            .specularBorder(pillShape, isDark, 0.5.dp)
            .clickable {
                ivyContext.datePicker(
                    minDate = if (border == IntervalBorder.TO) {
                        otherEndDateTime
                            ?.toLocalDate()
                            ?.plusDays(1)
                    } else {
                        null
                    },
                    maxDate = if (border == IntervalBorder.FROM) {
                        otherEndDateTime
                            ?.toLocalDate()
                            ?.minusDays(1)
                    } else {
                        null
                    },
                    initialDate = dateTime?.toLocalDate()
                ) {
                    onSelected(it.atStartOfDay())
                }
            }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (border == IntervalBorder.FROM) {
                stringResource(R.string.from)
            } else {
                stringResource(R.string.to)
            },
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (dateTime != null) (if (isDark) Color(0xFF67E8F9) else Color(0xFF1E823E)) else UI.colors.pureInverse
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = dateTime?.toLocalDate()?.formatDateOnlyWithYear()
                ?: stringResource(R.string.add_date),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (dateTime != null) UI.colors.pureInverse else UI.colors.mediumInverse
            )
        )

        if (dateTime != null) {
            Spacer(Modifier.width(12.dp))

            CircleButtonFilled(
                icon = R.drawable.ic_dismiss
            ) {
                onSelected(null)
            }
        }
    }
}

private enum class IntervalBorder {
    FROM, TO
}

@Composable
@Suppress("ParameterNaming")
private fun ColumnScope.LastNPeriod(
    modalScrollState: ScrollState,
    lastNTimeRange: LastNTimeRange?,
    onSelected: (LastNTimeRange) -> Unit
) {
    val rootView = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    onScreenStart {
        rootView.addKeyboardListener { keyboardShown ->
            if (keyboardShown) {
                coroutineScope.launch {
                    delay(200)
                    modalScrollState.animateScrollTo(modalScrollState.maxValue)
                }
            }
        }
    }

    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = stringResource(R.string.or_in_the_last),
        style = TextStyle(
            fontSize = 15.sp,
            color = if (lastNTimeRange != null) UI.colors.pureInverse else UI.colors.mediumInverse,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(16.dp))

    IntervalPickerRow(
        intervalN = lastNTimeRange?.periodN ?: 0,
        intervalType = lastNTimeRange?.periodType ?: IntervalType.WEEK,
        onSetIntervalN = {
            onSelected(
                lastNTimeRange?.copy(
                    periodN = it
                ) ?: LastNTimeRange(
                    periodN = it,
                    periodType = IntervalType.WEEK
                )
            )
        },
        onSetIntervalType = {
            onSelected(
                lastNTimeRange?.copy(
                    periodType = it
                ) ?: LastNTimeRange(
                    periodN = 1,
                    periodType = it
                )
            )
        }
    )
}

@Composable
@Suppress("ParameterNaming", "MagicNumber")
private fun ColumnScope.AllTime(
    timeRange: FromToTimeRange?,
    onSelected: (FromToTimeRange?) -> Unit,
) {
    val timeProvider = LocalTimeProvider.current
    val active = timeRange != null && timeRange.from == null &&
            timeRange.to != null && timeRange.to!!.isAfter(timeProvider.utcNow())

    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = stringResource(R.string.or_all_time),
        style = TextStyle(
            fontSize = 15.sp,
            color = if (active) UI.colors.pureInverse else UI.colors.mediumInverse,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(16.dp))

    MonthButton(
        modifier = Modifier.padding(start = 32.dp),
        selected = active,
        text = if (active) stringResource(R.string.unselect_all_time) else stringResource(R.string.select_all_time)
    ) {
        onSelected(
            if (active) {
                null
            } else {
                FromToTimeRange(
                    from = null,
                    to = timeProvider.utcNow().plusSeconds(TimeUnit.HOURS.toSeconds(24L))
                )
            }
        )
    }
}

@Preview
@Composable
private fun Preview_MonthSelected() {
    IvyWalletPreview {
        ChoosePeriodModal(
            modal = ChoosePeriodModalData(
                period = TimePeriod(
                    month = fromMonthValue(3)
                )
            ),
            dismiss = {}
        ) {
        }
    }
}
