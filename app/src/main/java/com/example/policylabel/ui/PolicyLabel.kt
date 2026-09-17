package com.example.policylabel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.policylabel.data.PolicyData
import com.example.policylabel.data.SampleData
import com.example.policylabel.ui.theme.PolicyLabelTheme

@Composable
fun PolicyLabel(
    policyData: PolicyData,
    modifier: Modifier = Modifier,
    onSignRequest: () -> Unit = {},
    onProceed: () -> Unit = {}
) {
    CompositionLocalProvider(LocalContentColor provides Color.Black) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .border(2.dp, Color.Black)
                .background(Color.White)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
        Text(
            text = buildAnnotatedString {
                append("Digital ")
                withStyle(style = SpanStyle(color = Color(0xFF0000FE))) {
                    append("Policy Facts")
                }
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp
        )
        Text(text = "App: ${policyData.appName}", fontWeight = FontWeight.Bold)
        Text(text = "Developer: ${policyData.developer}")

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 8.dp, color = Color.Black)

        SectionHeader("Data Collected", "Amount per App")
        policyData.dataCollected.forEach {
            DataRow(it.category, it.type)
        }

        HorizontalDivider(thickness = 4.dp, color = Color.Black)

        SectionHeader("Data Shared", "With Third Parties")
        policyData.dataShared.forEach {
            DataRow(it.category, it.type)
        }

        HorizontalDivider(thickness = 4.dp, color = Color.Black)

        SectionHeader("Retention", policyData.retentionPeriod)

        HorizontalDivider(thickness = 8.dp, color = Color.Black)

        Text(
            text = "Security Practices:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        policyData.securityPractices.forEach {
            Text(text = "• $it", fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "* Values are based on developer disclosures.",
            fontSize = 10.sp,
            lineHeight = 12.sp
        )

        HorizontalDivider(thickness = 8.dp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        if (policyData.signature != null) {
            SignatureBox(policyData.signerAddress ?: "Unknown", policyData.signature)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text("PROCEED TO APP", fontWeight = FontWeight.Black)
            }
        } else {
            Button(
                onClick = onSignRequest,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text("SIGN WITH SEEKER", fontWeight = FontWeight.Black)
            }
        }
    }
}
}

@Composable
fun SignatureBox(address: String, signature: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
            .padding(8.dp)
    ) {
        Text("VERIFIED CONSENT", fontWeight = FontWeight.Black, fontSize = 14.sp)
        Text(
            "Signer: $address",
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "Proof: $signature",
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionHeader(title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(text = detail, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = Color.Black)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontWeight = FontWeight.Bold)
            Text(text = value)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PolicyLabelPreview() {
    PolicyLabelTheme {
        PolicyLabel(policyData = SampleData.mockPolicy)
    }
}
