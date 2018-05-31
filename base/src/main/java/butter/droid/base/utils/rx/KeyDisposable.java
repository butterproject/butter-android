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

package butter.droid.base.utils.rx;

import androidx.annotation.NonNull;
import android.util.SparseArray;
import io.reactivex.disposables.Disposable;

public class KeyDisposable {

    private final SparseArray<Disposable> providerDataDisposable = new SparseArray<>();

    public void add(int key, @NonNull Disposable disposable) {
        synchronized (providerDataDisposable) {
            if (providerDataDisposable.get(key) != null) {
                throw new IllegalStateException("Key already added");
            }

            providerDataDisposable.append(key, disposable);
        }
    }

    public void disposeSingle(int key) {
        synchronized (providerDataDisposable) {
            Disposable disposable = providerDataDisposable.get(key);
            if (disposable != null) {
                disposable.dispose();
                providerDataDisposable.remove(key);
            }
        }
    }

    public void dispose() {
        synchronized (providerDataDisposable) {
            for (int i = 0; i < providerDataDisposable.size(); i++) {
                providerDataDisposable.get(providerDataDisposable.keyAt(i)).dispose();
            }
            providerDataDisposable.clear();
        }
    }

}
