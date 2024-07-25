package com.example.taxiservice

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.Layout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Text
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class RouteToUserFragment : BottomSheetDialogFragment() {
    private  lateinit var viewModel : InZoneViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreference = SharedPreferenceManager(requireContext())
        val repository = TaxiRepository()
        if(requireActivity().isDestroyed){
            Log.d("coord", "yes")
        }
        viewModel = ViewModelProvider(this,ViewModelWithSharedPReferenceFactory(requireActivity().application,
            sharedPreference, repository))[InZoneViewModel::class.java]
        val orderUID = sharedPreference.getStringData("currentOrderUID")
        return ComposeView(requireContext()).apply {
            setContent {
                setView(viewModel, orderUID!!, requireContext())
            }
        }
    }
}
@Composable
fun setView(viewModel: InZoneViewModel = viewModel(), orderUID: String, context: Context){
    val statusFragment by viewModel.getOrder(orderUID).observeAsState()
    statusFragment?.let {order ->
        Column(modifier = Modifier.fillMaxSize()) {
            viewModel.handleOrderStatus(order)
            when(order.status){
                "open" ->{
                    setViewGoToPassenger()
                }
                "Active" ->{
                    setViewGoWithPassenger()
                }
                "close" ->{
                    setViewFinish(viewModel,context)
                }
            }
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
fun setViewGoWithPassenger(viewModel : InZoneViewModel = viewModel()){
     val inZone by viewModel.inZone.observeAsState(false)
     val taxiColor = colorResource(id = R.color.taxi)
     Column {
         Surface(modifier = Modifier
             .fillMaxSize()
             .clip(MaterialTheme.shapes.extraLarge),
             shadowElevation = 8.dp,
             color = taxiColor,
             content = {
                 Column (modifier = Modifier
                     .fillMaxSize()
                     .align(Alignment.CenterHorizontally)){
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
                     Button(modifier = Modifier
                         .fillMaxWidth(1f)
                         .fillMaxHeight(0.28f)
                         .padding(10.dp), onClick = {
                                                    viewModel.changeOrderStatus("close")
                                                    viewModel.unistalGeoQuerry()

                     }, shape = RectangleShape, enabled = inZone
                     ) {
                         Text(
                             text = "In place", style = TextStyle(
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
fun setViewFinish(viewModel: InZoneViewModel = viewModel(), context: Context){
    val cost by viewModel.costTrip.observeAsState()
    val taxiColor = colorResource(id = R.color.taxi)
    val style = TextStyle(
        fontSize = 30.sp,
        fontWeight = FontWeight(600),
        fontFamily = FontFamily.Monospace,
        color = Color.Black,
    )
    Surface(modifier = Modifier
        .fillMaxSize()
        .clip(MaterialTheme.shapes.extraLarge),
        shadowElevation = 8.dp,
        color = taxiColor,
        content = {
            Column (modifier = Modifier
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(0.dp, 16.dp),
                    onDraw = {
                        drawLine(
                            color = Color.White,
                            start = Offset(480f, 20f),
                            end = Offset(600f, 20f),
                            strokeWidth = 30f,
                            cap = StrokeCap.Round
                        )
                    })
                Text(text = "Finished", style = style, modifier = Modifier.padding(0.dp, 10.dp))
                Text(text = cost!!, style = style, textAlign = TextAlign.Center)
                Button(onClick = {
                                 viewModel.changeOrderStatus("done")
                                viewModel.setOrder()
                                viewModel.restartApplication(context)
                }, modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.32f)
                    .padding(10.dp), shape = RectangleShape) {
                    Text(text = "Done", style = style)
                }
            }
        })
}