package io.legado.app.ui.main.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import coil3.Image
import io.legado.app.R
import io.legado.app.core.ui.bookshelf.DimensionalBookshelf
import io.legado.app.core.ui.bookshelf.DimensionalShelfBook
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.SearchBook
import io.legado.app.domain.model.HomeDashboardSection
import io.legado.app.domain.model.MAX_DAILY_READING_GOAL_MINUTES
import io.legado.app.lib.permission.Permissions
import io.legado.app.lib.permission.PermissionsCompat
import io.legado.app.ui.config.backupConfig.BackupOptionSheet
import io.legado.app.ui.config.backupConfig.RestoreOptionSheet
import io.legado.app.ui.main.LocalMainPageIndex
import io.legado.app.ui.main.LocalMainPagerTopBarHost
import io.legado.app.ui.main.MainPagerPageChromeEffect
import io.legado.app.ui.main.homepage.HomepageFeedActions
import io.legado.app.ui.main.homepage.HomepageManageActions
import io.legado.app.ui.main.homepage.HomepageModuleFeed
import io.legado.app.ui.main.homepage.HomepageModuleManageSheet
import io.legado.app.ui.main.homepage.HomepageUiState
import io.legado.app.ui.main.homepage.HomepageViewModel
import io.legado.app.ui.theme.LegadoTheme
import io.legado.app.ui.theme.LocalAppUiConfiguration
import io.legado.app.ui.theme.extendIntoHorizontalPadding
import io.legado.app.ui.theme.extractSeedColor
import io.legado.app.ui.widget.components.AppPullToRefresh
import io.legado.app.ui.widget.components.AppScaffold
import io.legado.app.ui.widget.components.alert.AppAlertDialog
import io.legado.app.ui.widget.components.book.SearchBookPreviewSheet
import io.legado.app.ui.widget.components.button.series.MediumTonalButton
import io.legado.app.ui.widget.components.button.series.SmallTonalButton
import io.legado.app.ui.widget.components.card.GlassCard
import io.legado.app.ui.widget.components.conflict.BookshelfConflictSheet
import io.legado.app.ui.widget.components.coui.appVerticalScroll
import io.legado.app.ui.widget.components.icon.AppIcon
import io.legado.app.ui.widget.components.icon.AppIcons
import io.legado.app.ui.widget.components.image.cover.BookshelfCover
import io.legado.app.ui.widget.components.menu.AppAction
import io.legado.app.ui.widget.components.menu.AppActionGroup
import io.legado.app.ui.widget.components.menu.AppActionMenu
import io.legado.app.ui.widget.components.progressIndicator.AppContainedLoadingIndicator
import io.legado.app.ui.widget.components.settingItem.TinySwitchSettingItem
import io.legado.app.ui.widget.components.text.AppText
import io.legado.app.ui.widget.components.topbar.GlassMediumFlexibleTopAppBar
import io.legado.app.ui.widget.components.topbar.GlassTopAppBarDefaults
import io.legado.app.ui.widget.components.topbar.TopBarActionButton
import io.legado.app.utils.isContentScheme
import io.legado.app.utils.sendToClip
import io.legado.app.utils.takePersistablePermissionSafely
import io.legado.app.utils.toastOnUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun HomeRouteScreen(
    onOpenBook: (Book) -> Unit,
    onNavigateToBookInfo: (
        name: String?,
        author: String?,
        bookUrl: String,
        origin: String?,
        coverPath: String?,
        sharedCoverKey: String?,
    ) -> Unit,
    onOpenExploreShow: (
        title: String?,
        sourceUrl: String,
        exploreUrl: String?,
    ) -> Unit,
    onOpenBackupSettings: () -> Unit,
    onNavigateToReadRecord: () -> Unit,
    onNavigateToReadRecordOverview: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: HomeViewModel = koinViewModel(),
    homepageViewModel: HomepageViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val homepageState by homepageViewModel.uiState.collectAsStateWithLifecycle()
    var previewBook by remember { mutableStateOf<SearchBook?>(null) }
    var previewSharedCoverKey by remember { mutableStateOf<String?>(null) }

    val feedActions = remember(homepageViewModel, onNavigateToBookInfo, onOpenExploreShow) {
        HomepageFeedActions(
            onModuleHeaderClick = { sourceUrl, exploreUrl, title ->
                onOpenExploreShow(title, sourceUrl, exploreUrl)
            },
            onRetryModule = homepageViewModel::retryModule,
            onLoadMoreModule = homepageViewModel::loadMoreModule,
            onBookClick = { book: SearchBook, sharedCoverKey: String? ->
                onNavigateToBookInfo(
                    book.name,
                    book.author,
                    book.bookUrl,
                    book.origin,
                    book.coverUrl,
                    sharedCoverKey,
                )
            },
            onKindUrlClick = homepageViewModel::onKindUrlClick,
            onRefreshButtonGroup = homepageViewModel::refreshButtonGroup,
        )
    }
    val manageActions = remember(homepageViewModel) {
        HomepageManageActions(
            onToggleSet = { url, isEnabled ->
                homepageViewModel.toggleSourceFilter(url, isEnabled)
            },
            onGetSourceModules = { url, setId ->
                homepageViewModel.getSourceModules(url, setId)
            },
            onSyncSourceModules = { homepageViewModel.syncSourceModules(it) },
            onToggleModule = { id, visible ->
                homepageViewModel.setModuleVisible(id, visible)
            },
            onJoinModule = { sourceUrl, targetSetId, def ->
                homepageViewModel.joinModule(sourceUrl, targetSetId, def)
            },
            onAddCustomModule = { sourceUrl, targetSetId, def ->
                homepageViewModel.addCustomModule(sourceUrl, targetSetId, def)
            },
            onAddButtonGroupFromKinds = { sourceUrl, targetSetId, title, kinds ->
                homepageViewModel.addButtonGroupFromKinds(sourceUrl, targetSetId, title, kinds)
            },
            onAddRankingFromKinds = { sourceUrl, targetSetId, title, type, kinds ->
                homepageViewModel.addRankingFromKinds(
                    sourceUrl,
                    targetSetId,
                    title,
                    type,
                    kinds
                )
            },
            onGetExploreKinds = { homepageViewModel.getSourceExploreKinds(it) },
            onUpdateModule = { globalId, def ->
                homepageViewModel.updateModule(globalId, def)
            },
            onDeleteModule = { homepageViewModel.deleteModule(it) },
            onReorderModules = { homepageViewModel.reorderJoinedModules(it) },
            onReorderSets = { homepageViewModel.reorderCustomSets(it) },
            onSetCustomSetTitle = { id, title ->
                homepageViewModel.setModuleCustomSetTitle(id, title)
            },
            onCreateCustomSet = { homepageViewModel.createCustomSet(it) },
            onRenameCustomSet = { id, name ->
                homepageViewModel.renameCustomSet(id, name)
            },
            onDeleteCustomSet = { homepageViewModel.deleteCustomSet(it) },
            onAssignModuleToCustomSet = { id, setId ->
                homepageViewModel.assignModuleToCustomSet(id, setId)
            },
        )
    }

    var pendingBackupDestination by remember {
        mutableStateOf<HomeBackupDestination?>(null)
    }
    val backupDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        val destination = pendingBackupDestination
        pendingBackupDestination = null
        if (uri != null && destination != null) {
            uri.takePersistablePermissionSafely(context)
            val path = if (uri.isContentScheme()) {
                uri.toString()
            } else {
                uri.path.orEmpty()
            }
            if (path.isNotEmpty()) {
                viewModel.onIntent(
                    HomeIntent.BackupDirectorySelected(
                        destination = destination,
                        path = path,
                    )
                )
            }
        }
    }
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            viewModel.onIntent(HomeIntent.RestoreLocalFileSelected(it.toString()))
        }
    }

    LaunchedEffect(viewModel, context) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is HomeEffect.OpenBook -> onOpenBook(effect.book)
                HomeEffect.OpenBackupSettings -> onOpenBackupSettings()
                is HomeEffect.SelectBackupDirectory -> {
                    pendingBackupDestination = effect.destination
                    runCatching { backupDirectoryLauncher.launch(null) }
                }

                is HomeEffect.RequestBackupStoragePermission -> {
                    PermissionsCompat.Builder()
                        .addPermissions(*Permissions.Group.STORAGE)
                        .rationale(R.string.tip_perm_request_storage)
                        .onGranted {
                            viewModel.onIntent(
                                HomeIntent.BackupDirectorySelected(
                                    destination = effect.destination,
                                    path = effect.path,
                                )
                            )
                        }
                        .request()
                }

                HomeEffect.SelectRestoreFile -> {
                    restoreFileLauncher.launch(arrayOf("application/zip"))
                }

                is HomeEffect.ShowMessage -> {
                    val message = buildString {
                        append(context.getString(effect.messageRes))
                        effect.detail?.takeIf { it.isNotBlank() }?.let {
                            append('\n')
                            append(it)
                        }
                    }
                    context.toastOnUi(message)
                }
            }
        }
    }

    LaunchedEffect(homepageViewModel) {
        homepageViewModel.effects.collectLatest { effect ->
            when (effect) {
                is io.legado.app.ui.main.homepage.HomepageEffect.NavigateToBookInfo ->
                    onNavigateToBookInfo(
                        effect.name,
                        effect.author,
                        effect.bookUrl,
                        effect.origin,
                        effect.coverPath,
                        effect.sharedCoverKey,
                    )

                is io.legado.app.ui.main.homepage.HomepageEffect.NavigateToExploreShow ->
                    onOpenExploreShow(effect.title, effect.sourceUrl, effect.exploreUrl)

                is io.legado.app.ui.main.homepage.HomepageEffect.ShowSnackbar -> {}
            }
        }
    }

    HomeScreen(
        state = state,
        homepageState = homepageState,
        homepageFeedActions = feedActions,
        homepageManageActions = manageActions,
        onIntent = viewModel::onIntent,
        onRefreshHomepage = homepageViewModel::onRefresh,
        onToggleHomepageManage = homepageViewModel::toggleManageMode,
        onNavigateToReadRecord = onNavigateToReadRecord,
        onNavigateToReadRecordOverview = onNavigateToReadRecordOverview,
        onDashboardBookDetails = { book ->
            book.bookUrl?.let { url ->
                onNavigateToBookInfo(book.name, book.author, url, book.origin, book.coverPath, null)
            }
        },
        onHomepageBookLongClick = { book, sharedCoverKey ->
            previewBook = book
            previewSharedCoverKey = sharedCoverKey
        },
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )

    SearchBookPreviewSheet(
        data = previewBook,
        shelfState = previewBook?.let { homepageViewModel.getCurrentBookShelfState(it) },
        sharedCoverKey = previewSharedCoverKey,
        onDismissRequest = { previewBook = null },
        onOpenDetail = { book, sharedCoverKey ->
            previewBook = null
            onNavigateToBookInfo(
                book.name,
                book.author,
                book.bookUrl,
                book.origin,
                book.coverUrl,
                sharedCoverKey,
            )
        },
        onAddToShelf = { book ->
            homepageViewModel.onAddToShelf(book)
        },
    )

    BookshelfConflictSheet(
        conflict = homepageState.bookshelfConflict,
        isResolving = homepageState.isResolvingBookshelfConflict,
        onDismissRequest = homepageViewModel::dismissBookshelfConflict,
        onOpenExistingBook = homepageViewModel::openBookshelfConflictBook,
        onCoexist = homepageViewModel::coexistWithBookshelfConflict,
        onMigrate = homepageViewModel::migrateBookshelfConflict,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun HomeScreen(
    state: HomeUiState,
    homepageState: HomepageUiState,
    homepageFeedActions: HomepageFeedActions,
    homepageManageActions: HomepageManageActions,
    onIntent: (HomeIntent) -> Unit,
    onRefreshHomepage: () -> Unit,
    onToggleHomepageManage: () -> Unit,
    onNavigateToReadRecord: () -> Unit = {},
    onNavigateToReadRecordOverview: () -> Unit = {},
    onDashboardBookDetails: (HomeRecentBookUi) -> Unit = {},
    onHomepageBookLongClick: (SearchBook, String?) -> Unit = { _, _ -> },
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val scrollBehavior = GlassTopAppBarDefaults.defaultScrollBehavior()
    val pagerTopBarHost = LocalMainPagerTopBarHost.current
    val mainPageIndex = LocalMainPageIndex.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val selectedSets = remember(homepageState.manageState.sets) {
        homepageState.manageState.sets.filter { it.isSelected }
    }
    val pagerState = rememberPagerState(pageCount = {
        selectedSets.size.coerceAtLeast(1)
    })

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentPageSourceName by remember(selectedSets, pagerState) {
        derivedStateOf { selectedSets.getOrNull(pagerState.currentPage)?.sourceName }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val url = selectedSets.getOrNull(page)?.sourceUrl ?: return@collect
                onIntent(HomeIntent.SelectSourceSet(url))
            }
    }

    LaunchedEffect(state.selectedSourceSetUrl, pagerState) {
        val targetUrl = state.selectedSourceSetUrl ?: return@LaunchedEffect
        val targetIndex = selectedSets.indexOfFirst { it.sourceUrl == targetUrl }
        if (targetIndex >= 0 && targetIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    val homeTopBarActions: @Composable RowScope.() -> Unit = {
        TopBarActionButton(
            onClick = onToggleHomepageManage,
            imageVector = AppIcons.Settings,
            contentDescription = stringResource(R.string.homepage_module_manage),
        )
        AppActionMenu(
            groups = persistentListOf(
                AppActionGroup(
                    id = "home_settings",
                    actions = persistentListOf(
                        AppAction(
                            "dashboard_settings",
                            stringResource(R.string.home_dashboard_settings),
                        ),
                    ),
                ),
                AppActionGroup(
                    id = "home_pages",
                    actions = selectedSets.mapIndexed { index, source ->
                        AppAction(
                            id = "page:$index",
                            label = source.sourceName,
                            selected = index == pagerState.currentPage,
                        )
                    }.toImmutableList(),
                ),
            ),
            onAction = { id ->
                when {
                    id == "dashboard_settings" ->
                        onIntent(HomeIntent.DashboardSettingsClick)
                    id.startsWith("page:") -> {
                        val index = id.removePrefix("page:").toIntOrNull()
                        if (index != null && index in selectedSets.indices) {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    }
                }
            },
        ) {
            AppIcon(
                imageVector = AppIcons.MoreVert,
                contentDescription = stringResource(R.string.more_menu),
                modifier = this.modifier,
            )
        }
    }

    if (pagerTopBarHost != null) {
        MainPagerPageChromeEffect(
            host = pagerTopBarHost,
            pageIndex = mainPageIndex,
            subtitle = currentPageSourceName,
            actions = homeTopBarActions,
        )
    }

    AppScaffold(
        modifier = if (pagerTopBarHost != null) {
            Modifier
        } else {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        },
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (pagerTopBarHost == null) {
                GlassMediumFlexibleTopAppBar(
                    title = stringResource(R.string.home),
                    subtitle = currentPageSourceName,
                    scrollBehavior = scrollBehavior,
                    actions = homeTopBarActions,
                )
            }
        },
    ) { paddingValues ->
        AppPullToRefresh(
            isRefreshing = homepageState.isRefreshing,
            onRefresh = onRefreshHomepage,
            modifier = Modifier.fillMaxSize(),
            // 横划顶栏由 OxygenX 自己处理收拢，宿主不再把旧顶栏的收拢进度交给刷新门控。
            scrollBehavior = if (pagerTopBarHost != null) null else scrollBehavior,
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val dashboardScrollState = rememberScrollState()
                val moduleNestedScrollConnection = remember(dashboardScrollState) {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource,
                        ): Offset {
                            return if (available.y >= 0f) {
                                Offset.Zero
                            } else {
                                val consumed = dashboardScrollState.dispatchRawDelta(-available.y)
                                Offset(0f, -consumed)
                            }
                        }

                        override fun onPostScroll(
                            consumed: Offset,
                            available: Offset,
                            source: NestedScrollSource,
                        ): Offset {
                            return if (available.y <= 0f) {
                                Offset.Zero
                            } else {
                                val c = dashboardScrollState.dispatchRawDelta(-available.y)
                                Offset(0f, -c)
                            }
                        }
                    }
                }
                val viewportHeight = maxHeight
                val hasDashboard = state.visibleSections.isNotEmpty()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .appVerticalScroll(dashboardScrollState),
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(paddingValues.calculateTopPadding() + 8.dp),
                    )
                    if (hasDashboard) {
                        HomeDashboardContent(
                            state = state,
                            onIntent = onIntent,
                            onNavigateToReadRecord = onNavigateToReadRecord,
                            onNavigateToReadRecordOverview = onNavigateToReadRecordOverview,
                            onBookDetails = onDashboardBookDetails,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(viewportHeight),
                    ) {
                        if (selectedSets.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                AppText(stringResource(R.string.homepage_no_source_sets_selected))
                            }
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .nestedScroll(moduleNestedScrollConnection),
                                key = { index ->
                                    selectedSets.getOrNull(index)?.sourceUrl ?: index
                                },
                            ) { pageIndex ->
                                val source = selectedSets.getOrNull(pageIndex)
                                val sourceModules = remember(homepageState.modules, source) {
                                    homepageState.modules.filter { module ->
                                        if (source?.isCustomSet == true) {
                                            val setId =
                                                HomepageViewModel.customSetIdFromUrl(source.sourceUrl)
                                            module.customSetId == setId
                                        } else {
                                            module.sourceUrl == source?.sourceUrl
                                        }
                                    }
                                }
                                HomepageModuleFeed(
                                    modules = sourceModules,
                                    actions = homepageFeedActions,
                                    modifier = Modifier.fillMaxSize(),
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onBookLongClick = onHomepageBookLongClick,
                                    onErrorClick = { errorMessage = it },
                                )
                            }
                        }
                    }
                }

            }
        }

        HomepageModuleManageSheet(
            data = if (homepageState.isManageMode) Unit else null,
            onDismissRequest = onToggleHomepageManage,
            state = homepageState.manageState,
            actions = homepageManageActions,
        )

        HomeDialogs(
            dialog = state.activeDialog,
            onIntent = onIntent,
        )
        HomeSheets(
            sheet = state.activeSheet,
            visibleSections = state.visibleSections,
            onIntent = onIntent,
        )
        AppAlertDialog(
            data = errorMessage,
            onDismissRequest = { errorMessage = null },
            title = stringResource(R.string.error_details),
            confirmText = stringResource(R.string.copy_text),
            onConfirm = { message ->
                context.sendToClip(message)
                errorMessage = null
            },
            dismissText = stringResource(R.string.close),
            onDismiss = { errorMessage = null },
            content = { message ->
                SelectionContainer {
                    AppText(
                        text = message,
                        style = LegadoTheme.typography.bodyMedium,
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .appVerticalScroll(rememberScrollState()),
                    )
                }
            },
        )
    }
}

