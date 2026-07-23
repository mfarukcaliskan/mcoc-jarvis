package com.example.myapplication.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class RemoteDataUpdaterTest {

    @Test
    fun `sha256 matches MessageDigest reference for known input`() {
        val input = "mcoc-jarvis-test".toByteArray(Charsets.UTF_8)
        val expected = MessageDigest.getInstance("SHA-256").digest(input)
            .joinToString("") { "%02x".format(it) }

        assertEquals(expected, RemoteDataUpdater.sha256(input))
    }

    @Test
    fun `sha256 is deterministic and 64 hex chars`() {
        val bytes = "champions_db.json".toByteArray(Charsets.UTF_8)
        val first = RemoteDataUpdater.sha256(bytes)
        val second = RemoteDataUpdater.sha256(bytes)

        assertEquals(first, second)
        assertEquals(64, first.length)
        assertTrue(first.all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun `sha256 differs for different content (tamper detection)`() {
        val original = RemoteDataUpdater.sha256("original content".toByteArray())
        val tampered = RemoteDataUpdater.sha256("tampered content".toByteArray())

        assertTrue(original != tampered)
    }

    @Test
    fun `manifest json parses dataVersion and file hash map correctly`() {
        val manifestJson = """
            {
              "dataVersion": 7,
              "generatedAt": "2026-07-23T00:00:00Z",
              "files": {
                "champions_db.json": "abc123",
                "details/hulk.json": "def456"
              }
            }
        """.trimIndent()

        val manifest = JSONObject(manifestJson)
        assertEquals(7, manifest.getInt("dataVersion"))

        val files = manifest.getJSONObject("files")
        assertEquals("abc123", files.getString("champions_db.json"))
        assertEquals("def456", files.getString("details/hulk.json"))
        assertEquals(2, files.length())
    }
}
