package dev.korafx.framework.theme

internal fun StylesheetBuilder.componentStyles(context: ThemeCssContext) {
    componentLayoutStyles(context)
    componentResourceExplorerStyles(context)
    componentDataGridStyles(context)
    componentVirtualizedStyles(context)
    componentInspectorStyles(context)
    componentTimelineStyles(context)
    componentCommandPaletteStyles(context)
    componentBadgeAndAlertStyles(context)
    componentCodeEditorStyles(context)
    componentSourceEditorStyles(context)
    componentHeaderStyles(context)
    componentFormAndStatusStyles(context)
    componentShellAndFeedbackStyles(context)
    componentMarkdownStyles(context)
}
