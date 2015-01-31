package pct.droid.base.providers.media;

import android.accounts.NetworkErrorException;
import android.os.Parcel;

import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

import pct.droid.base.R;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.providers.subs.OpenSubsProvider;

public class EZTVProvider extends MediaProvider {

	protected String mApiUrl = "http://eztvapi.re/";
	protected String mMirrorApiUrl = "http://api.popcorntime.io/";

	@Override
	public Call getList(final ArrayList<Media> existingList, Filters filters, final Callback callback) {
		final ArrayList<Media> currentList;
		if (existingList == null) {
			currentList = new ArrayList<>();
		} else {
			currentList = (ArrayList<Media>) existingList.clone();
		}

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("limit", "30"));

		if (filters == null) {
			filters = new Filters();
		}

		if (filters.keywords != null) {
			params.add(new BasicNameValuePair("keywords", filters.keywords));
		}

		if (filters.genre != null) {
			params.add(new BasicNameValuePair("genre", filters.genre));
		}

		if (filters.order == Filters.Order.ASC) {
			params.add(new BasicNameValuePair("order", "asc"));
		} else {
			params.add(new BasicNameValuePair("order", "desc"));
		}

		String sort = "";
		switch (filters.sort) {
			default:
			case POPULARITY:
				sort = "popularity";
				break;
            case YEAR:
                sort = "year";
                break;
            case DATE:
                sort = "'updated'";
                break;
            case RATING:
                sort = "rating";
                break;
            case ALPHABET:
                sort = "'name'";
                break;
		}

		params.add(new BasicNameValuePair("sort", sort));

		String url = mApiUrl + "shows/";
		if (filters.page != null) {
			url += filters.page;
		} else {
			url += "1";
		}

		Request.Builder requestBuilder = new Request.Builder();
		String query = buildQuery(params);
		requestBuilder.url(url + "?" + query);
		requestBuilder.tag(MEDIA_CALL);

