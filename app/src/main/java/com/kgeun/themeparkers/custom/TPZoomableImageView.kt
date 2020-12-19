package com.kgeun.themeparkers.custom

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

class TPZoomableImageView : androidx.appcompat.widget.AppCompatImageView {
    private var mContext: Context? = null

    var photoBitmap: Bitmap? = null
        private set

    private var containerWidth: Int = 0
    private var containerHeight: Int = 0

    internal var background: Paint? = null

    //Matrices will be used to move and zoom image
    internal var matrix = Matrix()
    internal var savedMatrix = Matrix()

    internal var start = PointF()

    internal var currentScale: Float = 0.toFloat()
    internal var curX: Float = 0.toFloat()
    internal var curY: Float = 0.toFloat()
    internal var mode = NONE

    //For animating stuff
    internal var targetX: Float = 0.toFloat()
    internal var targetY: Float = 0.toFloat()
    internal var targetScale: Float = 0.toFloat()
    internal var targetScaleX: Float = 0.toFloat()
    internal var targetScaleY: Float = 0.toFloat()
    internal var scaleChange: Float = 0.toFloat()
    internal var targetRatio: Float = 0.toFloat()
    internal var transitionalRatio: Float = 0.toFloat()

    internal var easing = 0.2f
    internal var isAnimating = false

    internal var scaleDampingFactor = 0.5f

    //For pinch and zoom
    internal var oldDist = 1f
    internal var mid = PointF()

    private val mHandler = Handler()

    internal var minScale: Float = 0.toFloat()
    internal var maxScale = 8.0f

    internal var wpRadius = 25.0f
    internal var wpInnerRadius = 20.0f

    internal var screenDensity: Float = 0.toFloat()

    private var gestureDetector: GestureDetector? = null

    var defaultScale: Int = 0


    private val mUpdateImagePositionTask = object : Runnable {
        override fun run() {
            val mvals: FloatArray

            if (Math.abs(targetX - curX) < 5 && Math.abs(targetY - curY) < 5) {
                isAnimating = false
                mHandler.removeCallbacks(this)

                mvals = FloatArray(9)
                matrix.getValues(mvals)

                currentScale = mvals[0]
                curX = mvals[2]
                curY = mvals[5]

                //Set the image parameters and invalidate display
                val diffX = targetX - curX
                val diffY = targetY - curY

                matrix.postTranslate(diffX, diffY)
            } else {
                isAnimating = true
                mvals = FloatArray(9)
                matrix.getValues(mvals)

                currentScale = mvals[0]
                curX = mvals[2]
                curY = mvals[5]

                //Set the image parameters and invalidate display
                val diffX = (targetX - curX) * 0.3f
                val diffY = (targetY - curY) * 0.3f

                matrix.postTranslate(diffX, diffY)
                mHandler.postDelayed(this, 25)
            }

            invalidate()
        }
    }

    private val mUpdateImageScale = object : Runnable {
        override fun run() {
            val transitionalRatio = targetScale / currentScale
            val dx: Float
            if (Math.abs(transitionalRatio - 1) > 0.05) {
                isAnimating = true
                if (targetScale > currentScale) {
                    dx = transitionalRatio - 1
                    scaleChange = 1 + dx * 0.2f

                    currentScale *= scaleChange

                    if (currentScale > targetScale) {
                        currentScale = currentScale / scaleChange
                        scaleChange = 1f
                    }
                } else {
                    dx = 1 - transitionalRatio
                    scaleChange = 1 - dx * 0.5f
                    currentScale *= scaleChange

                    if (currentScale < targetScale) {
                        currentScale = currentScale / scaleChange
                        scaleChange = 1f
                    }
                }


                if (scaleChange != 1f) {
                    matrix.postScale(scaleChange, scaleChange, targetScaleX, targetScaleY)
                    mHandler.postDelayed(this, 15)
                    invalidate()
                } else {
                    isAnimating = false
                    scaleChange = 1f
                    matrix.postScale(targetScale / currentScale, targetScale / currentScale, targetScaleX, targetScaleY)
                    currentScale = targetScale
                    mHandler.removeCallbacks(this)
                    invalidate()
                    checkImageConstraints()
                }
            } else {
                isAnimating = false
                scaleChange = 1f
                matrix.postScale(targetScale / currentScale, targetScale / currentScale, targetScaleX, targetScaleY)
                currentScale = targetScale
                mHandler.removeCallbacks(this)
                invalidate()
                checkImageConstraints()
            }
        }
    }

