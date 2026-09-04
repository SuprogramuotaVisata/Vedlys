package com.suprogramuota_visata.vedlys.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LazyListVerticalScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier
)
