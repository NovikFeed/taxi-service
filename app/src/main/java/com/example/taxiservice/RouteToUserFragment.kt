package com.example.taxiservice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.SharedPreferences
import android.preference.PreferenceManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Text
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.IOException

class RouteToUserFragment : BottomSheetDialogFragment() {
    private lateinit var viewModel : InZoneViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPReference = SharedPreferenceManager(requireContext())
        viewModel = ViewModelProvider(this, ViewModelWithSharedPReferenceFactory(requireActivity().application,
            sharedPReference)).get(InZoneViewModel::class.java)
        val driverUID = arguments?.getString("driverUID")
        val passengerCoord = arguments?.getDoubleArray("passengerCoord")
        viewModel.setupGeoQuery(driverUID!!, passengerCoord!!)
        return ComposeView(requireContext()).apply {
            setContent {
                setView(viewModel)
            }
        }
    }
}
@Composable
fun setView(viewModel: InZoneViewModel = viewModel()){
    val statusFragment by viewModel.fragmentVariant.observeAsState(false)
    Column(modifier = Modifier.fillMaxSize()) {
        if(!statusFragment){
            setViewGoToPassenger()
        }
        else{
            setViewGoWithPassenger()
        }
    }
}
@Composable
//@Preview
fun setViewGoToPassenger(viewModel: InZoneViewModel = viewModel()){
    val inZone by viewModel.inZone.observeAsState(false)
    val taxiColor = colorResource(id = R.color.taxi)
    Row{
        Surface(modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.extraLarge),
                shadowElevation = 8.dp,
                color = taxiColor,
                content = {
                    Column {
                        Canvas(modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(0.dp, 16.dp),
                            onDraw = {
                            drawLine(
                                color = Color.White,
                                start = Offset(480f,20f),
                                end= Offset(600f,20f),
                                strokeWidth = 30f,
                                cap= StrokeCap.Round
                            )
                        } )
                            Button(onClick = {
                                viewModel.unistalGeoQuerry()
                                viewModel.changeOrderStatus( "Active")
                                viewModel.setupFragmnetVariant()
                                             },
                                modifier = Modifier
                                    .fillMaxHeight(0.28f)
                                    .fillMaxWidth(1f)
                                    .padding(10.dp),
                                shape = RectangleShape,
                                enabled = inZone
                            ) {
                                Text(text = "In place",
                                    style = TextStyle(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight(600),
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                    }
                })
    }
}
 @Composable
fun setViewGoWithPassenger(){

}