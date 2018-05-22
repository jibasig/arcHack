package org.liesel.room1

import android.arch.lifecycle.MutableLiveData
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.MainThread
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.selects.select
import java.util.*
import javax.xml.datatype.DatatypeConstants.SECONDS
import android.R.attr.delay
import io.reactivex.Single
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    lateinit var repoDao : RepoDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repoDao = RepoDatabase
                .getInstance(this@MainActivity)
                .repoDao
        setContentView(R.layout.activity_main)

        launch {
            insertData()

            fetchData();
        }

        val observable: Observable<Int> = createObservable()
        /*  observable.subscribe{
            e -> Log.e("GO", e.toString())
        }*/

        val observable2 = createObservable().publish()
        observable2.connect()
        observable2.filter { e -> e < 10 }.buffer(2).subscribe { e ->
            Log.e("GO2", e.toString())
        }

        /*val observableByCallable = Observable.fromCallable {
            runBlocking {
                delay(2000)
                33 // runBlocking returns the value of the given lambda!
            }

        }*/

        Observable.range(1, 10).flatMap{
            i-> Observable.just("hi   $i")
        }.subscribe{ e ->
            Log.e("FLATMAP", e.toString())
        }

        Observable.range(1, 10).flatMap{
            i-> Observable.range(1,4)
        }.subscribe{ e ->
            Log.e("FLATMAP", e.toString())
        }

        Observable.just("ssssssss").subscribe( { value ->
            Log.e("subsc", value.toString())
        },
                { e ->
                })

        val observableByCallable = Observable.fromCallable {
            val test  = async {
                delay(2000)
                33
            }

            launch {
               val result:String =  select<String> {
                    test.onAwait{ answer ->
                         "hello my "+answer
                    }

                }
                Log.e("sdfasdf",result)
            }
        }
            observableByCallable.subscribe { v ->
                Log.e("FromCallable", v.toString())
            }

/*
        val a = { i: Int ->
            i + 1
        }
        Log.e("test" ,a(1).toString())
        Log.e("test2" ,exec(5,a).toString())

        Log.e("test3" ,exec(5,{
            x-> x*10
        }).toString())


        Log.e("test3" ,str{
            x-> x.toUpperCase()
        }).toString()

    }

    fun exec(i: Int, f: (Int)->(Int)) : Int{
        return f(i)
    }

    fun str(f: (String)->(String)) : String{
        return f("hello")
    }*/


        initUI()

    }

    val data : MutableLiveData<String> = MutableLiveData()

  //  val repos : MutableLiveData<List<Repo>> = MutableLiveData()

    var i : Int = 0

    val sampleAdapter = SampleAdapter()
    private fun initUI() {
        data.value = ""

        val tv = findViewById<TextView>(R.id.tv1)
        data.observe(this,android.arch.lifecycle.Observer<String> {
            value -> tv.text = value
        })

        launch {
            while (true){
                delay(100)

                launch(UI){
                    data.value = i++.toString()
                }

            }
        }


        val gridLayoutManager = LinearLayoutManager(this)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerV.layoutManager = gridLayoutManager
        recyclerV.adapter = sampleAdapter


        repoDao.allRepos.observe(this, android.arch.lifecycle.Observer<List<Repo>> {
            sampleAdapter.applyData(it!!)
        })


        launch {
            while (true)
            {

                val repo = Repo(UUID.randomUUID().toString(), "name", "url")
                repoDao.insert(repo)
                launch(UI) {


                }

                delay(1500)
            }
        }
    }

    class SampleAdapter : RecyclerView.Adapter<SampleViewHolder>() {

        private var data = emptyList<Repo>()

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SampleViewHolder {
            return SampleViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.row, parent, false))
        }

        override fun getItemCount(): Int {
            return data.count()
        }

        override fun onBindViewHolder(holder: SampleViewHolder?, position: Int) {
            val repo : Repo  = data[position]
            holder?.tv?.setText(repo.id)
        }

        fun applyData(repos: List<Repo>){
            data = repos
            notifyDataSetChanged()
        }


    }

    private fun createObservable(): Observable<Int> {
        return Observable.create{ e ->
            Log.e("Create ", "created")
            createrEmitter(e)


        }
    }

    private fun createrEmitter(e: ObservableEmitter<Int>) {
        async {
            while (true) {
                e.onNext(3)
                delay(1000)

            }
        }
    }

    private fun fetchData() {
        val builder = StringBuilder()
        RepoDatabase
                .getInstance(this@MainActivity)
                .repoDao
                //.allRepos.forEach { repo -> builder.append(repo.toString()) }
        Log.e("main", builder.toString())
    }

    private fun insertData() {
        RepoDatabase
                .getInstance(this)
                .repoDao
                .insert(Repo(UUID.randomUUID().toString(), "Cool Repo Name ", "url"))

    }
}

class Person {
    constructor(parent: String) {

    }
}

class Person2(val firstName: String) {
}



class SampleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val tv :TextView = itemView.findViewById(R.id.rowText)

}
