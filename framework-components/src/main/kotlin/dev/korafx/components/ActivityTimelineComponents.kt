package dev.korafx.components

import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.onAction
import dev.korafx.dsl.styleClass
import dev.korafx.dsl.styleClasses
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

class ActivityTimeline<T> internal constructor(
    events: Iterable<T>,
    emptyText: String,
) : VBox(0.0) {
    val content: VBox = VBox(0.0)
    val emptyLabel: Label = Label(emptyText)

    private var events: List<T> = events.toList()
    private var titleOf: (T) -> String = { it.toString() }
    private var messageOf: (T) -> String? = { null }
    private var timeOf: (T) -> String? = { null }
    private var toneOf: (T) -> ComponentTone = { ComponentTone.NEUTRAL }
    private var groupOf: (T) -> String? = { null }
    private var actionText: String? = null
    private var actionHandler: ((T) -> Unit)? = null

    init {
        styleClass("activity-timeline")
        maxWidth = Double.MAX_VALUE

        content.apply {
            styleClass("activity-timeline-content")
            maxWidth = Double.MAX_VALUE
        }
        emptyLabel.apply {
            styleClass("activity-timeline-empty")
            isWrapText = true
        }
        children += content
        rebuild()
    }

    fun setEvents(events: Iterable<T>) {
        this.events = events.toList()
        rebuild()
    }

    fun setTitleRenderer(titleOf: (T) -> String) {
        this.titleOf = titleOf
        rebuild()
    }

    fun setMessageRenderer(messageOf: (T) -> String?) {
        this.messageOf = messageOf
        rebuild()
    }

    fun setTimeRenderer(timeOf: (T) -> String?) {
        this.timeOf = timeOf
        rebuild()
    }

    fun setToneRenderer(toneOf: (T) -> ComponentTone) {
        this.toneOf = toneOf
        rebuild()
    }

    fun setGroupRenderer(groupOf: (T) -> String?) {
        this.groupOf = groupOf
        rebuild()
    }

    fun setEmptyText(text: String) {
        emptyLabel.text = text
    }

    fun setAction(
        text: String,
        handler: (T) -> Unit,
    ) {
        actionText = text
        actionHandler = handler
        rebuild()
    }

    fun clearAction() {
        actionText = null
        actionHandler = null
        rebuild()
    }

    private fun rebuild() {
        content.children.clear()

        if (events.isEmpty()) {
            content.children += emptyLabel
            return
        }

        var currentGroup: String? = null
        events.forEachIndexed { index, event ->
            val group = groupOf(event)
            if (group != null && group != currentGroup) {
                currentGroup = group
                content.children += groupLabel(group)
            }
            content.children += row(event, index == events.lastIndex)
        }
    }

    private fun groupLabel(text: String): Label =
        Label(text).apply {
            styleClass("activity-timeline-group")
            isWrapText = true
        }

    private fun row(
        event: T,
        isLast: Boolean,
    ): HBox {
        val tone = toneOf(event)
        return HBox(12.0).apply {
            styleClasses("activity-timeline-row", tone.styleClass)
            alignment = Pos.TOP_LEFT
            maxWidth = Double.MAX_VALUE
            children += markerColumn(isLast)
            children += eventContent(event).apply {
                HBox.setHgrow(this, Priority.ALWAYS)
            }
        }
    }

    private fun markerColumn(isLast: Boolean): VBox =
        VBox(0.0).apply {
            styleClass("activity-timeline-marker-column")
            alignment = Pos.TOP_CENTER
            children += Region().apply {
                styleClass("activity-timeline-marker")
            }
            children += Region().apply {
                styleClass("activity-timeline-connector")
                isVisible = !isLast
                isManaged = !isLast
                VBox.setVgrow(this, Priority.ALWAYS)
            }
        }

    private fun eventContent(event: T): VBox =
        VBox(6.0).apply {
            styleClass("activity-timeline-event")
            maxWidth = Double.MAX_VALUE

            val meta = HBox(8.0).apply {
                styleClass("activity-timeline-meta")
                alignment = Pos.CENTER_LEFT
            }
            val time = timeOf(event)
            if (!time.isNullOrBlank()) {
                meta.children += Label(time).apply {
                    styleClass("activity-timeline-time")
                }
            }
            val text = actionText
            val handler = actionHandler
            if (text != null && handler != null) {
                meta.children += Region().apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                }
                meta.children += Button(text).apply {
                    styleClass("activity-timeline-action")
                    onAction {
                        handler(event)
                    }
                }
            }
            if (meta.children.isNotEmpty()) {
                children += meta
            }

            children += Label(titleOf(event)).apply {
                styleClass("activity-timeline-title")
                isWrapText = true
            }

            val message = messageOf(event)
            if (!message.isNullOrBlank()) {
                children += Label(message).apply {
                    styleClass("activity-timeline-message")
                    isWrapText = true
                }
            }
        }
}

class ActivityTimelineBuilder<T> internal constructor(
    private val timeline: ActivityTimeline<T>,
) {
    fun events(events: Iterable<T>) {
        timeline.setEvents(events)
    }

    fun titleOf(titleOf: (T) -> String) {
        timeline.setTitleRenderer(titleOf)
    }

    fun messageOf(messageOf: (T) -> String?) {
        timeline.setMessageRenderer(messageOf)
    }

    fun timeOf(timeOf: (T) -> String?) {
        timeline.setTimeRenderer(timeOf)
    }

    fun toneOf(toneOf: (T) -> ComponentTone) {
        timeline.setToneRenderer(toneOf)
    }

    fun groupBy(groupOf: (T) -> String?) {
        timeline.setGroupRenderer(groupOf)
    }

    fun emptyState(text: String) {
        timeline.setEmptyText(text)
    }

    fun action(
        text: String = "Open",
        handler: (T) -> Unit,
    ) {
        timeline.setAction(text, handler)
    }
}

fun <T> activityTimeline(
    events: Iterable<T> = emptyList(),
    emptyText: String = "No activity",
    init: ActivityTimeline<T>.() -> Unit = {},
    content: ActivityTimelineBuilder<T>.() -> Unit = {},
): ActivityTimeline<T> =
    ActivityTimeline(
        events = events,
        emptyText = emptyText,
    ).apply(init).apply {
        ActivityTimelineBuilder(this).content()
    }

fun <T> NodeContainerBuilder.activityTimeline(
    events: Iterable<T> = emptyList(),
    emptyText: String = "No activity",
    init: ActivityTimeline<T>.() -> Unit = {},
    content: ActivityTimelineBuilder<T>.() -> Unit = {},
): ActivityTimeline<T> =
    add(
        dev.korafx.components.activityTimeline(
            events = events,
            emptyText = emptyText,
            init = init,
            content = content,
        ),
    )
