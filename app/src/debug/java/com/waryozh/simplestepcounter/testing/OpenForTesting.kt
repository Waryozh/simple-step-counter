package com.waryozh.simplestepcounter.testing

/**
 * This annotation allows to open classes in debug builds while they remain final in release builds.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class OpenClass

/**
 * This annotation makes a class extendable in debug builds.
 */
@OpenClass
@Target(AnnotationTarget.CLASS)
annotation class OpenForTesting
