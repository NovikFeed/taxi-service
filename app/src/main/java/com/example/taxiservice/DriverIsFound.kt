package com.example.taxiservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.fragment.app.Fragment
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.rpc.context.AttributeContext.Resource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.fragment.app.viewModels
import androidx.wear.compose.material.Button


class DriverIsFound : BottomSheetDialogFragment() {
    private val viewModel :DriverIsFoundViewModel by viewModels{
        DriverIsFoundViewModelFactory(TaxiRepository())
    }
    private lateinit var sharedPreference : SharedPreferenceManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreference = SharedPreferenceManager(requireContext())
        viewModel.setPhoneNumber(sharedPreference.getStringData("driverUID")!!)
//        checkVariantFragment()
        return ComposeView(requireContext()).apply {
            val uid = arguments?.getString("currentOrder")
            setContent {
                setCustomContent(viewModel, uid!!, requireContext())
            }
        }
    }
//    private fun checkVariantFragment(){
//        val info = arguments?.getString("info")
//        Log.i("coord", info.toString())
//        if(info != null){
//            viewModel.setFragmentWithPassenger()
//        }
//        else{
//            viewModel.setFragmentToPassenger()
//        }
//    }
}

@Composable
fun setCustomContent(viewModel: DriverIsFoundViewModel, uid: String, context: Context){
    val status by viewModel.getOrder(uid).observeAsState()
    status?.let{order ->
        Surface(modifier = Modifier
            .fillMaxSize()
            .height(100.dp)
            .clip(MaterialTheme.shapes.extraLarge),
            shadowElevation = 8.dp,
            color = colorResource(id = R.color.taxi),
            content = {
                Column {
                    Canvas(modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(0.dp, 15.dp, 0.dp, 0.dp), onDraw = {
                        drawLine(
                            color = Color.White,
                            start = Offset(480f, 20f),
                            end = Offset(600f, 20f),
                            strokeWidth = 30f,
                            cap = StrokeCap.Round
                        )
                    })

                    if (order.status== "open") {
                        setInfo(uid, viewModel)
                    } else if (order.status == "Active") {
                        setViewWithDriver(viewModel, context)
                    }
                }
            })
    }
}

//@Preview(showBackground = true, showSystemUi = false)
@Composable
fun setInfo(uid: String, viewModel: DriverIsFoundViewModel){
    val imageTaxi = painterResource(id = R.drawable.icon)
    val infiniteTransition = rememberInfiniteTransition()
    val position by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val animateColor by infiniteTransition.animateColor(
        initialValue = Color.Magenta,
        targetValue = Color.Green,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis= 2000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Column {
            setInfoDriver(uid, viewModel)
            Spacer(modifier = Modifier.padding(0.dp))
            Image(
                painter = imageTaxi,
                contentDescription = null,
                modifier = Modifier
                    .offset(x = position.dp)
                    .padding(0.dp)
            )
            Spacer(modifier = Modifier.padding(0.dp))
            Canvas(modifier = Modifier
                .fillMaxWidth(0.1f)
                .padding(0.dp), onDraw = {
                drawLine(
                    color = animateColor,
                    start = Offset(100f, 200f),
                    end = Offset(940f, 200f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            })
    }
}
@Composable
private fun setInfoDriver(uid : String,viewModel: DriverIsFoundViewModel){
     val order by viewModel.getOrder(uid).observeAsState()
    order?.let {
        val myTextStyle = TextStyle(
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Black
        )
        Column {
            Row(modifier = Modifier.padding(16.dp, 16.dp, 0.dp, 16.dp)) {
                Text(
                    text = "You driver : ",
                    style = myTextStyle
                )
                Text(
                    text = it.driverName,
                    style = myTextStyle
                )
            }
            Row(modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp)) {
                Text(
                    text = it.timeToUser,
                    style = myTextStyle
                )
            }
        }
    }
}
@Composable
private fun setViewWithDriver(viewModel: DriverIsFoundViewModel, context : Context){
    val phoneDriver by viewModel.phoneNumberDriver.observeAsState()
    val imageTaxi = painterResource(id = R.drawable.icon)
    val infiniteTransition = rememberInfiniteTransition()
    val position by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val myTextStyle = TextStyle(
        fontSize = 20.sp,
        fontFamily = FontFamily.Monospace,
        color =  Color.Black,
        fontWeight = FontWeight.Bold
    )
    Column(modifier = Modifier
        .fillMaxSize(1f)
        .padding(16.dp)) {
        Text(
            text = "Your drive in place, Let's start of the trip",
            style = myTextStyle
        )
        Spacer(modifier = Modifier.padding(0.dp))
        Image(
            painter = imageTaxi,
            contentDescription = null,
            modifier = Modifier
                .offset(x = position.dp)
                .padding(0.dp)
        )
        Spacer(modifier = Modifier.padding(0.dp))
        Button(onClick = {
                         viewModel.callDriver(context, phoneDriver!!)
        }, modifier = Modifier
            .fillMaxHeight(0.5f)
            .fillMaxWidth(1f)
            .padding(10.dp),
            shape = RectangleShape) {
            Text(text = "Call driver", style = myTextStyle)
        }
    }
}

