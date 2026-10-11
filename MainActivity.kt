package com.example.siyahcamdosya

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import java.util.Locale

private val Bg = Color(0xFF07090D)
private val Panel = Color(0xCC171B24)
private val Edge = Color(0xFF343B4C)
private val Cyan = Color(0xFF77E6F4)
private val Muted = Color(0xFF9BA6B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SiyahCamApp() }
    }
}

data class FileItem(val name: String, val uri: Uri, val size: Long, val mime: String?, val isDirectory: Boolean)

@Composable
fun SiyahCamApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var rootUri by remember { mutableStateOf<Uri?>(null) }
    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var scanItems by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var currentFolder by remember { mutableStateOf<Uri?>(null) }
    var folderName by remember { mutableStateOf("Henüz klasör seçilmedi") }
    var selectedTab by remember { mutableStateOf("Ana Sayfa") }
    var notice by remember { mutableStateOf("Başlamak için erişmek istediğin klasörü seç.") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteCandidates by remember { mutableStateOf<List<FileItem>>(emptyList()) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}
            rootUri = uri
            currentFolder = uri
            val root = DocumentFile.fromTreeUri(context, uri)
            folderName = root?.name ?: "Seçilen klasör"
            fun listFolder(u: Uri): List<FileItem> {
                val dir = DocumentFile.fromTreeUri(context, u) ?: return emptyList()
                return dir.listFiles().map {
                    FileItem(it.name ?: "Adsız dosya", it.uri, if (it.isFile) it.length() else 0L, it.type, it.isDirectory)
                }.sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase(Locale.getDefault()) })
            }
            files = listFolder(uri)
            scanItems = collectFiles(DocumentFile.fromTreeUri(context, uri))
            notice = "Klasör erişimi hazır. Dosyaların silinmesi için ayrıca onay istenir."
        }
    }

    val totalBytes = scanItems.sumOf { it.size }
    val largeFiles = scanItems.filter { !it.isDirectory && it.size >= 100L * 1024 * 1024 }
    val likelyJunk = scanItems.filter {
        !it.isDirectory && (
            it.name.startsWith(".") ||
            it.name.endsWith(".tmp", true) ||
            it.name.endsWith(".temp", true) ||
            it.name.endsWith(".log", true) ||
            it.name.endsWith(".bak", true)
        )
    }
    val imageCount = scanItems.count { it.mime?.startsWith("image/") == true }
    val videoCount = scanItems.count { it.mime?.startsWith("video/") == true }
    val docCount = scanItems.count {
        it.mime?.startsWith("text/") == true ||
        it.name.endsWith(".pdf", true) || it.name.endsWith(".doc", true) ||
        it.name.endsWith(".docx", true) || it.name.endsWith(".xls", true) ||
        it.name.endsWith(".xlsx", true) || it.name.endsWith(".ppt", true)
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan, background = Bg, surface = Panel, onBackground = Color.White,
        onSurface = Color.White, secondary = Color(0xFFB4A1FF)
    )) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF101722), Bg, Color(0xFF050609)))
            )
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("SİYAH CAM", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        Text("Dosya Yöneticisi", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    GlassCard(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Text("◈", color = Cyan, fontSize = 25.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Ana Sayfa", "Dosyalar", "Temizlik").forEach { tab ->
                        val active = selectedTab == tab
                        Box(
                            Modifier.weight(1f).background(if (active) Color(0xFF23323D) else Panel, RoundedCornerShape(18.dp))
                                .border(1.dp, if (active) Cyan.copy(alpha = .65f) else Edge, RoundedCornerShape(18.dp))
                                .clickable { selectedTab = tab }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(tab, color = if (active) Cyan else Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                when (selectedTab) {
                    "Ana Sayfa" -> {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Text("DEPOLAMA ANALİZİ", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(if (rootUri == null) "Klasörünü analiz et" else formatBytes(totalBytes), fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            Text(if (rootUri == null) "Dosyalarının toplam boyutunu görmek için klasör seç." else "${scanItems.size} dosya ve klasör tarandı", color = Muted, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatPill("Fotoğraflar", imageCount.toString(), Modifier.weight(1f))
                                StatPill("Videolar", videoCount.toString(), Modifier.weight(1f))
                                StatPill("Belgeler", docCount.toString(), Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { folderPicker.launch(null) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color(0xFF071016))) {
                                Text(if (rootUri == null) "Klasör seç ve analiz et" else "Başka klasör seç", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("HIZLI ERİŞİM", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickCard("▧", "Fotoğraflar", "$imageCount öğe", Modifier.weight(1f)) { selectedTab = "Dosyalar" }
                            QuickCard("▶", "Videolar", "$videoCount öğe", Modifier.weight(1f)) { selectedTab = "Dosyalar" }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickCard("▤", "Belgeler", "$docCount öğe", Modifier.weight(1f)) { selectedTab = "Dosyalar" }
                            QuickCard("✧", "Temizlik", "${likelyJunk.size} aday", Modifier.weight(1f)) { selectedTab = "Temizlik" }
                        }
                        Spacer(Modifier.height(14.dp))
                        GlassCard(Modifier.fillMaxWidth()) {
                            Text("GÜVENLİ TEMİZLİK", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.height(4.dp))
                            Text("Silinecek dosyalar önce listelenir; hiçbir şey otomatik silinmez.", color = Muted, fontSize = 13.sp)
                            Text(notice, color = Cyan, fontSize = 12.sp)
                        }
                    }
                    "Dosyalar" -> {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Dosyaların", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Text(folderName, color = Muted, fontSize = 12.sp)
                            }
                            TextButton(onClick = { folderPicker.launch(null) }) { Text("Klasör seç") }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (rootUri == null) EmptyState("Önce erişmek istediğin klasörü seç.")
                        else if (files.isEmpty()) EmptyState("Bu klasörde görüntülenecek öğe yok.")
                        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                            items(files) { item ->
                                GlassCard(Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (item.isDirectory) "▰" else iconFor(item.mime, item.name), color = Cyan, fontSize = 22.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(item.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            Text(if (item.isDirectory) "Klasör" else formatBytes(item.size), color = Muted, fontSize = 12.sp)
                                        }
                                        if (!item.isDirectory) TextButton(onClick = {
                                            deleteCandidates = listOf(item); showDeleteConfirm = true
                                        }) { Text("Sil", color = Color(0xFFFF8A8A)) }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Text("Temizlik merkezi", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Yalnızca seçtiğin klasör içindeki dosyalar incelenir.", color = Muted, fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        GlassCard(Modifier.fillMaxWidth()) {
                            Text("OLASI GEREKSİZ DOSYALAR", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("${likelyJunk.size} dosya adayı", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("Gizli dosyalar, .tmp, .temp, .log ve .bak uzantıları aday olarak gösterilir. Bunların hepsi gereksiz olmayabilir.", color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { deleteCandidates = likelyJunk; showDeleteConfirm = true },
                                enabled = likelyJunk.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color(0xFF071016))
                            ) { Text("Adayları gözden geçir", fontWeight = FontWeight.Bold) }
                        }
                        Spacer(Modifier.height(12.dp))
                        GlassCard(Modifier.fillMaxWidth()) {
                            Text("BÜYÜK DOSYALAR", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("${largeFiles.size} dosya · 100 MB üzeri", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Büyük dosyalar otomatik olarak gereksiz sayılmaz; silmeden önce tek tek kontrol et.", color = Muted, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        if (rootUri == null) Button(onClick = { folderPicker.launch(null) }, modifier = Modifier.fillMaxWidth()) { Text("Önce klasör seç") }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = Color(0xFF1A202B),
                title = { Text("Silme işlemini onayla") },
                text = {
                    Column {
                        Text("${deleteCandidates.size} dosya seçildi. Silme işlemi geri alınamayabilir.")
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.heightIn(max = 220.dp)) {
                            items(deleteCandidates.take(30)) { Text("• ${it.name}", color = Muted, fontSize = 12.sp) }
                        }
                        if (deleteCandidates.size > 30) Text("ve ${deleteCandidates.size - 30} dosya daha…", color = Muted)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        var removed = 0
                        deleteCandidates.forEach { item ->
                            try {
                                val doc = DocumentFile.fromSingleUri(context, item.uri)
                                if (doc?.delete() == true) removed++
                            } catch (_: Exception) {}
                        }
                        rootUri?.let { uri ->
                            files = (DocumentFile.fromTreeUri(context, uri)?.listFiles() ?: emptyArray()).map {
                                FileItem(it.name ?: "Adsız dosya", it.uri, if (it.isFile) it.length() else 0L, it.type, it.isDirectory)
                            }
                            scanItems = collectFiles(DocumentFile.fromTreeUri(context, uri))
                        }
                        notice = "$removed dosya silindi."
                        showDeleteConfirm = false
                    }) { Text("Seçilenleri sil", color = Color(0xFFFF8A8A)) }
                },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Vazgeç") } }
            )
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, contentAlignment: Alignment = Alignment.TopStart, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.background(
            Brush.linearGradient(listOf(Color(0xDD222A37), Color(0xB811151D), Color(0xCC1A202B))),
            RoundedCornerShape(24.dp)
        ).border(1.dp, Brush.linearGradient(listOf(Color(0xFF68778D), Color(0x553D4A60), Color(0xFF283544))), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        content = content
    )
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Color(0x552B3544), RoundedCornerShape(16.dp)).padding(10.dp)) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Muted, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
fun QuickCard(symbol: String, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier.clickable(onClick = onClick)) {
        Text(symbol, color = Cyan, fontSize = 22.sp)
        Spacer(Modifier.height(7.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}

@Composable
fun EmptyState(message: String) {
    GlassCard(Modifier.fillMaxWidth()) { Text(message, color = Muted) }
}

fun collectFiles(root: DocumentFile?, depth: Int = 0): List<FileItem> {
    if (root == null || depth > 12) return emptyList()
    val result = mutableListOf<FileItem>()
    try {
        root.listFiles().forEach { child ->
            result.add(FileItem(child.name ?: "Adsız dosya", child.uri, if (child.isFile) child.length() else 0L, child.type, child.isDirectory))
            if (child.isDirectory) result.addAll(collectFiles(child, depth + 1))
        }
    } catch (_: Exception) {}
    return result
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var idx = -1
    do { size /= 1024.0; idx++ } while (size >= 1024 && idx < units.lastIndex)
    return String.format(Locale.getDefault(), "%.1f %s", size, units[idx.coerceAtMost(units.lastIndex)])
}

fun iconFor(mime: String?, name: String): String = when {
    mime?.startsWith("image/") == true -> "▧"
    mime?.startsWith("video/") == true -> "▶"
    mime?.startsWith("audio/") == true -> "♫"
    name.endsWith(".pdf", true) -> "▤"
    else -> "◇"
}
