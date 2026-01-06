package dev.pranav.appintro

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A modern Material 3 App Intro component for Android applications.
 *
 * @param pages List of [IntroPage] to display in the intro
 * @param onSkip Called when the user skips the intro (optional)
 * @param onFinish Called when the user completes the intro
 * @param showSkipButton Whether to show the skip button (defaults to true)
 * @param useAnimatedPager Whether to use enhanced animations between pages (defaults to true)
 * @param nextButtonText Text for the next button (defaults to "Next")
 * @param skipButtonText Text for the skip button (defaults to "Skip")
 * @param finishButtonText Text for the finish button (defaults to "Get Started")
 * @param backButtonText Text for the back button (defaults to "Back")
 */
@OptIn(
    ExperimentalAnimationApi::class,
)
@Composable
fun AppIntro(
    pages: List<IntroPage>,
    onSkip: () -> Unit = {},
    onFinish: () -> Unit,
    showSkipButton: Boolean = true,
    useAnimatedPager: Boolean = true,
    nextButtonText: String = "Next",
    skipButtonText: String = "Skip",
    finishButtonText: String = "Get Started",
    backButtonText: String = "Back"
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    // Aligned defaults to match IntroPageContent.kt for visual consistency
    val currentPageBackgroundColor by remember(pagerState.currentPage) {
        derivedStateOf {
            pages.getOrNull(pagerState.currentPage)?.backgroundColor
                ?: colorScheme.primaryContainer
        }
    }

    val currentPageContentColor by remember(pagerState.currentPage) {
        derivedStateOf {
            pages.getOrNull(pagerState.currentPage)?.contentColor
                ?: colorScheme.onPrimaryContainer
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (useAnimatedPager) {
            IntroPager(
                pages = pages,
                pagerState = pagerState,
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->
                IntroPageContent(
                    page = pages[page],
                    modifier = Modifier.fillMaxSize(),
                    isVisible = page == pagerState.currentPage
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Indicators
                PageIndicators(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    activeColor = currentPageContentColor,
                    inactiveColor = currentPageContentColor.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip button with animation
                    AnimatedVisibility(
                        visible = showSkipButton && pagerState.currentPage < pages.size - 1,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        TextButton(
                            onClick = onSkip,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = currentPageContentColor
                            )
                        ) {
                            Text(
                                text = skipButtonText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }


                    // Back button with animation - only visible when not on first page
                    AnimatedVisibility(
                        visible = pagerState.currentPage > 0,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        TextButton(
                            onClick = {
                                navigateToPreviousPage(pagerState, coroutineScope)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = currentPageContentColor
                            )
                        ) {
                            Text(
                                text = backButtonText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!showSkipButton || pagerState.currentPage >= pages.size - 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Next/Finish button
                    if (pagerState.currentPage < pages.size - 1) {
                        FilledTonalButton(
                            onClick = {
                                // Get current page
                                val currentPage = pages[pagerState.currentPage]

                                // Execute the page's onNext callback if it exists
                                val canProceed = currentPage.onNext?.invoke() ?: true

                                // Only proceed to next page if callback returns true or is null
                                if (canProceed) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = currentPageContentColor.copy(alpha = 0.15f),
                                contentColor = currentPageContentColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .height(48.dp)
                        ) {
                            // Animated text content that transitions when text changes
                            AnimatedContent(
                                targetState = nextButtonText,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn() + scaleIn(
                                        initialScale = 0.9f
                                    ))
                                        .togetherWith(slideOutVertically { height -> -height } + fadeOut() + scaleOut(
                                            targetScale = 1.1f
                                        ))
                                        .using(SizeTransform(clip = false))
                                },
                                label = "Next Button Text Animation"
                            ) { targetText ->
                                Text(
                                    text = targetText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                // Execute the last page's onNext callback if it exists
                                val currentPage = pages[pagerState.currentPage]
                                val canFinish = currentPage.onNext?.invoke() ?: true

                                // Only finish if callback returns true or is null
                                if (canFinish) {
                                    onFinish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                // Use the content color for the container for high prominence
                                containerColor = currentPageContentColor,
                                // Use the background color for content for good contrast
                                contentColor = currentPageBackgroundColor
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .height(52.dp)
                        ) {
                            // Animated text content for the finish button
                            AnimatedContent(
                                targetState = finishButtonText,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn() + scaleIn(
                                        initialScale = 0.9f
                                    ))
                                        .togetherWith(slideOutVertically { height -> -height } + fadeOut() + scaleOut(
                                            targetScale = 1.1f
                                        ))
                                        .using(SizeTransform(clip = false))
                                },
                                label = "Finish Button Text Animation"
                            ) { targetText ->
                                Text(
                                    text = targetText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Navigates to the next page in the intro
 */
private fun navigateToNextPage(pagerState: PagerState, scope: kotlinx.coroutines.CoroutineScope) {
    scope.launch {
        if (pagerState.currentPage < pagerState.pageCount - 1) {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }
}

/**
 * Navigates to the previous page in the intro
 */
private fun navigateToPreviousPage(
    pagerState: PagerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    scope.launch {
        if (pagerState.currentPage > 0) {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }
}
