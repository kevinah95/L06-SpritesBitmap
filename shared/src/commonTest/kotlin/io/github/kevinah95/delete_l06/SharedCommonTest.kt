package io.github.kevinah95.delete_l06

import korlibs.image.bitmap.sliceWithSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SharedCommonTest {

    @Test
    fun testFallbackSpritesheetGeneration() {
        val sheet = createFallbackSpritesheet()
        assertEquals(160, sheet.width)
        assertEquals(80, sheet.height)
    }

    @Test
    fun testBitmapSegmentationSlicing() {
        val sheet = createFallbackSpritesheet()
        val slice = sheet.sliceWithSize(16, 18, 16, 15)
        assertNotNull(slice)
        assertEquals(16, slice.width)
        assertEquals(15, slice.height)
    }

    @Test
    fun testLoadSpritesheet() = kotlinx.coroutines.runBlocking {
        val bmp = loadSpritesheet()
        println("LOADED_BITMAP_SIZE: ${bmp.width}x${bmp.height}")
    }
}