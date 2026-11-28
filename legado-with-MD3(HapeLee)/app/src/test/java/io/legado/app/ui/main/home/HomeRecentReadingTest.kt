package io.legado.app.ui.main.home

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.asImage
import coil3.intercept.Interceptor
import coil3.request.SuccessResult
import io.legado.app.R
import io.legado.app.data.repository.CoverAlbumRepository
import io.legado.app.data.repository.SettingsRepository
import io.legado.app.domain.model.HomeDashboardSection
import io.legado.app.domain.model.settings.AppShellSettings
import io.legado.app.domain.model.settings.AppUiConfiguration
import io.legado.app.domain.usecase.CoverAlbumUseCase
import io.legado.app.help.config.AppConfigStore
import io.legado.app.ui.theme.AppTheme
import io.legado.app.ui.widget.components.text.AppText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import splitties.init.injectAsAppCtx
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35], qualifiers = "en-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeRecentReadingTest {
    @get:Rule
    val compose = createComposeRule()

    @Before
    fun setUp() {
        val application = RuntimeEnvironment.getApplication()
        application.injectAsAppCtx()
        AppConfigStore.init(application)
        stopKoin()
        startKoin {
            modules(module {
                single { CoverAlbumUseCase(CoverAlbumRepository(application, SettingsRepository())) }
            })
        }
    }

    @Test
    fun theRecentCardKeepsItsHeightWhenTitleLengthAndMetadataChangeAtLargeFontScale() {
        val short = book(1, null, null)
        val long = book(2, "Chapter 120", 0.68f).copy(
            name = "A long book title that wraps onto a second line and still has reading details",
        )
        compose.setContent {
            AppTheme(AppUiConfiguration(appShell = AppShellSettings(fontScale = 16))) {
                Column {
                    Dashboard(state.copy(recentBook = short, recentBooks = persistentListOf(long)))
                    AppText("Below the dashboard")
                }
            }
        }

        val below = compose.onNodeWithText("Below the dashboard")
        val initialTop = below.fetchSemanticsNode().boundsInRoot.top
        val pager = compose.onNode(hasScrollToIndexAction())
        pager.performScrollToIndex(1)
        compose.onNodeWithText(long.name).assertIsDisplayed().assertTextContains("68%")
        assertEquals(initialTop, below.fetchSemanticsNode().boundsInRoot.top, 0f)
        pager.performScrollToIndex(0)
        compose.onNodeWithText(short.name).assertIsDisplayed()
        assertEquals(initialTop, below.fetchSemanticsNode().boundsInRoot.top, 0f)
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun swipingWithinHomeUpdatesBookDetailsAndOpensOnlyTheSelectedBook() {
        val latest = book(1, "Opening chapter", 0.25f)
        val previous = book(2, "The turning point", 0.65f)
        val oldest = book(3, "Last chapter", 0.95f)
        val opened = mutableListOf<HomeIntent>()
        compose.setContent {
            AppTheme(AppUiConfiguration()) {
                val homePager = rememberPagerState { 2 }
                HorizontalPager(homePager) { page ->
                    if (page == 0) {
                        Dashboard(
                            state = state.copy(
                                recentBook = latest,
                                recentBooks = persistentListOf(previous, oldest),
                            ),
                            onIntent = { opened.add(it) },
                        )
                    } else {
                        AppText("Other home tab")
                    }
                }
            }
        }

        val firstPage = compose.onNodeWithText(latest.name)
        val secondPage = compose.onNodeWithText(previous.name)
        val thirdPage = compose.onNodeWithText(oldest.name)
        firstPage.assertIsDisplayed().assert(position(1, 3))
        firstPage.performTouchInput { swipeLeft() }
        secondPage
            .assertIsDisplayed()
            .assert(position(2, 3))
            .assertTextContains(previous.author)
            .assertTextContains("The turning point")
            .assertTextContains("65%")
        secondPage.performTouchInput { swipeLeft() }
        thirdPage.assertIsDisplayed().assert(position(3, 3))
        thirdPage.performTouchInput { swipeRight() }
        secondPage.assertHasClickAction().performTouchInput { click(Offset(48f, centerY)) }
        compose.runOnIdle {
            assertEquals(listOf(HomeIntent.RecentHistoryBookClick("book:2")), opened)
        }
    }

    @Test
    @OptIn(ExperimentalSharedTransitionApi::class)
    fun partialSwipeUpdatesTheStackAndTextBeforeReleaseAndReversesWithTheFinger() {
        val first = book(11, null, null).copy(name = "DEPTH", author = "")
        val second = book(12, null, null)
        val dashboard = state.copy(
            recentBook = first,
            recentBooks = persistentListOf(second, book(13, null, null), book(14, null, null)),
        )
        renderDashboardWithOverlay(dashboard)

        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(1)
        val currentPage = compose.onNodeWithText(second.name)
        currentPage.assertIsDisplayed()
        val step = currentPage.fetchSemanticsNode().boundsInRoot.width * 0.11f
        val screen = compose.onRoot()
        val initial = screen.captureToImage().toPixelMap()
        compose.mainClock.autoAdvance = false
        try {
            currentPage.performTouchInput {
                down(center)
                repeat(4) { moveBy(Offset(step, 0f)) }
            }
            compose.mainClock.advanceTimeByFrame()
            val dragged = screen.captureToImage().toPixelMap()
            assertTrue(
                "The cover and text both change while the finger is down",
                initial.differsIn(dragged, 34..92, 195..280) &&
                    initial.differsIn(dragged, 130..300, 202..262),
            )
            assertTrue(
                "The back edge changes as the trailing book fades",
                initial.differsIn(dragged, 107..117, 242..270),
            )
            compose.mainClock.advanceTimeBy(320)
            val held = screen.captureToImage().toPixelMap()
            assertFalse(
                "The transition holds its position while the finger is still",
                held.differsIn(dragged, 20..330, 185..300),
            )

            currentPage.performTouchInput { repeat(4) { moveBy(Offset(-step, 0f)) } }
            compose.mainClock.advanceTimeByFrame()
            val reversed = screen.captureToImage().toPixelMap()
            assertTrue(
                "Reversing the drag restores the titles before release",
                reversed.differsIn(dragged, 130..300, 202..262),
            )
            currentPage.performTouchInput { cancel() }
            compose.mainClock.advanceTimeBy(640)
            assertFalse(
                "Cancelling the drag restores the original covers and text",
                initial.differsIn(screen.captureToImage().toPixelMap(), 20..330, 185..300),
            )
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    @Test
    fun coverBottomEdgeAndShadowMatchBeforeAndAfterThePagerSettles() {
        val first = book(21, null, null)
        val second = book(22, null, null)
        renderDashboardWithOverlay(
            state.copy(
                recentBook = first,
                recentBooks = persistentListOf(second, book(23, null, null), book(24, null, null)),
            ),
        )
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(1)
        val startingPage = compose.onNodeWithText(second.name)
        val landingPage = compose.onNodeWithText(first.name)
        startingPage.assertIsDisplayed()
        val screen = compose.onRoot()

        compose.mainClock.autoAdvance = false
        try {
            startingPage.performTouchInput {
                down(Offset(2f, centerY))
                val step = (width + 12f) / 6f
                repeat(6) { moveBy(Offset(step, 0f)) }
            }
            compose.mainClock.advanceTimeByFrame()
            landingPage.assertIsDisplayed()
            val beforeRelease = screen.captureToImage().toPixelMap()
            landingPage.performTouchInput { up() }
            compose.mainClock.advanceTimeBy(640)
            landingPage.assertIsDisplayed().assert(position(1, 4))
            val settled = screen.captureToImage().toPixelMap()
            assertFalse(
                "Settling must not introduce a high-contrast bottom edge or shadow",
                beforeRelease.differsIn(settled, 37..110, 270..294, maxChannelDelta = 0.06f),
            )
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    private fun PixelMap.differsIn(
        other: PixelMap,
        xRange: IntRange,
        yRange: IntRange,
        maxChannelDelta: Float = 0f,
    ): Boolean = yRange.any { y ->
        xRange.any { x ->
            val before = this[x, y]
            val after = other[x, y]
            maxOf(
                abs(before.red - after.red),
                abs(before.green - after.green),
                abs(before.blue - after.blue),
                abs(before.alpha - after.alpha),
            ) > maxChannelDelta
        }
    }

    @Test
    fun theGlowUsesTheLoadedCoverAndChangesWithTheSelectedBook() {
        val red = "cover-fixture://red"
        val green = "cover-fixture://green"
        val colors = mapOf(red to android.graphics.Color.RED, green to android.graphics.Color.GREEN)
        val loader = ImageLoader.Builder(RuntimeEnvironment.getApplication()).components {
            add(Interceptor { chain ->
                val color = colors[chain.request.data.toString()] ?: return@Interceptor chain.proceed()
                val bitmap = Bitmap.createBitmap(60, 84, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(color)
                SuccessResult(bitmap.asImage(), chain.request)
            })
        }.build()
        loadKoinModules(module { single { loader } })
        try {
            val first = book(31, "Opening chapter", 0.68f).copy(coverPath = red)
            val second = book(32, "Last chapter", 0.32f).copy(coverPath = green)
            renderDashboardWithOverlay(
                state.copy(recentBook = first, recentBooks = persistentListOf(second)),
            )

            val screen = compose.onRoot()
            val firstPage = compose.onNodeWithText(first.name).assertIsDisplayed().assertTextContains("68%")
            compose.waitUntil(timeoutMillis = 5_000) {
                val glow = screen.captureToImage().toPixelMap(18, 229, 1, 1)[0, 0]
                glow.red > glow.blue + 0.02f
            }
            firstPage.performTouchInput { swipeLeft() }
            compose.onNodeWithText(second.name).assertTextContains("32%")
            compose.waitUntil(timeoutMillis = 5_000) {
                val glow = screen.captureToImage().toPixelMap(18, 229, 1, 1)[0, 0]
                glow.green > maxOf(glow.red, glow.blue) + 0.02f
            }
        } finally {
            loader.shutdown()
        }
    }

    @Test
    @Config(qualifiers = "en-w720dp-h800dp-mdpi")
    fun emptyHistoryAndRecordsWithoutABookshelfBookAreStillReadable() {
        val missingBook = book(1, null, null).copy(bookUrl = null)
        var dashboard by mutableStateOf(state)
        compose.setContent {
            AppTheme(AppUiConfiguration()) {
                Dashboard(dashboard)
            }
        }

        val empty = RuntimeEnvironment.getApplication().getString(R.string.home_no_recent_reading)
        compose.onNodeWithText(empty).assertIsDisplayed()
        compose.runOnIdle { dashboard = dashboard.copy(recentBook = missingBook) }
        compose.onNodeWithText(empty).assertDoesNotExist()
        compose.onNodeWithText(missingBook.name).assertIsDisplayed().assertHasNoClickAction()
    }

    private val state = HomeUiState(visibleSections = persistentSetOf(HomeDashboardSection.RecentBook))

    @OptIn(ExperimentalSharedTransitionApi::class)
    private fun renderDashboardWithOverlay(dashboard: HomeUiState) {
        compose.setContent {
            AppTheme(AppUiConfiguration()) {
                SharedTransitionLayout(Modifier.fillMaxSize()) {
                    Column {
                        Spacer(Modifier.height(144.dp))
                        Dashboard(dashboard, sharedTransitionScope = this@SharedTransitionLayout)
                    }
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalSharedTransitionApi::class)
    private fun Dashboard(
        state: HomeUiState,
        onIntent: (HomeIntent) -> Unit = {},
        sharedTransitionScope: SharedTransitionScope? = null,
    ) {
        HomeDashboardContent(
            state = state,
            onIntent = onIntent,
            onNavigateToReadRecord = {},
            onNavigateToReadRecordOverview = {},
            onBookDetails = {},
            sharedTransitionScope = sharedTransitionScope,
        )
    }

    private fun position(index: Int, total: Int) = SemanticsMatcher.expectValue(
        SemanticsProperties.StateDescription,
        RuntimeEnvironment.getApplication().getString(R.string.home_recent_book_position, index, total),
    )

    private fun book(index: Int, chapter: String?, progress: Float?) = HomeRecentBookUi(
        bookUrl = "book:$index",
        name = "Volume $index",
        author = "Author $index",
        origin = null,
        coverPath = null,
        chapterTitle = chapter,
        chapterProgress = progress,
    )
}
