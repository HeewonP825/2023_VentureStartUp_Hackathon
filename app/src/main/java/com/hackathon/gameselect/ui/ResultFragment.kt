package com.hackathon.gameselect.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.hackathon.gameselect.R
//import kotlinx.android.synthetic.main.fragment_example.* // UI 요소를 참조하기 위해 임포트
import okhttp3.*
import java.io.IOException

data class ResponseData(val success: Boolean, val data: Data)

data class Data(val games: List<String>, val comment: String)

class ResultFragment : Fragment() {

}
