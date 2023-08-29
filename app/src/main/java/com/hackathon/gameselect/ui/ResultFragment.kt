package com.hackathon.gameselect.ui
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.hackathon.gameselect.databinding.FragmentResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ResponseData(val success: Boolean, val data: Data)

data class Data(val games: List<String>, val comment: String)

class ResultFragment : Fragment() {

    private lateinit var binding: FragmentResultBinding // View Binding 변수 선언

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        Toast.makeText(requireContext(), "잠시만 기다려 주세요", Toast.LENGTH_SHORT).show()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS) // 연결 timeout
            .readTimeout(30, TimeUnit.SECONDS)    // 읽기 timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // 쓰기 timeout
            .build()


        val request = Request.Builder()
            .url("http://172.16.72.33:9078") // 실제 API URL로 변경
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("onFailure","${e}")
                // 오류 처리
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e("response","${response}")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.e("responseBody","${responseBody}")
                    if (responseBody != null) {
                        val gson = Gson()
                        val responseData = gson.fromJson(responseBody, ResponseData::class.java)

                        requireActivity().runOnUiThread {
                            // 받아온 데이터를 사용하여 UI 업데이트
                            val games = responseData.data.games
                            val comment = responseData.data.comment

                            // UI 업데이트
                            binding.gameName.text = games.joinToString(", ")
                            binding.commentTextView.text = comment
                        }
                    }
                }
                else {
                    Log.e("실패","")
                }
            }
        })
    }
}

