/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.providers.media;

import android.accounts.NetworkErrorException;
import android.os.Parcel;

import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pct.droid.base.R;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.meta.TraktProvider;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.providers.subs.YSubsProvider;

public class YTSProvider extends MediaProvider {

	private static final String API_URL = "https://cloudflare.com/api/v2/";
    private static final String MIRROR_URL = "https://reddit.com/api/v2/";

    @Override
    protected OkHttpClient getClient() {
        OkHttpClient client = super.getClient().clone();
        // Use only HTTP 1.1 for YTS
        List<Protocol> proto = new ArrayList<>();
        proto.add(Protocol.HTTP_1_1);
        client.setProtocols(proto);
        return client;
    }

	@Override
	public Call getList(final ArrayList<Media> existingList, Filters filters, final Callback callback) {
		final ArrayList<Media> currentList;
		if (existingList == null) {
			currentList = new ArrayList<>();
		} else {
			currentList = (ArrayList<Media>) existingList.clone();
		}

		ArrayList<BasicNameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("limit", "30"));

		if (filters == null) {
			filters = new Filters();
		}

		if (filters.keywords != null) {
			String keywords = filters.keywords.replaceAll("\\s", "% ");
			params.add(new BasicNameValuePair("query_term", keywords));
		}

		if (filters.genre != null) {
			params.add(new BasicNameValuePair("genre", filters.genre));
		}

		if (filters.order == Filters.Order.ASC) {
			params.add(new BasicNameValuePair("order_by", "asc"));
		} else {
			params.add(new BasicNameValuePair("order_by", "desc"));
		}

		String sort;
		switch (filters.sort) {
			default:
			case POPULARITY:
				sort = "seeds";
				break;
            case YEAR:
                sort = "year";
                break;
            case DATE:
                sort = "date_added";
                break;
            case RATING:
                sort = "rating";
                break;
            case ALPHABET:
                sort = "title";
                break;
		}

		params.add(new BasicNameValuePair("sort_by", sort));

		if (filters.page != null) {
			params.add(new BasicNameValuePair("page", Integer.toString(filters.page)));
		}

		Request.Builder requestBuilder = new Request.Builder();
		String query = buildQuery(params);
		requestBuilder.url(API_URL + "list_movies.json?" + query);
		requestBuilder.tag(MEDIA_CALL);

