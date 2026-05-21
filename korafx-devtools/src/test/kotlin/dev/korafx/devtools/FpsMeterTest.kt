package dev.korafx.devtools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FpsMeterTest {
    @Test
    fun `first frame does not produce a snapshot`() {
        val meter = FpsMeter()

        assertNull(meter.recordFrame(1_000_000L))
    }

    @Test
    fun `records current fps and average frame time`() {
        val meter = FpsMeter()

        meter.recordFrame(0L)
        val first = assertNotNull(meter.recordFrame(16_000_000L))
        val second = assertNotNull(meter.recordFrame(36_000_000L))

        assertEquals(62.5, first.currentFps, absoluteTolerance = 0.001)
        assertEquals(16.0, first.averageFrameMillis, absoluteTolerance = 0.001)
        assertEquals(50.0, second.currentFps, absoluteTolerance = 0.001)
        assertEquals(18.0, second.averageFrameMillis, absoluteTolerance = 0.001)
        assertEquals(2, second.sampleCount)
    }

    @Test
    fun `keeps only the rolling sample window`() {
        val meter = FpsMeter(sampleWindow = 2)

        meter.recordFrame(0L)
        meter.recordFrame(10_000_000L)
        meter.recordFrame(30_000_000L)
        val snapshot = assertNotNull(meter.recordFrame(60_000_000L))

        assertEquals(2, snapshot.sampleCount)
        assertEquals(25.0, snapshot.averageFrameMillis, absoluteTolerance = 0.001)
    }

    @Test
    fun `reset clears accumulated samples`() {
        val meter = FpsMeter()

        meter.recordFrame(0L)
        meter.recordFrame(16_000_000L)
        meter.reset()

        assertNull(meter.recordFrame(32_000_000L))
    }
}
