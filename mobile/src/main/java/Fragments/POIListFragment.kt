package Fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView

import com.example.de.testssapplication.R

import org.w3c.dom.Text

import java.text.DecimalFormat

import model.DataModel
import model.POI

/**
 * This fragment refers to the List Tab in the POI Selection Activity.
 * It basically implements a Adapter that wraps the POIs of the DataModel single instance.
 * The Adapter is used for displayment of the data in the GridView of the fragment.
 */
class POIListFragment : Fragment() {

    private var model: DataModel? = null
    private var poiAdapter: POIGridAdapter? = null
    var fab: FloatingActionButton? = null

    override fun onCreateView(inflater: LayoutInflater?, viewGroup: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.tab_list, viewGroup, false)

        model = DataModel.instance
        val gridView = rootView.findViewById<GridView>(R.id.poi_grid_list)
        poiAdapter = POIGridAdapter()
        gridView.adapter = poiAdapter

        // React to user clicks on a POI.
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val poi = model!!.getPOI(position)
            poi!!.isSelected = !poi.isSelected
            poiAdapter!!.notifyDataSetChanged()
        }

        if (model!!.poIcount == 0) {
            val noPOIInfo = rootView.findViewById<View>(R.id.noPOIText) as TextView
            noPOIInfo.visibility = View.VISIBLE
        }

        return rootView
    }

    fun notifySelectionChange() {
        poiAdapter!!.notifyDataSetChanged()
    }

    private inner class POIGridAdapter : BaseAdapter() {

        override fun getCount(): Int {
            if (fab != null) {
                if (model!!.selectedPOIs.size == 0){
                    fab!!.setForeground(getResources().getDrawable(R.drawable.right_arrow_red))
                }
                else {
                    fab!!.setForeground(getResources().getDrawable(R.drawable.right_arrow))
                }
                //fab!!.setForeground(getResources().getDrawable(model.selectedPOIs.size == 0 ? R.drawable.right_arrow_red : R.drawable.right_arrow));
            }

            return model!!.poIcount
        }

        override fun getItem(i: Int): Any? {
            return null
        }

        override fun getItemId(i: Int): Long {
            return 0
        }

        @SuppressLint("ResourceType")
        override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View {

            val poi = model!!.getPOI(i)
            val v: View

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                v = layoutInflater.inflate(R.layout.grid_view_poi, null)
            } else {
                v = convertView
            }

            val poiTextView = v.findViewById<View>(R.id.poi_grid_name) as TextView
            val poiImageView = v.findViewById<View>(R.id.poi_grid_image) as ImageView
            val poiTextDistance = v.findViewById<View>(R.id.poi_grid_distance) as TextView
            val poiRatingBar = v.findViewById<View>(R.id.poi_grid_rating) as RatingBar

            val name = poi!!.name
            if (name != null) {
                poiTextView.text = poi.name
            }
            val distance = poi.distanceToStart
            if (distance >= 0.0) { // Check if the distance is stored in the object.
                if (distance < 1.0) { // Check for the used unit.
                    poiTextDistance.text = (distance * 1000.0).toInt().toString() + " m"
                } else {
                    val df = DecimalFormat("#.##")
                    poiTextDistance.text = df.format(distance).toString() + " km"
                }
            }
            val rating = poi.rating
            if (rating == 0.0) {
                poiRatingBar.visibility = View.INVISIBLE
            }
            Log.d("TAG", "Rating " + rating + " " + rating.toFloat())
            poiRatingBar.rating = rating.toFloat()

            var bm: Bitmap? = poi.photo
            if (bm == null) { // Use a default Image if the Bitmap is empty.
                bm = BitmapFactory.decodeResource(resources, R.drawable.no_image)
            }
            val width = resources.getDimension(R.dimen.grid_poi_element_width).toInt()
            val textSize = resources.getDimension(R.dimen.grid_poi_element_textSize).toInt()
            // Scale the Bitmap size.
            bm = Bitmap.createScaledBitmap(bm!!, width, width - 3 * textSize, false)
            poiImageView.setImageBitmap(bm)
            // Leave a padding at the top for displayment of the POI name.
            poiImageView.setPadding(0, textSize + 6, 0, 0)

            if (poi.isSelected) {
                // If the user selected a poi, send draw a green border around it.
                v.setBackgroundResource(R.layout.grid_poi_shape_selected)
            } else {
                // If a poi is not selected, surround it with a gray border.
                v.setBackgroundResource(R.layout.grid_poi_shape_unselected)
            }

            return v
        }
    }
}
