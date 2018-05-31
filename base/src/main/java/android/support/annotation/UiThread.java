/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes that the annotated method or constructor should only be called on the UI thread.
 * If the annotated element is a class, then all methods in the class should be called
 * on the UI thread.
 * <p>
 * Example:
 * <pre><code>
 *  &#64;UiThread
 *
 *  public abstract void setText(@NonNull String text) { ... }
 * </code></pre>
 *
 * <p class="note"><b>Note:</b> Ordinarily, an app's UI thread is also the main
 * thread. However, However, under special circumstances, an app's UI thread
 * might not be its main thread; for more information, see
 * <a href="/studio/write/annotations.html#thread-annotations">Thread
 * annotations</a>.
 *
 * @see android.support.annotation.MainThread
 */
@Documented
@Retention(CLASS)
@Target({METHOD,CONSTRUCTOR,TYPE})
public @interface UiThread {
}
