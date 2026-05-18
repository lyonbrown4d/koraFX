package dev.korafx.components

import dev.korafx.dsl.hbox
import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SemanticComponentsTest {
    @Test
    fun `badge renders semantic tone classes`() {
        FxTestSupport.runOnFxThread {
            val badge = badge("Stable", ComponentTone.SUCCESS)

            assertEquals("Stable", badge.text)
            assertTrue("badge" in badge.styleClass)
            assertTrue("tone-success" in badge.styleClass)
        }
    }

    @Test
    fun `chip renders selected state and tone`() {
        FxTestSupport.runOnFxThread {
            val chip = chip(
                text = "Theme",
                tone = ComponentTone.PRIMARY,
                selected = true,
            )

            assertEquals("Theme", chip.text)
            assertTrue("chip" in chip.styleClass)
            assertTrue("chip-selected" in chip.styleClass)
            assertTrue("tone-primary" in chip.styleClass)
        }
    }

    @Test
    fun `metric card renders label value helper and extra content`() {
        FxTestSupport.runOnFxThread {
            val metric = metricCard(
                label = "Coverage",
                value = "18",
                helper = "Styled controls",
                tone = ComponentTone.INFO,
            ) {
                badge("Live", ComponentTone.SUCCESS)
            }

            val labels = metric.children.filterIsInstance<Label>()

            assertTrue("card" in metric.styleClass)
            assertTrue("metric-card" in metric.styleClass)
            assertTrue("tone-info" in metric.styleClass)
            assertEquals("Coverage", labels[0].text)
            assertTrue("metric-label" in labels[0].styleClass)
            assertEquals("18", labels[1].text)
            assertTrue("metric-value" in labels[1].styleClass)
            assertEquals("Styled controls", labels[2].text)
            assertTrue("metric-helper" in labels[2].styleClass)
            assertTrue("badge" in labels[3].styleClass)
        }
    }

    @Test
    fun `alert banner renders message action and tone`() {
        FxTestSupport.runOnFxThread {
            var handled = false
            val alert = alertBanner(
                title = "Publish blocked",
                message = "Fix validation errors before releasing.",
                tone = ComponentTone.WARNING,
                actionText = "Review",
                onAction = { handled = true },
            )

            val labels = alert.children.filterIsInstance<Label>()
            val action = assertIs<Button>(alert.children.last())

            assertTrue("card" in alert.styleClass)
            assertTrue("alert-banner" in alert.styleClass)
            assertTrue("tone-warning" in alert.styleClass)
            assertEquals("Publish blocked", labels[0].text)
            assertTrue("alert-title" in labels[0].styleClass)
            assertEquals("Fix validation errors before releasing.", labels[1].text)
            assertTrue("alert-message" in labels[1].styleClass)
            assertEquals("Review", action.text)
            assertTrue("alert-action" in action.styleClass)

            action.fire()

            assertTrue(handled)
        }
    }

    @Test
    fun `semantic components can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                hbox {
                    badge("New", ComponentTone.INFO)
                    chip("Selected", selected = true)
                }
                metricCard("Modules", "6")
                alertBanner("Ready")
            }

            val row = root.children[0] as javafx.scene.layout.HBox
            val metric = assertIs<VBox>(root.children[1])
            val alert = assertIs<VBox>(root.children[2])

            assertIs<Label>(row.children[0])
            assertIs<Button>(row.children[1])
            assertTrue("metric-card" in metric.styleClass)
            assertTrue("alert-banner" in alert.styleClass)
        }
    }
}
