package catchup.app.ui.activity

import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import catchup.app.data.LinkManager
import catchup.app.service.openUrl
import catchup.app.ui.activity.FlickToDismissState.FlickGestureState.Dismissed
import catchup.app.ui.activity.ImageViewerScreen.Event
import catchup.app.ui.activity.ImageViewerScreen.Event.Close
import catchup.app.ui.activity.ImageViewerScreen.Event.CopyImage
import catchup.app.ui.activity.ImageViewerScreen.Event.NoOp
import catchup.app.ui.activity.ImageViewerScreen.Event.OpenInBrowser
import catchup.app.ui.activity.ImageViewerScreen.Event.SaveImage
import catchup.app.ui.activity.ImageViewerScreen.Event.ShareImage
import catchup.app.ui.activity.ImageViewerScreen.State
import catchup.base.ui.NavButton
import catchup.base.ui.NavButtonType.CLOSE
import catchup.compose.CatchUpTheme
import catchup.compose.rememberStableCoroutineScope
import coil.request.ImageRequest.Builder
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.overlays.BottomSheetOverlay
import dev.zacsweers.catchup.app.scaffold.R
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

@Parcelize
data class ImageViewerScreen(
  val id: String,
  val url: String,
  val isBitmap: Boolean,
  val alias: String?,
  val sourceUrl: String,
) : Screen {
  data class State(
    val id: String,
    val url: String,
    val alias: String?,
    val sourceUrl: String,
    val isBitmap: Boolean,
    val eventSink: (Event) -> Unit,
  ) : CircuitUiState

  sealed interface Event : CircuitUiEvent {
    data object Close : Event

    data object NoOp : Event // Weird but necessary because of the reuse in bottom sheet

    data object ShareImage : Event

    data object CopyImage : Event

    data object SaveImage : Event

    data class OpenInBrowser(val url: String) : Event
  }
}

@Inject
class ImageViewerPresenter(
  @Assisted private val screen: ImageViewerScreen,
  @Assisted private val navigator: Navigator,
  private val linkManager: LinkManager,
) : Presenter<State> {
  @CircuitInject(ImageViewerScreen::class, AppScope::class)
  @AssistedFactory
  fun interface Factory {
    fun create(screen: ImageViewerScreen, navigator: Navigator): ImageViewerPresenter
  }

  @Composable
  override fun present(): State {
    val accentColor = colorResource(R.color.colorAccent)
    val scope = rememberStableCoroutineScope()
    return State(
      id = screen.id,
      url = screen.url,
      alias = screen.alias,
      isBitmap = screen.isBitmap,
      sourceUrl = screen.sourceUrl,
    ) { event ->
      // TODO finish implementing these. Also why is copying an image on android so terrible in
      //  2023.
      when (event) {
        Close -> navigator.pop()
        CopyImage -> {}
        is OpenInBrowser -> {
          scope.launch { linkManager.openUrl(event.url, accentColor) }
        }

        SaveImage -> {}
        ShareImage -> {}
        NoOp -> {}
      }
    }
  }
}

@CircuitInject(ImageViewerScreen::class, AppScope::class)
@Composable
fun ImageViewer(state: State, modifier: Modifier = Modifier) {
  var showChrome by remember { mutableStateOf(true) }

  val view = LocalView.current
  val insetsController = remember { view.windowInsetsController }

  // Control visibility of system bars
  SideEffect {
    insetsController?.let {
      if (showChrome) {
        it.show(WindowInsets.Type.systemBars())
      } else {
        it.hide(WindowInsets.Type.systemBars())
        it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    }
  }

  DisposableEffect(Unit) { onDispose { insetsController?.show(WindowInsets.Type.systemBars()) } }

  CatchUpTheme(useDarkTheme = true) {
    val backgroundAlpha: Float by
      animateFloatAsState(targetValue = 1f, animationSpec = tween(), label = "backgroundAlpha")
    Surface(
      modifier.fillMaxSize().animateContentSize(),
      color = Color.Black.copy(alpha = backgroundAlpha),
      contentColor = Color.White,
    ) {
      Box(Modifier.fillMaxSize()) {
        // Image + scrim

        val dismissState = rememberFlickToDismissState()
        if (dismissState.gestureState is Dismissed) {
          state.eventSink(Close)
        }
        // TODO bind scrim with flick. animate scrim out after flick finishes? Or with flick?
        FlickToDismiss(state = dismissState) {
          val overlayHost = LocalOverlayHost.current
          val scope = rememberStableCoroutineScope()
          val zoomableState = rememberZoomableState(ZoomSpec(maxZoomFactor = 2f))
          val imageState = rememberZoomableImageState(zoomableState)
          // TODO loading loading indicator if there's no memory cached alias
          ZoomableAsyncImage(
            model =
              Builder(LocalContext.current)
                .data(state.url)
                .apply { state.alias?.let(::placeholderMemoryCacheKey) }
                .build(),
            contentDescription = "TODO",
            modifier = Modifier.fillMaxSize(),
            state = imageState,
            onClick = { showChrome = !showChrome },
            onLongClick = { launchShareSheet(scope, overlayHost, state) },
          )
        }

        // TODO pick color based on if image is underneath it or not. Similar to badges
        AnimatedVisibility(showChrome, enter = fadeIn(), exit = fadeOut()) {
          NavButton(Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding(), CLOSE) {
            state.eventSink(Close)
          }
        }
      }
    }
  }
}

private fun launchShareSheet(scope: CoroutineScope, overlayHost: OverlayHost, state: State) =
  scope.launch {
    val result =
      overlayHost.show(
        BottomSheetOverlay<Unit, Event>(Unit, onDismiss = { NoOp }) { _, navigator ->
          Column {
            // TODO icons?
            Text(
              modifier =
                Modifier.fillMaxWidth().clickable { navigator.finish(ShareImage) }.padding(16.dp),
              text = "Share",
            )
            Text(
              modifier =
                Modifier.fillMaxWidth().clickable { navigator.finish(SaveImage) }.padding(16.dp),
              text = "Save",
            )
            Text(
              modifier =
                Modifier.fillMaxWidth().clickable { navigator.finish(CopyImage) }.padding(16.dp),
              text = "Copy",
            )
            Text(
              modifier =
                Modifier.fillMaxWidth()
                  .clickable { navigator.finish(OpenInBrowser(state.url)) }
                  .padding(16.dp),
              text = "Open in Browser",
            )
          }
        }
      )
    state.eventSink(result)
  }
