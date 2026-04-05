package com.example.test1.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class VersionInfo(
    val tagName: String,
    val body: String,
    val downloadUrl: String,
    val publishedAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isChecking by remember { mutableStateOf(false) }
    var versionInfo by remember { mutableStateOf<VersionInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    fun checkForUpdates() {
        scope.launch {
            isChecking = true
            errorMessage = null
            
            try {
                    val result = withContext(Dispatchers.IO) {
                        checkGitHubReleases("Zhang6111", "bear-accounting")
                    }
                
                if (result != null) {
                    val currentVersion = "0.0.3"
                    val latestVersion = result.tagName.removePrefix("v")
                    
                    if (isNewerVersion(latestVersion, currentVersion)) {
                        versionInfo = result
                        showUpdateDialog = true
                    } else {
                        errorMessage = "当前已是最新版本"
                    }
                } else {
                    errorMessage = "无法获取版本信息"
                }
            } catch (e: Exception) {
                errorMessage = "检查更新失败: ${e.message}"
            }
            
            isChecking = false
        }
    }

    LaunchedEffect(Unit) {
        checkForUpdates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("版本更新", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Icon(
                Icons.Default.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "小熊记账",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "当前版本: 0.0.3",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isChecking) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("正在检查更新...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (errorMessage?.contains("最新") == true) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (errorMessage?.contains("最新") == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (errorMessage?.contains("最新") == true) 
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(errorMessage!!)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { checkForUpdates() },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重新检查")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "检查更新请确保设备已连接网络",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showUpdateDialog && versionInfo != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Update, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发现新版本")
                }
            },
            text = {
                Column {
                    Text("最新版本: ${versionInfo!!.tagName}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("更新内容:", fontWeight = FontWeight.Bold)
                    Text(versionInfo!!.body.take(200), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo!!.downloadUrl))
                        context.startActivity(intent)
                        showUpdateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("立即更新")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("稍后再说")
                }
            }
        )
    }
}

private suspend fun checkGitHubReleases(owner: String, repo: String): VersionInfo? {
    return try {
        val url = URL("https://api.github.com/repos/$owner/$repo/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        
        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            
            val tagName = json.getString("tag_name")
            val body = json.optString("body", "暂无更新说明")
            val publishedAt = json.getString("published_at")
            
            val assets = json.getJSONArray("assets")
            var downloadUrl = ""
            
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.getString("name").endsWith(".apk")) {
                    downloadUrl = asset.getString("browser_download_url")
                    break
                }
            }
            
            if (downloadUrl.isEmpty()) {
                downloadUrl = json.getString("html_url")
            }
            
            VersionInfo(tagName, body, downloadUrl, publishedAt)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun isNewerVersion(latest: String, current: String): Boolean {
    try {
        val latestParts = latest.split(".").map { it.toInt() }
        val currentParts = current.split(".").map { it.toInt() }
        
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val latestNum = latestParts.getOrElse(i) { 0 }
            val currentNum = currentParts.getOrElse(i) { 0 }
            
            if (latestNum > currentNum) return true
            if (latestNum < currentNum) return false
        }
        return false
    } catch (e: Exception) {
        return false
    }
}
