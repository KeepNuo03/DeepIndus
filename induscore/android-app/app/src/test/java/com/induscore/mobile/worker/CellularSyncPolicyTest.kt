package com.induscore.mobile.worker

import com.induscore.mobile.data.local.entity.UploadQueueEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CellularSyncPolicyTest {

    @Test
    fun `should warn when task count exceeds threshold`() {
        val queue = List(4) { idx ->
            UploadQueueEntity(
                id = idx.toLong() + 1,
                idempotencyKey = "K-$idx",
                localFilePath = "/tmp/$idx.jpg",
                status = "pending"
            )
        }
        val risk = CellularSyncPolicy.evaluate(queue) { 1024L }
        assertTrue(risk.exceedTaskThreshold)
        assertTrue(risk.shouldWarn)
    }

    @Test
    fun `should warn when total bytes exceeds threshold`() {
        val queue = listOf(
            UploadQueueEntity(id = 1, idempotencyKey = "K-1", localFilePath = "/tmp/1.jpg", status = "pending"),
            UploadQueueEntity(id = 2, idempotencyKey = "K-2", localFilePath = "/tmp/2.jpg", status = "failed")
        )
        val risk = CellularSyncPolicy.evaluate(queue) { 12L * 1024L * 1024L }
        assertTrue(risk.exceedBytesThreshold)
        assertTrue(risk.shouldWarn)
    }

    @Test
    fun `should not warn when queue within threshold`() {
        val queue = listOf(
            UploadQueueEntity(id = 1, idempotencyKey = "K-1", localFilePath = "/tmp/1.jpg", status = "pending"),
            UploadQueueEntity(id = 2, idempotencyKey = "K-2", localFilePath = "/tmp/2.jpg", status = "failed")
        )
        val risk = CellularSyncPolicy.evaluate(queue) { 1024L * 1024L }
        assertFalse(risk.exceedTaskThreshold)
        assertFalse(risk.exceedBytesThreshold)
        assertFalse(risk.shouldWarn)
    }
}
