package com.neighbwang.support.annotations


/**
 * Version of Android plugin that fixes the problem. Workaround not applied if current Android plugin version is the same or later.
 * @author neighbWang
 */
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class AndroidIssue(val introducedIn: String, val fixedIn: String = "", val description: String)