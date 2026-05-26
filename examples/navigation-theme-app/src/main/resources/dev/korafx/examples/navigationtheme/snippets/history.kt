// Kotlin snippet: back / forward / popToRoot
navigator.navigatePath("/routes/11/history")
navigator.navigatePath("/state/10")
navigator.back() // true, 回到 /routes/11/history
navigator.forward() // true, 回到 /state/10

val atRoot = navigator.popToRoot()
val moved = navigator.canGoBack
navigator.clearNavigationHistory()
