package com.example.data.model

import com.example.data.entity.ProductEntity

data class DashboardMetrics(
    val totalSalesRevenue: Double = 0.0,
    val totalEstimatedCost: Double = 0.0,
    val totalNetProfit: Double = 0.0,
    val totalTransactionsCount: Int = 0,
    val totalUnitsSold: Double = 0.0,
    val totalInventoryValueAtCost: Double = 0.0,
    val totalInventoryValueAtRetail: Double = 0.0,
    val totalDistinctProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val topSellingProducts: List<TopProductMetric> = emptyList(),
    val salesByPaymentMethod: Map<String, Double> = emptyMap(),
    val salesByCategory: Map<String, Double> = emptyMap(),
    val dailyChartPoints: List<SalesChartPoint> = emptyList(),
    val lowStockProductsList: List<ProductEntity> = emptyList()
)

data class TopProductMetric(
    val productName: String,
    val unitsSold: Double,
    val revenue: Double
)

data class SalesChartPoint(
    val label: String,      // e.g. "Lun", "10 Ago"
    val timestamp: Long,
    val amount: Double,
    val salesCount: Int
)

enum class ReportPeriod(val label: String) {
    TODAY("Hoy"),
    LAST_7_DAYS("7 Días"),
    THIS_MONTH("Este Mes"),
    ALL_TIME("Histórico")
}