@Composable
internal fun HomeDashboardContent(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onNavigateToReadRecord: () -> Unit,
    onNavigateToReadRecordOverview: () -> Unit,
    onBookDetails: (HomeRecentBookUi) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val sidePadding = 16.dp
    Column(
        modifier = modifier.padding(horizontal = sidePadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (HomeDashboardSection.DimensionalBookshelf in state.visibleSections &&
            state.dimensionalShelfBooks.isNotEmpty()
        ) {
            val books = state.dimensionalShelfBooks
            val shelfBooks = remember(books) {
                books.map { book ->
                    val url = requireNotNull(book.bookUrl)
                    DimensionalShelfBook(
                        key = url,
                        bookUrl = url,
                        title = book.name,
                        author = book.author,
                        cover = book.coverPath,
                        origin = book.origin,
                    )
                }.toImmutableList()
            }
            DimensionalBookshelf(
                books = shelfBooks,
                onOpen = { onIntent(HomeIntent.RecentHistoryBookClick(shelfBooks[it].bookUrl)) },
                onPreview = { onBookDetails(books[it]) },
                modifier = Modifier.extendIntoHorizontalPadding(sidePadding),
                preferCache = true,
            )
        }

        if (HomeDashboardSection.RecentBook in state.visibleSections) {
            val recentBooks = remember(state.recentBook, state.recentBooks) {
                (listOfNotNull(state.recentBook) + state.recentBooks).toImmutableList()
            }
            RecentBookCard(
                books = recentBooks,
                onBookClick = { url -> onIntent(HomeIntent.RecentHistoryBookClick(url)) },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }

        val hasStatRead = HomeDashboardSection.TotalReadBooks in state.visibleSections
        val hasStatTime = HomeDashboardSection.TotalReadTime in state.visibleSections
        if (hasStatRead || hasStatTime) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (hasStatRead) {
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Leaderboard,
                        title = stringResource(R.string.home_total_read_books),
                        value = state.totalReadBooks.toString(),
                        unit = stringResource(R.string.unit_books),
                        onClick = onNavigateToReadRecord,
                    )
                }
                if (hasStatTime) {
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccessTime,
                        title = stringResource(R.string.home_total_reading_time),
                        value = String.format(
                            "%.1f",
                            state.totalReadTimeMillis / 3_600_000.0,
                        ),
                        unit = stringResource(R.string.unit_hours),
                        onClick = onNavigateToReadRecordOverview,
                    )
                }
            }
        }

        if (HomeDashboardSection.RecentBooks in state.visibleSections && state.recentBooks.isNotEmpty()) {
            RecentBooksRow(
                books = state.recentBooks,
                onBookClick = { url -> onIntent(HomeIntent.RecentHistoryBookClick(url)) },
            )
        }

        if (HomeDashboardSection.DailyGoal in state.visibleSections) {
            ReadingGoalCard(
                todayReadTimeMillis = state.todayReadTimeMillis,
                goalMinutes = state.dailyGoalMinutes,
                onClick = { onIntent(HomeIntent.ReadingGoalClick) },
            )
        }

        if (HomeDashboardSection.WebDavBackup in state.visibleSections) {
            WebDavBackupCard(
                latestBackup = state.latestBackup,
                isLoading = state.isBackupLoading,
                isLoadError = state.isBackupLoadError,
                isActionRunning = state.isBackupActionRunning,
                onBackup = { onIntent(HomeIntent.BackupClick) },
                onRestore = { onIntent(HomeIntent.RestoreClick) },
                onOpenSettings = { onIntent(HomeIntent.BackupSettingsClick) },
                onRetry = { onIntent(HomeIntent.RetryBackupInfo) },
            )
        }
    }
}

