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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OpenSubItem {

    @SerializedName("MatchedBy")
    @Expose
    private String matchedBy;
    @SerializedName("IDSubMovieFile")
    @Expose
    private String iDSubMovieFile;
    @SerializedName("MovieHash")
    @Expose
    private String movieHash;
    @SerializedName("MovieByteSize")
    @Expose
    private String movieByteSize;
    @SerializedName("MovieTimeMS")
    @Expose
    private String movieTimeMS;
    @SerializedName("IDSubtitleFile")
    @Expose
    private String iDSubtitleFile;
    @SerializedName("SubFileName")
    @Expose
    private String subFileName;
    @SerializedName("SubActualCD")
    @Expose
    private String subActualCD;
    @SerializedName("SubSize")
    @Expose
    private String subSize;
    @SerializedName("SubHash")
    @Expose
    private String subHash;
    @SerializedName("SubLastTS")
    @Expose
    private String subLastTS;
    @SerializedName("SubTSGroup")
    @Expose
    private String subTSGroup;
    @SerializedName("InfoReleaseGroup")
    @Expose
    private String infoReleaseGroup;
    @SerializedName("InfoFormat")
    @Expose
    private String infoFormat;
    @SerializedName("InfoOther")
    @Expose
    private String infoOther;
    @SerializedName("IDSubtitle")
    @Expose
    private String iDSubtitle;
    @SerializedName("UserID")
    @Expose
    private String userID;
    @SerializedName("SubLanguageID")
    @Expose
    private String subLanguageID;
    @SerializedName("SubFormat")
    @Expose
    private String subFormat;
    @SerializedName("SubSumCD")
    @Expose
    private String subSumCD;
    @SerializedName("SubAuthorComment")
    @Expose
    private String subAuthorComment;
    @SerializedName("SubAddDate")
    @Expose
    private String subAddDate;
    @SerializedName("SubBad")
    @Expose
    private String subBad;
    @SerializedName("SubRating")
    @Expose
    private String subRating;
    @SerializedName("SubSumVotes")
    @Expose
    private String subSumVotes;
    @SerializedName("SubDownloadsCnt")
    @Expose
    private String subDownloadsCnt;
    @SerializedName("MovieReleaseName")
    @Expose
    private String movieReleaseName;
    @SerializedName("MovieFPS")
    @Expose
    private String movieFPS;
    @SerializedName("IDMovie")
    @Expose
    private String iDMovie;
    @SerializedName("IDMovieImdb")
    @Expose
    private String iDMovieImdb;
    @SerializedName("MovieName")
    @Expose
    private String movieName;
    @SerializedName("MovieNameEng")
    @Expose
    private String movieNameEng;
    @SerializedName("MovieYear")
    @Expose
    private String movieYear;
    @SerializedName("MovieImdbRating")
    @Expose
    private String movieImdbRating;
    @SerializedName("SubFeatured")
    @Expose
    private String subFeatured;
    @SerializedName("UserNickName")
    @Expose
    private String userNickName;
    @SerializedName("SubTranslator")
    @Expose
    private String subTranslator;
    @SerializedName("ISO639")
    @Expose
    private String iSO639;
    @SerializedName("LanguageName")
    @Expose
    private String languageName;
    @SerializedName("SubComments")
    @Expose
    private String subComments;
    @SerializedName("SubHearingImpaired")
    @Expose
    private String subHearingImpaired;
    @SerializedName("UserRank")
    @Expose
    private String userRank;
    @SerializedName("SeriesSeason")
    @Expose
    private String seriesSeason;
    @SerializedName("SeriesEpisode")
    @Expose
    private String seriesEpisode;
    @SerializedName("MovieKind")
    @Expose
    private String movieKind;
    @SerializedName("SubHD")
    @Expose
    private String subHD;
    @SerializedName("SeriesIMDBParent")
    @Expose
    private String seriesIMDBParent;
    @SerializedName("SubEncoding")
    @Expose
    private String subEncoding;
    @SerializedName("SubAutoTranslation")
    @Expose
    private String subAutoTranslation;
    @SerializedName("SubForeignPartsOnly")
    @Expose
    private String subForeignPartsOnly;
    @SerializedName("SubFromTrusted")
    @Expose
    private String subFromTrusted;

    @SerializedName("SubTSGroupHash")
    @Expose
    private String subTSGroupHash;
    @SerializedName("SubDownloadLink")
    @Expose
    private String subDownloadLink;
    @SerializedName("ZipDownloadLink")
    @Expose
    private String zipDownloadLink;
    @SerializedName("SubtitlesLink")
    @Expose
    private String subtitlesLink;
    @SerializedName("QueryNumber")
    @Expose
    private String queryNumber;
    @SerializedName("Score")
    @Expose
    private Double score;

    public String getMatchedBy() {
        return matchedBy;
    }

    public void setMatchedBy(String matchedBy) {
        this.matchedBy = matchedBy;
    }

    public String getIDSubMovieFile() {
        return iDSubMovieFile;
    }

    public void setIDSubMovieFile(String iDSubMovieFile) {
        this.iDSubMovieFile = iDSubMovieFile;
    }

    public String getMovieHash() {
        return movieHash;
    }

    public void setMovieHash(String movieHash) {
        this.movieHash = movieHash;
    }

    public String getMovieByteSize() {
        return movieByteSize;
    }

    public void setMovieByteSize(String movieByteSize) {
        this.movieByteSize = movieByteSize;
    }

    public String getMovieTimeMS() {
        return movieTimeMS;
    }

    public void setMovieTimeMS(String movieTimeMS) {
        this.movieTimeMS = movieTimeMS;
    }

    public String getIDSubtitleFile() {
        return iDSubtitleFile;
    }

    public void setIDSubtitleFile(String iDSubtitleFile) {
        this.iDSubtitleFile = iDSubtitleFile;
    }

    public String getSubFileName() {
        return subFileName;
    }

    public void setSubFileName(String subFileName) {
        this.subFileName = subFileName;
    }

    public String getSubActualCD() {
        return subActualCD;
    }

    public void setSubActualCD(String subActualCD) {
        this.subActualCD = subActualCD;
    }

    public String getSubSize() {
        return subSize;
    }

    public void setSubSize(String subSize) {
        this.subSize = subSize;
    }

    public String getSubHash() {
        return subHash;
    }

    public void setSubHash(String subHash) {
        this.subHash = subHash;
    }

    public String getSubLastTS() {
        return subLastTS;
    }

    public void setSubLastTS(String subLastTS) {
        this.subLastTS = subLastTS;
    }

    public String getSubTSGroup() {
        return subTSGroup;
    }

    public void setSubTSGroup(String subTSGroup) {
        this.subTSGroup = subTSGroup;
    }

    public String getInfoReleaseGroup() {
        return infoReleaseGroup;
    }

    public void setInfoReleaseGroup(String infoReleaseGroup) {
        this.infoReleaseGroup = infoReleaseGroup;
    }

    public String getInfoFormat() {
        return infoFormat;
    }

    public void setInfoFormat(String infoFormat) {
        this.infoFormat = infoFormat;
    }

    public String getInfoOther() {
        return infoOther;
    }

    public void setInfoOther(String infoOther) {
        this.infoOther = infoOther;
    }

    public String getIDSubtitle() {
        return iDSubtitle;
    }

    public void setIDSubtitle(String iDSubtitle) {
        this.iDSubtitle = iDSubtitle;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSubLanguageID() {
        return subLanguageID;
    }

    public void setSubLanguageID(String subLanguageID) {
        this.subLanguageID = subLanguageID;
    }

    public String getSubFormat() {
        return subFormat;
    }

    public void setSubFormat(String subFormat) {
        this.subFormat = subFormat;
    }

    public String getSubSumCD() {
        return subSumCD;
    }

    public void setSubSumCD(String subSumCD) {
        this.subSumCD = subSumCD;
    }

    public String getSubAuthorComment() {
        return subAuthorComment;
    }

    public void setSubAuthorComment(String subAuthorComment) {
        this.subAuthorComment = subAuthorComment;
    }

    public String getSubAddDate() {
        return subAddDate;
    }

    public void setSubAddDate(String subAddDate) {
        this.subAddDate = subAddDate;
    }

    public String getSubBad() {
        return subBad;
    }

    public void setSubBad(String subBad) {
        this.subBad = subBad;
    }

    public String getSubRating() {
        return subRating;
    }

    public void setSubRating(String subRating) {
        this.subRating = subRating;
    }

    public String getSubSumVotes() {
        return subSumVotes;
    }

    public void setSubSumVotes(String subSumVotes) {
        this.subSumVotes = subSumVotes;
    }

    public String getSubDownloadsCnt() {
        return subDownloadsCnt;
    }

    public void setSubDownloadsCnt(String subDownloadsCnt) {
        this.subDownloadsCnt = subDownloadsCnt;
    }

    public String getMovieReleaseName() {
        return movieReleaseName;
    }

    public void setMovieReleaseName(String movieReleaseName) {
        this.movieReleaseName = movieReleaseName;
    }

    public String getMovieFPS() {
        return movieFPS;
    }

    public void setMovieFPS(String movieFPS) {
        this.movieFPS = movieFPS;
    }

    public String getIDMovie() {
        return iDMovie;
    }

    public void setIDMovie(String iDMovie) {
        this.iDMovie = iDMovie;
    }

    public String getIDMovieImdb() {
        return iDMovieImdb;
    }

    public void setIDMovieImdb(String iDMovieImdb) {
        this.iDMovieImdb = iDMovieImdb;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieNameEng() {
        return movieNameEng;
    }

    public void setMovieNameEng(String movieNameEng) {
        this.movieNameEng = movieNameEng;
    }

    public String getMovieYear() {
        return movieYear;
    }

    public void setMovieYear(String movieYear) {
        this.movieYear = movieYear;
    }

    public String getMovieImdbRating() {
        return movieImdbRating;
    }

    public void setMovieImdbRating(String movieImdbRating) {
        this.movieImdbRating = movieImdbRating;
    }

    public String getSubFeatured() {
        return subFeatured;
    }

    public void setSubFeatured(String subFeatured) {
        this.subFeatured = subFeatured;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getSubTranslator() {
        return subTranslator;
    }

    public void setSubTranslator(String subTranslator) {
        this.subTranslator = subTranslator;
    }

    public String getISO639() {
        return iSO639;
    }

    public void setISO639(String iSO639) {
        this.iSO639 = iSO639;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getSubComments() {
        return subComments;
    }

    public void setSubComments(String subComments) {
        this.subComments = subComments;
    }

    public String getSubHearingImpaired() {
        return subHearingImpaired;
    }

    public void setSubHearingImpaired(String subHearingImpaired) {
        this.subHearingImpaired = subHearingImpaired;
    }

    public String getUserRank() {
        return userRank;
    }

    public void setUserRank(String userRank) {
        this.userRank = userRank;
    }

    public String getSeriesSeason() {
        return seriesSeason;
    }

    public void setSeriesSeason(String seriesSeason) {
        this.seriesSeason = seriesSeason;
    }

    public String getSeriesEpisode() {
        return seriesEpisode;
    }

    public void setSeriesEpisode(String seriesEpisode) {
        this.seriesEpisode = seriesEpisode;
    }

    public String getMovieKind() {
        return movieKind;
    }

    public void setMovieKind(String movieKind) {
        this.movieKind = movieKind;
    }

    public String getSubHD() {
        return subHD;
    }

    public void setSubHD(String subHD) {
        this.subHD = subHD;
    }

    public String getSeriesIMDBParent() {
        return seriesIMDBParent;
    }

    public void setSeriesIMDBParent(String seriesIMDBParent) {
        this.seriesIMDBParent = seriesIMDBParent;
    }

    public String getSubEncoding() {
        return subEncoding;
    }

    public void setSubEncoding(String subEncoding) {
        this.subEncoding = subEncoding;
    }

    public String getSubAutoTranslation() {
        return subAutoTranslation;
    }

    public void setSubAutoTranslation(String subAutoTranslation) {
        this.subAutoTranslation = subAutoTranslation;
    }

    public String getSubForeignPartsOnly() {
        return subForeignPartsOnly;
    }

    public void setSubForeignPartsOnly(String subForeignPartsOnly) {
        this.subForeignPartsOnly = subForeignPartsOnly;
    }

    public String getSubFromTrusted() {
        return subFromTrusted;
    }

    public void setSubFromTrusted(String subFromTrusted) {
        this.subFromTrusted = subFromTrusted;
    }

    public String getSubTSGroupHash() {
        return subTSGroupHash;
    }

    public void setSubTSGroupHash(String subTSGroupHash) {
        this.subTSGroupHash = subTSGroupHash;
    }

    public String getSubDownloadLink() {
        return subDownloadLink;
    }

    public void setSubDownloadLink(String subDownloadLink) {
        this.subDownloadLink = subDownloadLink;
    }

    public String getZipDownloadLink() {
        return zipDownloadLink;
    }

    public void setZipDownloadLink(String zipDownloadLink) {
        this.zipDownloadLink = zipDownloadLink;
    }

    public String getSubtitlesLink() {
        return subtitlesLink;
    }

    public void setSubtitlesLink(String subtitlesLink) {
        this.subtitlesLink = subtitlesLink;
    }

    public String getQueryNumber() {
        return queryNumber;
    }

    public void setQueryNumber(String queryNumber) {
        this.queryNumber = queryNumber;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

}
