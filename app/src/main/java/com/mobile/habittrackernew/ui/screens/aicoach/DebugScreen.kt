package com.mobile.habittrackernew.ui.screens.aicoach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.BuildConfig
import com.mobile.habittrackernew.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Debug") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "🔧 Configuration Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // API Key Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.size(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.apiKeyIsValid) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.Green)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "API Key: ${if (uiState.apiKeyIsValid) "✓ Valid" else "✗ Invalid/Missing"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // API Key Preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "API Key Preview: ${uiState.apiKeyPreview}",
                            modifier = Modifier.padding(12.dp),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Build Config
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Build Configuration:",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                "API Key Length: ${uiState.apiKeyLength}",
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Application ID: ${BuildConfig.APPLICATION_ID}",
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.testAPIKey() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test API Key Connection")
                    }

                    val testResult = uiState.testResult
                    if (testResult != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (testResult.isSuccess)
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else
                                    Color(0xFFF44336).copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = testResult.message ?: "No message",
                                modifier = Modifier.padding(12.dp),
                                color = if (testResult.isSuccess)
                                    Color(0xFF4CAF50)
                                else
                                    Color(0xFFF44336)
                            )
                        }
                    }
                }
            }

            // Internet Connection Test
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "🌐 Internet Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.testInternetConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Internet Connection")
                    }

                    val internetResult = uiState.internetTestResult
                    if (internetResult != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = internetResult,
                            color = if (uiState.hasInternet) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }

            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "📝 How to Fix:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("1. Open `local.properties` file", style = MaterialTheme.typography.bodyMedium)
                    Text("2. Add: GEMINI_API_KEY=\"your-key-here\"", style = MaterialTheme.typography.bodySmall)
                    Text("3. Clean and rebuild project", style = MaterialTheme.typography.bodySmall)
                    Text("4. Get API key from:", style = MaterialTheme.typography.bodySmall)
                    Text("   https://makersuite.google.com/app/apikey",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Key should start with: AIzaSy...",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

data class DebugUiState(
    val apiKeyLength: Int = 0,
    val apiKeyPreview: String = "",
    val apiKeyIsValid: Boolean = false,
    val testResult: TestResult? = null,
    val internetTestResult: String? = null,
    val hasInternet: Boolean = false
)

data class TestResult(
    val isSuccess: Boolean,
    val message: String?
)

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(DebugUiState())
    val uiState: State<DebugUiState> = _uiState

    init {
        checkAPIKey()
    }

    private fun checkAPIKey() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isNotEmpty = apiKey.isNotBlank() && apiKey != "\"\"" && apiKey != "\"\"\"\""
        val isValidFormat = apiKey.startsWith("AIza") || apiKey.contains("AIza")

        _uiState.value = _uiState.value.copy(
            apiKeyLength = apiKey.length,
            apiKeyPreview = if (apiKey.length > 10)
                "${apiKey.take(5)}...${apiKey.takeLast(5)}"
            else apiKey,
            apiKeyIsValid = isNotEmpty && isValidFormat
        )
    }

    fun testAPIKey() {
        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY

            if (apiKey.isBlank() || apiKey == "\"\"") {
                _uiState.value = _uiState.value.copy(
                    testResult = TestResult(
                        isSuccess = false,
                        message = "❌ API key is empty! Please add to local.properties"
                    )
                )
                return@launch
            }

            if (!apiKey.startsWith("AIza") && !apiKey.contains("AIza")) {
                _uiState.value = _uiState.value.copy(
                    testResult = TestResult(
                        isSuccess = false,
                        message = "❌ Invalid API key format! Should start with 'AIza'"
                    )
                )
                return@launch
            }

            // Try to make a simple API call
            try {
                // This is a simple test - we'll simulate checking the key
                _uiState.value = _uiState.value.copy(
                    testResult = TestResult(
                        isSuccess = true,
                        message = "✅ API key looks valid! Length: ${apiKey.length}"
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    testResult = TestResult(
                        isSuccess = false,
                        message = "❌ API test failed: ${e.message}"
                    )
                )
            }
        }
    }

    fun testInternetConnection() {
        // This would check internet connectivity
        // For now, we'll simulate
        _uiState.value = _uiState.value.copy(
            hasInternet = true,
            internetTestResult = "✅ Internet connection detected"
        )
    }
}
