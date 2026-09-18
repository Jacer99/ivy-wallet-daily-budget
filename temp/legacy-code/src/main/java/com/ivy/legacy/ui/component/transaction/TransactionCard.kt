package com.ivy.legacy.ui.component.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.LegacyTag
import com.ivy.base.legacy.Transaction
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.api.LocalTimeProvider
import com.ivy.design.l0_system.BlueLight
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.data.AppBaseData
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.utils.capitalizeLocal
import com.ivy.legacy.utils.dateNowUTC
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.legacy.utils.timeNowUTC
import com.ivy.navigation.Navigation
import com.ivy.navigation.TransactionsScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.specularBorder
import com.ivy.ui.time.TimeFormatter
import com.ivy.wallet.domain.data.IvyCurrency
import com.ivy.wallet.ui.theme.Blue
import com.ivy.wallet.ui.theme.Gradient
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.GradientIvy
import com.ivy.wallet.ui.theme.GradientOrangeRevert
import com.ivy.wallet.ui.theme.GradientRed
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.GreenDark
import com.ivy.wallet.ui.theme.Ivy
import com.ivy.wallet.ui.theme.IvyDark
import com.ivy.wallet.ui.theme.Orange
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun TransactionCard(
    baseData: AppBaseData,
    transaction: Transaction,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
    onSkipTransaction: (Transaction) -> Unit = {},
    onClick: (Transaction) -> Unit,
) {
    val isDark = isAppInDarkTheme()
    val cardShape = remember { RoundedCornerShape(20.dp) }

    LiquidGlassCard(
        shape = cardShape,
        isDark = isDark,
        strokeWidth = 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp)
            .clickable {
                if (baseData.accounts.find { it.id == transaction.accountId } != null) {
                    onClick(transaction)
                }
            }
            .testTag("transaction_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val transactionCurrency =
                baseData.accounts.find { it.id == transaction.accountId }?.currency
                    ?: baseData.baseCurrency

            val toAccountCurrency =
                baseData.accounts.find { it.id == transaction.toAccountId }?.currency
                    ?: baseData.baseCurrency

            TransactionHeaderRow(
                transaction = transaction,
                categories = baseData.categories,
                accounts = baseData.accounts,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions
            )

            if (transaction.dueDate != null) {
                Spacer(Modifier.height(10.dp))
                val timeFormatter = LocalTimeFormatter.current
                val timeProvider = LocalTimeProvider.current
                val dueDateColor = remember(transaction.dueDate, isDark) {
                    if (transaction.dueDate!!.isAfter(timeProvider.utcNow())) {
                        Orange
                    } else {
                        if (isDark) Color(0xFFF43F5E) else Color(0xFFB91C1C)
                    }
                }
                Text(
                    text = stringResource(
                        R.string.due_on,
                        with(timeFormatter) {
                            transaction.dueDate!!.formatLocal(
                                TimeFormatter.Style.DateOnly(includeWeekDay = true)
                            )
                        }
                    ).uppercase(),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = dueDateColor
                    )
                )
            }

            if (transaction.title.isNotNullOrBlank()) {
                Spacer(Modifier.height(if (transaction.dueDate != null) 6.dp else 10.dp))
                Text(
                    text = transaction.title!!,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UI.colors.pureInverse
                    )
                )
            }

            val description = getTransactionDescription(transaction)
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(if (transaction.title.isNotNullOrBlank()) 4.dp else 6.dp))
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = UI.colors.mediumInverse
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))

            TypeAmountCurrency(
                transactionType = transaction.type,
                dueDate = with(LocalTimeConverter.current) {
                    transaction.dueDate?.toLocalDateTime()
                },
                currency = transactionCurrency,
                amount = transaction.amount.toDouble(),
                isDark = isDark
            )

            if (transaction.type == TransactionType.TRANSFER && toAccountCurrency != transactionCurrency) {
                Spacer(Modifier.height(4.dp))
                Text(
                    modifier = Modifier.padding(start = 48.dp),
                    text = "${
                        transaction.toAmount.toDouble()
                            .format(IvyCurrency.getDecimalPlaces(toAccountCurrency))
                    } $toAccountCurrency",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = UI.colors.mediumInverse
                    )
                )
            }

            if (transaction.dueDate != null && transaction.dateTime == null) {
                // Settle / Pay CTA button for planned transactions
                Spacer(Modifier.height(14.dp))
                val isExpense = transaction.type == TransactionType.EXPENSE
                val buttonShape = remember { RoundedCornerShape(16.dp) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Skip button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(buttonShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f),
                                buttonShape
                            )
                            .specularBorder(buttonShape, isDark, 0.5.dp)
                            .clickable { onSkipTransaction(transaction) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.skip),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = UI.colors.pureInverse
                            )
                        )
                    }

                    // Pay / Get button
                    val payBg = remember(isExpense, isDark) {
                        if (isExpense) {
                            if (isDark) Color(0xFFF43F5E).copy(alpha = 0.25f) else Color(0xFFB91C1C).copy(alpha = 0.15f)
                        } else {
                            if (isDark) Color(0xFF00F2FE).copy(alpha = 0.25f) else Color(0xFF1E823E).copy(alpha = 0.15f)
                        }
                    }
                    val payTextColor = remember(isExpense, isDark) {
                        if (isExpense) {
                            if (isDark) Color(0xFFFECDD3) else Color(0xFFB91C1C)
                        } else {
                            if (isDark) Color(0xFF67E8F9) else Color(0xFF1E823E)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(buttonShape)
                            .background(payBg, buttonShape)
                            .specularBorder(buttonShape, isDark, 0.5.dp)
                            .clickable { onPayOrGet(transaction) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isExpense) stringResource(R.string.pay) else stringResource(R.string.get),
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = payTextColor
                            )
                        )
                    }
                }
            }

            if (transaction.tags.isNotEmpty()) {
                TransactionTags(transaction.tags)
            }
        }
    }
}

