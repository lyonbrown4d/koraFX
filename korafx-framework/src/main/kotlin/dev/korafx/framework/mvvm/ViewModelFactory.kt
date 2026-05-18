package dev.korafx.framework.mvvm

fun interface ViewModelFactory<out VM : ViewModel<*, *, *>> {
    fun create(): VM
}

fun <VM : ViewModel<*, *, *>> viewModelFactory(
    create: () -> VM,
): ViewModelFactory<VM> =
    ViewModelFactory(create)

fun Iterable<ViewModel<*, *, *>>.closeAll() {
    forEach { viewModel ->
        viewModel.close()
    }
}
