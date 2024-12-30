package org.myapplication.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code ReflectiveUse} annotation is a marker annotation used to indicate
 * that a method or class is intended for reflective access. This annotation
 * is used as a marker for code that should be accessed or invoked using Java
 * reflection mechanisms at runtime.
 *
 * <p>This annotation is primarily used for frameworks or utilities that require
 * reflection, such as dynamically invoking methods or inspecting classes at runtime.
 * It does not impose any functionality or behavior itself but serves to mark
 * code for special treatment in reflection-based processes.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 *     {@code
 *     @ReflectiveUse
 *     public void someReflectiveMethod() {
 *         // This method can be accessed via reflection
 *     }
 *     }
 * </pre>
 *
 * <p>Since this annotation is retained at runtime, it can be queried using
 * reflection to identify methods and classes that have been marked for reflective
 * use.</p>
 *
 * @since 1.0
 * @see java.lang.reflect.Method
 * @see java.lang.reflect.Field
 * @see java.lang.reflect.Constructor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ReflectiveUse { }