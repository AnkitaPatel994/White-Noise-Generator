package com.iteration.relaxio.network;

import com.iteration.relaxio.model.Message;
import com.iteration.relaxio.model.SoundList;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GetProductDataService {

    @GET("relaxio_api/sound.php")
    Call<SoundList> getSoundData();

    @FormUrlEncoded
    @POST("relaxio_api/token.php")
    Call<Message> getTokenData(@Field("wifi_mac") String wifi_mac,
                               @Field("token") String token);

}
