package io.neighbwang.support

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.VariantManager
import com.android.build.gradle.internal.variant.VariantFactory
import org.gradle.api.Project
import java.lang.reflect.Proxy
import java.util.regex.Pattern

/**
 * @author neighbWang
 */
private val Project.startCommand
    get() = gradle.startParameter.taskRequests.toString()

internal val Project.buildType
    get() = variantManager?.run {
        buildTypes.keys.firstOrNull {
            startCommand.contains(it, true)
        }
    }

fun Project.filterFlavors(variantManager: VariantManager, buildtype: String?) = startCommand.toLowerCase().let {
    Pattern.compile("(assemble|install|generate)(\\w+)$buildtype").matcher(it)
}.takeIf {
    it.find()
}?.group(2)?.letCatching { flavor ->
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

internal fun Project.filterBuildTypes(variantManager: VariantManager, buildtype: String?) = variantManager.buildTypes.filter {
    it.key == buildtype
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
                (extension as TestedExtension).testBuildType = buildtype
            }
        }
    }
}

internal fun Project.filterTestScopes(variantManager: VariantManager) = letCatching {
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