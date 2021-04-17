package com.tassiecomp.airPolution.retrofit

import android.util.Log
import com.tassiecomp.airPolution.utils.API
import com.tassiecomp.airPolution.utils.isJsonArray
import com.tassiecomp.airPolution.utils.isJsonObject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    //레트로핏 클라이언트 선언
    private var retrofitClient: Retrofit? = null

    //레트로핏 클라이언트 가져오기
    fun getClient(baseUrl: String):Retrofit?{
        Log.d("TAG","RetrofitClient - getClient()called")

        //logging interceptor 추가
        //왜추가하나 지금 로그로 모두 확인이되 지않는다. 그래서 인터셉터로 API와 서버사이 진행상황을 보기위해

        //okhttp 인스턴스 생성
        val client = OkHttpClient.Builder()

        //로그를 찍기위해 로깅 인터셉터 추가
        val loggingIntercepter = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger{
            override fun log(message: String) {
//                Log.d("TAG","RetrofitClient - log() called / message : $message")

                when {
                    message.isJsonObject() ->
                        Log.d("TAG", JSONObject(message).toString(4))

                    message.isJsonArray() ->
                        Log.d("TAG", JSONObject(message).toString(4))
                    else -> {
                        try{
                            Log.d("TAG", JSONObject(message).toString(4))
                        } catch (e:Exception) {
                            Log.d("TAG", message)
                        }
                    }
                }
            }



        })

        loggingIntercepter.setLevel(HttpLoggingInterceptor.Level.BODY)

        //위에서 설정한 로깅 인터셉터를 okhttp클라이언트에 추가한다.
        client.addInterceptor(loggingIntercepter)

        //기본 parameter intercepter 생성

        val baseParameterInterceptor: Interceptor = (object : Interceptor {

            override fun intercept(chain: Interceptor.Chain): Response {
                Log.d("TAG","RetrofitClient - intercept() called")
                //오리지널 request
                val originalRequest = chain.request()

                //?client_id= asdf 이걸 해주는 과정
                //query parameter 추가하기
                val addedUrl = originalRequest.url.newBuilder().addQueryParameter("client_id", API.CLIENT_ID).build()

                val finalRequest = originalRequest.newBuilder()
                    .url(addedUrl)
                    .method(originalRequest.method,originalRequest.body)
                    .build()
                return chain.proceed(finalRequest)
            }
        })
        //위에서 설정한 기본parameter intercepter를 okhttp클라이언트에 추가한다.
        client.addInterceptor(baseParameterInterceptor)

        //커넥션 타임아웃
        client.connectTimeout(10, TimeUnit.SECONDS)
        client.readTimeout(10,TimeUnit.SECONDS)
        client.writeTimeout(10,TimeUnit.SECONDS)
        client.retryOnConnectionFailure(true)





        if (retrofitClient ==null) { //레트로핏 클라이언트 유무 확인

            retrofitClient = Retrofit.Builder()
                .baseUrl(baseUrl) //위에 getclient에 변수를 가져온다.
                .addConverterFactory(GsonConverterFactory.create()) //Gson converter추가
                //위에서 설정한 클라이언트로 레트로핏 클라이언트를 설정한다.
                .client(client.build())
                .build()

        }
        return retrofitClient
    }
}