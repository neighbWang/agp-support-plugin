package io.neighbwang.support

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author neighbWang
 */
class SupportPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.allprojects { subProject ->
            subProject.afterEvaluate {
                it.run {
                    filterBuildTypes()
                    filterFlavors()
                    filterTestScopes()
                }
            }
        }
    }
}