/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.provider.base.util;

import java.util.NoSuchElementException;

public class Optional<T> {

    private T value;

    private Optional() {
        this.value = null;
    }

    private Optional(T value) {
        if (value == null) {
            throw new NullPointerException();
        }

        this.value = value;
    }

    public static<T> Optional<T> empty() {
        return new Optional<>();
    }

    public static<T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public interface Action<T> {
        void apply(T value);
    }

    public boolean isPresent() {
        return value != null;
    }

    public T get() {
        if (isPresent()) {
            return value;
        } else {
            throw new NoSuchElementException("No value");
        }
    }

    public void ifPresent(Action<T> action) {
        if (isPresent()) {
            action.apply(value);
        }
    }

}
