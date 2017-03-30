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

package butter.droid.ui.main;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.youtube.YouTubeManager;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MainPresenterImplUnitTest {

    @Mock MainView view;
    @Mock YouTubeManager youTubeManager;
    @Mock ProviderManager providerManager;
    @Mock BeamManager beamManager;
    @Mock Context context;
    @Mock PreferencesHandler preferencesHandler;
    @Mock PrefManager prefManager;

    MainPresenterImpl presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new MainPresenterImpl(view, youTubeManager, providerManager, beamManager, context,
                preferencesHandler, prefManager);
    }

    @Test
    public void onCreateInitial() throws Exception {
        when(preferencesHandler.getDefaultProvider()).thenReturn(ProviderManager.PROVIDER_TYPE_MOVIE);

        presenter.onCreate(true);

        //noinspection WrongConstant
        verify(view).initProviders(eq(ProviderManager.PROVIDER_TYPE_MOVIE));
        verify(view).openDrawer();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void onCreateStavedState() throws Exception {
        presenter.onCreate(false);

        verifyNoMoreInteractions(view);
    }
}
