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

package butter.droid.provider.subs.opensubs.data.model.response;

import nl.nl2312.xmlrpc.deserialization.MemberName;

public class OpenSubItem {

    @MemberName("SubFormat") String format;
    @MemberName("IDMovieImdb") String imdbId;
    @MemberName("MovieYear") String year;
    @MemberName("SubDownloadLink") String downalodLink;
    @MemberName("LanguageName") String languageName;
    @MemberName("ISO639") String languageCode;
    @MemberName("SubDownloadsCnt") String downloads;
    @MemberName("MatchedBy") String matchedBy;
    @MemberName("UserRank") String userRank;

    public String getFormat() {
        return format;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getYear() {
        return year;
    }

    public String getDownalodLink() {
        return downalodLink;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getDownloads() {
        return downloads;
    }

    public String getMatchedBy() {
        return matchedBy;
    }

    public String getUserRank() {
        return userRank;
    }
}