		return fetchList(currentList, requestBuilder, callback);
	}

	/**
	 * Fetch the list of movies from EZTV
	 *
	 * @param currentList Current shown list to be extended
	 * @param requestBuilder Request to be executed
	 * @param callback Network callback
	 *
	 * @return Call
	 */
	private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Callback callback) {
		return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				String url = requestBuilder.build().urlString();
				if (url.equals(mMirrorApiUrl)) {
					callback.onFailure(e);
				} else {
					url = url.replace(mApiUrl, mMirrorApiUrl);
					requestBuilder.url(url);
					fetchList(currentList, requestBuilder, callback);
				}
			}

			@Override
			public void onResponse(Response response) throws IOException {
				try {
					if (response.isSuccessful()) {
						String responseStr = response.body().string();
						ArrayList<LinkedTreeMap<String, Object>> list =
								(ArrayList<LinkedTreeMap<String, Object>>) mGson.fromJson(responseStr, ArrayList.class);
						EZTVReponse result = new EZTVReponse(list);
						if (list == null) {
							callback.onFailure(new NetworkErrorException("Empty response"));
						} else {
							ArrayList<Media> formattedData = result.formatListForPopcorn(currentList);
							callback.onSuccess(formattedData);
							return;
						}
					}
				} catch (Exception e) {
					callback.onFailure(e);
				}
                callback.onFailure(new NetworkErrorException("Couldn't connect to EZTVAPI"));
			}
		});
	}

	@Override
	public Call getDetail(String videoId, final Callback callback) {
		Request.Builder requestBuilder = new Request.Builder();
		requestBuilder.url(mApiUrl + "show/" + videoId);
		requestBuilder.tag(MEDIA_CALL);

		return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				callback.onFailure(e);
			}

			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String responseStr = response.body().string();
					LinkedTreeMap<String, Object> map = mGson.fromJson(responseStr, LinkedTreeMap.class);
					EZTVReponse result = new EZTVReponse(map);
					if (map == null) {
						callback.onFailure(new NetworkErrorException("Empty response"));
					} else {
						ArrayList<Media> formattedData = result.formatDetailForPopcorn();

						if (formattedData.size() > 0) {
							Show show = (Show) formattedData.get(0);
							callback.onSuccess(formattedData);
							return;
						}
						callback.onFailure(new IllegalStateException("Empty list"));
                        return;
					}
				}
                callback.onFailure(new NetworkErrorException("Couldn't connect to EZTVAPI"));
			}
		});
	}

	private class EZTVReponse {
		LinkedTreeMap<String, Object> showData;
		ArrayList<LinkedTreeMap<String, Object>> showsList;

		public EZTVReponse(LinkedTreeMap<String, Object> showData) {
			this.showData = showData;
		}

		public ArrayList<Media> formatDetailForPopcorn() {
			ArrayList<Media> list = new ArrayList<>();
			try {
				Show show = new EZTVShow();

				show.title = (String) showData.get("title");
				show.videoId = (String) showData.get("imdb_id");
				show.imdbId = (String) showData.get("imdb_id");
				show.tvdbId = (String) showData.get("tvdb_id");
				show.seasons = ((Double) showData.get("num_seasons")).intValue();
				show.year = (String) showData.get("year");
				LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) showData.get("images");
				show.image = images.get("poster").replace("/original/", "/medium/");
				show.fullImage = images.get("poster");
				show.headerImage = images.get("fanart").replace("/original/", "/medium/");

                if(showData.get("status") != null) {
                    String status = (String) showData.get("status");
                    if(status.equalsIgnoreCase("ended")) {
                        show.status = Show.Status.ENDED;
                    } else if(status.equalsIgnoreCase("returning series")) {
                        show.status = Show.Status.CONTINUING;
                    } else if(status.equalsIgnoreCase("canceled")) {
                        show.status = Show.Status.CANCELED;
                    }
                }

				show.country = (String) showData.get("country");
				show.network = (String) showData.get("network");
				show.synopsis = (String) showData.get("synopsis");
				show.runtime = (String) showData.get("runtime");
				show.airDay = (String) showData.get("air_day");
				show.airTime = (String) showData.get("air_time");
				show.genre = ((ArrayList<String>) showData.get("genres")).get(0);
				show.rating = Double.toString(((LinkedTreeMap<String, Double>) showData.get("rating")).get("percentage") / 10);

				ArrayList<LinkedTreeMap<String, Object>> episodes = (ArrayList<LinkedTreeMap<String, Object>>) showData.get("episodes");
				for (LinkedTreeMap<String, Object> episode : episodes) {
					Show.Episode episodeObject = new EZTVShow.EZTVEpisode();
					LinkedTreeMap<String, LinkedTreeMap<String, Object>> torrents =
							(LinkedTreeMap<String, LinkedTreeMap<String, Object>>) episode.get("torrents");
					for (String key : torrents.keySet()) {
						if (!key.equals("0")) {
							LinkedTreeMap<String, Object> item = torrents.get(key);
							Media.Torrent torrent = new Media.Torrent();
							torrent.url = item.get("url").toString();
							torrent.seeds = item.get("seeds").toString();
							torrent.peers = item.get("peers").toString();
							episodeObject.torrents.put(key, torrent);
						}
					}

					episodeObject.dateBased = (Boolean) episode.get("date_based");
					episodeObject.aired = ((Double) episode.get("first_aired")).intValue();
                    episodeObject.title = (String) episode.get("title");
					episodeObject.overview = (String) episode.get("overview");
					episodeObject.season = ((Double) episode.get("season")).intValue();
					episodeObject.episode = ((Double) episode.get("episode")).intValue();
					episodeObject.videoId = show.videoId + episodeObject.season + episodeObject.episode;

					show.episodes.add(episodeObject);
				}

				list.add(show);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return list;
		}

		public EZTVReponse(ArrayList<LinkedTreeMap<String, Object>> showsList) {
			this.showsList = showsList;
		}

		public ArrayList<Media> formatListForPopcorn(ArrayList<Media> existingList) {
			for (LinkedTreeMap<String, Object> item : showsList) {
				Show show = new EZTVShow();

				show.title = item.get("title").toString();
				show.videoId = item.get("imdb_id").toString();
				show.seasons = (Integer) item.get("seasons");
				show.tvdbId = item.get("tvdb_id").toString();
				show.year = item.get("year").toString();
				LinkedTreeMap<String, String> images = (LinkedTreeMap<String, String>) item.get("images");
				show.image = images.get("poster").replace("/original/", "/medium/");
				show.headerImage = images.get("fanart").replace("/original/", "/medium/");

				existingList.add(show);
			}
			return existingList;
		}
	}

	@Override
    public int getLoadingMessage() {
		return R.string.loading_shows;
	}

    public static class EZTVShow extends Show {
        public EZTVShow() {
            super();
            mSubsProvider = new OpenSubsProvider();
        }

        public EZTVShow(Parcel in) {
            super(in);
            mSubsProvider = new OpenSubsProvider();
        }

        public static class EZTVEpisode extends Episode {
            public EZTVEpisode() {
                super();
                mSubsProvider = new OpenSubsProvider();
            }

            public EZTVEpisode(Parcel in) {
                super(in);
                mSubsProvider = new OpenSubsProvider();
            }
        }
    }
}