@Composable
private fun StatisticCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    unit: String,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = modifier,
        containerColor = LegadoTheme.colorScheme.surfaceContainerLow,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(
                imageVector = icon,
                contentDescription = null,
                tint = LegadoTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Column {
                AppText(
                    text = title,
                    style = LegadoTheme.typography.bodySmall,
                    color = LegadoTheme.colorScheme.onSurfaceVariant,
                )
                Row {
                    AppText(
                        text = value,
                        style = LegadoTheme.typography.titleMediumEmphasized,
                        maxLines = 1,
                        modifier = Modifier
                            .alignBy(FirstBaseline)
                            .basicMarquee(),
                    )
                    AppText(
                        text = unit,
                        style = LegadoTheme.typography.bodySmall,
                        color = LegadoTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .alignBy(FirstBaseline)
                            .padding(start = 2.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecentBookCard(
    books: ImmutableList<HomeRecentBookUi>,
    onBookClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    val pagerState = rememberPagerState { books.size }
    val bodyHeight = recentBookBodyHeight()
    val colors = LegadoTheme.colorScheme
    val coverSettings = LocalAppUiConfiguration.current.cover
    val isDark = LegadoTheme.isDark
    val coverColors = remember(coverSettings, isDark) {
        mutableStateMapOf<Pair<String, String?>, Color>()
    }
    LaunchedEffect(books, coverColors) {
        coverColors.keys.retainAll(books.mapTo(mutableSetOf()) { it.recentCoverColorKey() })
    }
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.surfaceContainerLow,
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (books.isNotEmpty()) {
                RecentBookCoverGlow(
                    books = books,
                    coverColors = coverColors,
                    pagerState = pagerState,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText(
                        text = stringResource(R.string.home_recent_reading),
                        modifier = Modifier.weight(1f),
                        style = LegadoTheme.typography.titleMediumEmphasized,
                    )
                    if (books.size > 1) {
                        RecentBookPageIndicator(pagerState = pagerState, pageCount = books.size)
                    }
                }

                if (books.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bodyHeight)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppText(
                            text = stringResource(R.string.home_no_recent_reading),
                            color = colors.onSurfaceVariant,
                        )
                    }
                } else {
                    val stackWidth = if (books.size > 1) 84.dp else 68.dp
                    val landingVisible = remember { mutableStateOf(false) }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(bodyHeight)
                            .onGloballyPositioned {
                                landingVisible.value = !it.boundsInWindow().isEmpty
                            },
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = books.size > 1,
                            key = { index -> books[index].recentBookKey() },
                        ) { index ->
                            RecentBookPage(
                                book = books[index],
                                position = if (books.size > 1) {
                                    stringResource(R.string.home_recent_book_position, index + 1, books.size)
                                } else {
                                    null
                                },
                                stackWidth = stackWidth,
                                onBookClick = onBookClick,
                            )
                        }

                        RecentBookStack(
                            books = books,
                            pagerState = pagerState,
                            stackWidth = stackWidth,
                            coverColors = coverColors,
                            landingVisible = { landingVisible.value },
                            modifier = Modifier.matchParentSize(),
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                }
            }
        }
    }
}

private val RecentBookDetailsTopPadding = 10.dp
private val RecentBookDetailsBottomPadding = 14.dp
private val RecentBookDetailsSpacing = 6.dp

@Composable
private fun recentBookBodyHeight(): Dp {
    val typography = LegadoTheme.typography
    val measurer = rememberTextMeasurer(cacheSize = 3)
    val density = LocalDensity.current
    // Include common fallback glyphs and reserve space independently of the selected book.
    val sample = "Ag中📚"
    val title = measurer.measure(
        "$sample\n$sample",
        style = typography.titleMediumEmphasized,
        maxLines = 2,
    ).size.height
    val metadataLine = measurer.measure(sample, style = typography.bodySmall, maxLines = 1).size.height
    val progressLabel = measurer.measure("100%", style = typography.labelSmall, maxLines = 1).size.height
    return with(density) {
        val details = title + metadataLine * 2 + progressLabel.coerceAtLeast(3.dp.roundToPx()) +
            RecentBookDetailsTopPadding.roundToPx() + RecentBookDetailsBottomPadding.roundToPx() +
            RecentBookDetailsSpacing.roundToPx() * 3
        details.toDp().coerceAtLeast(120.dp)
    }
}

@Composable
private fun RecentBookCoverGlow(
    books: ImmutableList<HomeRecentBookUi>,
    coverColors: Map<Pair<String, String?>, Color>,
    pagerState: PagerState,
    modifier: Modifier,
) {
    val fallback = LegadoTheme.colorScheme.secondary
    val strength = if (LegadoTheme.isDark) 0.13f else 0.09f
    val tints = books.map { book ->
        key(book.recentBookKey()) {
            animateColorAsState(
                targetValue = coverColors[book.recentCoverColorKey()] ?: fallback,
                label = "RecentCoverTint",
            )
        }
    }
    Canvas(modifier) {
        val page = pagerState.recentBookPosition(tints.lastIndex)
        val start = page.toInt()
        val end = (start + 1).coerceAtMost(tints.lastIndex)
        val tint = lerp(tints[start].value, tints[end].value, page - start)
        val coverCenter = 49.dp.toPx().coerceAtMost(size.width / 2f)
        val center = Offset(
            x = if (layoutDirection == LayoutDirection.Rtl) size.width - coverCenter else coverCenter,
            y = size.height / 2f + 9.dp.toPx(),
        )
        val radius = 152.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                0f to tint.copy(alpha = strength),
                0.55f to tint.copy(alpha = strength * 0.50f),
                1f to tint.copy(alpha = 0f),
                center = center,
                radius = radius,
            ),
            center = center,
            radius = radius,
        )
    }
}

