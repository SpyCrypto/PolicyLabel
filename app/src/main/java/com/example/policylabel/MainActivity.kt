package com.example.policylabel

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.policylabel.data.SampleData
import com.example.policylabel.ui.PolicyLabel
import com.example.policylabel.ui.theme.PolicyLabelTheme
import com.solana.mobilewalletadapter.clientlib.*
import kotlinx.coroutines.launch

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
            var policy by remember { mutableStateOf(SampleData.mockPolicy) }

            PolicyLabelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                                    is TransactionResult.Failure -> {
                                        // Handle failure
                                    }
                                    is TransactionResult.NoWalletFound -> {
                                        // Handle no wallet
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
