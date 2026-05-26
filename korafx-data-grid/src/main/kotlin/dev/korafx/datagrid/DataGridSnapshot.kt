package dev.korafx.datagrid

data class DataGridSelectionSummary<T>(
    val selectedItems: List<T>,
    val visibleRowCount: Int,
    val totalRowCount: Int,
    val loading: Boolean,
) {
    val selectedCount: Int = selectedItems.size
}

data class DataGridDataSnapshot<T>(
    val sourceRows: List<T>,
    val headers: List<String>,
    val rows: List<List<String>>,
) {
    internal var defaultSeparator: String = "\t"
    internal var defaultIncludeHeaders: Boolean = true

    fun toDelimitedText(
        separator: String = defaultSeparator,
        includeHeaders: Boolean = defaultIncludeHeaders,
    ): String {
        val lines = mutableListOf<List<String>>()
        if (includeHeaders) {
            lines += headers
        }
        lines += rows
        return lines.joinToString("\n") { cells ->
            cells.joinToString(separator) { cell ->
                cell.sanitizeDelimitedCell(separator)
            }
        }
    }

    private fun String.sanitizeDelimitedCell(separator: String): String =
        replace("\r", " ")
            .replace("\n", " ")
            .replace(separator, " ")
}
