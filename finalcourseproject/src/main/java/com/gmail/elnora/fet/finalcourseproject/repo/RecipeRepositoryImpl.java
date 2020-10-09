package com.gmail.elnora.fet.finalcourseproject.repo;

import com.gmail.elnora.fet.finalcourseproject.data.RecipeDataModel;
import com.gmail.elnora.fet.finalcourseproject.data.data_converter.RecipesDataModelConverter;

import java.io.IOException;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecipeRepositoryImpl implements RecipeRepository {

    private static final String API_KEY = "b02c4852aab443b183f8d791cc2ccace";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private RecipesDataModelConverter recipesDataModelConverter;

    public RecipeRepositoryImpl(OkHttpClient okHttpClient, RecipesDataModelConverter recipesDataModelConverter) {
        this.okHttpClient = okHttpClient;
        this.recipesDataModelConverter = recipesDataModelConverter;
    }

    private Request builtRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=100-1500")
                .build();
    }

    private void createResponse(SingleEmitter<String> emitter, Request request) throws IOException {
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            emitter.onError(new Throwable(String.valueOf(response.code())));
        } else if (response.body() == null) {
            emitter.onError(new Throwable(new NullPointerException("Body is null")));
        } else {
            emitter.onSuccess(response.body().string());
        }
    }

    @Override
    public Single<List<RecipeDataModel>> getRecipesByType(String type) {
        String url = "https://api.spoonacular.com/recipes/complexSearch/?query=" +
                type.toLowerCase() + "&addRecipeInformation=true&apiKey=" + API_KEY;
        Request request = builtRequest(url);
        return Single.create((SingleOnSubscribe<String>) emitter -> createResponse(emitter, request))
                .subscribeOn(Schedulers.io())
                .map(new Function<String, List<RecipeDataModel>>() {
                    @Override
                    public List<RecipeDataModel> apply(String jsonData) throws Exception {
                        return recipesDataModelConverter.fromJsonToRecipeListConverter(jsonData);
                    }
                });
    }

}