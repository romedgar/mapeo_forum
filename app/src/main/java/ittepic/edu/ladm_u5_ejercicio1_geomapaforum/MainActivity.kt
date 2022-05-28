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
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import ittepic.edu.ladm_u5_ejercicio1_geomapaforum.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    lateinit var locacion : LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),2)
        }

        baseRemota.collection("forum")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException!=null){
                binding.tvAdvise.setText(firebaseFirestoreException.message)
                return@addSnapshotListener
            }

            var resultado = ""
            posicion.clear()
            for (document in querySnapshot!!){
                var data = Data()
                data.nombre = document.getString("nombre").toString()
                data.posicion1 = document.getGeoPoint("posicion1")!!
                data.posicion2 = document.getGeoPoint("posicion2")!!

                resultado += data.toString()+"\n\n"
                posicion.add(data)
            }
            val cocina = Data()
                cocina.nombre = "cocina"
                cocina.posicion1 = GeoPoint(21.530907293230076, -104.86746994395885)
                cocina.posicion2 = GeoPoint(21.530875832767045, -104.86742221759195)
                cocina.img = R.drawable.icono1
            posicion.add(cocina)
                val sala = Data()
                sala.nombre = "sala"
                sala.posicion1 = GeoPoint(21.53086086250086, -104.8674376402937)
                sala.posicion2 = GeoPoint(21.530830298202595, -104.8673437629787)
                sala.img = R.drawable.icono3
            posicion.add(sala)
        }

        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 01f, oyente)

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
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),2)
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener {
                var geoPosicion = GeoPoint(it.latitude, it.longitude)
                binding.tvPosition.setText("${it.latitude}, ${it.longitude}")
                for(item in posicion){
                    if(item.estoyEn(geoPosicion)){
                        mostrarInfo(item)
                        AlertDialog.Builder(this)
                            .setMessage("Usted se encuentra en: "+item.nombre)
                            .setTitle("Atención")
                            .setPositiveButton("Ok"){p,q->}
                            .show()
                    }
                }
            }.addOnFailureListener {
                binding.tvPosition.setText("Error al obtener ubicacion")
            }


    }

    fun mostrarInfo(item: Data) {
        val builder = android.app.AlertDialog.Builder(this)
        // Get the layout inflater
        val inflater = this.layoutInflater;
        val v = inflater.inflate(ittepic.edu.ladm_u5_ejercicio1_geomapaforum.R.layout.custom_dialog,null)
        var img_place = v.findViewById<ImageView>(R.id.img_place)
        var tv_title = v.findViewById<TextView>(R.id.tv_title)
        var tv_descr = v.findViewById<TextView>(R.id.tv_descr)

        img_place.setImageResource(item.img)
        tv_title.setText(item.nombre)
        
        builder.setView(v)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                   dialog.dismiss()
                })

        builder.create()
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_acerca_de, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.about ->{
                AlertDialog.Builder(this)
                    .setTitle("Balines")
                    .setMessage("Daniel Alejandro Calderon Virgen\n" +
                            "18401090\n\n" +
                            "Edgar Gerardo Rojas Medina\n" +
                            "18401193")
                    .setPositiveButton("OK", {d,i ->})
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}

class Oyente(puntero: MainActivity): LocationListener{
    var p = puntero

    override fun onLocationChanged(location: Location) {
        p.binding.tvPosition.setText("${location.latitude}, ${location.longitude}")
        var geoPosicionGPS = GeoPoint(location.latitude, location.longitude)

        for(item in p.posicion){
            Log.i("####",geoPosicionGPS.toString())
            Log.i("####", item.toString())
            if(item.estoyEn(geoPosicionGPS)){
                p.binding.tvPlace.setText("Estas en ${item.nombre}")
                p.binding.imageViewPlane.setImageResource(item.img)
                p.binding.tvAdvise.visibility = View.VISIBLE
                p.binding.imageViewPlane.setOnClickListener {
                    p.mostrarInfo(item)
                }
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