		return fetchList(currentList, requestBuilder, callback);
	}

	/**
	 * Fetch the list of movies from YTS
	 *
	 * @param currentList Current shown list to be extended
	 * @param requestBuilder Request to be executed
	 * @param callback Network callback
	 *
	 * @return Call
	 */
	private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Callback callback) {
        requestBuilder.addHeader("Host", "eqwww.image.yt");
		return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				String url = requestBuilder.build().urlString();
				if (url.equals(MIRROR_URL)) {
					callback.onFailure(e);
				} else {
					url = url.replace(API_URL, MIRROR_URL);
					requestBuilder.url(url);
					fetchList(currentList, requestBuilder, callback);
				}
			}

			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String responseStr;
					try {
						responseStr = response.body().string();
					} catch (SocketException e) {
						onFailure(response.request(), new IOException("Socket failed"));
						return;
					}

					YTSReponse result;
					try {
						result = mGson.fromJson(responseStr, YTSReponse.class);
					} catch (IllegalStateException e) {
						onFailure(response.request(), new IOException("JSON Failed"));
						return;
					} catch (JsonSyntaxException e) {
                        onFailure(response.request(), new IOException("JSON Failed"));
                        return;
                    }

					if (result.status != null && result.status.equals("error")) {
						callback.onFailure(new NetworkErrorException(result.status_message));
					} else {
						ArrayList<Media> formattedData = result.formatForPopcorn(currentList);
						callback.onSuccess(formattedData);
						return;
					}
				}
				callback.onFailure(new NetworkErrorException("Couldn't connect to YTS"));
			}
		});
	}

	@Override
	public Call getDetail(String videoId, final Callback callback) {
		Request.Builder requestBuilder = new Request.Builder();
		requestBuilder.url(API_URL + "movie_details.json?movie_id=" + videoId);
        requestBuilder.addHeader("Host", "eqwww.image.yt");
		requestBuilder.tag(MEDIA_CALL);

		return enqueue(requestBuilder.build(), new com.squareup.okhttp.Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				callback.onFailure(e);
			}

			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					String responseStr;
					try {
						responseStr = response.body().string();
					} catch (SocketException e) {
						onFailure(response.request(), new IOException("Socket failed"));
						return;
					}

					YTSReponse result;
					try {
						result = mGson.fromJson(responseStr, YTSReponse.class);
					} catch (IllegalStateException e) {
						onFailure(response.request(), new IOException("JSON Failed"));
						return;
					} catch (JsonSyntaxException e) {
                        onFailure(response.request(), new IOException("JSON Failed"));
                        return;
                    }

					if (result.status != null && result.status.equals("error")) {
						callback.onFailure(new NetworkErrorException(result.status_message));
					} else {
						final YTSMovie movie = result.formatDetailForPopcorn();

                        TraktProvider traktProvider = new TraktProvider();

                        TraktProvider.MetaData meta = traktProvider.getSummary(movie.imdbId);
                        if (meta.images != null) {
                            if(meta.images.poster != null) {
                                movie.image = meta.images.poster.replace("/original/", "/medium/");
                                movie.fullImage = meta.images.poster;
                            }

                            if (meta.images != null && meta.images.backdrop != null) {
                                movie.headerImage = meta.images.backdrop.replace("/original/", "/medium/");
                            }
                        } else {
                            movie.fullImage = movie.image;
                            movie.headerImage = movie.image;
                        }


                        if (meta.title != null) {
                            movie.title = meta.title;
                        }

                        if (meta.overview != null) {
                            movie.synopsis = meta.overview;
                        }

                        if (meta.tagline != null) {
                            movie.tagline = meta.tagline;
                        }

                        if (meta.trailer != null) {
                            movie.trailer = meta.trailer;
                        }

                        if (meta.runtime != null) {
                            movie.runtime = Integer.toString(meta.runtime);
                        }

                        if (meta.certification != null) {
                            movie.certification = meta.certification;
                        }

                        final ArrayList<Media> returnData = new ArrayList<>();
                        returnData.add(movie);

                        YSubsProvider subsProvider = new YSubsProvider();
                        subsProvider.getList(movie, new SubsProvider.Callback() {
                            @Override
                            public void onSuccess(Map<String, String> items) {
                                movie.subtitles = items;
                                returnData.set(0, movie);
                                callback.onSuccess(returnData);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onSuccess(returnData);
                            }
                        });

                        return;
					}
				}
				callback.onFailure(new NetworkErrorException("Couldn't connect to YTS"));
			}
		});
	}

	private class YTSReponse {
		public String status;
		public String status_message;
		public LinkedTreeMap<String, Object> data;

		/**
		 * Test if there is an item that already exists
		 *
		 * @param results List with items
		 * @param id Id of item to check for
		 *
		 * @return Return the index of the item in the results
		 */
		private int isInResults(ArrayList<Media> results, String id) {
			int i = 0;
			for (Media item : results) {
				if (item.videoId.equals(id)) return i;
				i++;
			}
			return -1;
		}

		/**
		 * Format data for the application
		 *
		 * @return List with items
		 */
		public YTSMovie formatDetailForPopcorn() {
            YTSMovie movie = new YTSMovie();
            LinkedTreeMap<String, Object> movieObj = data;
            if(movieObj == null) return movie;

            movie.videoId = movieObj.get("id").toString();
            movie.imdbId = (String) movieObj.get("imdb_code");

            movie.title = (String) movieObj.get("title");
            movie.year = movieObj.get("year").toString();
            movie.rating = movieObj.get("rating").toString();
            movie.genre = ((ArrayList<String>) movieObj.get("genres")).get(0);

            ArrayList<LinkedTreeMap<String, Object>> torrents =
                    (ArrayList<LinkedTreeMap<String, Object>>) movieObj.get("torrents");
            if(torrents != null) {
                for (LinkedTreeMap<String, Object> torrentObj : torrents) {
                    String quality = (String) torrentObj.get("quality");
                    if(quality == null || quality.equals("3D")) continue;

                    Media.Torrent torrent = new Media.Torrent();

                    torrent.seeds = torrentObj.get("seeds").toString();
                    torrent.peers = torrentObj.get("peers").toString();
                    torrent.hash = (String) torrentObj.get("hash");
                    try {
                        String magnet = "magnet:?xt=urn:btih:" + torrent.hash + "&amp;dn=" + URLEncoder.encode(movieObj.get("title_long").toString(), "utf-8") + "&amp;tr=http://exodus.desync.com:6969/announce&amp;tr=udp://tracker.openbittorrent.com:80/announce&amp;tr=udp://open.demonii.com:1337/announce&amp;tr=udp://exodus.desync.com:6969/announce&amp;tr=udp://tracker.yify-torrents.com/announce";
                        torrent.url = magnet;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        torrent.url = (String) torrentObj.get("url");
                    }

                    movie.torrents.put(quality, torrent);
                }
            }


            return movie;
		}

		/**
		 * Format data for the application
		 *
		 * @param existingList List to be extended
		 *
		 * @return List with items
		 */
		public ArrayList<Media> formatForPopcorn(ArrayList<Media> existingList) {
            ArrayList<LinkedTreeMap<String, Object>> movies = new ArrayList<>();
            if(data != null) {
                movies = (ArrayList<LinkedTreeMap<String, Object>>) data.get("movies");
            }

			for (LinkedTreeMap<String, Object> item : movies) {
				Movie movie = new YTSMovie();

				movie.videoId = item.get("id").toString();
                movie.imdbId = (String) item.get("imdb_code");

				int existingItem = isInResults(existingList, movie.videoId);
				if (existingItem == -1) {
                    movie.title = (String) item.get("title");
                    movie.year = item.get("year").toString();
                    movie.rating = item.get("rating").toString();
                    movie.genre = ((ArrayList<String>)item.get("genres")).get(0);
                    movie.image = (String) item.get("medium_cover_image");

                    if(movie.image != null) {
                        movie.image = movie.image.replace("medium-cover", "large-cover");
                    }

                    ArrayList<LinkedTreeMap<String, Object>> torrents =
                            (ArrayList<LinkedTreeMap<String, Object>>) item.get("torrents");
                    if(torrents != null) {
                        for (LinkedTreeMap<String, Object> torrentObj : torrents) {
                            String quality = (String) torrentObj.get("quality");
                            if(quality == null || quality.equals("3D")) continue;

                            Media.Torrent torrent = new Media.Torrent();

                            torrent.seeds = torrentObj.get("seeds").toString();
                            torrent.peers = torrentObj.get("peers").toString();
                            torrent.hash = (String) torrentObj.get("hash");
                            try {
                                String magnet = "magnet:?xt=urn:btih:" + torrent.hash + "&amp;dn=" + URLEncoder.encode(item.get("title_long").toString(), "utf-8") + "&amp;tr=http://exodus.desync.com:6969/announce&amp;tr=udp://tracker.openbittorrent.com:80/announce&amp;tr=udp://open.demonii.com:1337/announce&amp;tr=udp://exodus.desync.com:6969/announce&amp;tr=udp://tracker.yify-torrents.com/announce";
                                torrent.url = magnet;
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                torrent.url = (String) torrentObj.get("url");
                            }

                            movie.torrents.put(quality, torrent);
                        }
                    }

                    existingList.add(movie);
				}
			}
			return existingList;
		}
	}

	@Override
    public int getLoadingMessage() {
		return R.string.loading_movies;
	}

    public static class YTSMovie extends Movie {
        public YTSMovie() {
            super();
            mSubsProvider = new YSubsProvider();
        }

        public YTSMovie(Parcel in) {
            super(in);
            mSubsProvider = new YSubsProvider();
        }
    }

}
