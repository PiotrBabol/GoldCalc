package sda.code.gold.Main;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import sda.code.gold.GoldPrice;

import java.util.List;


public interface GoldInterface {

    @GET("api/cenyzlota/{path1}/{path2}/")
    Call<List<GoldPrice>> calling (
            @Path("path1") String date1,
            @Path("path2") String date2,
            @Query("/?format=json") String json

    );

}
