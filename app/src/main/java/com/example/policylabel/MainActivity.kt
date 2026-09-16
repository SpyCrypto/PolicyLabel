package com.example.policylabel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.policylabel.data.SampleData
import com.example.policylabel.ui.PolicyLabel
import com.example.policylabel.ui.theme.PolicyLabelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolicyLabelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PolicyLabel(
                        policyData = SampleData.mockPolicy,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    PolicyLabelTheme {
        PolicyLabel(policyData = SampleData.mockPolicy)
    }
}