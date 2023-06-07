package com.hilmihanif.earthquakeandtsunamihazardzones.ui.notifications


import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color

import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.UniqueValue
import com.arcgismaps.mapping.symbology.UniqueValueRenderer
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.MapView
import com.hilmihanif.earthquakeandtsunamihazardzones.R
import com.hilmihanif.earthquakeandtsunamihazardzones.databinding.FragmentNotificationsBinding
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val mMapView: MapView by lazy {
       binding.KerawananMapView
   }

    private lateinit var mGraphicsOverlay: GraphicsOverlay




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
        }

        lifecycle.addObserver(mMapView)
        setApiKey()


        val map = ArcGISMap(BasemapStyle.ArcGISImageryStandard)

        mGraphicsOverlay = GraphicsOverlay()
        mMapView.graphicsOverlays.add(mGraphicsOverlay)
        addLayer(map)








        return root
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun addLayer(map :ArcGISMap){



        val sumutGempaLayer = FeatureLayer.createWithFeatureTable(ServiceFeatureTable("https://services7.arcgis.com/5U3WUC2hg7PzozWK/arcgis/rest/services/krb_gempa_sumatera_utara_2012/FeatureServer/0"))



        val srFillSymbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color(ContextCompat.getColor(requireContext(),R.color.krb_sangatrendah)) ,null)
        val rFillSYmbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color(ContextCompat.getColor(requireContext(),R.color.krb_rendah)) ,null)
        val sFillSYmbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color(ContextCompat.getColor(requireContext(),R.color.krb_sedang)) ,null)
        val tFillSYmbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color(ContextCompat.getColor(requireContext(),R.color.krb_tinggi)) ,null)


        val sangatRendah = UniqueValue(
            "Kerawanan Gempa Sangat Rendah","Sangat Rendah",srFillSymbol, listOf("Sangat Rendah"))
        val rendah = UniqueValue(
            "Kerawanan Gempa Rendah","Rendah",rFillSYmbol,listOf("Rendah"))
        val sedang = UniqueValue(
            "Kerawanan Gempa Rendah","Sedang",sFillSYmbol,listOf("Sedang"))
        val tinggi = UniqueValue(
            "Kerawanan Gempa Rendah","Tinggi",tFillSYmbol,listOf("Tinggi"))

        val uniqueValueList = listOf(sangatRendah,rendah,sedang,tinggi)

        val fieldNames = listOf("Kerawanan")

        val renderer = UniqueValueRenderer(fieldNames,uniqueValueList)
        sumutGempaLayer.renderer = renderer
        sumutGempaLayer.opacity = 0.2f



        map.operationalLayers.add(sumutGempaLayer)
//        mMapView.setViewpointAnimated(Viewpoint(3.028, 98.905,10000000.0),1f, AnimationCurve.EaseInCirc)
        mMapView.map = map
        mMapView.setViewpoint(Viewpoint(3.028, 98.905,10000000.0))

        lifecycleScope.launch {
            mMapView.onSingleTapConfirmed.collect{
                val wgs84Point = GeometryEngine.projectOrNull(it.mapPoint!!, SpatialReference.wgs84())
                Toast.makeText(context,"lat :${wgs84Point?.y}, long:${wgs84Point?.x}",Toast.LENGTH_SHORT).show()

                val pinLocation = BitmapFactory.decodeResource(resources,R.drawable.placeholder).toDrawable(resources)
                mGraphicsOverlay.graphics.let{graphicList ->
                    val pinLocationSymbol = PictureMarkerSymbol.createWithImage(pinLocation)
                    val pinSize = 60f
                    if (graphicList.isNotEmpty()) graphicList.removeLast()

                    pinLocationSymbol.height = pinSize
                    pinLocationSymbol.width = pinSize
                    pinLocationSymbol.offsetY = pinSize/2
                    val pinLocationGraphic = Graphic(wgs84Point,pinLocationSymbol)
                    graphicList.add(pinLocationGraphic)
                }

            }


        }




    }

    private fun setApiKey(){
        ArcGISEnvironment.apiKey = ApiKey.create(getString(R.string.api_key))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}