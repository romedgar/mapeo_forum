package ittepic.edu.ladm_u5_ejercicio1_geomapaforum

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.Visibility
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import ittepic.edu.ladm_u5_ejercicio1_geomapaforum.adapters.AdapterSlider
import ittepic.edu.ladm_u5_ejercicio1_geomapaforum.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    lateinit var locacion: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 2
            )
        }

        baseRemota.collection("forum")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    binding.tvAdvise.setText(firebaseFirestoreException.message)
                    return@addSnapshotListener
                }

                var resultado = ""
                posicion.clear()
                for (document in querySnapshot!!) {
                    var data = Data()
                    Log.i("$$$ 71", document.data.toString())
                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!
                    data.descripcion = document.getString("descripcion")!!
                    resultado += data.toString() + "\n\n"
                    posicion.add(data)
                }
                Log.i("Resultado", resultado)
            }

        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 01f, oyente)

    }

    private fun miUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 2
            )
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener {
                var geoPosicion = GeoPoint(it.latitude, it.longitude)
                binding.tvPosition.setText("${it.latitude}, ${it.longitude}")
                for (item in posicion) {
                    if (item.estoyEn(geoPosicion)) {
                        mostrarInfo(item)
                        AlertDialog.Builder(this)
                            .setMessage("Usted se encuentra en: " + item.nombre)
                            .setTitle("Atención")
                            .setPositiveButton("Ok") { p, q -> }
                            .show()
                    }
                }
            }.addOnFailureListener {
                binding.tvPosition.setText("Error al obtener ubicacion")
            }


    }

    fun mostrarInfo(item: Data) {
        val builder = android.app.AlertDialog.Builder(this)

        val inflater = this.layoutInflater;
        val v = inflater.inflate(R.layout.custom_dialog, null)
        val pager = v.findViewById<ViewPager>(R.id.pager)
        val tvTitle = v.findViewById<TextView>(R.id.tv_title)
        val tvDescr = v.findViewById<TextView>(R.id.tv_descr)

        val imgs = enviarImagenes(item.nombre)

        val adapter = AdapterSlider(imgs, this)

        pager.adapter = adapter
        val cadena = item.nombre.toString()
        val upperCamelCase = cadena.substring(0, 1).uppercase() + cadena.substring(1)
        tvTitle.text = upperCamelCase
        tvDescr.text = item.descripcion

        builder.setView(v)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                })

        builder.create()
        builder.show()
    }

    fun enviarImagenes(name: String): ArrayList<Int> {

        when (name) {
            "oasis" -> return arrayListOf(
                R.drawable.oasis_1,
                R.drawable.oasis_2,
                R.drawable.oasis_3,
                R.drawable.oasis_4
            )
            "play city" -> return arrayListOf(
                R.drawable.play_1,
                R.drawable.play_2,
                R.drawable.play_3,
                R.drawable.play_4,
                R.drawable.play_5
            )
            "liverpool" -> return arrayListOf(
                R.drawable.liver_1,
                R.drawable.liver_2,
                R.drawable.liver_3
            )
            "cinemex" -> return arrayListOf(R.drawable.cine_1, R.drawable.cine_2, R.drawable.cine_3)
            "mac store" -> return arrayListOf(R.drawable.mac_1, R.drawable.mac_2, R.drawable.mac_3)
            "recorcholis" -> return arrayListOf(
                R.drawable.recocholis_1,
                R.drawable.recocholis_2,
                R.drawable.recocholis_3,
                R.drawable.recocholis_4
            )
            else -> return arrayListOf()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_acerca_de, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                AlertDialog.Builder(this)
                    .setTitle("Balines")
                    .setMessage(
                        "Daniel Alejandro Calderon Virgen\n" +
                                "18401090\n\n" +
                                "Edgar Gerardo Rojas Medina\n" +
                                "18401193"
                    )
                    .setPositiveButton("OK", { d, i -> })
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}

class Oyente(puntero: MainActivity) : LocationListener {
    var p = puntero

    override fun onLocationChanged(location: Location) {
        p.binding.tvPosition.text = "${location.latitude}, ${location.longitude}"
        var geoPosicionGPS = GeoPoint(location.latitude, location.longitude)
        Log.i("Position", "${location.latitude}, ${location.longitude}")
        var esta = false

        for (item in p.posicion) {
            if (item.estoyEn(geoPosicionGPS)) {
                esta = true
                //Log.i("#### item", item.toString())
                p.binding.tvPlace.text = "Estás en ${item.nombre}"
                p.binding.tvAdvise.visibility = View.VISIBLE
                p.binding.tvAdvise.setText("Presiona en la imagen para más infromación")
                when (item.nombre) {
                    "oasis" -> item.img = R.drawable.oasis
                    "play city" -> item.img = R.drawable.play
                    "liverpool" -> item.img = R.drawable.liver
                    "cinemex" -> item.img = R.drawable.cine
                    "recorcholis" -> item.img = R.drawable.recocholis
                    "mac store" -> item.img = R.drawable.mac
                }
                p.binding.imageViewPlace.setImageResource(item.img)
                p.binding.imageViewPlace.setOnClickListener {
                    p.mostrarInfo(item)
                }
            } else {
            }
        }
        if (esta)
            Log.i("##", "Estás en un lugar específico")
        else {
            p.binding.tvPlace.text = "Estás en Forum"
            p.binding.tvAdvise.visibility = View.VISIBLE
            p.binding.tvAdvise.text = "Sigue caminando"
            p.binding.imageViewPlace.setImageResource(R.drawable.forum)
            p.binding.imageViewPlace.setOnClickListener {
                Toast.makeText(p.baseContext, "Sigue caminando", Toast.LENGTH_SHORT).show()
                Log.i("##", "No estás en ningun lugar específico")
            }
        }

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }

}