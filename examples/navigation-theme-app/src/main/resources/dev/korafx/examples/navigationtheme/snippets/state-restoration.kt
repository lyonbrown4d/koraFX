// Kotlin snippet: 按 location 缓存状态
val noteInput = textField()
actionBar {
    button("Save") {
        onAction {
            navigator.saveState("note", noteInput.text)
        }
    }
    button("Load") {
        onAction {
            noteInput.text = navigator.restoredState<String>("note") ?: ""
        }
    }
}
