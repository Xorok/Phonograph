package com.kabouzeid.gramophone.lastfm.artist;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.lastfm.LastFMUtil;
import com.kabouzeid.gramophone.provider.ArtistJSONStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 24.12.14.
 */
public class LastFMArtistInfoUtil {
    public static final String TAG = LastFMArtistInfoUtil.class.getSimpleName();

    private static String AUTO_CORRECT = "1";

    public static String getArtistUrl(String artist) {
        if (artist != null) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority(LastFMUtil.BASE_URL)
                    .appendPath("2.0")
                    .appendQueryParameter("method", "artist.getinfo")
                    .appendQueryParameter("artist", artist)
                            //.appendQueryParameter("lang", "de")
                    .appendQueryParameter("autocorrect", AUTO_CORRECT)
                    .appendQueryParameter("api_key", LastFMUtil.API_KEY)
                    .appendQueryParameter("format", "json");
            return builder.build().toString();
        }
        return "";
    }

    public static String getArtistNameFromJSON(JSONObject rootJSON) {
        try {
            return rootJSON.getJSONObject("artist").getString("name");
        } catch (JSONException e) {
            //Log.e(TAG, "Error while getting artist name from JSON parameter!", e);
            return "";
        }
    }

    public static String getArtistThumbnailUrlFromJSON(JSONObject rootJSON) {
        try {
            JSONArray images = getArtistImageArrayFromJSON(rootJSON);
            if (images.length() > 2) {
                return images.getJSONObject(2).getString("#text");
            } else if (images.length() > 1) {
                return images.getJSONObject(1).getString("#text");
            }
            return images.getJSONObject(0).getString("#text");
        } catch (JSONException | NullPointerException e) {
            //Log.e(TAG, "Error while getting artist thumbnail image from JSON parameter!", e);
            return "";
        }
    }

    public static JSONArray getArtistImageArrayFromJSON(JSONObject rootJSON) {
        try {
            return rootJSON.getJSONObject("artist").getJSONArray("image");
        } catch (JSONException e) {
            //Log.e(TAG, "Error while getting artist image array from JSON parameter!", e);
            return null;
        }
    }

    public static String getArtistImageUrlFromJSON(JSONObject rootJSON) {
        try {
            JSONArray images = getArtistImageArrayFromJSON(rootJSON);
            return images.getJSONObject(images.length() - 1).getString("#text");
        } catch (JSONException | NullPointerException e) {
            //Log.e(TAG, "Error while getting artist image from JSON parameter!", e);
            return "";
        }
    }

    public static String getArtistBiographyFromJSON(JSONObject rootJSON) {
        try {
            return rootJSON.getJSONObject("artist").getJSONObject("bio").getString("content");
        } catch (JSONException e) {
            //Log.e(TAG, "Error while getting artist biography from JSON parameter!", e);
            return "";
        }
    }

    public static void saveArtistJSONDataToCacheAndDisk(Context context, String artist, JSONObject jsonObject) {
        ArtistJSONStore.getInstance(context).removeItem(artist);
        ArtistJSONStore.getInstance(context).addArtistJSON(artist, jsonObject.toString());
    }

    public static void downloadArtistJSON(final Context context, final String artist, final Response.Listener<JSONObject> callback) {
        App app = (App) context.getApplicationContext();
        String artistUrl = LastFMArtistInfoUtil.getArtistUrl(artist);
        JsonObjectRequest artistInfoJSONRequest = new JsonObjectRequest(0, artistUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                LastFMArtistInfoUtil.saveArtistJSONDataToCacheAndDisk(context, artist, response);
                callback.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Download failed!", error);
            }
        });
        app.addToVolleyRequestQueue(artistInfoJSONRequest);
    }

}