@Composable
private fun ColumnScope.TransactionTags(tags: ImmutableList<LegacyTag>) {
    Spacer(Modifier.height(10.dp))

    LazyRow {
        item {
            Text(
                text = "Tags:",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = UI.colors.mediumInverse
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        items(tags, key = { it.id }) { tag ->
            Text(
                text = "#${tag.name}",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlueLight
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionHeaderRow(
    transaction: Transaction,
    categories: List<Category>,
    accounts: List<Account>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
) {
    val nav = navigation()

    val category = category(
        categoryId = transaction.categoryId,
        categories = categories
    )

    if (transaction.type == TransactionType.TRANSFER) {
        Column {
            if (category != null) {
                CategoryBadgeDisplay(category, nav)
                Spacer(modifier = Modifier.height(6.dp))
            }
            TransferHeader(
                accounts = accounts,
                transaction = transaction,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions
            )
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (category != null) {
                CategoryBadgeDisplay(category, nav)
            }

            val account = account(
                accountId = transaction.accountId,
                accounts = accounts
            )

            val isDark = isAppInDarkTheme()
            val badgeShape = remember { CircleShape }

            Row(
                modifier = Modifier
                    .clip(badgeShape)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f),
                        badgeShape
                    )
                    .specularBorder(badgeShape, isDark, 0.5.dp)
                    .clickable {
                        account?.let {
                            nav.navigateTo(
                                TransactionsScreen(
                                    accountId = account.id,
                                    categoryId = null
                                )
                            )
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotColor = account?.color?.toComposeColor() ?: UI.colors.mediumInverse
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = account?.name ?: stringResource(R.string.deleted),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.mediumInverse
                    )
                )
            }
        }
    }
}

@Composable
fun CategoryBadgeDisplay(
    category: Category,
    nav: Navigation,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val categoryColor = category.color.value.toComposeColor()
    val circleShape = remember { CircleShape }
    val badgeShape = remember { RoundedCornerShape(14.dp) }

    Row(
        modifier = modifier
            .clip(badgeShape)
            .background(
                categoryColor.copy(alpha = if (isDark) 0.25f else 0.15f),
                badgeShape
            )
            .specularBorder(badgeShape, isDark, 0.5.dp)
            .clickable {
                nav.navigateTo(
                    TransactionsScreen(
                        accountId = null,
                        categoryId = category.id.value
                    )
                )
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(circleShape)
                .background(categoryColor.copy(alpha = if (isDark) 0.40f else 0.30f), circleShape),
            contentAlignment = Alignment.Center
        ) {
            ItemIconSDefaultIcon(
                iconName = category.icon?.id,
                defaultIcon = R.drawable.ic_custom_category_s,
                tint = UI.colors.pureInverse
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = category.name.value,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UI.colors.pureInverse
            )
        )
    }
}

@Composable
private fun getTransactionDescription(transaction: Transaction): String? {
    val paidFor = with(LocalTimeConverter.current) {
        transaction.paidFor?.toLocalDateTime()
    }
    return when {
        transaction.description.isNotNullOrBlank() -> transaction.description!!
        transaction.recurringRuleId != null &&
                transaction.dueDate == null &&
                paidFor != null -> {
            stringResource(
                R.string.bill_paid,
                paidFor.month.name.lowercase().capitalizeLocal(),
                paidFor.year.toString()
            )
        }

        else -> null
    }
}

@Composable
private fun TransferHeader(
    accounts: List<Account>,
    transaction: Transaction,
    shouldShowAccountSpecificColorInTransactions: Boolean
) {
    val isDark = isAppInDarkTheme()
    val account = remember(accounts, transaction) {
        accounts.find { transaction.accountId == it.id }
    }
    val toAccount = remember(accounts, transaction) {
        accounts.find { transaction.toAccountId == it.id }
    }
    val pillShape = remember { CircleShape }

    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f),
                pillShape
            )
            .specularBorder(pillShape, isDark, 0.5.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemIconSDefaultIcon(
            iconName = account?.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = UI.colors.pureInverse
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = account?.name.toString(),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UI.colors.pureInverse
            )
        )

        Spacer(Modifier.width(8.dp))

        IvyIcon(icon = R.drawable.ic_arrow_right, tint = UI.colors.mediumInverse)

        Spacer(Modifier.width(8.dp))

        ItemIconSDefaultIcon(
            iconName = toAccount?.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = UI.colors.pureInverse
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = toAccount?.name.toString(),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UI.colors.pureInverse
            )
        )
    }
}