    constructor(context: Context) : super(context) {
        mContext = context
        isFocusable = true
        isFocusableInTouchMode = true

        screenDensity = context.resources.displayMetrics.density
        initPaints()
        gestureDetector = GestureDetector(mContext, MyGestureDetector())
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context

        screenDensity = context.resources.displayMetrics.density
        initPaints()
        gestureDetector = GestureDetector(mContext, MyGestureDetector())

        defaultScale = TPZoomableImageView.DEFAULT_SCALE_FIT_INSIDE
    }

    private fun initPaints() {
        background = Paint()
        background?.isDither = true
        background?.isAntiAlias = true
        background?.isFilterBitmap = true
    }


    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        //Reset the width and height. Will draw bitmap and change
        containerWidth = width
        containerHeight = height

        if (photoBitmap != null) {
            val imgHeight = photoBitmap!!.height
            val imgWidth = photoBitmap!!.width

            val scale: Float
            var initX = 0
            var initY = 0

            if (defaultScale == TPZoomableImageView.DEFAULT_SCALE_FIT_INSIDE) {

                scale = containerWidth.toFloat() / imgWidth
                val newHeight = imgHeight * scale
                initY = (containerHeight - newHeight.toInt()) / 2

                matrix.setScale(scale, scale)
                matrix.postTranslate(0f, initY.toFloat())

                curX = initX.toFloat()
                curY = initY.toFloat()

                currentScale = scale
                minScale = scale
            } else {
                if (imgWidth > containerWidth) {
                    initY = (containerHeight - imgHeight) / 2
                    matrix.postTranslate(0f, initY.toFloat())
                } else {
                    initX = (containerWidth - imgWidth) / 2
                    matrix.postTranslate(initX.toFloat(), 0f)
                }

                curX = initX.toFloat()
                curY = initY.toFloat()

                currentScale = 1.0f
                minScale = 1.0f
            }


            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (photoBitmap != null && canvas != null) {
            canvas.drawBitmap(photoBitmap!!, matrix, background)
        }
    }

    //Checks and sets the target image x and y co-ordinates if out of bounds
    private fun checkImageConstraints() {
        if (photoBitmap == null) {
            return
        }

        val mvals = FloatArray(9)
        matrix.getValues(mvals)

        currentScale = mvals[0]

        if (currentScale < minScale) {
            val deltaScale = minScale / currentScale
            val px = (containerWidth / 2).toFloat()
            val py = (containerHeight / 2).toFloat()
            matrix.postScale(deltaScale, deltaScale, px, py)
            invalidate()
        }

        matrix.getValues(mvals)
        currentScale = mvals[0]
        curX = mvals[2]
        curY = mvals[5]

        val rangeLimitX = containerWidth - (photoBitmap!!.width * currentScale).toInt()
        val rangeLimitY = containerHeight - (photoBitmap!!.height * currentScale).toInt()


        var toMoveX = false
        var toMoveY = false

        if (rangeLimitX < 0) {
            if (curX > 0) {
                targetX = 0f
                toMoveX = true
            } else if (curX < rangeLimitX) {
                targetX = rangeLimitX.toFloat()
                toMoveX = true
            }
        } else {
            targetX = (rangeLimitX / 2).toFloat()
            toMoveX = true
        }

        if (rangeLimitY < 0) {
            if (curY > 0) {
                targetY = 0f
                toMoveY = true
            } else if (curY < rangeLimitY) {
                targetY = rangeLimitY.toFloat()
                toMoveY = true
            }
        } else {
            targetY = (rangeLimitY / 2).toFloat()
            toMoveY = true
        }

        if (toMoveX == true || toMoveY == true) {
            if (toMoveY == false) {
                targetY = curY
            }
            if (toMoveX == false) {
                targetX = curX
            }

            //Disable touch event actions
            isAnimating = true
            //Initialize timer
            mHandler.removeCallbacks(mUpdateImagePositionTask)
            mHandler.postDelayed(mUpdateImagePositionTask, 100)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector!!.onTouchEvent(event)) {
            return true
        }

        if (isAnimating == true) {
            return true
        }

        //Handle touch events here
        val mvals = FloatArray(9)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> if (isAnimating == false) {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE

                matrix.getValues(mvals)
                curX = mvals[2]
                curY = mvals[5]
                currentScale = mvals[0]

                if (isAnimating == false) {
                    checkImageConstraints()
                }
            }

            MotionEvent.ACTION_MOVE -> if (mode == DRAG && isAnimating == false) {
                matrix.set(savedMatrix)
                val diffX = event.x - start.x
                val diffY = event.y - start.y

                matrix.postTranslate(diffX, diffY)

                matrix.getValues(mvals)
                curX = mvals[2]
                curY = mvals[5]
                currentScale = mvals[0]
            } else if (mode == ZOOM && isAnimating == false) {
                val newDist = spacing(event)
                if (newDist > 10f) {
                    matrix.set(savedMatrix)
                    val scale = newDist / oldDist
                    matrix.getValues(mvals)
                    currentScale = mvals[0]

                    if (currentScale * scale <= minScale) {
                        matrix.postScale(minScale / currentScale, minScale / currentScale, mid.x, mid.y)
                    } else if (currentScale * scale >= maxScale) {
                        matrix.postScale(maxScale / currentScale, maxScale / currentScale, mid.x, mid.y)
                    } else {
                        matrix.postScale(scale, scale, mid.x, mid.y)
                    }


                    matrix.getValues(mvals)
                    curX = mvals[2]
                    curY = mvals[5]
                    currentScale = mvals[0]
                }
            }
        }

