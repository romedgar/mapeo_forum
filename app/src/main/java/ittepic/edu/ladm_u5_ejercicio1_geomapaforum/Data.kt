package ittepic.edu.ladm_u5_ejercicio1_geomapaforum

import com.google.firebase.firestore.GeoPoint

class Data {
    var nombre : String = ""
    var posicion1 : GeoPoint = GeoPoint(0.0,0.0)
    var posicion2 : GeoPoint = GeoPoint(0.0,0.0)
    var descripcion = ""
    var img = 0

    override fun toString(): String {
        return nombre + "\n" + posicion1.latitude + "," + posicion1.longitude + "\n"+
                posicion2.latitude + "," + posicion2.longitude +"\nImg: "+img.toString()
    }

    fun estoyEn(posicionActual: GeoPoint) : Boolean{
        if (posicionActual.latitude>= posicion2.latitude && posicionActual.latitude <= posicion1.latitude){
            if(invertir(posicionActual.longitude) >= invertir(posicion2.longitude) && invertir(posicionActual.longitude) <= invertir(posicion1.longitude)){
                return true
            }
        }
        return false
    }

    private fun invertir(valor : Double) : Double{
        return valor*-1
    }

}