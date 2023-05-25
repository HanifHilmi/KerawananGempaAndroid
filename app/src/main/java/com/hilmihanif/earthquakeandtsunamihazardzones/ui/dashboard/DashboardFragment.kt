package com.hilmihanif.earthquakeandtsunamihazardzones.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.view.MapView
import com.hilmihanif.earthquakeandtsunamihazardzones.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {


    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val mapView:MapView by lazy {
        binding.mainMapView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


//        val textView: TextView = binding.textDashboard

        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it

        }

        lifecycle.addObserver(mapView)
        setApiKey()
        viewLifecycleOwner.lifecycleScope.launch {
            setupMap()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private suspend fun setupMap(){
        val map = ArcGISMap(BasemapStyle.ArcGISTopographic)

        mapView.map = map

        mapView.setViewpointAnimated(Viewpoint(34.0270, -118.8050, 72000.0))
    }

    private fun setApiKey(){
        ArcGISEnvironment.apiKey = ApiKey.create("AAPK6aaf3da59c354a9fbce50c5b4bcf77adu21eYG07ytwEhmwxrRKmW_ugKHZJFPLLqmqd9TsylgypJ7_KdgJ687b_2X20rOPu")
    }
    private fun showError(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
        Log.e(this.javaClass.name,message)
    }
}