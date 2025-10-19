package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed)
            throws BreedNotFoundException, IOException {
        // TODO Task 1: Complete this method based on its provided documentation
        //      and the documentation for the dog.ceo API. You may find it helpful
        //      to refer to the examples of using OkHttpClient from the last lab,
        //      as well as the code for parsing JSON responses.
        // return statement included so that the starter code can compile and run.
        if (breed == null || breed.isBlank()) {
            throw new BreedNotFoundException("Breed is null or blank");
        }
        String safe = breed.trim().toLowerCase();
        String url = "https://dog.ceo/api/breed/" + safe + "/list";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error");
            }
            if (response.body() == null) {
                throw new IOException("Empty response");
            }
            String body = response.body().string();
            final JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (JSONException e) {
                throw new IOException(e);
            }
            final String status;
            try {
                status = json.getString("status");
            } catch (JSONException e) {
                throw new IOException(e);
            }
            if ("success".equals(status)) {
                final JSONArray array;
                try {
                    array = json.getJSONArray("message");
                } catch (JSONException e) {
                    throw new IOException(e);
                }
                List<String> result = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    result.add(array.getString(i));
                }
                return result;
            } else if ("error".equals(status)) {
                final String message;
                try {
                    message = json.getString("message");
                } catch (JSONException e) {
                    throw new IOException(e);
                }
                if (message.contains("Breed not found")) {
                    throw new BreedNotFoundException(breed);
                }
                throw new IOException(message);
            } else {
                throw new IOException(status);
            }
        }
    }
}