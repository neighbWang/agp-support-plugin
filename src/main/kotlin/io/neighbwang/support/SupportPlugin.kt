package io.neighbwang.support

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author neighbWang
 */
class SupportPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val enableFilterBuildTypes = "true" == project.properties["gradle.support.filter.buildtype.enable"]
        val enableFilterFlavors = "true" == project.properties["gradle.support.filter.flavors.enable"]
        val enableFilterTestScopes = "true" == project.properties["gradle.support.filter.tests.enable"]
        project.allprojects { subProject ->
            subProject.afterEvaluate {
                val buildType by lazy {
                    it.buildType
                }
                it.run {
                    if (enableFilterBuildTypes) {
                        filterBuildTypes(buildType)
                    }
                    if (enableFilterFlavors) {
                        filterFlavors(buildType)
                    }
                    if (enableFilterTestScopes) {
                        filterTestScopes()
                    }
                }
            }
        }
    }
}