package dev.korafx.components

import dev.korafx.dsl.panel
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ActivityTimelineComponentsTest {
    private data class Event(
        val title: String,
        val message: String,
        val time: String,
        val group: String,
        val tone: ComponentTone,
    )

    @Test
    fun `activity timeline renders grouped rows with tone classes`() {
        FxTestSupport.runOnFxThread {
            val events = listOf(
                Event("Commit a1b2", "Initial DSL commit", "09:12", "Today", ComponentTone.SUCCESS),
                Event("Query finished", "24 rows returned", "09:20", "Today", ComponentTone.INFO),
                Event("Migration failed", "Missing index", "Yesterday", "Yesterday", ComponentTone.DANGER),
            )
            val timeline = activityTimeline(events) {
                titleOf { it.title }
                messageOf { it.message }
                timeOf { it.time }
                groupBy { it.group }
                toneOf { it.tone }
            }

            assertTrue("activity-timeline" in timeline.styleClass)
            assertEquals(5, timeline.content.children.size)
            assertEquals("Today", assertIs<Label>(timeline.content.children[0]).text)
            assertEquals("Yesterday", assertIs<Label>(timeline.content.children[3]).text)

            val firstRow = assertIs<HBox>(timeline.content.children[1])
            assertTrue("activity-timeline-row" in firstRow.styleClass)
            assertTrue("tone-success" in firstRow.styleClass)
            val firstEvent = assertIs<VBox>(firstRow.children[1])
            assertEquals("09:12", assertIs<Label>(assertIs<HBox>(firstEvent.children[0]).children[0]).text)
            assertEquals("Commit a1b2", assertIs<Label>(firstEvent.children[1]).text)
            assertEquals("Initial DSL commit", assertIs<Label>(firstEvent.children[2]).text)
        }
    }

    @Test
    fun `activity timeline supports event actions`() {
        FxTestSupport.runOnFxThread {
            val events = listOf(
                Event("Run query", "select 1", "10:00", "SQL", ComponentTone.INFO),
            )
            var opened = ""
            val timeline = activityTimeline(events) {
                titleOf { it.title }
                messageOf { it.message }
                timeOf { it.time }
                action("Open") { opened = it.title }
            }
            val row = assertIs<HBox>(timeline.content.children.single())
            val event = assertIs<VBox>(row.children[1])
            val meta = assertIs<HBox>(event.children.first())
            val button = assertIs<Button>(meta.children.last())

            button.fire()

            assertEquals("Run query", opened)
            assertTrue("activity-timeline-action" in button.styleClass)
        }
    }

    @Test
    fun `activity timeline shows empty state`() {
        FxTestSupport.runOnFxThread {
            val timeline = activityTimeline<Event>(emptyText = "No execution history") {
                emptyState("No activity yet")
            }

            assertEquals(timeline.emptyLabel, timeline.content.children.single())
            assertEquals("No activity yet", timeline.emptyLabel.text)
            assertTrue("activity-timeline-empty" in timeline.emptyLabel.styleClass)
        }
    }

    @Test
    fun `activity timeline can be added from node container builder`() {
        FxTestSupport.runOnFxThread {
            val root = panel {
                activityTimeline(
                    listOf(Event("Commit", "main", "11:00", "Git", ComponentTone.SUCCESS)),
                ) {
                    titleOf { it.title }
                    groupBy { it.group }
                }
            }
            val timeline = assertIs<ActivityTimeline<Event>>(root.children.single())

            assertEquals("Git", assertIs<Label>(timeline.content.children.first()).text)
            assertTrue(timeline.content.children.any { it is HBox })
        }
    }
}
