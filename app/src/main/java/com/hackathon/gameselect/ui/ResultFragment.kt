package com.hackathon.gameselect.ui
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.hackathon.gameselect.databinding.FragmentResultBinding
import okhttp3.*
import java.io.IOException

data class ResponseData(val success: Boolean, val data: Data)

data class Data(val games: List<String>, val comment: String)

class ResultFragment : Fragment() {

    private lateinit var binding: FragmentResultBinding // View Binding 변수 선언

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://172.16.72.33:9078") // 실제 API URL로 변경
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 오류 처리
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
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
            }
        })
    }
}
