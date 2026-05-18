package dev.korafx.examples.dsl

import dev.korafx.dsl.borderPane
import dev.korafx.dsl.cssStyle
import dev.korafx.dsl.form
import dev.korafx.dsl.hbox
import dev.korafx.dsl.onAction
import dev.korafx.dsl.scrollPane
import dev.korafx.dsl.styleClasses
import dev.korafx.dsl.vbox
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(DslBasicApp::class.java, *args)
}

class DslBasicApp : Application() {
    override fun start(stage: Stage) {
        var statusLabel: Label? = null

        val root = borderPane(
            init = {
                padding = Insets(24.0)
                cssStyle {
                    fontSize(14.0)
                }
            },
        ) {
            top {
                hbox(spacing = 12.0) {
                    label("KoraFX DSL") {
                        styleClasses("headline")
                        cssStyle {
                            fontSize(22.0)
                            fontWeight("bold")
                        }
                    }
                    spacer()
                    button("Save") {
                        onAction {
                            statusLabel?.text = "Saved from DSL action."
                        }
                    }
                }
            }
            center {
                scrollPane(
                    init = {
                        isFitToWidth = true
                        vboxConstraints()
                    },
                ) {
                    content {
                        form(
                            init = {
                                padding = Insets(24.0, 0.0, 0.0, 0.0)
                            },
                        ) {
                            textField(
                                label = "Project",
                                helper = "Plain JavaFX controls, built with Kotlin DSL.",
                                text = "KoraFX",
                            ) {
                                maxWidth = Double.MAX_VALUE
                            }
                            textArea(
                                label = "Notes",
                                text = "Use the DSL where it removes boilerplate; keep JavaFX APIs close.",
                            ) {
                                prefRowCount = 5
                                maxWidth = Double.MAX_VALUE
                            }
                            checkBox(
                                label = "Scope",
                                text = "Keep MVVM independent from DI",
                            ) {
                                isSelected = true
                            }
                            submitBar {
                                secondaryButton("Reset") {
                                    onAction {
                                        statusLabel?.text = "Reset requested."
                                    }
                                }
                                primaryButton("Apply") {
                                    onAction {
                                        statusLabel?.text = "Applied."
                                    }
                                }
                            }
                        }
                    }
                }
            }
            bottom {
                vbox(spacing = 8.0, init = { padding = Insets(16.0, 0.0, 0.0, 0.0) }) {
                    statusLabel = label("Ready.") {
                        cssStyle {
                            textFill("#5f6b7a")
                        }
                    }
                }
            }
        }

        stage.title = "KoraFX DSL Basic"
        stage.scene = Scene(root, 720.0, 480.0)
        stage.show()
    }
}

private fun javafx.scene.Node.vboxConstraints() {
    javafx.scene.layout.VBox.setVgrow(this, Priority.ALWAYS)
}
