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

package butter.droid.base.providers.subs.open;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.timroes.axmlrpc.XMLRPCClient;

@Module
public class OpenSubsModule {

    @Provides @Singleton public XMLRPCClient provideXmlrpcClient() {
        try {
            return new XMLRPCClient(new URL(OpenSubsProvider.API_URL), OpenSubsProvider.USER_AGENT);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid url " + OpenSubsProvider.API_URL, e);
        }
    }

}
