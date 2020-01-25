package com.example.myapplication

/**
 * Created by Kriti Dudhiya on 1/25/2020.
 */


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.security.KeyStore
import javax.net.ssl.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener {

            //using okhttp
            callUsingOkhttp()

            //using http url connection
            //callUsingTrustMngr()
        }

    }

    private fun callUsingOkhttp() {
        val clientCertPassword = "D0ba!AMC0r3"

        //step 1. laod the client certificate into the keystore
        val keyStore = KeyStore.getInstance("PKCS12")
        val fs = resources.openRawResource(
            resources.getIdentifier("dubaiamclient",
                "raw", packageName
            ))
        keyStore.load(fs, clientCertPassword.toCharArray())

        //step 2 create keystore factory
        val kmf: KeyManagerFactory = KeyManagerFactory.getInstance("X509")
        kmf.init(keyStore, clientCertPassword.toCharArray())
        val keyManagers: Array<KeyManager> = kmf.keyManagers

        //step 2 create trustmanager factory
        val trustManagerFactory =
            TrustManagerFactory.getInstance("X509")
        trustManagerFactory.init(keyStore)

        //step 4. build SSLcontext using the keystore built
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagers, null, null)

        //logging interceptor
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.HEADERS

        val httpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        val client1: OkHttpClient = httpBuilder

            /*add sslcontext create to the okhttp client to
            enable client present certificate and connect to an https server*/
            .sslSocketFactory(sslContext.socketFactory,
                trustManagerFactory.trustManagers[0] as X509TrustManager)
            .addInterceptor(logInterceptor)
            .build()

        //create req body
        val json = JSONObject()
        json.put("Captcha_Token", "")
        json.put("Password", "tgt")
        json.put("UserName", "058 0001122")
        val reqBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        //create request
        val req = Request.Builder()
            .header("Ocp-Apim-Subscription-Key", "72c6834956144b93ac156a7bcddfe1eb")
            .url("https://dubaiam-apigateway-qa.azure-api.net/QA/api/Profile/VerifyMobile")
            .post(reqBody)
            .build()

        Thread(Runnable {
            //calling api on worker thread and print response
            val response = client1.newCall(req).execute()
            println("response string : ${response.body?.string()}")
        }).start()
    }

    /*private fun callUsingTrustMngr() {

        //reqbody json
        val json = JSONObject()
        json.put("Captcha_Token", "")
        json.put("Password", "tgt")
        json.put("UserName", "058 0001122")

        //step 1. laod the client certificate into the keystore
        val clientCertPassword = "D0ba!AMC0r3"
        val keyStore = KeyStore.getInstance("PKCS12")
        val fs = resources.openRawResource(
            resources.getIdentifier("dubaiamclient",
                "raw", packageName
            ))
        keyStore.load(fs, clientCertPassword.toCharArray())


        //step 2. build SSLcontext using the keystore built
        val kmf: KeyManagerFactory = KeyManagerFactory.getInstance("X509")
        kmf.init(keyStore, clientCertPassword.toCharArray())
        val keyManagers: Array<KeyManager> = kmf.keyManagers
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagers, null, null)


        Thread(Runnable {

            //step.3 - call api using HTTPURlConnection with SSLContext created
            var urlConnection: HttpURLConnection? = null


            try {
                val requestedUrl =
                    URL("https://dubaiam-apigateway-qa.azure-api.net/QA/api/Profile/VerifyMobile")
                urlConnection = requestedUrl.openConnection() as HttpURLConnection
                if (urlConnection is HttpsURLConnection) {
                    urlConnection.sslSocketFactory = sslContext.socketFactory
                }
                urlConnection.addRequestProperty("Ocp-Apim-Subscription-Key", "72c6834956144b93ac156a7bcddfe1eb")
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type","application/json")
                urlConnection.outputStream.write( json.toString().toByteArray() )

                val lastResponseCode = urlConnection.responseCode
                println("http lastResponseCode : $lastResponseCode")

                if(urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    println("http success : ${getString(urlConnection.inputStream)}")
                }else {
                    println("http error : ${getString(urlConnection.errorStream)}")
                }

            } catch (ex: Exception) {
                val error = ex.toString()
                println("http exception : $error")
            } finally {
                urlConnection?.disconnect()
            }
        }).start()

    }

    private fun getString(inputStream : InputStream) : StringBuffer {
        val `in` = BufferedReader(
            InputStreamReader(
                inputStream
            )
        )
        var inputLine: String?
        val response = StringBuffer()

        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        `in`.close()
        return response
    }*/

}