@Composable
private fun RecentBookPageIndicator(pagerState: PagerState, pageCount: Int) {
    val colors = LegadoTheme.colorScheme
    val inactive = colors.onSurfaceVariant.copy(alpha = 0.25f)
    Canvas(Modifier.size(width = 12.dp * pageCount, height = 8.dp)) {
        val page = pagerState.recentBookPosition(pageCount - 1)
        val height = 4.dp.toPx()
        repeat(pageCount) { index ->
            val emphasis = (1f - abs(index - page)).coerceIn(0f, 1f)
            val width = (4.dp + 6.dp * emphasis).toPx()
            val startCenter = 12.dp.toPx() * (index + 0.5f)
            val center = if (layoutDirection == LayoutDirection.Rtl) {
                size.width - startCenter
            } else {
                startCenter
            }
            drawRoundRect(
                color = lerp(inactive, colors.primary, emphasis),
                topLeft = Offset(center - width / 2f, (size.height - height) / 2f),
                size = Size(width, height),
                cornerRadius = CornerRadius(height / 2f),
            )
        }
    }
}

@Composable
private fun RecentBookPage(
    book: HomeRecentBookUi,
    position: String?,
    stackWidth: Dp,
    onBookClick: (String) -> Unit,
) {
    val continueReading = stringResource(R.string.continue_read)
    val clickModifier = book.bookUrl?.let { url ->
        Modifier.clickable(role = Role.Button, onClickLabel = continueReading) { onBookClick(url) }
    } ?: Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(clickModifier)
            .semantics(mergeDescendants = true) {
                position?.let { stateDescription = it }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        // Pager pages keep their measured height and expose the text and click action.
        // The overlay draws the visible copy so its perspective can extend past the pager.
        RecentBookDetails(
            book = book,
            stackWidth = stackWidth,
            modifier = Modifier.drawWithContent {},
            marquee = false,
        )
    }
}

