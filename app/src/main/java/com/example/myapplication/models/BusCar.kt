package com.example.myapplication.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

data class BusCar(
    val id: String,
    val position: LatLng,
    val license_plate: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(LatLng::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeParcelable(position, flags)
        parcel.writeString(license_plate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BusCar> {
        override fun createFromParcel(parcel: Parcel): BusCar {
            return BusCar(parcel)
        }

        override fun newArray(size: Int): Array<BusCar?> {
            return arrayOfNulls(size)
        }
    }
}