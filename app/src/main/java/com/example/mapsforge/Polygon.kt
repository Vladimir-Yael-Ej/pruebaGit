package com.example.mapsforge

import org.mapsforge.core.graphics.Canvas
import org.mapsforge.core.graphics.GraphicFactory
import org.mapsforge.core.graphics.Paint
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.core.util.LatLongUtils
import org.mapsforge.core.util.MercatorProjection
import org.mapsforge.map.layer.Layer
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A `Polygon` draws a closed connected series of line segments specified by a list of [LatLongs][LatLong].
 * If the first and the last `LatLong` are not equal, the `Polygon` will be closed automatically.
 * A `Polygon` holds two [Paint] objects to allow for different outline and filling. These paints define
 * drawing parameters such as color, stroke width, pattern and transparency.
 */
class Polygon
/**
 * @param paintFill      the initial `Paint` used to fill this polygon (may be null).
 * @param paintStroke    the initial `Paint` used to stroke this polygon (may be null).
 * @param graphicFactory the GraphicFactory
 */ @JvmOverloads constructor(
    /**
     * @param paintFill the new `Paint` used to fill this polygon (may be null).
     */
    @set:Synchronized
    @get:Synchronized var paintFill: Paint?,
    /**
     * @param paintStroke the new `Paint` used to stroke this polygon (may be null).
     */
    @set:Synchronized
    @get:Synchronized var paintStroke: Paint?, private val graphicFactory: GraphicFactory,
    /**
     * @return true if it keeps the bitmap aligned with the map, to avoid a
     * moving effect of a bitmap shader, false otherwise.
     */
    val isKeepAligned: Boolean = false
) : Layer() {
    private var boundingBox: BoundingBox? = null
    private val latLongs: MutableList<LatLong> = CopyOnWriteArrayList()
    /**
     * @return the `Paint` used to fill this polygon (may be null).
     */
    /**
     * @return the `Paint` used to stroke this polygon (may be null).
     */
    @Synchronized
    fun addPoint(point: LatLong) {
        latLongs.add(point)
        updatePoints()
    }

    @Synchronized
    fun addPoints(points: List<LatLong>?) {
        latLongs.addAll(points!!)
        updatePoints()
    }

    @Synchronized
    fun clear() {
        latLongs.clear()
        updatePoints()
    }

    @Synchronized
    operator fun contains(tapLatLong: LatLong?): Boolean {
        return LatLongUtils.contains(latLongs, tapLatLong)
    }

    @Synchronized
    override fun draw(
        boundingBox: BoundingBox,
        zoomLevel: Byte,
        canvas: Canvas,
        topLeftPoint: Point
    ) {
        if (latLongs.size < 2 || paintStroke == null && paintFill == null) {
            return
        }
        if (this.boundingBox != null && !this.boundingBox!!.intersects(boundingBox)) {
            return
        }
        val iterator: Iterator<LatLong> = latLongs.iterator()
        val path = graphicFactory.createPath()
        var latLong = iterator.next()
        val mapSize = MercatorProjection.getMapSize(zoomLevel, displayModel.tileSize)
        var x = (MercatorProjection.longitudeToPixelX(
            latLong.longitude,
            mapSize
        ) - topLeftPoint.x).toFloat()
        var y = (MercatorProjection.latitudeToPixelY(
            latLong.latitude,
            mapSize
        ) - topLeftPoint.y).toFloat()
        path.moveTo(x, y)
        while (iterator.hasNext()) {
            latLong = iterator.next()
            x = (MercatorProjection.longitudeToPixelX(
                latLong.longitude,
                mapSize
            ) - topLeftPoint.x).toFloat()
            y = (MercatorProjection.latitudeToPixelY(
                latLong.latitude,
                mapSize
            ) - topLeftPoint.y).toFloat()
            path.lineTo(x, y)
        }
        if (paintStroke != null) {
            if (isKeepAligned) {
                paintStroke!!.setBitmapShaderShift(topLeftPoint)
            }
            canvas.drawPath(path, paintStroke)
        }
        if (paintFill != null) {
            if (isKeepAligned) {
                paintFill!!.setBitmapShaderShift(topLeftPoint)
            }
            canvas.drawPath(path, paintFill)
        }
    }

    /**
     * @return a thread-safe list of LatLongs in this polygon.
     */
    fun getLatLongs(): List<LatLong> {
        return latLongs
    }

    @Synchronized
    fun setPoints(points: List<LatLong>?) {
        latLongs.clear()
        latLongs.addAll(points!!)
        updatePoints()
    }

    private fun updatePoints() {
        boundingBox = if (latLongs.isEmpty()) null else BoundingBox(latLongs)
    }
    /**
     * @param paintFill      the initial `Paint` used to fill this polygon (may be null).
     * @param paintStroke    the initial `Paint` used to stroke this polygon (may be null).
     * @param graphicFactory the GraphicFactory
     * @param isKeepAligned    if set to true it will keep the bitmap aligned with the map,
     * to avoid a moving effect of a bitmap shader.
     */
}