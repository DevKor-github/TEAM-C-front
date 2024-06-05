package com.example.deckor_teamc_front

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView

class InnerMapTouchHandler(
    private val context: Context,
    private val container: FrameLayout,
    private val svgFileNames: List<String>,
    private val folderName: String,
    private val viewModel: FetchDataViewModel,
    private val lifecycleOwner: LifecycleOwner // Add LifecycleOwner to observe LiveData
) : View.OnTouchListener {

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val touchedX = event.rawX.toInt()
            val touchedY = event.rawY.toInt()
            val clickedFileNames = mutableListOf<String>()

            Log.d("TouchHandler", "터치한 실제 좌표: ($touchedX, $touchedY)")

            for (i in 0 until container.childCount) {
                val child = container.getChildAt(i) as FrameLayout
                val svgImageView = child.getChildAt(0) as SVGImageView
                if (isPointInsideView(touchedX, touchedY, svgImageView)) {
                    val drawablePoint = getDrawableCoordinates(svgImageView, touchedX, touchedY)
                    if (drawablePoint != null) {
                        Log.d("TouchHandler", "변환된 좌표: (${drawablePoint.x}, ${drawablePoint.y})")
                        if (isNonTransparentPixel(svgImageView, drawablePoint.x, drawablePoint.y)) {
                            clickedFileNames.add(svgFileNames[i])
                            changeColorInSvg(svgImageView, drawablePoint.x, drawablePoint.y, svgFileNames[i], folderName)
                            makelog(svgFileNames[i])
                        }
                    }
                }
            }

            if (clickedFileNames.isNotEmpty()) {
                logClickedFileNames(clickedFileNames)
            }
        }
        return true
    }

    private fun isPointInsideView(x: Int, y: Int, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]
        val viewWidth = view.width
        val viewHeight = view.height

        return x >= viewX && x <= viewX + viewWidth && y >= viewY && y <= viewY + viewHeight
    }

    private fun getDrawableCoordinates(view: SVGImageView, x: Int, y: Int): Point? {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = x - location[0]
        val viewY = y - location[1]

        if (viewX < 0 || viewY < 0 || viewX >= view.width || viewY >= view.height) {
            return null
        }

        val drawable = view.drawable ?: return null
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val imageViewWidth = view.width
        val imageViewHeight = view.height

        Log.d("TouchHandler", "뷰의 크기: ${imageViewWidth}x${imageViewHeight}")
        Log.d("TouchHandler", "이미지의 원래 크기: ${drawableWidth}x${drawableHeight}")

        // 이미지가 뷰에 맞게 스케일된 비율을 계산
        val scaleX = drawableWidth.toFloat() / imageViewWidth
        val scaleY = drawableHeight.toFloat() / imageViewHeight

        val drawableX = (viewX * scaleX).toInt()
        val drawableY = (viewY * scaleY).toInt()

        Log.d("TouchHandler", "터치 좌표: ($viewX, $viewY), 변환된 좌표: ($drawableX, $drawableY)")

        return Point(drawableX, drawableY)
    }

    private fun isNonTransparentPixel(view: SVGImageView, x: Int, y: Int): Boolean {
        val drawable = view.drawable ?: return false

        if (x < 0 || y < 0 || x >= drawable.intrinsicWidth || y >= drawable.intrinsicHeight) {
            return false
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        return bitmap.getPixel(x, y) != Color.TRANSPARENT
    }

    private fun changeColorInSvg(view: SVGImageView, x: Int, y: Int, svgFileName: String, folderName: String) {
        try {
            val originalSvgString = loadSvgFromAssets(context, "$folderName/$svgFileName")
            val modifiedSvgString = originalSvgString.replace("#FED0D0", "#FFB786")
            val svg = SVG.getFromString(modifiedSvgString)
            view.setSVG(svg)
            view.invalidate()
        } catch (e: Exception) {
            Log.e("TouchHandler", "SVG 수정 오류", e)
        }
    }

    private fun loadSvgFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun logClickedFileNames(fileNames: List<String>) {
        Log.d("TouchHandler", "클릭한 비투명 SVG 파일들: $fileNames")
    }

    private fun makelog(svgFileName: String) {

        val nameWithoutExtension = svgFileName.substringBeforeLast(".svg")
        Log.d("TouchHandlernew", "Room details: $nameWithoutExtension")

        viewModel.roomList.observe(lifecycleOwner, Observer { roomList ->
            roomList.filter { it.name == nameWithoutExtension }.forEach { room ->
                Log.d("TouchHandlernew", "Room details: $room")
            }
        })
    }
}
