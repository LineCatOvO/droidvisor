package com.droidvisor.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidvisor.vm.AvfCapabilityChecker
import com.droidvisor.ui.viewmodel.PermissionViewModel
import com.droidvisor.ui.viewmodel.PermissionState

@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionState by viewModel.permissionState.collectAsState()
    var showAvfTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.updatePermissionState(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "环境检测",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Droidvisor 需要以下条件才能正常运行",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            PermissionCard(
                icon = Icons.Default.NetworkCheck,
                title = "网络访问",
                description = "用于虚拟机网络通信和 Docker 镜像拉取",
                isGranted = permissionState.hasInternetPermission
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 私有存储信息卡（始终显示已授权，因为不申请任何外部存储权限）
            PermissionCard(
                icon = Icons.Default.FolderSpecial,
                title = "私有存储",
                description = "所有数据安全存储在应用私有空间，无需额外权限",
                isGranted = true,
                isInfoOnly = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Default.Android,
                title = "Android 14+",
                description = "需要 Android 14 或更高版本",
                isGranted = permissionState.meetsMinSdk
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Default.Memory,
                title = "虚拟化框架 (AVF)",
                description = when {
                    permissionState.avfSupported -> "Android 虚拟化框架已启用"
                    permissionState.hasPermissionIssues -> "设备支持 AVF，但应用未获得管理权限"
                    else -> "Android 虚拟化框架支持（运行真实虚拟机的必要条件）"
                },
                isGranted = permissionState.avfSupported,
                isCritical = true,
                statusLabel = when {
                    permissionState.avfSupported -> "已启用"
                    permissionState.hasPermissionIssues -> "需授权"
                    else -> null
                },
                trailingContent = {
                    if (permissionState.hasPermissionIssues) {
                        TextButton(
                            onClick = { showAvfTutorial = !showAvfTutorial },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF2196F3)
                            )
                        ) {
                            Icon(
                                imageVector = if (showAvfTutorial) Icons.Default.ExpandLess else Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("教程")
                        }
                    }
                }
            )

            // AVF 权限教程面板
            AnimatedVisibility(
                visible = showAvfTutorial,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                AvfTutorialPanel(context = context)
            }

            if (showAvfTutorial) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Default.Shield,
                title = "受保护虚拟机 (pKVM)",
                description = "pKVM 支持，提供硬件级安全隔离",
                isGranted = permissionState.protectedVmSupported,
                isCritical = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Default.Computer,
                title = "普通虚拟机 (KVM)",
                description = "KVM 支持，适合开发和测试环境",
                isGranted = permissionState.nonProtectedVmSupported,
                isCritical = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (permissionState.avfUnavailableReasons.isNotEmpty()) {
                AvfUnavailableWarning(
                    reasons = permissionState.avfUnavailableReasons,
                    isAvfSupported = permissionState.avfSupported
                )
            } else if (permissionState.avfWarnings.isNotEmpty()) {
                AvfPartialWarning(warnings = permissionState.avfWarnings)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = !permissionState.allPermissionsGranted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (permissionState.missingPermissions.isNotEmpty()) {
                        Text(
                            text = "缺少必要条件: ${permissionState.missingPermissions.joinToString(", ")}",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!permissionState.hasInternetPermission) {
                            OutlinedButton(
                                onClick = { requestInternetPermission(context) }
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("授予权限")
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = permissionState.allPermissionsGranted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (permissionState.isAvfFullyAvailable) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Green
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "所有条件已满足",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Green
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onAllPermissionsGranted,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            @Suppress("DEPRECATION")
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始使用")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AVF 不可用，将以模拟模式运行",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "模拟模式下虚拟机、Docker 和终端功能均为演示数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.updatePermissionState(context) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("重新检测")
                            }
                            Button(
                                onClick = onAllPermissionsGranted,
                                modifier = Modifier.weight(1f)
                            ) {
                                @Suppress("DEPRECATION")
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("继续使用")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AvfUnavailableWarning(
    reasons: List<AvfCapabilityChecker.AvfUnavailableReason>,
    isAvfSupported: Boolean
) {
    val context = LocalContext.current
    val packageName = context.packageName

    // 将原因分为三类：权限问题、硬件限制、其他
    val permissionReasons = reasons.filter { it.isPermissionIssue }
    val hardwareReasons = reasons.filter { it.isHardwareLimitation }
    val otherReasons = reasons.filter { !it.isPermissionIssue && !it.isHardwareLimitation }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 权限问题卡片（可解决）
        if (permissionReasons.isNotEmpty()) {
            AvfPermissionGuideCard(
                permissionReasons = permissionReasons,
                packageName = packageName,
                context = context
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 硬件限制卡片（无法解决）
        if (hardwareReasons.isNotEmpty()) {
            AvfHardwareLimitationCard(reasons = hardwareReasons)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 其他原因卡片
        if (otherReasons.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800).copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "其他问题",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    otherReasons.forEach { reason ->
                        ReasonItem(reason = reason, tint = Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

@Composable
private fun AvfPermissionGuideCard(
    permissionReasons: List<AvfCapabilityChecker.AvfUnavailableReason>,
    packageName: String,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "需要授予权限",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            permissionReasons.forEach { reason ->
                Row(
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF2196F3).copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reason.displayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2196F3).copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2196F3).copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "开启方法",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 步骤 1
            Text(
                text = "1. 在电脑上安装 ADB 工具（Android SDK Platform-Tools）",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 步骤 2
            Text(
                text = "2. 在手机上开启 USB 调试（设置 → 开发者选项）",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 步骤 3
            Text(
                text = "3. 用 USB 连接手机到电脑，执行以下命令：",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ADB 命令卡片 - 可复制
            val adbCommand = "adb shell pm grant $packageName android.permission.MANAGE_VIRTUAL_MACHINE"
            AdbCommandCard(command = adbCommand, context = context)

            Spacer(modifier = Modifier.height(8.dp))

            // 步骤 4
            Text(
                text = "4. 授予后点击「重新检测」验证权限",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 补充说明
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MANAGE_VIRTUAL_MACHINE 是系统级权限，无法通过应用内弹窗授予，必须通过 ADB 命令手动开启。授权后无需重启应用即可生效。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdbCommandCard(command: String, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$",
                    color = Color(0xFF4CAF50),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = command,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", command))
                        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = Color(0xFF90CAF9),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvfTutorialPanel(context: Context) {
    val packageName = context.packageName

    // AVF 相关权限列表
    data class AvfPermission(
        val permission: String,
        val name: String,
        val description: String
    )

    val avfPermissions = remember {
        listOf(
            AvfPermission(
                permission = "android.permission.MANAGE_VIRTUAL_MACHINE",
                name = "虚拟化管理权限",
                description = "允许应用创建、管理和监控虚拟机实例"
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(enabled = true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2196F3).copy(alpha = 0.3f))
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AVF 权限开启教程",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = "通过 ADB 命令为应用授予虚拟化所需权限",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF2196F3).copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            // 前置条件
            TutorialSection(
                stepNumber = "前置",
                title = "准备工作",
                contentColor = Color(0xFF2196F3)
            ) {
                TutorialStep(
                    number = "1",
                    text = "在电脑上安装 ADB 工具",
                    subText = "下载 Android SDK Platform-Tools 并解压到任意目录"
                )
                Spacer(modifier = Modifier.height(8.dp))
                TutorialStep(
                    number = "2",
                    text = "在手机上开启 USB 调试",
                    subText = "设置 → 关于手机 → 连续点击「版本号」7次 → 返回设置 → 开发者选项 → 开启 USB 调试"
                )
                Spacer(modifier = Modifier.height(8.dp))
                TutorialStep(
                    number = "3",
                    text = "使用 USB 数据线连接手机到电脑",
                    subText = "首次连接时手机上会弹出授权弹窗，勾选「一律允许」后确认"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 权限授权命令
            TutorialSection(
                stepNumber = "核心",
                title = "执行授权命令",
                contentColor = Color(0xFF4CAF50)
            ) {
                Text(
                    text = "在电脑终端中依次执行以下命令（每条命令执行后应无报错输出）：",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                avfPermissions.forEachIndexed { index, avfPerm ->
                    val command = "adb shell pm grant $packageName ${avfPerm.permission}"
                    TutorialCommandCard(
                        index = index + 1,
                        name = avfPerm.name,
                        description = avfPerm.description,
                        command = command,
                        context = context
                    )
                    if (index < avfPermissions.size - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 验证部分
            TutorialSection(
                stepNumber = "验证",
                title = "验证授权结果",
                contentColor = Color(0xFFFF9800)
            ) {
                val verifyCommand = "adb shell pm list permissions -d -g | grep $packageName | grep virtual_machine"
                Text(
                    text = "执行以下命令确认权限已授予：",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                AdbCommandCard(command = verifyCommand, context = context)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "如果输出包含 MANAGE_VIRTUAL_MACHINE 且状态为 granted，说明授权成功",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 一键脚本
            TutorialSection(
                stepNumber = "快捷",
                title = "一键授权脚本",
                contentColor = Color(0xFF9C27B0)
            ) {
                Text(
                    text = "也可以将以下脚本保存为 grant_avf.sh 并执行：",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                val scriptCommands = buildString {
                    appendLine("#!/bin/bash")
                    appendLine("# AVF 权限一键授权脚本")
                    appendLine("echo \"正在为 $packageName 授予 AVF 权限...\"")
                    avfPermissions.forEach { avfPerm ->
                        appendLine("adb shell pm grant $packageName ${avfPerm.permission}")
                    }
                    appendLine("echo \"授权完成！\"")
                }
                AdbCommandCard(command = scriptCommands, context = context)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF2196F3).copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // 底部说明
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                @Suppress("DEPRECATION")
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "关于 AVF 权限",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MANAGE_VIRTUAL_MACHINE 是 Android 系统级权限（signature|privileged 级别），" +
                                "无法通过应用内弹窗授予，只能通过 ADB 命令手动授权。授予后无需重启应用即可生效。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "提示：每次重新安装应用后需要重新授权；设备重启后已授权权限保持不变。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialSection(
    stepNumber: String,
    title: String,
    contentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // 步骤标识
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun TutorialStep(
    number: String,
    text: String,
    subText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TutorialCommandCard(
    index: Int,
    name: String,
    description: String,
    command: String,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            AdbCommandCard(command = command, context = context)
        }
    }
}

@Composable
private fun AvfHardwareLimitationCard(reasons: List<AvfCapabilityChecker.AvfUnavailableReason>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "设备不支持虚拟化",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            reasons.forEach { reason ->
                ReasonItem(reason = reason, tint = Color.Red)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Red.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "这是设备硬件/固件层面的限制，无法通过软件方式解决。应用将以模拟模式运行，虚拟机操作均为演示数据。",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ReasonItem(reason: AvfCapabilityChecker.AvfUnavailableReason, tint: Color) {
    Row(
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = tint.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = reason.displayText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = tint.copy(alpha = 0.9f)
            )
            Text(
                text = reason.suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun AvfPartialWarning(warnings: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "部分虚拟化功能受限",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            warnings.forEach { warning ->
                Row(
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800).copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800).copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isCritical: Boolean = false,
    isInfoOnly: Boolean = false,
    statusLabel: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val statusColor = when {
        isInfoOnly -> Color(0xFF4CAF50) // 绿色 - 信息展示
        isGranted -> Color.Green
        isCritical -> Color.Red
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isInfoOnly && isCritical && !isGranted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "必要",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Red
                        )
                    }
                    if (isInfoOnly) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "安全",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    if (statusLabel != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (trailingContent != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    trailingContent()
                }
            }

            Icon(
                imageVector = when {
                    isInfoOnly -> Icons.Default.Lock
                    isGranted -> Icons.Default.CheckCircle
                    else -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun requestInternetPermission(context: android.content.Context) {
    // 仅请求网络相关权限（INTERNET 是 normal 权限，自动授予）
    // 不再请求任何存储权限
}
