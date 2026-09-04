package com.suprogramuota_visata.vedlys.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun LazyListVerticalScrollbar(
    state: LazyListState,
    modifier: Modifier
) {
    // No-op on Android (touch gestures have native scroll indicator)
}