@Composable
private fun RecentBookDetails(
    book: HomeRecentBookUi,
    stackWidth: Dp,
    modifier: Modifier = Modifier,
    marquee: Boolean = true,
    coverColor: Color? = null,
) {
    val colors = LegadoTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 30.dp + stackWidth,
                top = RecentBookDetailsTopPadding,
                end = 16.dp,
                bottom = RecentBookDetailsBottomPadding,
            )
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(RecentBookDetailsSpacing),
    ) {
        AppText(
            text = book.name,
            style = LegadoTheme.typography.titleMediumEmphasized,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        AppText(
            text = book.author,
            style = LegadoTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        book.chapterTitle?.takeIf { it.isNotBlank() }?.let { chapter ->
            AppText(
                text = chapter,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (marquee) Modifier.basicMarquee() else Modifier,
                    ),
                style = LegadoTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
        book.chapterProgress?.let { progress ->
            val normalizedProgress = progress.coerceIn(0f, 1f)
            val accent = coverColor?.let { lerp(colors.primary, it, 0.45f) } ?: colors.primary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RecentReadingProgress(
                    progress = normalizedProgress,
                    color = accent,
                    modifier = Modifier.weight(1f),
                )
                AppText(
                    text = "${(normalizedProgress * 100f).roundToInt()}%",
                    style = LegadoTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

private fun HomeRecentBookUi.recentBookKey(): String =
    bookUrl?.let { "book:$it" } ?: "record:${name.length}:$name$author"

private fun HomeRecentBookUi.recentCoverColorKey(): Pair<String, String?> =
    recentBookKey() to coverPath

private fun PagerState.recentBookPosition(lastIndex: Int): Float =
    (currentPage + currentPageOffsetFraction).coerceIn(0f, lastIndex.toFloat())

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecentBookStack(
    books: ImmutableList<HomeRecentBookUi>,
    pagerState: PagerState,
    stackWidth: Dp,
    coverColors: MutableMap<Pair<String, String?>, Color>,
    landingVisible: () -> Boolean,
    modifier: Modifier,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    val lastIndex = books.lastIndex
    val position = remember(pagerState, lastIndex) { { pagerState.recentBookPosition(lastIndex) } }
    val basePage by remember(position) { derivedStateOf { floor(position()).toInt() } }
    val settledPage = pagerState.settledPage
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val textPivot = TransformOrigin(if (direction < 0f) 1f else 0f, 0.5f)
    val resources = LocalContext.current.resources
    val fallback = LegadoTheme.colorScheme.secondary
    val overlay = with(sharedTransitionScope) {
        if (this == null) Modifier else Modifier.renderInSharedTransitionScopeOverlay {
            pagerState.isScrollInProgress &&
                lifecycleState.isAtLeast(Lifecycle.State.RESUMED) &&
                landingVisible() && !isTransitionActive
        }
    }

    Box(
        modifier = modifier
            .clearAndSetSemantics { hideFromAccessibility() }
            .then(overlay),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier
            .padding(start = 16.dp)
            .height(108.dp)) {
            for (index in (basePage + 3).coerceAtMost(lastIndex) downTo basePage + 1) {
                key(books[index].recentBookKey()) {
                    RecentStackedCover(
                        book = books[index],
                        transform = {
                            val depth = (index - position()).coerceIn(0f, 3f)
                            translationX = direction * 8.dp.toPx() * depth
                            translationY = 4.dp.toPx() * depth
                            rotationZ = direction * (-3f + 4f * depth.coerceAtMost(2f))
                            alpha = when {
                                depth < 1f -> depth * depth
                                depth < 2f -> 1f - 0.18f * (depth - 1f)
                                else -> 0.82f * (3f - depth)
                            }
                        },
                    )
                }
            }
        }

        for (index in basePage..(basePage + 1).coerceAtMost(lastIndex)) {
            val book = books[index]
            val isSettledBook = index == settledPage
            key(book.recentBookKey()) {
                val colorKey = book.recentCoverColorKey()
                val coverColor = coverColors[colorKey]
                var image by remember(colorKey) { mutableStateOf<Image?>(null) }
                LaunchedEffect(image, coverColors) {
                    val loaded = image ?: return@LaunchedEffect
                    loaded.extractSeedColor(resources, fallback)?.let { coverColors[colorKey] = it }
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(if (isSettledBook) 0f else 1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(Modifier
                        .padding(start = 16.dp)
                        .height(108.dp)) {
                        RecentStackedCover(
                            book = book,
                            transform = {
                                val distance = abs(index - position()).coerceAtMost(1f)
                                projectRecentReadingDepth(
                                    distance = distance,
                                    depthAtEdge = if (isSettledBook) -0.18f else 0.52f,
                                    pitchAtEdge = if (isSettledBook) 10f else -24f,
                                )
                                rotationZ = -3f * direction
                            },
                            sharedCoverKey = book.bookUrl?.let { "home_recent_$it" },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onImageLoaded = { image = it },
                        )
                    }
                    RecentBookDetails(
                        book = book,
                        stackWidth = stackWidth,
                        marquee = isSettledBook,
                        coverColor = coverColor,
                        modifier = Modifier.graphicsLayer {
                            val distance = abs(index - position()).coerceAtMost(1f)
                            projectRecentReadingDepth(
                                distance = distance,
                                depthAtEdge = if (isSettledBook) -0.12f else 0.32f,
                                pitchAtEdge = if (isSettledBook) 7f else -15f,
                            )
                            // Fade most of the outgoing title before the new title occupies its line.
                            alpha = if (isSettledBook) {
                                (1f - distance / 0.44f).coerceAtLeast(0f)
                            } else {
                                (1f - distance / 0.72f).coerceAtLeast(0f)
                            }
                            transformOrigin = textPivot
                        },
                    )
                }
            }
        }
    }
}

private fun GraphicsLayerScope.projectRecentReadingDepth(
    distance: Float,
    depthAtEdge: Float,
    pitchAtEdge: Float,
) {
    // Perspective makes the nearer page larger while both landing points stay fixed.
    alpha = 1f - distance
    scaleX = 1f / (1f - depthAtEdge * distance)
    scaleY = scaleX
    rotationX = pitchAtEdge * distance
    cameraDistance = maxOf(480.dp.toPx(), size.height * 2f)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecentStackedCover(
    book: HomeRecentBookUi,
    transform: GraphicsLayerScope.() -> Unit,
    sharedCoverKey: String? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onImageLoaded: ((Image) -> Unit)? = null,
) {
    val coverShadow = remember {
        Shadow(
            radius = 3.dp,
            color = Color.Black.copy(alpha = 0.16f),
            offset = DpOffset(0.dp, 1.5.dp),
        )
    }
    BookshelfCover(
        name = book.name,
        author = book.author,
        path = book.coverPath,
        sourceOrigin = book.origin,
        bookUrl = book.bookUrl,
        radius = 5.dp,
        modifier = Modifier
            .offset(x = 4.dp, y = 4.dp)
            .width(58.dp)
            .aspectRatio(5f / 7f)
            .graphicsLayer {
                transform()
                // Keep the whole shadow in the same buffer on both sides of alpha == 1.
                compositingStrategy = CompositingStrategy.Offscreen
                outsets = LayerOutsets(6.dp)
            },
        coverModifier = Modifier.fillMaxSize(),
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedCoverKey = sharedCoverKey,
        coverShadow = coverShadow,
        onImageLoaded = onImageLoaded,
    )
}

@Composable
private fun RecentReadingProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val track = LegadoTheme.colorScheme.onSurface.copy(alpha = 0.11f)
    Canvas(modifier.height(3.dp)) {
        val corners = CornerRadius(size.height / 2f)
        drawRoundRect(color = track, cornerRadius = corners)
        val filled = size.width * progress
        if (filled > 0f) {
            drawRoundRect(
                color = color,
                topLeft = Offset(
                    if (layoutDirection == LayoutDirection.Rtl) size.width - filled else 0f,
                    0f,
                ),
                size = Size(filled, size.height),
                cornerRadius = corners,
            )
        }
    }
}

@Composable
private fun RecentBooksRow(
    books: List<HomeRecentBookUi>,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(books, key = { index, book -> "${book.bookUrl}_$index" }) { index, book ->
            RecentHistoryBookCard(
                book = book,
                onClick = { book.bookUrl?.let(onBookClick) },
            )
        }
    }
}

@Composable
private fun RecentHistoryBookCard(
    book: HomeRecentBookUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .aspectRatio(5f / 7f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = bookAccessibilityLabel(book.name, book.author)
                role = Role.Button
            },
    ) {
        BookshelfCover(
            name = book.name,
            author = book.author,
            path = book.coverPath,
            sourceOrigin = book.origin,
            modifier = Modifier.fillMaxSize(),
            coverModifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ReadingGoalCard(
    todayReadTimeMillis: Long,
    goalMinutes: Int,
    onClick: () -> Unit,
) {
    val todayMinutes = (todayReadTimeMillis / 60_000L).toInt()
    val progress = (todayReadTimeMillis / 60_000f / goalMinutes)
        .coerceIn(0f, 1f)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = LegadoTheme.colorScheme.surfaceContainerLow,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppIcon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = LegadoTheme.colorScheme.primary,
                )
                AppText(
                    text = stringResource(R.string.home_today_reading_goal),
                    style = LegadoTheme.typography.titleMediumEmphasized,
                )
                Spacer(modifier = Modifier.weight(1f))
                MediumTonalButton(
                    onClick = onClick,
                    icon = Icons.Default.Edit,
                    containerColor = LegadoTheme.colorScheme.surface,
                    contentDescription = stringResource(R.string.home_set_goal),
                )
            }

            SemiCircleProgress(
                progress = progress,
                modifier = Modifier
                    .width(240.dp)
                    .padding(vertical = 8.dp)
                    .aspectRatio(2f),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AppText(
                        text = stringResource(
                            R.string.home_today_goal_value,
                            todayMinutes,
                            goalMinutes,
                        ),
                        style = LegadoTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    AppText(
                        text = stringResource(R.string.home_minutes),
                        style = LegadoTheme.typography.bodySmall,
                        color = LegadoTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SemiCircleProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "DailyReadingProgress",
    )
    val trackColor = LegadoTheme.colorScheme.surfaceContainerHighest
    val progressColor = LegadoTheme.colorScheme.primary

    Box(
        // 装饰性进度环不暴露 progressBarRangeInfo：裸的进度语义会在 TalkBack 里
        // 形成一个朗读“进度条”却无任何交互的空焦点；进度已由环内文本表达，
        // 合并成一个节点即可。
        modifier = modifier.semantics(mergeDescendants = true) {},
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val boundingSize = Size(arcSize, arcSize)
            drawArc(
                color = trackColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = boundingSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = 180f,
                sweepAngle = 180f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = boundingSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Composable
private fun WebDavBackupCard(
    latestBackup: HomeBackupUi?,
    isLoading: Boolean,
    isLoadError: Boolean,
    isActionRunning: Boolean,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    val lastBackupText = when {
        isLoading -> stringResource(R.string.home_loading_webdav_backup)
        latestBackup != null -> {
            val date = remember(latestBackup.lastModify) {
                DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT,
                ).format(Date(latestBackup.lastModify))
            }
            stringResource(R.string.home_latest_backup_value, date)
        }

        isLoadError -> stringResource(R.string.home_webdav_backup_load_error)
        else -> stringResource(R.string.home_no_webdav_backup)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = LegadoTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppIcon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = LegadoTheme.colorScheme.primary,
                    modifier = Modifier.offset(y = 1.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = stringResource(R.string.home_webdav_backup),
                        style = LegadoTheme.typography.titleMediumEmphasized,
                    )
                    AppText(
                        text = lastBackupText,
                        style = LegadoTheme.typography.bodySmall,
                        color = if (isLoadError) {
                            LegadoTheme.colorScheme.error
                        } else {
                            LegadoTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                if (isLoading || isActionRunning) {
                    AppContainedLoadingIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isLoadError && !isLoading) {
                SmallTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                    icon = Icons.Default.Refresh,
                    text = stringResource(R.string.retry),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SmallTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = onBackup,
                        enabled = !isActionRunning,
                        containerColor = LegadoTheme.colorScheme.surface,
                        icon = Icons.Default.CloudUpload,
                        text = stringResource(R.string.backup),
                    )
                    SmallTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = onRestore,
                        enabled = !isActionRunning,
                        containerColor = LegadoTheme.colorScheme.surface,
                        icon = Icons.Default.CloudDownload,
                        text = stringResource(R.string.restore),
                    )
                    SmallTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSettings,
                        enabled = !isActionRunning,
                        containerColor = LegadoTheme.colorScheme.surface,
                        icon = Icons.Default.Settings,
                        text = stringResource(R.string.setting),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDashboardSettingsSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    visibleSections: Set<HomeDashboardSection>,
    onSectionVisibilityChanged: (HomeDashboardSection, Boolean) -> Unit,
) {
    AppModalBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (section in HomeDashboardSection.entries) {
                TinySwitchSettingItem(
                    title = stringResource(section.labelRes()),
                    checked = section in visibleSections,
                    onCheckedChange = { visible ->
                        onSectionVisibilityChanged(section, visible)
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeDialogs(
    dialog: HomeDialog?,
    onIntent: (HomeIntent) -> Unit,
) {
    val goalDialog = dialog as? HomeDialog.SetReadingGoal
    var goalInput by remember(goalDialog?.currentMinutes) {
        mutableStateOf(goalDialog?.currentMinutes?.toString().orEmpty())
    }
    val goalMinutes = goalInput.toIntOrNull()
    val isGoalValid = goalMinutes != null &&
            goalMinutes in 1..MAX_DAILY_READING_GOAL_MINUTES

    AppAlertDialog(
        show = goalDialog != null,
        onDismissRequest = { onIntent(HomeIntent.DismissDialog) },
        title = stringResource(R.string.home_set_reading_goal),
        content = {
            OutlinedTextField(
                value = goalInput,
                onValueChange = { value ->
                    goalInput = value.filter(Char::isDigit)
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    AppText(stringResource(R.string.home_goal_minutes_label))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmText = stringResource(R.string.ok),
        onConfirm = if (isGoalValid) {
            {
                onIntent(HomeIntent.UpdateReadingGoal(goalMinutes))
            }
        } else {
            null
        },
        dismissText = stringResource(R.string.cancel),
        onDismiss = { onIntent(HomeIntent.DismissDialog) },
    )

    val restoreDialog = dialog as? HomeDialog.ConfirmRestore
    AppAlertDialog(
        show = restoreDialog != null,
        onDismissRequest = { onIntent(HomeIntent.DismissDialog) },
        title = stringResource(R.string.restore_confirmation),
        text = restoreDialog?.let {
            stringResource(R.string.home_webdav_restore_confirmation, it.backupName)
        },
        confirmText = stringResource(R.string.ok),
        onConfirm = { onIntent(HomeIntent.ConfirmRestore) },
        dismissText = stringResource(R.string.cancel),
        onDismiss = { onIntent(HomeIntent.DismissDialog) },
    )
}

@Composable
private fun HomeSheets(
    sheet: HomeSheet?,
    visibleSections: Set<HomeDashboardSection>,
    onIntent: (HomeIntent) -> Unit,
) {
    HomeDashboardSettingsSheet(
        show = sheet is HomeSheet.DashboardSettings,
        onDismissRequest = { onIntent(HomeIntent.DismissSheet) },
        visibleSections = visibleSections,
        onSectionVisibilityChanged = { section, visible ->
            onIntent(HomeIntent.SetSectionVisible(section, visible))
        },
    )
    BackupOptionSheet(
        show = sheet is HomeSheet.BackupOptions,
        onDismissRequest = { onIntent(HomeIntent.DismissSheet) },
        onBackupToLocal = {
            onIntent(
                HomeIntent.BackupDestinationSelected(HomeBackupDestination.Local)
            )
        },
        onBackupToNetwork = {
            onIntent(
                HomeIntent.BackupDestinationSelected(HomeBackupDestination.WebDav)
            )
        },
        onBackupToLocalAndNetwork = {
            onIntent(
                HomeIntent.BackupDestinationSelected(HomeBackupDestination.LocalAndWebDav)
            )
        },
    )
    RestoreOptionSheet(
        show = sheet is HomeSheet.RestoreOptions,
        onDismissRequest = { onIntent(HomeIntent.DismissSheet) },
        onRestoreFromLocal = { onIntent(HomeIntent.RestoreFromLocal) },
        onRestoreFromNetwork = { onIntent(HomeIntent.RestoreFromNetwork) },
    )
}

@Composable
private fun AppModalBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    io.legado.app.ui.widget.components.modalBottomSheet.AppModalBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
        title = title,
    ) {
        content()
    }
}

@StringRes
private fun HomeDashboardSection.labelRes(): Int = when (this) {
    HomeDashboardSection.RecentBook -> R.string.home_recent_reading
    HomeDashboardSection.TotalReadBooks -> R.string.home_total_read_books
    HomeDashboardSection.TotalReadTime -> R.string.home_total_reading_time
    HomeDashboardSection.RecentBooks -> R.string.home_recent_books
    HomeDashboardSection.DailyGoal -> R.string.home_today_reading_goal
    HomeDashboardSection.WebDavBackup -> R.string.home_webdav_backup
    HomeDashboardSection.DimensionalBookshelf -> R.string.dimensional_bookshelf
}

private fun bookAccessibilityLabel(
    name: String,
    author: String,
): String {
    return if (author.isBlank()) name else "$name, $author"
}
