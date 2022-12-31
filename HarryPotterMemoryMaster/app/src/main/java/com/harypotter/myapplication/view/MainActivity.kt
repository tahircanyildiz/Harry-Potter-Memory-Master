package com.harypotter.myapplication.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.harypotter.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var firestore:FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        binding.progressBar.visibility= View.GONE

        auth=Firebase.auth
        firestore=Firebase.firestore

        //başlangıçta görünmeyecek öğeler
        binding.editPassRepeat.visibility=View.GONE
        binding.buttonSifreDegistir.visibility=View.GONE


        binding.buttonKayit.setOnClickListener {
            val userEmail=binding.editEmailText.text.toString().trim()
            val passWord=binding.editPasswordText.text.toString().trim()
            binding.progressBar.visibility=View.VISIBLE

            //email ve şifre yazılmadıysa uyarı verecek
            if (userEmail.isEmpty()){
                binding.progressBar.visibility=View.GONE
                Toast.makeText(this,"Email adresinizi yazınız",Toast.LENGTH_SHORT).show()
            }else if (passWord.isEmpty()){
                binding.progressBar.visibility=View.GONE
                Toast.makeText(this,"Parolanızı giriniz",Toast.LENGTH_SHORT).show()
            }else{
                auth.createUserWithEmailAndPassword(userEmail,passWord).addOnSuccessListener {
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,"Hoşgeldiniz ${it.user?.email}",Toast.LENGTH_LONG).show()
                    val intent= Intent(this, MainActivity2::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.buttonGiris.setOnClickListener {
            val userEmail=binding.editEmailText.text.toString().trim()
            val passWord=binding.editPasswordText.text.toString().trim()
            binding.progressBar.visibility=View.VISIBLE

            //eposta ve şifre yazılmadıysa uyarı verecek
            if (userEmail.isEmpty()){
                binding.progressBar.visibility=View.GONE
                Toast.makeText(this,"Email adresinizi yazınız",Toast.LENGTH_SHORT).show()
            }else if (passWord.isEmpty()){
                binding.progressBar.visibility=View.GONE
                Toast.makeText(this,"Parolanızı giriniz",Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(userEmail,passWord).addOnSuccessListener {
                    binding.progressBar.visibility=View.GONE
                    val intent= Intent(this, MainActivity2::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.sifreUnuttum.setOnClickListener {
            binding.progressBar.visibility=View.VISIBLE
            val userEmail=binding.editEmailText.text.toString().trim()
            if (userEmail.isEmpty()){
                binding.progressBar.visibility=View.GONE
                Toast.makeText(this,"Email adresinizi yazınız",Toast.LENGTH_SHORT).show()
            }else{
                auth.sendPasswordResetEmail(userEmail).addOnSuccessListener {
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,"Eposta adresinize şifre sıfırlama maili gönderildi.",Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }

        }

        binding.textSifreDegis.setOnClickListener {
            //şifre değiştire basınca görünecek ve gizlenecek öğelerin ayarlanması
            binding.editPassRepeat.visibility=View.VISIBLE
            binding.buttonSifreDegistir.visibility=View.VISIBLE
            binding.buttonGiris.visibility=View.GONE
            binding.buttonKayit.visibility=View.GONE
            binding.sifreUnuttum.visibility=View.GONE
            binding.textSifreDegis.visibility=View.GONE

            //şifre değiştir butonuna basınca yapılacaklar
            binding.buttonSifreDegistir.setOnClickListener {
                val userEmail=binding.editEmailText.text.toString().trim()
                val passWord=binding.editPasswordText.text.toString().trim()
                val userPassRepeat=binding.editPassRepeat.text.toString().trim()
                binding.progressBar.visibility=View.VISIBLE
                if (userEmail.isEmpty()){
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,"Email adresinizi yazınız",Toast.LENGTH_SHORT).show()
                }else if (passWord.isEmpty()){
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,"Parolanızı giriniz",Toast.LENGTH_SHORT).show()
                }else if (userPassRepeat.isEmpty()){
                    binding.progressBar.visibility=View.GONE
                    Toast.makeText(this,"Yeni parolanızı giriniz",Toast.LENGTH_SHORT).show()
                }else{
                    auth.signInWithEmailAndPassword(userEmail,passWord).addOnSuccessListener {
                        it.user!!.updatePassword(userPassRepeat).addOnSuccessListener {
                            //şifre değişince gizlenecek ve görünecek öğeler
                            Toast.makeText(this,"Parolanız değiştirildi",Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility=View.GONE
                            binding.editPassRepeat.visibility=View.GONE
                            binding.textSifreDegis.visibility=View.GONE
                            binding.buttonSifreDegistir.visibility=View.GONE
                            binding.buttonKayit.visibility=View.VISIBLE
                            binding.buttonGiris.visibility=View.VISIBLE
                            binding.sifreUnuttum.visibility=View.VISIBLE
                            binding.textSifreDegis.visibility=View.VISIBLE
                        }.addOnFailureListener {
                            binding.progressBar.visibility=View.GONE
                            Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener {
                        binding.progressBar.visibility=View.GONE
                        Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                    }
                }
            }

        }


    }
}