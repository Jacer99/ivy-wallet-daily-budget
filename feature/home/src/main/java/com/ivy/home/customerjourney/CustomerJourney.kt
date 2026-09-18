package com.ivy.home.customerjourney

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.domain.RootScreen
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.rootScreen
import com.ivy.navigation.IvyPreview
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.specularBorder
import com.ivy.wallet.ui.theme.components.IvyIcon
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CustomerJourney(
    customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,
    modifier: Modifier = Modifier,
    onDismiss: (CustomerJourneyCardModel) -> Unit,
) {
    val ivyContext = ivyWalletCtx()
    val nav = navigation()
    // Check is added for Paparazzi Test where context is different
    if (LocalContext.current is RootScreen) {
        val rootScreen = rootScreen()

        if (customerJourneyCards.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
        }

        for (card in customerJourneyCards) {
            Spacer(Modifier.height(12.dp))

            CustomerJourneyCard(
                modifier = modifier,
                cardData = card,
                onDismiss = {
                    onDismiss(card)
                }
            ) {
                card.onAction(nav, ivyContext, rootScreen)
            }
        }
    } else {
        Box(modifier)
    }
}

@Composable
fun CustomerJourneyCard(
    cardData: CustomerJourneyCardModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onCTA: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (cardData.id == "adjust_balance") {
        Color(0xFF7C4DFF)
    } else {
        cardData.background.startColor
    }
    val bgTint = tintColor.copy(alpha = if (isDark) 0.22f else 0.12f)

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                onCTA()
            },
        shape = RoundedCornerShape(26.dp),
        isDark = isDark,
        fillColor = bgTint
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = cardData.title,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.pureInverse
                    )
                )

                if (cardData.hasDismiss) {
                    IvyIcon(
                        modifier = Modifier
                            .clickable {
                                onDismiss()
                            }
                            .padding(4.dp),
                        icon = R.drawable.ic_dismiss,
                        tint = UI.colors.mediumInverse,
                        contentDescription = "prompt_dismiss",
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = cardData.description,
                style = TextStyle(
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Normal,
                    color = UI.colors.mediumInverse
                )
            )

            if (cardData.cta != null) {
                Spacer(Modifier.height(20.dp))

                val buttonBg = if (isDark) {
                    Color.White.copy(alpha = 0.14f)
                } else {
                    Color.White.copy(alpha = 0.70f)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp)
                        .clickable { onCTA() }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .testTag("cta_prompt_${cardData.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IvyIcon(
                            modifier = Modifier.size(16.dp),
                            icon = cardData.ctaIcon,
                            tint = UI.colors.pureInverse
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = cardData.cta,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = UI.colors.pureInverse
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCard() {
    IvyPreview {
        CustomerJourneyCard(
            cardData = CustomerJourneyCardsProvider.adjustBalanceCard(),
            onCTA = { },
            onDismiss = {}
        )
    }
}
