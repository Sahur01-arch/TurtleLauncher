/*
 * Turtle Launcher home dashboard (layout terinspirasi dari referensi UI lain)
 * ganti/lengkapi LauncherScreen.kt yang lama.
 *
 * CATATAN SEBELUM DI-COMPILE:
 * 1. semua painterResource(R.drawable.dash_ic_xxx) di bawah ini masih PLACEHOLDER.
 *    kamu perlu nambahin drawable vector baru di res/drawable dengan nama itu,
 *    atau ganti manual ke resource icon yang udah ada di project kamu.
 * 2. fungsi checkForUpdates / openGameFolder / openResourceMonitor / deleteInstance
 *    masih TODO — sambungin ke fungsi asli di VersionsManager / Version kamu.
 * 3. section "Activity" masih placeholder statis, karena project belum punya
 *    data source playtime/activity. sambungin ke sumber data kamu sendiri.
 */

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage

private val SidebarWidth = 84.dp
private val RightPanelWidth = 260.dp

// ------------------------------------------------------------------
// ROOT: 3 kolom (sidebar | dashboard | panel kanan)
// ------------------------------------------------------------------
@Composable
fun HomeDashboardScreen(
    onLaunchGame: (Version?) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toDownloadScreen: () -> Unit,
    toMultiplayerScreen: () -> Unit,
    toSettingsScreen: () -> Unit,
) {
    var selectedNav by remember { mutableStateOf(DashboardNavItem.HOME) }

    Row(modifier = Modifier.fillMaxSize()) {
        DashboardSidebar(
            modifier = Modifier
                .width(SidebarWidth)
                .fillMaxHeight(),
            selected = selectedNav,
            onSelect = { item ->
                selectedNav = item
                when (item) {
                    DashboardNavItem.MODS -> toDownloadScreen()
                    DashboardNavItem.SERVERS -> toMultiplayerScreen()
                    DashboardNavItem.ACCOUNTS -> toAccountManageScreen()
                    DashboardNavItem.SETTINGS -> toSettingsScreen()
                    else -> Unit // HOME / SANDBOX / AI / RECORDER / FRIENDS: belum ada layar tujuan
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            DashboardHero(
                modifier = Modifier.fillMaxWidth(),
                onLaunchGame = onLaunchGame,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActivitySection(modifier = Modifier.fillMaxWidth().weight(1f))
        }

        Column(
            modifier = Modifier
                .width(RightPanelWidth)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountSelectorCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = toAccountManageScreen
            )
            InstancesListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onNewInstance = toVersionManageScreen,
                onSelectInstance = { version -> VersionsManager.saveVersion(version) },
                onOpenSettings = onOpenVersionSettings,
            )
        }
    }
}

// ------------------------------------------------------------------
// SIDEBAR
// ------------------------------------------------------------------
private enum class DashboardNavItem(val labelRes: Int, val icon: Int) {
    HOME(R.string.dash_nav_home, R.drawable.dash_ic_home),
    SANDBOX(R.string.dash_nav_sandbox, R.drawable.dash_ic_sandbox),
    MODS(R.string.dash_nav_mods, R.drawable.dash_ic_mods),
    AI(R.string.dash_nav_ai, R.drawable.dash_ic_ai),
    RECORDER(R.string.dash_nav_recorder, R.drawable.dash_ic_recorder),
    SERVERS(R.string.dash_nav_servers, R.drawable.dash_ic_servers),
    ACCOUNTS(R.string.dash_nav_accounts, R.drawable.dash_ic_accounts),
    SETTINGS(R.string.dash_nav_settings, R.drawable.dash_ic_settings),
    FRIENDS(R.string.dash_nav_friends, R.drawable.dash_ic_friends),
}

@Composable
private fun DashboardSidebar(
    selected: DashboardNavItem,
    onSelect: (DashboardNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DashboardNavItem.entries.forEach { item ->
                DashboardSidebarItem(
                    item = item,
                    isSelected = item == selected,
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}

@Composable
private fun DashboardSidebarItem(
    item: DashboardNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = stringResource(item.labelRes),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

// ------------------------------------------------------------------
// DASHBOARD HERO (kartu besar tengah, kayak "COBA BARU" di screenshot)
// ------------------------------------------------------------------
@Composable
private fun DashboardHero(
    onLaunchGame: (Version?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()

    BackgroundCard(
        modifier = modifier.height(320.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // TODO: ganti Box background ini dengan gambar instance (screenshot/background pack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VersionIconImage(version = version, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = version?.getVersionName() ?: stringResource(R.string.versions_manage_no_versions),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        version?.takeIf { it.isValid() }?.let {
                            Text(
                                text = it.getVersionSummary(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = { onLaunchGame(version) },
                        colors = ButtonDefaults.buttonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_arrow_filled),
                            contentDescription = stringResource(R.string.main_launch_game)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.main_launch_game))
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DashboardActionButton(
            label = "Check for Updates",
            icon = R.drawable.dash_ic_refresh,
            modifier = Modifier.weight(1f)
        ) { /* TODO: sambungin ke pengecekan update instance */ }

        DashboardActionButton(
            label = "Open Game Folder",
            icon = R.drawable.dash_ic_folder,
            modifier = Modifier.weight(1f)
        ) { /* TODO: buka folder instance, misal via PathManager */ }

        DashboardActionButton(
            label = "Resource Monitor",
            icon = R.drawable.dash_ic_monitor,
            modifier = Modifier.weight(1f)
        ) { /* TODO: tampilkan dialog CPU/RAM usage */ }

        DashboardActionButton(
            label = "Delete Instance",
            icon = R.drawable.dash_ic_delete,
            modifier = Modifier.weight(1f),
            danger = true
        ) { /* TODO: konfirmasi lalu VersionsManager.deleteVersion(...) */ }
    }
}

@Composable
private fun DashboardActionButton(
    label: String,
    icon: Int,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor = if (danger) {
        MaterialTheme.colorScheme.errorContainer
    } else MaterialTheme.colorScheme.secondaryContainer

    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(painter = painterResource(icon), contentDescription = label, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, maxLines = 2)
        }
    }
}

// ------------------------------------------------------------------
// PANEL KANAN: account selector
// ------------------------------------------------------------------
@Composable
private fun AccountSelectorCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    BackgroundCard(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Account Selector", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = onClick)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                account?.let {
                    PlayerFace(account = it, avatarSize = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = it.username, style = MaterialTheme.typography.bodyMedium)
                        Text(text = getAccountTypeName(it), style = MaterialTheme.typography.labelSmall)
                    }
                } ?: Text(text = stringResource(R.string.account_add_new_account))
            }
        }
    }
}

// ------------------------------------------------------------------
// PANEL KANAN: daftar instance
// ------------------------------------------------------------------
@Composable
private fun InstancesListCard(
    onNewInstance: () -> Unit,
    onSelectInstance: (Version) -> Unit,
    onOpenSettings: (Version) -> Unit,
    modifier: Modifier = Modifier,
) {
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val current by VersionsManager.currentVersion.collectAsStateWithLifecycle()

    BackgroundCard(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Instances", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "+ New",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(onClick = onNewInstance)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(versions) { v ->
                    val isSelected = v == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { onSelectInstance(v) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VersionIconImage(version = v, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = v.getVersionName(), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            if (v.isValid()) {
                                Text(text = v.getVersionSummary(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// ACTIVITY (placeholder — belum ada data source di project)
// ------------------------------------------------------------------
@Composable
private fun ActivitySection(modifier: Modifier = Modifier) {
    BackgroundCard(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Activity", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "belum ada data playtime/activity — sambungin ke sumber data kamu di sini.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
