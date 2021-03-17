package ir.shahabazimi.instagrampicker.gallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import ir.shahabazimi.instagrampicker.R
import java.io.File

class CameraAdapter() : RecyclerView.Adapter<CameraAdapter.Holder>() {
    var data:ArrayList<File> = ArrayList()
    set(value) {
        field=value
        notifyDataSetChanged()
    }

    fun addItem(item:File){
        data.add(item)
        notifyDataSetChanged()
        //notifyItemInserted(data.size)
    }

    fun deleteItem(pos:Int){
        data.removeAt(pos)
        notifyItemRemoved(pos)
    }
    inner class Holder(val itemView:View) : RecyclerView.ViewHolder(itemView){
        fun bind(item:File){
            //viewBinding.address=item
            itemView.findViewById<ImageView>(R.id.imageView).apply {
                setImageURI(Uri.fromFile(item))
                setOnClickListener {
                    deleteItem(adapterPosition)
                }
            }

          //  viewBinding.imageView.setImageURI(Uri.fromFile(item))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.camera_item_view,parent,false)
        return Holder(view)
      //  return Holder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.camera_item_view,parent,false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    interface CameraListener{
        fun onDeleteCLick(item:File,pos:Int)
    }
}