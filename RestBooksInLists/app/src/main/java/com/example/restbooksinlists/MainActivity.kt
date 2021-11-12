 package com.example.restbooksinlists


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import android.view.inputmethod.EditorInfo
import android.widget.*

import android.widget.TextView.OnEditorActionListener
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


 data class Book(val id:String ,val title:String , val smallThumbnail :String , val thumbnail:String , val previewLink  : String,  val authors : ArrayList<String>,  val description  : String ,  var isfaved: Boolean )
 class MainActivity : AppCompatActivity() {

     lateinit var recycler : RecyclerView;

     private var  books : ArrayList<Book> = arrayListOf()


     private val client = OkHttpClient()    //criaçao de um cliente http asyncrono


     lateinit var responseString :String
     lateinit var jsonObject: JSONObject

     private var mHandler: Handler? = null  //2ª thread para tratar de updates em ui

     private var c=0;
     private var isViewingFavs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recyclerview)
        recycler.layoutManager =  GridLayoutManager(
            this,
            2,
            RecyclerView.VERTICAL,
            false
        )
        recycler.adapter = BookListAdapter(books , this , isViewingFavs)
        mHandler =  Handler(Looper.getMainLooper());
        //o maxResults manipula a quantidade dos q vem
        //o index é o contador de pagina e tem isso em conta
        //podemos pegar por aqui e colocar 8 como resultados maximos e idex 0 , chega de 8 em 8
        //o listener de scroll trata de incrementar o index..
        val Url = "https://www.googleapis.com/books/v1/volumes?q=ios&maxResults=8&startIndex="+c.toString()
        run(Url)
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && !isViewingFavs) {
                    //Toast.makeText(this@MainActivity, "Last", Toast.LENGTH_LONG).show()
                    c++
                    val Urlincreased = "https://www.googleapis.com/books/v1/volumes?q=ios&maxResults=8&startIndex="+c.toString()
                    run(Urlincreased)

                }
            }
        })
        //"resetar a pequisa"
        findViewById<ImageView>(R.id.returnbutt).setOnClickListener {

                books.clear()
                recycler.adapter?.notifyDataSetChanged()
                run("https://www.googleapis.com/books/v1/volumes?q=ios&maxResults=8&startIndex=0")
        }

        //alternar entre favoritos e normal
        findViewById<ImageView>(R.id.favorites).setOnClickListener {
        if(!isViewingFavs){
            showFavBooks()
        }else{
            books.clear()
            recycler.adapter?.notifyDataSetChanged()
            run("https://www.googleapis.com/books/v1/volumes?q=ios&maxResults=8&startIndex=0")
        }
            isViewingFavs = !isViewingFavs
        }

        val edittext = findViewById<EditText>(R.id.editTextTextPersonName)

        findViewById<ImageView>(R.id.searchbutt).setOnClickListener(){
            if(edittext.getText().toString() != ""){
                books.clear()
                recycler.adapter?.notifyDataSetChanged()
                Log.d("BROWSINGNG" ,  "" + "https://www.googleapis.com/books/v1/volumes?q=" + edittext.getText())

                run("https://www.googleapis.com/books/v1/volumes?q=" + edittext.getText())
            }


        }

    }

     private fun showFavBooks(){

         books.clear()
         recycler.adapter?.notifyDataSetChanged()   // can be optimized

         for (b in getFavoriteBooks()){
             books.add(b)
         }

         recycler.adapter?.notifyDataSetChanged()   // can be optimized

     }

     public fun getFavoriteBooks() : ArrayList<Book> {

         var arrayItems= arrayListOf<Book>()
         val sPrefs = getSharedPreferences("favoriteStorage", Context.MODE_PRIVATE)

         val serializedObject: String? = sPrefs.getString("favorites", null)
         if (serializedObject != null) {
             val gson = Gson()
             val type = object : TypeToken<ArrayList<Book?>?>() {}.type
             arrayItems = gson.fromJson<ArrayList<Book>>(serializedObject, type)
         }

         return arrayItems
     }


     //metodo que faz o get
     fun run(url: String) {
         val request = Request.Builder()
             .url(url)
             .build()

         client.newCall(request).enqueue(object : Callback {
             override fun onFailure(call: okhttp3.Call, e: IOException) {}
             override fun onResponse(call: okhttp3.Call, response: Response) {

                 responseString = response.body()?.string()!!   //resposta so pode ser consumida 1vez
                 jsonObject= JSONObject(responseString)// tranforma-se localmente aqui

                 val booksArray = jsonObject.getJSONArray("items")

                 var morebooks = arrayListOf<Book>()
                 for(i in 0 until booksArray.length()){
                     val book = booksArray.getJSONObject(i)
                     val id =book.get("id").toString()
                     val title = JSONObject(book.get("volumeInfo").toString()).get("title").toString()
                     val smallThumbnail =JSONObject(JSONObject(JSONObject(book.get("volumeInfo").toString()).toString()).get("imageLinks").toString()).get("smallThumbnail").toString()
                     val thumbnail =JSONObject(JSONObject(JSONObject(book.get("volumeInfo").toString()).toString()).get("imageLinks").toString()).get("thumbnail").toString()
                     val previewLink =JSONObject(book.get("volumeInfo").toString()).get("previewLink").toString()
                     var autores = arrayListOf<String>()
                     var decritp = "Sem descrição"
                     try{
                         decritp  = JSONObject(book.get("volumeInfo").toString()).get("description").toString()
                         autores = arrayListOf<String>(JSONObject(book.get("volumeInfo").toString()).get("authors").toString())
                     }catch (e: JSONException){
                        Log.d("JSONEXEPTION", " :-- $e\n");
                     }

                        morebooks.add(Book(id,title, smallThumbnail , thumbnail , previewLink , autores , decritp, false))
                 }
                 //check for doubles can be optimized
                 var alreadyInBooksid = arrayListOf<String>()
                 for(b in books){
                     alreadyInBooksid.add(b.id )
                 }
                for (mb in morebooks){
                    if (!alreadyInBooksid.contains(mb.id)) books.add(mb)
                }
                // nessario thread pois isto está a correr asyncronamente.
                 mHandler!!.post {
                     recycler.adapter?.notifyDataSetChanged()
                 }


             }
         })



     }


}