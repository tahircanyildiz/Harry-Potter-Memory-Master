package com.harypotter.myapplication.view


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.common.io.BaseEncoding.base64
import com.google.common.io.BaseEncoding.base64Url
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.harypotter.myapplication.R

import com.harypotter.myapplication.databinding.ActivityMain2Binding
import com.harypotter.myapplication.util.Kart
import com.harypotter.myapplication.util.Singleton
import com.harypotter.myapplication.util.Singleton.kartList
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream
import java.lang.Exception


class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMain2Binding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
    }



}