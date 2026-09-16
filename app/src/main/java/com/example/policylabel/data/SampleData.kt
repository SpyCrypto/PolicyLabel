package com.example.policylabel.data

object SampleData {
    val mockPolicy = PolicyData(
        appName = "SocialConnect",
        developer = "Connect Labs Inc.",
        dataCollected = listOf(
            DataPoint("Identity", "Name, Email, Phone", "Account creation"),
            DataPoint("Location", "Precise Location", "Personalized ads"),
            DataPoint("Financial", "Payment info", "Subscriptions")
        ),
        dataShared = listOf(
            DataPoint("Analytics", "Usage data", "Performance tracking"),
            DataPoint("Marketing", "Email address", "Promotion partners")
        ),
        retentionPeriod = "2 years after inactivity",
        securityPractices = listOf(
            "Data encryption in transit",
            "Biometric authentication support",
            "Regular security audits"
        )
    )
}
