package dev.korafx.sample.ui.pages

import dev.korafx.components.ComponentTone
import dev.korafx.components.badge
import dev.korafx.components.section
import dev.korafx.dsl.NodeContainerBuilder
import dev.korafx.dsl.hbox
import dev.korafx.virtuallist.VirtualSelectionMode
import dev.korafx.virtuallist.virtualList
import dev.korafx.virtuallist.virtualTable
import dev.korafx.virtuallist.virtualTerminal
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

fun NodeContainerBuilder.virtualListPage() {
    val events = (1..250).map { index ->
        "Build event #$index"
    }
    data class ProcessRow(
        val pid: Int,
        val name: String,
        val cpu: String,
        val memory: String,
    )
    val processes = (1..1_000).map { index ->
        ProcessRow(
            pid = 3_000 + index,
            name = "korafx-worker-$index",
            cpu = "${(index * 7) % 94}%",
            memory = "${128 + (index * 19) % 768} MB",
        )
    }

    section(
        title = "Virtual List",
        description = "A fixed-height demo viewport that loads more rows as you scroll.",
        init = {
            maxWidth = Double.MAX_VALUE
        },
    ) {
        virtualList(
            dataLoader = { offset, limit ->
                events.drop(offset.toInt()).take(limit)
            },
            totalCountEstimate = { events.size },
            pageSize = 40,
            rowHeight = 56.0,
            selectionMode = VirtualSelectionMode.SINGLE,
            init = {
                prefHeight = 560.0
                minHeight = 520.0
                maxHeight = 720.0
                maxWidth = Double.MAX_VALUE
                listView.prefHeight = 560.0
                listView.minHeight = 520.0
                VBox.setVgrow(listView, Priority.ALWAYS)
            },
        ) {
            item {
                node(
                    hbox(
                        spacing = 10.0,
                        init = {
                            alignment = Pos.CENTER_LEFT
                            minHeight = 52.0
                            prefHeight = 52.0
                            maxWidth = Double.MAX_VALUE
                        },
                    ) {
                        badge("#${item.substringAfter('#')}", ComponentTone.INFO)
                        label(item) {
                            maxWidth = Double.MAX_VALUE
                        }
                    },
                )
            }
        }
    }

    section(
        title = "Virtual Table",
        description = "Paged table surface for large process lists, query results or repository indexes.",
        init = {
            maxWidth = Double.MAX_VALUE
        },
    ) {
        virtualTable(
            dataLoader = { offset, limit ->
                processes.drop(offset.toInt()).take(limit)
            },
            totalCountEstimate = { processes.size },
            pageSize = 80,
            selectionMode = VirtualSelectionMode.SINGLE,
            init = {
                prefHeight = 360.0
                minHeight = 320.0
                maxWidth = Double.MAX_VALUE
                tableView.prefHeight = 360.0
            },
        ) {
            constrainedResize()
            textColumn("PID", valueOf = { it.pid })
            textColumn("Process", valueOf = { it.name })
            textColumn("CPU", valueOf = { it.cpu })
            textColumn("Memory", valueOf = { it.memory })
        }
    }

    section(
        title = "Virtual Terminal",
        description = "Append-only virtualized terminal/log viewport with bounded history and auto-scroll.",
        init = {
            maxWidth = Double.MAX_VALUE
        },
    ) {
        virtualTerminal(
            maxLines = 2_000,
            autoScroll = true,
            init = {
                prefHeight = 300.0
                minHeight = 260.0
                maxWidth = Double.MAX_VALUE
                listView.prefHeight = 300.0
            },
        ) {
            lineRenderer { line ->
                Label(line.text).apply {
                    styleClass += "virtual-terminal-line-label"
                    styleClass += line.styleClasses
                    maxWidth = Double.MAX_VALUE
                }
            }
            line("[00:00:01] booting KoraFX workbench", "terminal-info")
            line("[00:00:02] connecting to repository index", "terminal-muted")
            (3..80).forEach { index ->
                line("[00:00:${index.toString().padStart(2, '0')}] streamed log row $index")
            }
            line("[00:01:21] ready", "terminal-success")
        }
    }
}
