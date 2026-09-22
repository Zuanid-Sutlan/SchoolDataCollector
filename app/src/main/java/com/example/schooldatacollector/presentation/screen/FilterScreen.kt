package com.example.schooldatacollector.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.schooldatacollector.domain.FilterManager
import com.example.schooldatacollector.domain.FilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onNavigateBack: () -> Unit
) {
    val filterState by FilterManager.filterState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Filters") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select filters to apply across all screens:", style = MaterialTheme.typography.titleMedium)
            
            FilterRow(
                title = "Show ONLY missing info (Red)",
                subtitle = "Students missing CNIC or Father's Name",
                checked = filterState.showOnlyMissingInfo,
                onCheckedChange = { 
                    FilterManager.updateFilter(filterState.copy(showOnlyMissingInfo = it)) 
                }
            )
            
            FilterRow(
                title = "Show ONLY with comments (Yellow)",
                subtitle = "Students that have an active comment/issue",
                checked = filterState.showOnlyWithComments,
                onCheckedChange = { 
                    FilterManager.updateFilter(filterState.copy(showOnlyWithComments = it)) 
                }
            )

            FilterRow(
                title = "Show ONLY clear records (Green)",
                subtitle = "Students with no missing info and no comments",
                checked = filterState.showOnlyClear,
                onCheckedChange = { 
                    FilterManager.updateFilter(filterState.copy(showOnlyClear = it)) 
                }
            )

            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    FilterManager.updateFilter(
                        FilterState() // Reset to defaults
                    ) 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Filters")
            }
        }
    }
}

@Composable
fun FilterRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