        //Calculate the transformations and then invalidate
        invalidate()
        return true
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    override fun setImageBitmap(b: Bitmap?) {
        if (b != null) {
            photoBitmap = b

            containerWidth = width
            containerHeight = height

            val imgHeight = photoBitmap!!.height
            val imgWidth = photoBitmap!!.width

            val scale: Float
            var initX = 0
            var initY = 0

            matrix.reset()

            if (defaultScale == TPZoomableImageView.DEFAULT_SCALE_FIT_INSIDE) {

                scale = containerWidth.toFloat() / imgWidth
                val newHeight = imgHeight * scale
                initY = (containerHeight - newHeight.toInt()) / 2

                matrix.setScale(scale, scale)
                matrix.postTranslate(0f, initY.toFloat())


                curX = initX.toFloat()
                curY = initY.toFloat()

                currentScale = scale
                minScale = scale
            } else {
                if (imgWidth > containerWidth) {
                    initX = 0
                    if (imgHeight > containerHeight) {
                        initY = 0
                    } else {
                        initY = (containerHeight - imgHeight) / 2
                    }

                    matrix.postTranslate(0f, initY.toFloat())
                } else {
                    initX = (containerWidth - imgWidth) / 2
                    scale = containerWidth.toFloat() / imgWidth
                    if (imgHeight > containerHeight) {
                        initY = 0
                    } else {
                        initY = (containerHeight - imgHeight) / 2
                    }

                    matrix.postTranslate(initX.toFloat(), 0f)
                }

                curX = initX.toFloat()
                curY = initY.toFloat()

                currentScale = 1.0f
                minScale = 1.0f
            }

            invalidate()
        } else {
            Log.d(TAG, "bitmap is null")
        }
    }

    /** Show an event in the LogCat view, for debugging  */
    private fun dumpEvent(event: MotionEvent) {
        val names = arrayOf("DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?")
        val sb = StringBuilder()
        val action = event.action
        val actionCode = action and MotionEvent.ACTION_MASK
        sb.append("event ACTION_").append(names[actionCode])
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
            sb.append(")")
        }
        sb.append("[")

        for (i in 0 until event.pointerCount) {
            sb.append("#").append(i)
            sb.append("(pid ").append(event.getPointerId(i))
            sb.append(")=").append(event.getX(i).toInt())
            sb.append(",").append(event.getY(i).toInt())
            if (i + 1 < event.pointerCount)
                sb.append(";")
        }
        sb.append("]")
    }

    internal inner class MyGestureDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(event: MotionEvent): Boolean {
            if (isAnimating == true) {
                return true
            }

            scaleChange = 1f
            isAnimating = true
            targetScaleX = event.x
            targetScaleY = event.y

            if (Math.abs(currentScale - maxScale) > 0.1) {
                targetScale = maxScale
            } else {
                targetScale = minScale
            }
            targetRatio = targetScale / currentScale
            mHandler.removeCallbacks(mUpdateImageScale)
            mHandler.post(mUpdateImageScale)
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDown(e: MotionEvent): Boolean {
            return false
        }
    }

    companion object {
        private val TAG = "NSPZoomableImageView"

        //We can be in one of these 3 states
        internal val NONE = 0
        internal val DRAG = 1
        internal val ZOOM = 2

        val DEFAULT_SCALE_FIT_INSIDE = 0
        val DEFAULT_SCALE_ORIGINAL = 1
    }
}