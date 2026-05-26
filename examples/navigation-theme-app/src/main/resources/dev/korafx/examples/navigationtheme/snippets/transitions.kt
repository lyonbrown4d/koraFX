// Kotlin snippet: 切换转场策略
transitionPreset.value = NavigationTransitionProfile.Adaptive
transitionPreset.value = NavigationTransitionProfile.PushSlide
transitionPreset.value = NavigationTransitionProfile.Fade
transitionPreset.value = NavigationTransitionProfile.Scale
transitionPreset.value = NavigationTransitionProfile.None

navigator.navigatePath("/routes/42/history")
navigator.back() // 使用 POP 策略
