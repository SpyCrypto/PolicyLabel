package com.example.policylabel.data

data class PolicyData(
    val appName: String,
    val developer: String,
    val dataCollected: List<DataPoint>,
    val dataShared: List<DataPoint>,
    val retentionPeriod: String,
    val securityPractices: List<String>,
    val signature: String? = null,
    val signerAddress: String? = null
)

data class DataPoint(
    val category: String,
    val type: String,
    val purpose: String
)
