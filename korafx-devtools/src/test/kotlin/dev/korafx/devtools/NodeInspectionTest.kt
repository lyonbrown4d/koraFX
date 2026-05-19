package dev.korafx.devtools

import javafx.css.PseudoClass
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
