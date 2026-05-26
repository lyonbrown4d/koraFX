package dev.korafx.sourceeditor

import dev.korafx.dsl.NodeContainerBuilder

fun sourceEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onTextChange: ((String) -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: SourceEditorBuilder.() -> Unit = {},
): SourceEditor =
    SourceEditor(
        title = title,
        text = text,
        language = language,
        readOnly = readOnly,
        placeholder = placeholder,
        showLineNumbers = showLineNumbers,
        showSearch = showSearch,
        wrapText = wrapText,
        diagnostics = diagnostics,
        onTextChange = onTextChange,
    ).apply(init).apply {
        SourceEditorBuilder(this).content()
    }

fun queryEditor(
    title: String? = "Query.sql",
    text: String = "",
    placeholder: String? = "Write SQL...",
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onRun: ((String) -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: QueryEditorBuilder.() -> Unit = {},
): SourceEditor =
    SourceEditor(
        title = title,
        text = text,
        language = "sql",
        readOnly = false,
        placeholder = placeholder,
        showLineNumbers = showLineNumbers,
        showSearch = showSearch,
        wrapText = wrapText,
        diagnostics = diagnostics,
        onTextChange = null,
    ).apply(init).apply {
        val builder = SourceEditorBuilder(this)
        if (onRun != null) {
            builder.action("Run") {
                onRun(this.editor.textArea.text.orEmpty())
            }
        }
        if (onStop != null) {
            builder.action("Stop") {
                onStop()
            }
        }
        QueryEditorBuilder(this).content()
    }

fun NodeContainerBuilder.sourceEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onTextChange: ((String) -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: SourceEditorBuilder.() -> Unit = {},
): SourceEditor =
    add(
        dev.korafx.sourceeditor.sourceEditor(
            title = title,
            text = text,
            language = language,
            readOnly = readOnly,
            placeholder = placeholder,
            showLineNumbers = showLineNumbers,
            showSearch = showSearch,
            wrapText = wrapText,
            diagnostics = diagnostics,
            onTextChange = onTextChange,
            init = init,
            content = content,
        ),
    )

fun NodeContainerBuilder.queryEditor(
    title: String? = "Query.sql",
    text: String = "",
    placeholder: String? = "Write SQL...",
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    diagnostics: Iterable<SourceDiagnostic> = emptyList(),
    onRun: ((String) -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    init: SourceEditor.() -> Unit = {},
    content: QueryEditorBuilder.() -> Unit = {},
): SourceEditor =
    add(
        dev.korafx.sourceeditor.queryEditor(
            title = title,
            text = text,
            placeholder = placeholder,
            showLineNumbers = showLineNumbers,
            showSearch = showSearch,
            wrapText = wrapText,
            diagnostics = diagnostics,
            onRun = onRun,
            onStop = onStop,
            init = init,
            content = content,
        ),
    )
