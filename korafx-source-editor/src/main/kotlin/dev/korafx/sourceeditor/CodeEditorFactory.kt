package dev.korafx.sourceeditor

import dev.korafx.dsl.NodeContainerBuilder

fun codeEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showToolbar: Boolean = true,
    showStatus: Boolean = true,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    onTextChange: ((String) -> Unit)? = null,
    init: CodeEditor.() -> Unit = {},
): CodeEditor =
    CodeEditor(
        title = title,
        text = text,
        language = language,
        readOnly = readOnly,
        placeholder = placeholder,
        showToolbar = showToolbar,
        showStatus = showStatus,
        showLineNumbers = showLineNumbers,
        showSearch = showSearch,
        wrapText = wrapText,
        onTextChange = onTextChange,
    ).apply(init)

fun NodeContainerBuilder.codeEditor(
    title: String? = null,
    text: String = "",
    language: String? = null,
    readOnly: Boolean = false,
    placeholder: String? = null,
    showToolbar: Boolean = true,
    showStatus: Boolean = true,
    showLineNumbers: Boolean = true,
    showSearch: Boolean = false,
    wrapText: Boolean = false,
    onTextChange: ((String) -> Unit)? = null,
    init: CodeEditor.() -> Unit = {},
): CodeEditor =
    add(
        dev.korafx.sourceeditor.codeEditor(
            title = title,
            text = text,
            language = language,
            readOnly = readOnly,
            placeholder = placeholder,
            showToolbar = showToolbar,
            showStatus = showStatus,
            showLineNumbers = showLineNumbers,
            showSearch = showSearch,
            wrapText = wrapText,
            onTextChange = onTextChange,
            init = init,
        ),
    )
