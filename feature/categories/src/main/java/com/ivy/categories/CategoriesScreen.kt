package com.ivy.categories

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Theme
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.balancePrefix
import com.ivy.legacy.utils.compactBalancePrefix
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.selectEndTextFieldValue
import com.ivy.navigation.CategoriesScreen
import com.ivy.navigation.TransactionsScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.pocketMoneyBackground
import com.ivy.ui.component.specularBorder
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.wallet.domain.data.SortOrder
import com.ivy.wallet.ui.theme.Gradient
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.GreenDark
import com.ivy.wallet.ui.theme.GreenLight
import com.ivy.wallet.ui.theme.IvyDark
import com.ivy.wallet.ui.theme.Orange
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.ReorderModalSingleType
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalSet
import com.ivy.wallet.ui.theme.modal.ModalTitle
import com.ivy.wallet.ui.theme.modal.edit.CategoryModal
import com.ivy.wallet.ui.theme.modal.edit.CategoryModalData
import com.ivy.wallet.ui.theme.toComposeColor
import com.ivy.wallet.ui.theme.wallet.AmountCurrencyB1
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.CategoriesScreen(screen: CategoriesScreen) {
    val viewModel: CategoriesViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

    UI(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: CategoriesScreenState = CategoriesScreenState(
        compactCategoriesModeEnabled = false,
        showCategorySearchBar = false
    ),
    onEvent: (CategoriesScreenEvent) -> Unit = {}
) {
    val nav = navigation()
    val isDark = !UI.colors.isLight
    val ivyContext = ivyWalletCtx()
    var listState = rememberLazyListState()
    if (!state.categories.isEmpty()) {
        listState = rememberScrollPositionListState(
            key = "categories_lazy_column",
            initialFirstVisibleItemIndex = ivyContext.categoriesListState?.firstVisibleItemIndex
                ?: 0,
            initialFirstVisibleItemScrollOffset = ivyContext.categoriesListState?.firstVisibleItemScrollOffset
                ?: 0
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pocketMoneyBackground(isDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            state = listState,
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Spacer(Modifier.height(24.dp))

                // --- Header & Sort / Filter Bar ---------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.categories),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = UI.colors.pureInverse
                        )
                    )

                    Spacer(Modifier.weight(1f))

                    // Circular frosted sort button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f)
                                else Color.White.copy(alpha = 0.60f)
                            )
                            .specularBorder(CircleShape, isDark, strokeWidth = 0.5.dp)
                            .clickable {
                                onEvent(CategoriesScreenEvent.OnSortOrderModalVisible(visible = true))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        IvyIcon(
                            icon = R.drawable.ic_sort_by_alpha_24,
                            tint = UI.colors.pureInverse,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Circular frosted reorder button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f)
                                else Color.White.copy(alpha = 0.60f)
                            )
                            .specularBorder(CircleShape, isDark, strokeWidth = 0.5.dp)
                            .clickable {
                                onEvent(CategoriesScreenEvent.OnReorderModalVisible(true))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        IvyIcon(
                            icon = R.drawable.ic_reorder,
                            tint = UI.colors.pureInverse,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Frosted glass search pill
                SearchField(
                    onSearch = { onEvent(CategoriesScreenEvent.OnSearchQueryUpdate(it)) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(8.dp))
            }

            items(state.categories, key = { it.category.id.value }) { categoryData ->
                CategoryCard(
                    currency = state.baseCurrency,
                    categoryData = categoryData,
                    compactModeEnabled = state.compactCategoriesModeEnabled,
                    onLongClick = {
                        onEvent(CategoriesScreenEvent.OnReorderModalVisible(true))
                    }
                ) {
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = null,
                            categoryId = categoryData.category.id.value
                        )
                    )
                }
            }

            item {
                Spacer(Modifier.height(110.dp)) // bottom floating bar clearance
            }
        }

        // Floating frosted glass CTA bottom bar
        CategoriesBottomBar(
            onAddCategory = {
                onEvent(
                    CategoriesScreenEvent.OnCategoryModalVisible(
                        CategoryModalData(category = null)
                    )
                )
            },
            onClose = {
                nav.back()
            },
        )

        ReorderModalSingleType(
            visible = state.reorderModalVisible,
            initialItems = state.categories,
            dismiss = {
                onEvent(CategoriesScreenEvent.OnReorderModalVisible(false))
            },
            onReordered = {
                onEvent(CategoriesScreenEvent.OnReorder(it))
            }
        ) { _, item ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp)
                    .padding(vertical = 8.dp),
                text = item.category.name.value,
                style = UI.typo.b1.style(
                    color = item.category.color.value.toComposeColor(),
                    fontWeight = FontWeight.Bold
                )
            )
        }

        this@UI.CategoryModal(
            modal = state.categoryModalData,
            onCreateCategory = {
                onEvent(CategoriesScreenEvent.OnCreateCategory(it))
            },
            onEditCategory = { },
            dismiss = {
                onEvent(CategoriesScreenEvent.OnCategoryModalVisible(null))
            }
        )

        this@UI.SortModal(
            initialType = state.sortOrder,
            items = state.sortOrderItems,
            visible = state.sortModalVisible,
            dismiss = {
                onEvent(CategoriesScreenEvent.OnSortOrderModalVisible(visible = false))
            },
            onSortOrderChange = {
                onEvent(CategoriesScreenEvent.OnReorder(state.categories, it))
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCard(
    currency: String,
    categoryData: CategoryData,
    compactModeEnabled: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val isDark = !UI.colors.isLight
    val category = categoryData.category
    val categoryColor = category.color.value.toComposeColor()
    val tintAlpha = if (isDark) 0.20f else 0.12f
    val glassTint = categoryColor.copy(alpha = tintAlpha)
    val cardShape = remember { RoundedCornerShape(26.dp) }
    val circleShape = remember { CircleShape }
    val compactCardShape = remember { RoundedCornerShape(20.dp) }

    if (!compactModeEnabled) {
        Spacer(Modifier.height(12.dp))
        LiquidGlassCard(
            shape = cardShape,
            isDark = isDark,
            fillColor = glassTint,
            strokeWidth = 0.5.dp,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row: Icon, Category Name, and Monthly Total Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(circleShape)
                            .background(categoryColor.copy(alpha = if (isDark) 0.35f else 0.25f))
                            .specularBorder(circleShape, isDark, strokeWidth = 0.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ItemIconSDefaultIcon(
                            iconName = category.icon?.id,
                            defaultIcon = R.drawable.ic_custom_category_s,
                            tint = UI.colors.pureInverse
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Text(
                        text = category.name.value,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = UI.colors.pureInverse
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    val rawPrefix = balancePrefix(
                        income = categoryData.monthlyIncome,
                        expenses = categoryData.monthlyExpenses
                    )
                    val balancePrefixValue = if (rawPrefix.isNullOrBlank()) "" else rawPrefix
                    val formattedAmount = categoryData.monthlyBalance
                        .format(currency)
                        .replace(Regex("[+-]"), "")

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$balancePrefixValue$formattedAmount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = UI.colors.pureInverse
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = UI.colors.mediumInverse
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Subtle translucent horizontal divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.10f)
                            else Color.Black.copy(alpha = 0.08f)
                        )
                )

                Spacer(Modifier.height(14.dp))

                // Monthly sub-metrics ("EXPENSES THIS MONTH", "INCOME THIS MONTH")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expenses this month
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.month_expenses).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = UI.colors.mediumInverse,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val expFormatted = categoryData.monthlyExpenses
                                .format(currency)
                                .replace(Regex("[+-]"), "")
                            Text(
                                text = expFormatted,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = UI.colors.pureInverse
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = UI.colors.mediumInverse,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Subtle vertical translucent divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.10f)
                                else Color.Black.copy(alpha = 0.08f)
                            )
                    )

                    // Income this month
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.month_income).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = UI.colors.mediumInverse,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val incFormatted = categoryData.monthlyIncome
                                .format(currency)
                                .replace(Regex("[+-]"), "")
                            Text(
                                text = incFormatted,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = UI.colors.pureInverse
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = UI.colors.mediumInverse,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    } else {
        Spacer(Modifier.height(8.dp))
        LiquidGlassCard(
            shape = compactCardShape,
            isDark = isDark,
            fillColor = glassTint,
            strokeWidth = 0.5.dp,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(circleShape)
                        .background(categoryColor.copy(alpha = if (isDark) 0.35f else 0.25f))
                        .specularBorder(circleShape, isDark, strokeWidth = 0.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ItemIconSDefaultIcon(
                        iconName = category.icon?.id,
                        defaultIcon = R.drawable.ic_custom_account_s,
                        tint = UI.colors.pureInverse
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = category.name.value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.pureInverse
                    ),
                    modifier = Modifier.weight(1f)
                )

                val rawCompactPrefix = compactBalancePrefix(
                    income = categoryData.monthlyIncome,
                    expenses = categoryData.monthlyExpenses
                )
                val balancePrefixValue = if (rawCompactPrefix.isBlank()) "" else rawCompactPrefix
                val currencyFormatted = categoryData.monthlyBalance
                    .format(currency)
                    .replace(Regex("[+-]"), "")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$balancePrefixValue$currencyFormatted",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = UI.colors.mediumInverse,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AddedSpent(
    monthlyIncome: Double,
    monthlyExpenses: Double,
    currency: String,
    modifier: Modifier = Modifier,
    textColor: Color = UI.colors.pureInverse,
    dividerColor: Color = UI.colors.medium,
    center: Boolean = true,
    dividerSpacer: Dp? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (center) {
            Spacer(Modifier.weight(1f))
        }

        LabelAmount(
            textColor = textColor,
            label = stringResource(R.string.month_expenses),
            amount = monthlyExpenses,
            currency = currency,
            center = center
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }

        if (dividerSpacer != null) {
            Spacer(modifier = Modifier.width(dividerSpacer))
        }

        // Divider
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .height(48.dp)
                .background(dividerColor, UI.shapes.rFull)
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }

        if (dividerSpacer != null) {
            Spacer(modifier = Modifier.width(dividerSpacer))
        }

        LabelAmount(
            textColor = textColor,
            label = stringResource(R.string.month_income),
            amount = monthlyIncome,
            currency = currency,
            center = center
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun LabelAmount(
    label: String,
    amount: Double,
    currency: String,
    textColor: Color,
    center: Boolean
) {
    Column(
        horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = label,
            style = UI.typo.c.style(
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AmountCurrencyB1(
                textColor = textColor,
                amount = amount,
                currency = currency
            )
        }
    }
}

@Suppress("UnusedParameter")
@Composable
fun BoxWithConstraintsScope.SortModal(
    items: ImmutableList<SortOrder>,
    visible: Boolean,
    initialType: SortOrder,
    dismiss: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.sort_by),
    id: UUID = UUID.randomUUID()
) {
    var sortOrder by remember(initialType) {
        mutableStateOf(initialType)
    }

    val applyChange = {
        onSortOrderChange(sortOrder)
        dismiss()
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalSet {
                applyChange()
            }
        },
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = title)

        Spacer(Modifier.height(32.dp))

        items.forEach {
            SelectTypeButton(
                text = it.displayName,
                icon = when (it) {
                    SortOrder.DEFAULT -> R.drawable.ic_custom_star_s
                    SortOrder.BALANCE_AMOUNT -> R.drawable.ic_vue_money_coins
                    SortOrder.EXPENSES -> R.drawable.ic_expense
                    SortOrder.ALPHABETICAL -> R.drawable.ic_sort_by_alpha_24
                },
                selected = it == sortOrder
            ) {
                sortOrder = it
                applyChange()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SelectTypeButton(
    text: String,
    @DrawableRes icon: Int,
    selected: Boolean,
    selectedGradient: Gradient = GradientGreen,
    textSelectedColor: Color = White,
    onClick: () -> Unit
) {
    val isDark = !UI.colors.isLight
    val shape = remember { RoundedCornerShape(16.dp) }

    val selectedBrush = remember(isDark) {
        if (isDark) {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF7C4DFF).copy(alpha = 0.35f),
                    Color(0xFF00F2FE).copy(alpha = 0.25f)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF9E4622).copy(alpha = 0.25f),
                    Color(0xFFC2623A).copy(alpha = 0.18f)
                )
            )
        }
    }

    val unselectedColor = remember(isDark) {
        if (isDark) {
            Color.White.copy(alpha = 0.06f)
        } else {
            Color.White.copy(alpha = 0.50f)
        }
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(60.dp)
            .clip(shape)
            .background(
                brush = if (selected) selectedBrush else SolidColor(unselectedColor)
            )
            .specularBorder(shape, isDark, strokeWidth = 0.5.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textColor = if (selected) {
            if (isDark) Color(0xFF99F6E4) else Color(0xFFC2623A)
        } else {
            UI.colors.pureInverse
        }

        IvyIcon(
            icon = icon,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = UI.typo.b1.style(
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
        )

        if (selected) {
            IvyIcon(
                icon = R.drawable.ic_check,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCategoriesCompactModeEnabled(theme: Theme = Theme.LIGHT) {
    Preview(theme = theme, compactModeEnabled = true)
}

@Preview
@Composable
private fun PreviewCategoriesCompactModeEnabledAndSearchBarEnabled(theme: Theme = Theme.LIGHT) {
    Preview(theme = theme, compactModeEnabled = true, displaySearchBarEnabled = true)
}

@Preview
@Composable
private fun Preview(
    theme: Theme = Theme.LIGHT,
    compactModeEnabled: Boolean = false,
    displaySearchBarEnabled: Boolean = false
) {
    IvyWalletPreview(theme) {
        val state = CategoriesScreenState(
            baseCurrency = "BGN",
            compactCategoriesModeEnabled = compactModeEnabled,
            showCategorySearchBar = displaySearchBarEnabled,
            categories = persistentListOf(
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Groceries"),
                        color = ColorInt(Green.toArgb()),
                        icon = IconAsset.unsafe("groceries"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 2125.0,
                    monthlyExpenses = 920.0,
                    monthlyIncome = 3045.0
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Fun"),
                        color = ColorInt(Orange.toArgb()),
                        icon = IconAsset.unsafe("game"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 1200.0,
                    monthlyExpenses = 750.0,
                    monthlyIncome = 0.0
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Ivy"),
                        color = ColorInt(IvyDark.toArgb()),
                        icon = IconAsset.unsafe("star"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 1200.0,
                    monthlyExpenses = 0.0,
                    monthlyIncome = 5000.0
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Food"),
                        color = ColorInt(GreenLight.toArgb()),
                        icon = IconAsset.unsafe("atom"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 12125.21,
                    monthlyExpenses = 1350.50,
                    monthlyIncome = 8000.48
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Shisha"),
                        color = ColorInt(GreenDark.toArgb()),
                        icon = IconAsset.unsafe("drink"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 820.0,
                    monthlyExpenses = 340.0,
                    monthlyIncome = 400.0
                ),
            )
        )
        UI(state = state)
    }
}

@Preview
@Composable
private fun PreviewWithSearchBarEnabled(
    theme: Theme = Theme.LIGHT,
    compactModeEnabled: Boolean = false,
    displaySearchBarEnabled: Boolean = true
) {
    IvyWalletPreview(theme) {
        val state = CategoriesScreenState(
            baseCurrency = "BGN",
            compactCategoriesModeEnabled = compactModeEnabled,
            showCategorySearchBar = displaySearchBarEnabled,
            categories = persistentListOf(
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Groceries"),
                        color = ColorInt(Green.toArgb()),
                        icon = IconAsset.unsafe("groceries"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 2125.0,
                    monthlyExpenses = 920.0,
                    monthlyIncome = 3045.0
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Fun"),
                        color = ColorInt(Orange.toArgb()),
                        icon = IconAsset.unsafe("game"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 1200.0,
                    monthlyExpenses = 750.0,
                    monthlyIncome = 0.0
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Ivy"),
                        color = ColorInt(IvyDark.toArgb()),
                        icon = IconAsset.unsafe("star"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 1200.0,
                    monthlyExpenses = 0.0,
                    monthlyIncome = 5000.0
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Food"),
                        color = ColorInt(GreenLight.toArgb()),
                        icon = IconAsset.unsafe("atom"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 12125.21,
                    monthlyExpenses = 1350.50,
                    monthlyIncome = 8000.48
                ),
                CategoryData(
                    category = Category(
                        id = CategoryId(UUID.randomUUID()),
                        name = NotBlankTrimmedString.unsafe("Shisha"),
                        color = ColorInt(GreenDark.toArgb()),
                        icon = IconAsset.unsafe("drink"),
                        orderNum = 0.0,
                    ),
                    monthlyBalance = 820.0,
                    monthlyExpenses = 340.0,
                    monthlyIncome = 400.0
                ),
            )
        )
        UI(state = state)
    }
}

@Composable
private fun SearchField(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = !UI.colors.isLight
    var searchQueryTextFieldValue by remember {
        mutableStateOf(selectEndTextFieldValue(""))
    }

    LiquidGlassCard(
        shape = CircleShape,
        isDark = isDark,
        fillColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.65f),
        strokeWidth = 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IvyIcon(
                icon = R.drawable.ic_search,
                tint = UI.colors.mediumInverse,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQueryTextFieldValue.text.isEmpty()) {
                    Text(
                        text = "Search categories",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = UI.colors.mediumInverse
                        )
                    )
                }
                BasicTextField(
                    value = searchQueryTextFieldValue,
                    onValueChange = {
                        searchQueryTextFieldValue = it
                        onSearch(it.text)
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (searchQueryTextFieldValue.text.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(UI.colors.mediumInverse.copy(alpha = 0.2f))
                        .clickable {
                            searchQueryTextFieldValue = selectEndTextFieldValue("")
                            onSearch("")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = UI.colors.pureInverse,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/** For screenshot testing */
@Composable
fun CategoriesScreenUiTest(isDark: Boolean) {
    val theme = when (isDark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    Preview(theme)
}

/** For screenshot testing */
@Composable
fun CategoriesScreenWithSearchBarUiTest(isDark: Boolean) {
    val theme = when (isDark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    Preview(theme = theme, displaySearchBarEnabled = true)
}

/** For screenshot testing */
@Composable
fun CategoriesScreenCompactUiTest(isDark: Boolean) {
    val theme = when (isDark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    Preview(theme, compactModeEnabled = true)
}

/** For screenshot testing */
@Composable
fun CategoriesScreenWithSearchBarCompactUiTest(isDark: Boolean) {
    val theme = when (isDark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    Preview(theme, compactModeEnabled = true, displaySearchBarEnabled = true)
}
