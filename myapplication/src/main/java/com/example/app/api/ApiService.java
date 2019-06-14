package com.example.app.api;

import com.example.app.bean.Bean;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by DVO on 2017/7/20 0020.
 */

public interface ApiService {

    @GET("福利/{count}/{page}")
    Observable<Bean> getMsg(@Path("count") int count, @Path("page") int page);

}
