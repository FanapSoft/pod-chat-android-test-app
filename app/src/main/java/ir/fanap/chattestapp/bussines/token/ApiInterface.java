package ir.fanap.chattestapp.bussines.token;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;


/**
 * Created by Sepehr on 1/25/2017.
 */

public interface ApiInterface {

    @POST("otp/handshake/")
    @FormUrlEncoded
    Call<HandShakeRes> handShake(@Field("deviceUID") String deviceid);

    @POST("otp/handshake")
    Call<HandShakeRes> handShake(@Query("deviceUID") String deviceid,
                                 @Query("deviceName") String deviceName ,
                                 @Query("deviceOsVersion")String deviceOsVersion ,
                                 @Query("deviceOs")String deviceOs ,
                                 @Query("deviceType")String deviceType);



    @POST("otp/authorize/")
    @FormUrlEncoded
    Call<LoginRes> login(@Field("identity") String number, @Field("keyId") String keyId);




    @POST("otp/verify")
    @FormUrlEncoded
    Call<VerifyRes> verifyNumber(@Field("keyId") String keyId
            , @Field("identity") String number
            , @Field("verifyCode") String verifyCode
    );


    @POST("accessToken/")
    @FormUrlEncoded
    Call<SSoTokenRes> getOTPToken(@Field("code") String code);


    @POST("refreshToken/")
    @FormUrlEncoded
    Call<SSoTokenRes> refreshToken(
            @Field("refreshToken") String refreshToken
    );

}