@Composable
fun TypeAmountCurrency(
    transactionType: TransactionType,
    dueDate: LocalDateTime?,
    currency: String,
    amount: Double,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme()
) {
    val pureInverse = UI.colors.pureInverse

    Row(
        modifier = modifier.testTag("type_amount_currency"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val amountColor = remember(transactionType, isDark, pureInverse) {
            when (transactionType) {
                TransactionType.EXPENSE -> if (isDark) Color(0xFFFECDD3) else Color(0xFFB91C1C)
                TransactionType.INCOME -> if (isDark) Color(0xFF67E8F9) else Color(0xFF1E823E)
                TransactionType.TRANSFER -> pureInverse
            }
        }

        val iconBg = remember(transactionType, isDark) {
            when (transactionType) {
                TransactionType.EXPENSE -> if (isDark) Color(0xFFF43F5E).copy(alpha = 0.20f) else Color(0xFFB91C1C).copy(alpha = 0.12f)
                TransactionType.INCOME -> if (isDark) Color(0xFF00F2FE).copy(alpha = 0.20f) else Color(0xFF1E823E).copy(alpha = 0.12f)
                TransactionType.TRANSFER -> if (isDark) Color(0xFF7C4DFF).copy(alpha = 0.20f) else Color(0xFF9E4622).copy(alpha = 0.12f)
            }
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconBg, CircleShape)
                .specularBorder(CircleShape, isDark, 0.5.dp),
            contentAlignment = Alignment.Center
        ) {
            IvyIcon(
                icon = when (transactionType) {
                    TransactionType.INCOME -> R.drawable.ic_income
                    TransactionType.EXPENSE -> R.drawable.ic_expense
                    TransactionType.TRANSFER -> R.drawable.ic_transfer
                },
                tint = amountColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = amount.format(currency),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor,
                    letterSpacing = (-0.02).sp
                )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = currency,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UI.colors.mediumInverse
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewUpcomingExpense() {
    IvyWalletPreview {
        LazyColumn(Modifier.fillMaxSize()) {
            val cash = Account(name = "Cash", Green.toArgb())
            val food = Category(
                name = NotBlankTrimmedString.unsafe("Food"),
                color = ColorInt(Blue.toArgb()),
                icon = null,
                id = CategoryId(UUID.randomUUID()),
                orderNum = 0.0,
            )

            item {
                TransactionCard(
                    baseData = AppBaseData(
                        baseCurrency = "BGN",
                        categories = persistentListOf(food),
                        accounts = persistentListOf(cash)
                    ),
                    transaction = Transaction(
                        accountId = cash.id,
                        title = "Lidl pazar",
                        categoryId = food.id.value,
                        amount = 250.75.toBigDecimal(),
                        dueDate = timeNowUTC().plusDays(5).toInstant(ZoneOffset.UTC),
                        dateTime = null,
                        type = TransactionType.EXPENSE,
                    ),
                    shouldShowAccountSpecificColorInTransactions = false,
                    onPayOrGet = {},
                ) {
                }
            }
        }
    }
}
