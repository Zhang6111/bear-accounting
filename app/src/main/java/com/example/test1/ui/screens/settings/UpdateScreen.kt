package com.example.test1.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
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
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class VersionInfo(
    val tagName: String,
    val body: String,
    val downloadUrl: String,
    val publishedAt: String,
    val versionCode: Int = 0
)

enum class DownloadStatus {
    Idle, Downloading, Downloaded, Installing, Installed, Failed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val packageInfo = remember {
        try {
            val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode
            }
            Triple(
                pkgInfo.versionName ?: "1.0.0",
                versionCode,
                context.packageManager.getApplicationInfo(context.packageName, 0).packageName
            )
        } catch (e: Exception) {
            Triple("1.0.0", 1, "com.example.test1")
        }
    }
    
    val currentVersion = packageInfo.first
    val currentVersionCode = packageInfo.second
    val packageName = packageInfo.third
    
    var isChecking by remember { mutableStateOf(false) }
    var versionInfo by remember { mutableStateOf<VersionInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    var downloadStatus by remember { mutableStateOf(DownloadStatus.Idle) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadProgressText by remember { mutableStateOf("") }

    fun checkForUpdates() {
        scope.launch {
            isChecking = true
            errorMessage = null
            
            try {
                val result = withContext(Dispatchers.IO) {
                    checkGitHubReleases("Zhang6111", "bear-accounting")
                }
                
                if (result != null) {
                    val currentVer = currentVersion
                    val latestVersion = result.tagName.removePrefix("v")
                    
                    val isNew = isNewerVersion(latestVersion, currentVer) || 
                               result.versionCode > currentVersionCode
                    
                    if (isNew) {
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

    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
            }
            context.startActivity(intent)
        }
    }

    fun installApk(context: Context, apkFile: File) {
        downloadStatus = DownloadStatus.Installing
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            downloadStatus = DownloadStatus.Installed
        } catch (e: Exception) {
            downloadStatus = DownloadStatus.Failed
            errorMessage = "安装失败: ${e.message}"
        }
    }

    fun downloadAndInstall() {
        scope.launch {
            try {
                downloadStatus = DownloadStatus.Downloading
                downloadProgress = 0
                
                val apkFile = withContext(Dispatchers.IO) {
                    downloadFile(context, versionInfo!!.downloadUrl) { progress, text ->
                        downloadProgress = progress
                        downloadProgressText = text
                    }
                }
                
                if (apkFile != null && apkFile.exists()) {
                    downloadStatus = DownloadStatus.Downloaded
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val hasInstallPermission = context.packageManager.canRequestPackageInstalls()
                        if (hasInstallPermission) {
                            installApk(context, apkFile)
                        } else {
                            requestInstallPermission(context)
                        }
                    } else {
                        installApk(context, apkFile)
                    }
                } else {
                    downloadStatus = DownloadStatus.Failed
                    errorMessage = "下载失败"
                }
            } catch (e: Exception) {
                downloadStatus = DownloadStatus.Failed
                errorMessage = "下载失败: ${e.message}"
            }
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
                "当前版本: $currentVersion (${packageName})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when (downloadStatus) {
                DownloadStatus.Downloading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { downloadProgress / 100f }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "正在下载: $downloadProgressText",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${downloadProgress}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                DownloadStatus.Downloaded -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("下载完成，准备安装...")
                        }
                    }
                }
                DownloadStatus.Installing -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在安装...")
                    }
                }
                DownloadStatus.Installed -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("安装完成，请重新打开应用")
                        }
                    }
                }
                else -> {}
            }
            
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
                        downloadAndInstall()
                        showUpdateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("下载并安装")
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
            var versionCode = 0
            
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                if (name.endsWith(".apk")) {
                    downloadUrl = asset.getString("browser_download_url")
                    val verMatch = Regex("(\\d+\\.\\d+\\.\\d+)").find(name)
                    if (verMatch != null) {
                        val v = verMatch.groupValues[1]
                        versionCode = v.replace(".", "").toIntOrNull() ?: 0
                    }
                    break
                }
            }
            
            if (downloadUrl.isEmpty()) {
                downloadUrl = json.getString("html_url")
            }
            
            VersionInfo(tagName, body, downloadUrl, publishedAt, versionCode)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private suspend fun downloadFile(
    context: Context,
    url: String,
    onProgress: (Int, String) -> Unit
): File? {
    return try {
        val downloadUrl = URL(url)
        val connection = downloadUrl.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        
        val fileLength = connection.contentLength
        val inputStream = connection.inputStream
        val fileName = "bear_accounting_${System.currentTimeMillis()}.apk"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(4096)
        var total: Long = 0
        var count: Int
        
        while (inputStream.read(buffer).also { count = it } != -1) {
            total += count
            val progress = if (fileLength > 0) (total * 100 / fileLength).toInt() else 0
            val progressText = "${formatFileSize(total)} / ${formatFileSize(fileLength.toLong())}"
            
            withContext(Dispatchers.Main) {
                onProgress(progress, progressText)
            }
            
            outputStream.write(buffer, 0, count)
        }
        
        outputStream.close()
        inputStream.close()
        
        file
    } catch (e: Exception) {
        null
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
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
