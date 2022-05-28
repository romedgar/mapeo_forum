package ittepic.edu.ladm_u5_ejercicio1_geomapaforum

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
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
                binding.textView.setText(firebaseFirestoreException.message)
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
                binding.textView.setText(resultado)
        }

        binding.button.setOnClickListener {
           // miUbicacion()
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
                binding.textView2.setText("${it.latitude}, ${it.longitude}")
                for(item in posicion){
                    if(item.estoyEn(geoPosicion)){
                        AlertDialog.Builder(this)
                            .setMessage("Usted se encuentra en: "+item.nombre)
                            .setTitle("Atención")
                            .setPositiveButton("Ok"){p,q->}
                            .show()
                    }
                }
            }.addOnFailureListener {
                binding.textView2.setText("Error al obtener ubicacion")
            }
    }
}

class Oyente(puntero: MainActivity): LocationListener{
    var p = puntero

    override fun onLocationChanged(location: Location) {
        p.binding.textView2.setText("${location.latitude}, ${location.longitude}")
        var geoPosicionGPS = GeoPoint(location.latitude, location.longitude)

        for(item in p.posicion){
            if(item.estoyEn(geoPosicionGPS)){
                p.binding.textView3.setText("Estas en ${item.nombre}")
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