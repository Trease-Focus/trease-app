package neth.iecal.trease.ui.components.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import neth.iecal.trease.models.FocusStats

@Composable
fun EfficiencyDonutChart(stats: List<FocusStats>) {
    val total = stats.size
    val failed = stats.count { it.isFailed }
    val success = total - failed

    val successColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.error
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHigh

    if (total == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Start planting to see health", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2

                drawCircle(color = emptyColor, radius = radius, style = Stroke(width = strokeWidth))

                val successSweep = (success.toFloat() / total) * 360f
                drawArc(
                    color = successColor,
                    startAngle = -90f,
                    sweepAngle = successSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${((success.toFloat()/total)*100).toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.width(42.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricLegendItem(color = successColor, text = "Thriving ($success)")
            MetricLegendItem(color = failColor, text = "Withered ($failed)")
        }
    }
}

@Composable
private fun MetricLegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}