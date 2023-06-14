package com.hilmihanif.earthquakeandtsunamihazardzones.ui.kerawanan


import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import com.arcgismaps.data.ArcGISFeature

import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleRenderer
import com.arcgismaps.mapping.symbology.UniqueValue
import com.arcgismaps.mapping.symbology.UniqueValueRenderer
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.IdentifyLayerResult
import com.arcgismaps.mapping.view.MapView
import com.arcgismaps.tasks.geocode.LocatorTask
import com.arcgismaps.tasks.geocode.ReverseGeocodeParameters
import com.hilmihanif.earthquakeandtsunamihazardzones.R
import com.hilmihanif.earthquakeandtsunamihazardzones.databinding.FragmentKerawananBinding
import kotlinx.coroutines.launch

class KerawananFragment : Fragment() {

    companion object {
        val TEST_LOG = "Test Identifier"
    }

    private var _binding: FragmentKerawananBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val mMapView: MapView by lazy {
        binding.KerawananMapView
    }

    private lateinit var mGraphicsOverlay: GraphicsOverlay

    private lateinit var testlayer: FeatureLayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val kerawananViewModel =
            ViewModelProvider(this).get(KerawananViewModel::class.java)

        _binding = FragmentKerawananBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textNotifications
        kerawananViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
        }

        lifecycle.addObserver(mMapView)
        setApiKey()

        val baseMap = ArcGISMap(BasemapStyle.ArcGISImageryStandard).apply {
            maxExtent = Envelope(91.404757,-8.65,109.586,7.956929, spatialReference = SpatialReference.wgs84())
        }
        mMapView.map = baseMap
        mGraphicsOverlay = GraphicsOverlay()
        mMapView.graphicsOverlays.add(mGraphicsOverlay)

        mMapView.setViewpoint(Viewpoint(3.028, 98.905, 10000000.0))

        addFaultModelLayer(baseMap)
        addKerawananLayer(baseMap)

        return root
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun addKerawananLayer(baseMap: ArcGISMap) {


        val sumutGempaLayer =
            FeatureLayer.createWithFeatureTable(ServiceFeatureTable("https://services7.arcgis.com/5U3WUC2hg7PzozWK/arcgis/rest/services/krb_gempa_sumatera_utara_2012/FeatureServer/0"))


        val srFillSymbol = SimpleFillSymbol(
            SimpleFillSymbolStyle.Solid,
            Color(ContextCompat.getColor(requireContext(), R.color.krb_sangatrendah)),
            null
        )
        val rFillSYmbol = SimpleFillSymbol(
            SimpleFillSymbolStyle.Solid,
            Color(ContextCompat.getColor(requireContext(), R.color.krb_rendah)),
            null
        )
        val sFillSYmbol = SimpleFillSymbol(
            SimpleFillSymbolStyle.Solid,
            Color(ContextCompat.getColor(requireContext(), R.color.krb_sedang)),
            null
        )
        val tFillSYmbol = SimpleFillSymbol(
            SimpleFillSymbolStyle.Solid,
            Color(ContextCompat.getColor(requireContext(), R.color.krb_tinggi)),
            null
        )


        val sangatRendah = UniqueValue(
            "Kerawanan Gempa Sangat Rendah", "Sangat Rendah", srFillSymbol, listOf("Sangat Rendah")
        )
        val rendah = UniqueValue(
            "Kerawanan Gempa Rendah", "Rendah", rFillSYmbol, listOf("Rendah")
        )
        val sedang = UniqueValue(
            "Kerawanan Gempa Rendah", "Sedang", sFillSYmbol, listOf("Sedang")
        )
        val tinggi = UniqueValue(
            "Kerawanan Gempa Rendah", "Tinggi", tFillSYmbol, listOf("Tinggi")
        )

        val uniqueValueList = listOf(sangatRendah, rendah, sedang, tinggi)

        val fieldNames = listOf("Kerawanan")

        val renderer = UniqueValueRenderer(fieldNames, uniqueValueList)
        sumutGempaLayer.renderer = renderer
        sumutGempaLayer.opacity = 0.5f



        baseMap.operationalLayers.add(sumutGempaLayer)
//        mMapView.setViewpointAnimated(Viewpoint(3.028, 98.905,10000000.0),1f, AnimationCurve.EaseInCirc)

        setTapPinLocation(sumutGempaLayer)


    }

    private fun addFaultModelLayer(baseMap: ArcGISMap) {
        val faultModelLayer =
            FeatureLayer.createWithFeatureTable(ServiceFeatureTable("https://services7.arcgis.com/5U3WUC2hg7PzozWK/arcgis/rest/services/patahan_tektonik_fixed/FeatureServer/0"))

        val lineSymbol = SimpleLineSymbol(
            SimpleLineSymbolStyle.Solid,
            Color(ContextCompat.getColor(requireContext(), R.color.orange)),
            2.0f
        )

        val renderer = SimpleRenderer(lineSymbol)
        faultModelLayer.renderer = renderer
        faultModelLayer.opacity = 0.75f

        baseMap.operationalLayers.add(faultModelLayer)
    }

    private fun setTapPinLocation(layer: FeatureLayer) {
        lifecycleScope.launch {
            mMapView.onSingleTapConfirmed.collect {
                val wgs84Point =
                    GeometryEngine.projectOrNull(it.mapPoint!!, SpatialReference.wgs84())


                val pinLocation = BitmapFactory.decodeResource(resources, R.drawable.placeholder)
                    .toDrawable(resources)

                val reverseGeocodeMapResult = reverseGeocodingOrNull(it.mapPoint)



                mGraphicsOverlay.graphics.let { graphicList ->
                    val pinLocationSymbol = PictureMarkerSymbol.createWithImage(pinLocation)
                    val pinSize = 50f
                    if (graphicList.isNotEmpty()) graphicList.removeLast()

                    pinLocationSymbol.height = pinSize
                    pinLocationSymbol.width = pinSize
                    pinLocationSymbol.offsetY = pinSize / 2
                    val pinLocationGraphic = Graphic(wgs84Point, pinLocationSymbol)
                    graphicList.add(pinLocationGraphic)


                    // identify layer value
                    val identifyLayerResultFuture = mMapView.identifyLayer(
                        layer,
                        mMapView.locationToScreen(it.mapPoint!!),
                        10.0,
                        false,
                        1
                    )
                    val identifyLayerResult: IdentifyLayerResult =
                        identifyLayerResultFuture.getOrNull()!!
                    val geoElementList = identifyLayerResult.geoElements


                    var kerawanan = ""
                    Log.d("TestIdentifier", geoElementList.toString())
                    Log.d("TestIdentifier", " LongLat :${wgs84Point?.x},${wgs84Point?.y}")
                    for (element in geoElementList) {
                        if (element is ArcGISFeature) {
                            Log.d("TestIdentifier", element.attributes.toString())
                            kerawanan = element.attributes.getValue("Kerawanan").toString()
                        } else {
                            Log.d("TestIdentifier", "bukan raster")
                        }
                    }



                    Toast.makeText(
                        context,
                        "Kerawanan $kerawanan lat :${wgs84Point?.y}, long:${wgs84Point?.x}",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            }

        }

    }

    private fun reverseGeocodingOrNull(mapPoint: Point?): Map<String,Any>?{
        var mapList : Map<String,Any>? = null
        viewLifecycleOwner.lifecycleScope.launch {
            val locatorTask =
                LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer").apply {
                    apiKey = ApiKey.create(getString(R.string.api_key))
                }

            val geocodeList =
                locatorTask.reverseGeocode(mapPoint!!, ReverseGeocodeParameters().apply {
                    outputSpatialReference = mMapView.spatialReference.value
                }).getOrNull()

            if (geocodeList!!.isNotEmpty()) {
                val geocode = geocodeList[0]
                Log.d(TEST_LOG, "geocode atrributes : ${geocode.attributes.toString()}")
                mMapView.setViewpointCenter(geocode.displayLocation!!)
                mapList =  geocode.attributes
            } else {
                Log.d(TEST_LOG, "geocode atrributes empty")
            }
        }
        return mapList
    }


    private fun setApiKey() {
        ArcGISEnvironment.apiKey = ApiKey.create(getString(R.string.api_key))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}