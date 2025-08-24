package com.ruko.system.presentation.navigation


import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType

@OptIn(ExperimentalAnimationApi::class)
inline fun <reified T : Any> NavGraphBuilder.fadeThroughComposable(
    deepLinks: List<NavDeepLink> = emptyList(),
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    noinline content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = {
        fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(220, delayMillis = 90)
                )
    },
    exitTransition = {
        fadeOut(animationSpec = tween(90))
    },
    popEnterTransition = {
        fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(220, delayMillis = 90)
                )
    },
    popExitTransition = {
        fadeOut(animationSpec = tween(90))
    },
    typeMap = typeMap,
    content = content
)

const val DURATION_ENTER = 400
const val DURATION_EXIT = 200
const val initialOffset = 0.10f

//
//fun PathInterpolator.toEasing(): Easing {
//    return Easing { f -> this.getInterpolation(f) }
//}
//
//private val path = Path().apply {
//    moveTo(0f, 0f)
//    cubicTo(0.05F, 0F, 0.133333F, 0.06F, 0.166666F, 0.4F)
//    cubicTo(0.208333F, 0.82F, 0.25F, 1F, 1F, 1F)
//}
//fun pathToEasing(samples: List<Float>): Easing = Easing { t ->
//    val n = (samples.size - 1) * t
//    val i = n.toInt().coerceIn(0, samples.size - 2)
//    val frac = n - i
//    samples[i] + (samples[i + 1] - samples[i]) * frac
//}
//private val emphasizePathInterpolator = PathInterpolator(path)
private val emphasizeEasing = CubicBezierEasing(.2f, 0f, 0f, 1f)
private val emphasizeEasingVariant = CubicBezierEasing(.2f, 0f, 0f, 1f)
private val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)

private val standardDecelerate = CubicBezierEasing(.0f, .0f, 0f, 1f)

private val motionEasingStandard = CubicBezierEasing(0.4F, 0.0F, 0.2F, 1F)

private val tweenSpec = tween<Float>(durationMillis = DURATION_ENTER, easing = emphasizeEasing)

val enterTween =
    tween<IntOffset>(durationMillis = DURATION_ENTER, easing = emphasizeEasing)
val exitTween =
    tween<IntOffset>(durationMillis = DURATION_ENTER, easing = emphasizeEasing)

val fadeSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
)
val fadeTween = tween<Float>(durationMillis = DURATION_EXIT)

val fadeSpec = fadeTween

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = {
        slideInHorizontally(
            enterTween,
            initialOffsetX = { (it * initialOffset).toInt() }) + fadeIn(fadeSpec)
    },
    exitTransition = {
        slideOutHorizontally(
            exitTween,
            targetOffsetX = { -(it * initialOffset).toInt() }) + fadeOut(fadeSpec)
    },
    popEnterTransition = {
        slideInHorizontally(
            enterTween,
            initialOffsetX = { -(it * initialOffset).toInt() }) + fadeIn(fadeSpec)
    },
    popExitTransition = {
        slideOutHorizontally(
            exitTween,
            targetOffsetX = { (it * initialOffset).toInt() }) + fadeOut(fadeSpec)
    },
    content = content
)


inline fun <reified T : Any> NavGraphBuilder.animatedComposableVariant(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = {
        slideInHorizontally(
            enterTween,
            initialOffsetX = { (it * initialOffset).toInt() }) + fadeIn(fadeSpec)
    },
    exitTransition = {
        fadeOut(fadeSpec)
    },
    popEnterTransition = {
        fadeIn(fadeSpec)
    },
    popExitTransition = {
        slideOutHorizontally(
            exitTween,
            targetOffsetX = { (it * initialOffset).toInt() }) + fadeOut(fadeSpec)
    },
    content = content
)


inline fun <reified T : Any> NavGraphBuilder.animatedComposableV1(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) =
    composable<T>(
        deepLinks = deepLinks,
        enterTransition = {
            slideInHorizontally(
                enterTween,
                initialOffsetX = { (it * initialOffset).toInt() }) + fadeIn(fadeSpec)
        },
        exitTransition = {
            slideOutHorizontally(
                exitTween,
                targetOffsetX = { -(it * initialOffset).toInt() }) + fadeOut(fadeSpec)
        },
        popEnterTransition = {
            slideInHorizontally(
                enterTween,
                initialOffsetX = { -(it * initialOffset).toInt() }) + fadeIn(fadeSpec)
        },
        popExitTransition = {
            slideOutHorizontally(
                exitTween,
                targetOffsetX = { (it * initialOffset).toInt() }) + fadeOut(fadeSpec)
        },
        content = content
    )


//fun slideInVertically(
//    animationSpec: FiniteAnimationSpec<IntOffset> =
//        spring(
//            stiffness = Spring.StiffnessMedium,
//            visibilityThreshold = IntOffset.VisibilityThreshold
//        ),
//    initialOffsetY: (fullHeight: Int) -> Int = { it },
//): EnterTransition =
//    slideIn(
//        initialOffset = { IntOffset(0, initialOffsetY(it.height)) },
//        animationSpec = animationSpec
//    )

val springSpec = spring(
    stiffness = Spring.StiffnessMedium,
    visibilityThreshold = IntOffset.VisibilityThreshold
)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.slideInVerticallyComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        slideInVertically(
            initialOffsetY = { it }, animationSpec = enterTween
        ) + fadeIn()
    },
    exitTransition = { slideOutVertically() },
    popEnterTransition = { slideInVertically() },
    popExitTransition = {
        slideOutVertically(
            targetOffsetY = { it },
            animationSpec = enterTween
        ) + fadeOut()
    },
    content = content
)
