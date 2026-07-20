package com.mobile.tamatami.ui.screens.settings.sections

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.data.backup.BackupRepository
import com.mobile.tamatami.data.backup.InvalidBackupException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * "Data" settings section: export the full database to a JSON file (via the
 * system CreateDocument picker → share/save) and import a previously exported
 * file (via OpenDocument), which **replaces all current data** after a
 * confirmation dialog.
 *
 * File I/O lives here rather than in a ViewModel because the Storage Access
 * Framework launchers and `contentResolver` need the Activity `Context`;
 * (de)serialization + the DB transaction stay in [BackupRepository].
 */
@Composable
fun DataBackupSection(
    backupRepository: BackupRepository,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Holds JSON produced by an export until the user picks a destination Uri.
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    // Holds parsed-and-validated JSON text awaiting the replace confirmation.
    var pendingImportText by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

    val createDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val jsonToWrite = pendingExportJson
        pendingExportJson = null
        if (uri == null || jsonToWrite == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(jsonToWrite.toByteArray())
                    } ?: error("Could not open file for writing.")
                }
                toast("Backup exported.")
            } catch (e: Exception) {
                toast("Export failed: ${e.message}")
            }
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                        ?: error("Could not open file for reading.")
                }
                // Validate before prompting so we don't ask to wipe data for a bad file.
                withContext(Dispatchers.IO) { backupRepository.parse(text) }
                pendingImportText = text
            } catch (e: InvalidBackupException) {
                toast(e.message ?: "Invalid backup file.")
            } catch (e: Exception) {
                toast("Import failed: ${e.message}")
            }
        }
    }

    SettingsSectionCard(title = "Data", modifier = modifier) {
        Text(
            "Export all your data to a JSON file, or restore from a previous export. " +
                "Importing replaces everything currently in the app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                enabled = !busy,
                onClick = {
                    scope.launch {
                        try {
                            val ts = today.toEpochDay()
                            val jsonText = withContext(Dispatchers.IO) {
                                backupRepository.exportToJson(exportedAtEpochMs = ts * MILLIS_PER_DAY)
                            }
                            pendingExportJson = jsonText
                            createDocLauncher.launch("tamatami-backup-$ts.json")
                        } catch (e: Exception) {
                            toast("Export failed: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            ) { Text("Export") }

            OutlinedButton(
                enabled = !busy,
                onClick = { openDocLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.weight(1f),
            ) { Text("Import") }
        }
    }

    val importText = pendingImportText
    if (importText != null) {
        AlertDialog(
            onDismissRequest = { pendingImportText = null },
            title = { Text("Replace all data?") },
            text = {
                Text(
                    "This will erase your current data and replace it with the " +
                        "contents of the backup file. This can't be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        pendingImportText = null
                        busy = true
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) { backupRepository.importFromJson(importText) }
                                toast("Data restored.")
                            } catch (e: Exception) {
                                toast("Import failed: ${e.message}")
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) { Text("Replace") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportText = null }) { Text("Cancel") }
            },
        )
    }
}

private const val MILLIS_PER_DAY = 86_400_000L
