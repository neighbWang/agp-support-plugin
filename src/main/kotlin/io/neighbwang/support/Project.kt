package io.neighbwang.support

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.variant.VariantFactory
import org.gradle.api.Project
import java.lang.reflect.Proxy
import java.util.regex.Pattern

/**
 * @author neighbWang
 */
private const val BUILD_PATTEN = "(assemble|install|generate)(\\w+)(Release|Debug)"

internal fun Project.filterFlavors() = variantManager?.let { variantManager ->
    gradle.startParameter.taskRequests.toString()
        .let {
            Pattern.compile(BUILD_PATTEN).matcher(it)
        }.takeIf {
            it.find()
        }?.group(2)?.toLowerCase()?.letCatching { flavor ->
            println("==flavor is $flavor=====")
            variantManager.productFlavors.filter {
                it.key.contains(flavor, true)
            }.takeIf {
                it.isNotEmpty()
            }?.let { result ->
                variantManager.javaClass.getDeclaredField("productFlavors").apply {
                    isAccessible = true
                    this[variantManager] = result
                }
            }
        }
}

internal fun Project.filterBuildTypes() = variantManager?.let { variantManager ->
    val buildType = when {
        gradle.startParameter.taskRequests.toString().contains("debug", true) -> "debug"
        gradle.startParameter.taskRequests.toString().contains("release", true) -> "release"
        else -> return@let
    }
    variantManager.buildTypes.filter {
        it.key == buildType
    }.takeIf {
        it.isNotEmpty()
    }?.letCatching {
        variantManager.javaClass.run {
            getDeclaredField("buildTypes").apply {
                isAccessible = true
                this[variantManager] = it
            }
            // fix hook logic here
            getDeclaredField("extension").apply {
                isAccessible = true
                this[variantManager].takeIf {
                    it is TestedExtension
                }?.let { extension ->
                    (extension as TestedExtension).testBuildType = buildType
                }
            }
        }
    }
}

internal fun Project.filterTestScopes() = variantManager?.letCatching { variantManager ->
    variantManager.javaClass.run {
        // Proxy variantFactory to remove test-releative tasks
        getDeclaredField("variantFactory").also {
            it.isAccessible = true
        }.apply {
            val originalVariantFactory = this[variantManager]
            this[variantManager] = Proxy.newProxyInstance(this[variantManager].javaClass.classLoader, arrayOf(VariantFactory::class.java)) { _, method, args ->
                if (method.name == "hasTestScope") {
                    false
                } else {
                    method.invoke(originalVariantFactory, *(args ?: emptyArray()))
                }
            }
        }
    }
}

internal val Project.variantManager
    get() = when {
        plugins.hasPlugin(AppPlugin::class.java) -> plugins.getPlugin(AppPlugin::class.java)
        plugins.hasPlugin(LibraryPlugin::class.java) -> plugins.getPlugin(LibraryPlugin::class.java)
        else -> null
    }?.variantManager

private fun <T, R> T.letCatching(block: (T) -> R) = try {
    let(block)
} catch (e: Throwable) {
    e.printStackTrace()
}