package dev.korafx.devtools

import javafx.css.PseudoClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeInspectionTest {
    @Test
    fun `pseudo class names ignore null entries`() {
        val names = safePseudoClassNames(
            listOf(
                PseudoClass.getPseudoClass("hover"),
                null,
                PseudoClass.getPseudoClass("focused"),
            ),
        )

        assertEquals(listOf("focused", "hover"), names)
    }

    @Test
    fun `pseudo class names ignore broken states`() {
        val names = safePseudoClassNames(
            listOf(
                PseudoClass.getPseudoClass("disabled"),
                object : PseudoClass() {
                    override fun getPseudoClassName(): String = throw IllegalStateException("broken")
                },
            ),
        )

        assertEquals(listOf("disabled"), names)
    }

    @Test
    fun `node inspection handles pseudo class description gracefully`() {
        assertTrue(
            safePseudoClassNames(listOf(PseudoClass.getPseudoClass("selected"))).contains("selected"),
        )
    }
}
