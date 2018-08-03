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

package butter.droid.provider.subs.opensubs.data;

import java.util.List;

import butter.droid.provider.subs.opensubs.data.model.response.OpenSubItem;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface OpenSubsService {

    @Headers({"Accept: application/json", "User-Agent: Butter v1"})
    @GET("search/imdbid-{imdbid}/sublanguageid-{lang}")
    Single<List<OpenSubItem>> searchByImdbId(
            @Header("User-Agent") String userAgent,
            @Path(value = "imdbid", encoded = true) String imdbId,
            @Path("lang") String language);

    @Headers({"Accept: application/json", "User-Agent: Butter v1"})
    @GET("search/episode-{episode}/imdbid-{imdbid}/season-{season}/sublanguageid-{lang}")
    Single<List<OpenSubItem>> searchByImdbSeasonEpisode(
            @Header("User-Agent") String userAgent,
            @Path(value = "imdbid", encoded = true) String imdbId,
            @Path("season") String season,
            @Path("episode") String episode,
            @Path("lang") String language);

    @GET
    Single<ResponseBody> download(@Url String downloadLink);
}
