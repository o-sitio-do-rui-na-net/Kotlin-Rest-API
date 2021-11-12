package com.example.restbooksinlists


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken



class BookListAdapter(private var books : ArrayList<Book> ,  private var activity: MainActivity , private var isViewingFAVS  : Boolean)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    inner class ViewHolder(v: View ) : RecyclerView.ViewHolder(v){
        init {
            v.setOnClickListener {      //click no livro
                val position :Int =adapterPosition
                shoeExapandedView(position)
            }

        }


    }

    //vizualizaçao do livro
    private  fun shoeExapandedView(position: Int){

        val builder = AlertDialog.Builder(activity)
        var view = View.inflate(activity,R.layout.expanded,null)
        view.findViewById<TextView>(R.id.TaskTextView).text = books[position].title
        view.findViewById<TextView>(R.id.description).text = books[position].description
        view.findViewById<TextView>(R.id.authors).text = books[position].authors.toString()

        var favoriteBooksIds = arrayListOf<String>()

        for (b in activity.getFavoriteBooks()){
            favoriteBooksIds.add(b.id)
        }
        if(favoriteBooksIds.contains(books[position].id)){
            view.findViewById<ImageView>(R.id.favbutt).setBackgroundResource(R.drawable.ic_baseline_star_24)

        }
         view.findViewById<ImageView>(R.id.favbutt).setOnClickListener {
            //SET BOOK FAVORITE
             if(favoriteBooksIds.contains(books[position].id)){
                 Toast.makeText(activity, "UNFAVING nº" + position.toString(),Toast.LENGTH_SHORT).show()
                 removeBookFromFav(position)
                 view.findViewById<ImageView>(R.id.favbutt).setBackgroundResource(R.drawable.ic_baseline_star_border_24)

             }else{
                 Toast.makeText(activity, "FAVING nº" + position.toString(),Toast.LENGTH_SHORT).show()
                 addBookToFav(position)
                 view.findViewById<ImageView>(R.id.favbutt).setBackgroundResource(R.drawable.ic_baseline_star_24)
             }

        }
        view.findViewById<ImageView>(R.id.buybutt).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(books[position].previewLink))
            startActivity(activity,browserIntent,null)
        }

       Glide.with(activity)
            .load(books[position].smallThumbnail)
            .into(  view.findViewById<ImageView>(R.id.BOOK_IMAGE_VIEW))



        builder.setView(view)
        builder.show()
    }
    //adicionar e remover  livro
    //guarda e altera o array de livros favoritos
    private fun addBookToFav(position: Int){
        val  storageLoc= "favoriteStorage"  //Context.MODE_PRIVATE
        val sPref = activity.getSharedPreferences(storageLoc,Context.MODE_PRIVATE).edit()

        var currentFavorits = activity.getFavoriteBooks()

        currentFavorits.add(books[position])

        val gson = Gson()
        val json: String = gson.toJson(currentFavorits)
        sPref.putString("favorites", json)
        sPref.commit()
    }
    private fun removeBookFromFav(position:Int){
        val  storageLoc= "favoriteStorage"  //Context.MODE_PRIVATE
        val sPref = activity.getSharedPreferences(storageLoc,Context.MODE_PRIVATE).edit()

        var currentFavorits = activity.getFavoriteBooks()
        val to_Remove_id = books[position].id
        for( b in currentFavorits){
            if(b.id == to_Remove_id){
                currentFavorits.remove(b)
                break
            }
        }

        val gson = Gson()
        val json: String = gson.toJson(currentFavorits)
        sPref.putString("favorites", json)
        sPref.commit()
    }


    //metodos do recycler

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.bookview,parent,false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return  books.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
     holder.itemView.findViewById<TextView>(R.id.TaskTextView).text = books[position].title



        Glide.with(activity)
            .load(books[position].smallThumbnail)
            .into(holder.itemView.findViewById<ImageView>(R.id.BOOK_IMAGE_VIEW))

    }

    }



