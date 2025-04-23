package com.ferd.foodiegram.data.supabase;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseStorageApi {
    @Multipart
    @POST("storage/v1/object/{bucket}/{fileName}")
    Call<Void> uploadImage(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Path("bucket") String bucket,
            @Path(value = "fileName", encoded = true) String fileName,  // <-- encoded=true
            @Query("upsert") boolean upsert,                            // <-- nuevo parámetro
            @Part MultipartBody.Part file
    );

    @DELETE("storage/v1/object/{bucket}/{fileName}")
    Call<Void> deleteImage(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Path("bucket") String bucket,
            @Path(value = "fileName", encoded = true) String fileName
    );
}
