package com.neighbwang.support.bugfix.nativelibs.missing

import com.android.builder.model.Version
import com.android.repository.Revision
import com.neighbwang.support.annotations.AndroidIssue
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

/**
 * This plugin is used for fixing merge native libs bug.
 * @author neighbWang
 */
@AndroidIssue(introducedIn = "3.5.0", fixedIn = "3.6.0", description = "When incremental build, so libs may dismiss in package.")
class MergeNativeLibsWorkaroundPlugin : Plugin<Project> {

    companion object {
        @Volatile
        private var inited = false
    }

    override fun apply(project: Project) {

        if (!GTE_V3_5_X) {
            return
        }

        if (inited) {
            return
        }

        val resourcesClassLoader = Class.forName(DEXARCHIVE_CLASS).classLoader

        val originalStream: ByteArray? = resourcesClassLoader.getResourceAsStream(MERGE_NATIVE_LIBS_TASK_CLASS)?.readBytes()

        val transformStream = ClassWriter(ClassWriter.COMPUTE_MAXS)
            .also { writer ->
                ClassNode().also { klass ->
                    ClassReader(originalStream).accept(klass, 0)
                    klass.methods?.removeIf {
                        it.name == "getIncremental" && it.desc == "()Z" && it.access == Opcodes.ACC_PROTECTED
                    }
                }.accept(writer)
            }.toByteArray()

        ClassLoader::class.java.getDeclaredMethod("defineClass", byteArrayOf().javaClass, Int::class.java, Int::class.java)
            .also {
                it.isAccessible = true
            }.invoke(resourcesClassLoader, transformStream, 0, transformStream.size)
        inited = true
    }
}


private const val DEXARCHIVE_CLASS = "com.android.builder.dexing.DexArchive"
private const val MERGE_NATIVE_LIBS_TASK_CLASS = "com/android/build/gradle/internal/tasks/MergeNativeLibsTask.class"
private val ANDROID_GRADLE_PLUGIN_VERSION = Revision.parseRevision(Version.ANDROID_GRADLE_PLUGIN_VERSION)
private val GTE_V3_5_X = ANDROID_GRADLE_PLUGIN_VERSION.major == 3 && ANDROID_GRADLE_PLUGIN_VERSION.minor == 5
