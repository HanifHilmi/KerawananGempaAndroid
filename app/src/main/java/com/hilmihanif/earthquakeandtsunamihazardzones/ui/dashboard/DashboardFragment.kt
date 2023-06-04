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
import com.arcgismaps.Color
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.AnimationCurve
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.MapView
import com.hilmihanif.earthquakeandtsunamihazardzones.R
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

        viewLifecycleOwner.lifecycleScope.launch {
            setupMap()
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
    }

    private suspend fun setupMap(){
        // setup API key
        ArcGISEnvironment.apiKey = ApiKey.create(getString(R.string.api_key))

        // Display Map to Mapview
        val map = ArcGISMap(BasemapStyle.ArcGISImageryStandard).apply {

        }
        mapView.map = map


        val nwPoint = Point(93.699897,6.657766)
        val sePoint = Point(106.460050,-8.209479)
        val batasMap = Envelope(nwPoint,sePoint)





        // set ViewPoint
        //mapView.setViewpointAnimated(Viewpoint(3.028, 98.905, 10000000.0))

        mapView.setViewpointAnimated(Viewpoint(3.028, 98.905,10000000.0),1f,AnimationCurve.EaseInCirc)
        addPointGraphics()
        //map.maxExtent = batasMap

    }

    private fun addPointGraphics(){
        val graphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)
        val nwPoint = Point(93.699897,6.657766 , SpatialReference.wgs84())

        val simpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle,Color.red)

        // create a blue outline symbol and assign it to the outline property of the simple marker symbol
        val blueOutlineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 2f)
        simpleMarkerSymbol.outline = blueOutlineSymbol

        val pointGraphic = Graphic(nwPoint,simpleMarkerSymbol)

        graphicsOverlay.graphics.add(pointGraphic)

    }

    private fun showError(message:String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
        Log.e(this.javaClass.name,message)
    }
}