package com.probuilder.scrollcount.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.probuilder.scrollcount.service.AccessibilityPermission
import com.probuilder.scrollcount.ui.AppViewModelProvider
import com.probuilder.scrollcount.ui.components.rememberAccessibilityEnabled
import kotlinx.coroutines.launch

/**
 * Three screens the user sees once, in this order:
 *
 *   1. what the app does,
 *   2. exactly what the accessibility permission is used for,
 *   3. the button that opens the system settings.
 *
 * Page 2 is not optional politeness - Google requires a clear, in-app
 * disclosure shown BEFORE the permission is requested, and reviewers look for
 * it. It says plainly what is read, what is not, and that nothing leaves the
 * phone.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val serviceEnabled = rememberAccessibilityEnabled()

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )

    // Ask for notification permission as the user reaches the last page, so the
    // system dialog does not appear before they know what the app is.
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == PAGE_COUNT - 1 &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> PrivacyPage()
                else -> PermissionPage(
                    serviceEnabled = serviceEnabled,
                    onOpenSettings = { AccessibilityPermission.openAccessibilitySettings(context) },
                )
            }
        }

        PageIndicator(
            pageCount = PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (pagerState.currentPage < PAGE_COUNT - 1) {
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = { viewModel.finish(onFinished) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = serviceEnabled,
                ) {
                    Text(if (serviceEnabled) "Start counting" else "Waiting for permission")
                }
                TextButton(onClick = { viewModel.finish(onFinished) }) {
                    Text("Skip for now")
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPage(
        icon = Icons.Filled.Visibility,
        title = "How many was that?",
        body = "ScrollCount quietly counts the Reels and Shorts you watch, so the " +
            "number is there when you want to look at it.\n\n" +
            "No blocking. No lectures. Just an honest count, a daily limit you " +
            "choose yourself, and a picture of your week you can share.",
    )
}

@Composable
private fun PrivacyPage() {
    OnboardingPage(
        icon = Icons.Filled.Lock,
        title = "What this app can and cannot see",
        body = "To notice a new reel, ScrollCount uses Android's Accessibility " +
            "Service. Here is exactly what that means.\n\n" +
            "It is used for one thing only: spotting when a new Reel or Short " +
            "appears on screen, so it can add 1 to your count.\n\n" +
            "It does not read your messages, captions, comments, usernames, " +
            "search terms or anything you type. It never records video or audio.\n\n" +
            "It only runs inside Instagram and YouTube. Every other app on your " +
            "phone is invisible to it.\n\n" +
            "Your counts stay on this phone. ScrollCount has no internet " +
            "permission, so it could not send them anywhere even if it wanted to.",
    )
}

@Composable
private fun PermissionPage(
    serviceEnabled: Boolean,
    onOpenSettings: () -> Unit,
) {
    OnboardingPage(
        icon = if (serviceEnabled) Icons.Filled.CheckCircle else Icons.Filled.Visibility,
        title = if (serviceEnabled) "You are all set" else "One switch to flip",
        body = if (serviceEnabled) {
            "ScrollCount is on and counting. You can turn it off again any time " +
                "from Settings inside the app, or from your phone's accessibility " +
                "settings."
        } else {
            "Tap the button below, find ScrollCount in the list, and switch it on.\n\n" +
                "Android will show you its own warning screen about what " +
                "accessibility services can do. That warning is the same for every " +
                "app of this kind - what ScrollCount actually does is on the " +
                "previous page."
        },
    ) {
        if (!serviceEnabled) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onOpenSettings) {
                Text("Open accessibility settings")
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Installed this as an APK rather than from Play Store? If the " +
                    "switch is greyed out, go to Settings, Apps, ScrollCount, then the " +
                    "three-dot menu, and choose Allow restricted settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    icon: ImageVector,
    title: String,
    body: String,
    extra: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (extra != null) extra()
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (selected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            )
        }
    }
}

private const val PAGE_COUNT = 3
