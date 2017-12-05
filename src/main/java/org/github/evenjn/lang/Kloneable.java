/**
 *
 * Copyright 2017 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.lang;

/**
 * <p>
 * An object implementing the {@code Kloneable} interface provides a method,
 * {@link #klone(Kloneable)}} that returns a distinct object that is equivalent
 * to it in some sense, but has a different object identity.
 * </p>
 * 
 * <p>
 * The type of each non-static field of an object implementing the
 * {@code Kloneable} interface is either a primitive, or an immutable type, or a
 * read-only class or interface, or a class implementing Kloneable, or an
 * interface extending Kloneable.
 * </p>
 * 
 * <p>
 * This class is part of package {@link org.github.evenjn.lang Lang}.
 * </p>
 * 
 * @since 1.0
 */
public interface Kloneable {

	/**
	 * Returns a copy of the argument object, which must be {@code this}.
	 * 
	 * @param <K>
	 *          The type of the argument.
	 * @param kloneable
	 *          The object to copy, which must be {@code this}.
	 * @return A different object that is equivalent to {@code this} in some
	 *         sense.
	 * @throws IllegalArgumentException
	 *           when the argument is not {@code this}.
	 * @since 1.0
	 */
	<K extends Kloneable> K klone( K kloneable )
			throws IllegalArgumentException;
}
