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

package butter.droid.base.manager.internal.subtitle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import butter.droid.base.Internal;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.subs.SubsProvider;
import butter.droid.provider.subs.model.Subtitle;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

@Internal
public class SubtitleManager {

    private final PreferencesHandler preferencesHandler;

    @Inject
    public SubtitleManager(final PreferencesHandler preferencesHandler) {
        this.preferencesHandler = preferencesHandler;
    }

    @NonNull public Maybe<SubtitleWrapper> downloadSubtitle(@Nullable final SubsProvider subsProvider, @NonNull Media media,
            @Nullable final SubtitleWrapper wrapper) {
        if (wrapper != null && subsProvider != null) {

            final Subtitle subtitle = wrapper.getSubtitle();

            if (wrapper.getFileUri() != null) {
                return Maybe.just(wrapper);
            } else if (subtitle != null) {
                return subsProvider.downloadSubs(media, subtitle)
                        .doOnSuccess(wrapper::setFileUri)
                        .map(uri -> wrapper)
                        .subscribeOn(Schedulers.io());
            } else {
                String subtitleLanguage = preferencesHandler.getSubtitleDefaultLanguage();

                if (subtitleLanguage != null) {
                    Subtitle s = new Subtitle(subtitleLanguage, LocaleUtils.toLocale(subtitleLanguage).getDisplayName());
                    final SubtitleWrapper newWrapper = new SubtitleWrapper(s);

                    return subsProvider.list(media)
                            .flattenAsObservable(it -> it)
                            .filter(sub -> subtitleLanguage.equals(sub.getLanguage()))
                            .firstElement()
                            .flatMap(sub -> subsProvider.downloadSubs(media, sub))
                            .doOnSuccess(newWrapper::setFileUri)
                            .map(uri -> newWrapper)
                            .subscribeOn(Schedulers.io());
                } else {
                    return Maybe.empty();
                }
            }

        } else {
            return Maybe.empty();
        }
    }

}
