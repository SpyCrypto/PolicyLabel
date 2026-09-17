package com.example.policylabel

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.policylabel.data.SampleData
import com.example.policylabel.ui.PolicyLabel
import com.example.policylabel.ui.theme.PolicyLabelTheme
import com.solana.mobilewalletadapter.clientlib.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppState { LANDING, ANALYZING, LABEL, HOME }

class MainActivity : ComponentActivity() {
    private lateinit var activityResultSender: ActivityResultSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        activityResultSender = ActivityResultSender(this)

        val identity = ConnectionIdentity(
            identityUri = Uri.parse("https://policylabel.io"),
            iconUri = Uri.parse("favicon.ico"),
            identityName = "PolicyLabel"
        )

        val walletAdapter = MobileWalletAdapter(identity).apply {
            blockchain = Solana.Mainnet
        }

        setContent {
            var appState by remember { mutableStateOf(AppState.LANDING) }
            var policy by remember { mutableStateOf(SampleData.mockPolicy) }
            var urlInput by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()

            PolicyLabelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (appState) {
                        AppState.LANDING -> {
                            LandingScreen(
                                url = urlInput,
                                onUrlChange = { urlInput = it },
                                onGenerate = {
                                    val domain = urlInput.split("/")
                                        .firstOrNull { it.contains(".") }
                                        ?.replace("https://", "")
                                        ?.replace("http://", "")
                                        ?.replace("www.", "")
                                        ?.split("?")
                                        ?.first()
                                        ?: "Unknown App"
                                    
                                    Log.d("PolicyLabel", "Generating label for domain: $domain")
                                    appState = AppState.ANALYZING
                                    scope.launch {
                                        delay(2500) // Realistic "thinking" time
                                        
                                        // Update the mock data to look like it was "scraped"
                                        policy = policy.copy(
                                            appName = domain,
                                            developer = "$domain Inc.",
                                            signature = null, // Reset signature for new analysis
                                            signerAddress = null
                                        )
                                        appState = AppState.LABEL
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppState.ANALYZING -> {
                            AnalyzingScreen(modifier = Modifier.padding(innerPadding))
                        }
                        AppState.LABEL -> {
                            PolicyLabel(
                                policyData = policy,
                                modifier = Modifier.padding(innerPadding),
                                onSignRequest = {
                                    lifecycleScope.launch {
                                        val result = walletAdapter.transact(activityResultSender) { authResult ->
                                            val message = "I consent to the policy facts disclosed by ${policy.appName}."
                                            val signingResult = signMessagesDetached(
                                                arrayOf(message.toByteArray()),
                                                arrayOf(authResult.accounts[0].publicKey)
                                            )
                                            
                                            val signature = Base64.encodeToString(signingResult.messages[0].signatures[0], Base64.NO_WRAP)
                                            val address = Base64.encodeToString(authResult.accounts[0].publicKey, Base64.NO_WRAP)
                                            
                                            address to signature
                                        }

                                        when (result) {
                                            is TransactionResult.Success -> {
                                                val (address, signature) = result.payload
                                                policy = policy.copy(
                                                    signature = signature,
                                                    signerAddress = address
                                                )
                                            }
                                            is TransactionResult.Failure -> {}
                                            is TransactionResult.NoWalletFound -> {}
                                        }
                                    }
                                },
                                onProceed = {
                                    appState = AppState.HOME
                                }
                            )
                        }
                        AppState.HOME -> {
                            HomeScreen(
                                signer = policy.signerAddress ?: "User",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyzingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color.Black)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Analyzing Policy...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Identifying data collection practices...",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun LandingScreen(
    url: String,
    onUrlChange: (String) -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "PolicyLabel Logo",
            modifier = Modifier.size(120.dp).padding(bottom = 16.dp)
        )
        Text(
            text = "PolicyLabel",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Generate a nutrition label for any digital policy.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("Enter Policy URL") },
            placeholder = { Text("https://example.com/privacy") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.extraSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LogoButton(
            text = "GENERATE LABEL",
            onClick = onGenerate,
            enabled = url.isNotBlank()
        )
    }
}

@Composable
fun LogoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        color = if (enabled) Color.Black else Color.Gray,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun HomeScreen(signer: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LogoButton(
            text = "POLICYLABEL: THE NUTRITION LABEL",
            onClick = {},
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "FOR DIGITAL POLICY CONSENT.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Consent Verified for Wallet:", fontSize = 16.sp)
        Text(signer, fontSize = 10.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(48.dp))
        Text("This is the main app experience.", fontSize = 14.sp, color = Color.Gray)
    }
}
