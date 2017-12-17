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

package butter.droid.provider.subs;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.subs.model.Subtitle;
import io.reactivex.Maybe;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import okio.BufferedSink;
import okio.Okio;

public abstract class AbsSubsProvider implements SubsProvider {

    private final Context context;

    protected AbsSubsProvider(final Context context) {
        this.context = context;
    }

    @Override public final Maybe<Uri> downloadSubs(@NonNull final Media media, @NonNull final Subtitle subtitle) {
        return provideSubs(media, subtitle)
                .map(s -> {
                    File subsFile = new File(getIdealCacheDirectory(), genrateFileName(media, subtitle));

                    // What if file already exists
                    BufferedSink sink = Okio.buffer(Okio.sink(subsFile));
                    sink.writeAll(Okio.source(s));
                    sink.close();

                    return subsFile;
                })
                .map(Uri::fromFile);
    }

    protected abstract Maybe<InputStream> provideSubs(@NonNull final Media media, @NonNull final Subtitle subtitle);

    /**
     * Get ideal cache directory based on available
     * TODO Use storage set in preferences. Should not be passed in as constructor as implementor may change it.
     *
     * @return Ideal file location for caching
     */
    private File getIdealCacheDirectory() {
//        File externalCacheDir = context.getExternalCacheDir();
//        if (getTotalExternalMemorySize() < getTotalInternalMemorySize() || externalCacheDir == null) {
        File cacheDir = context.getCacheDir();
        cacheDir.mkdirs();
        return cacheDir;
//        }
//        return externalCacheDir;
    }

    private String genrateFileName(@NonNull final Media media, @NonNull final Subtitle subtitle) throws NoSuchAlgorithmException {
        // TODO better naming policy needed
        MessageDigest md = MessageDigest.getInstance("MD5");
        return Base64.encodeToString(md.digest((media.getId() + subtitle.getLanguage() + subtitle.getName()).getBytes()), Base64.DEFAULT);
    }

}
