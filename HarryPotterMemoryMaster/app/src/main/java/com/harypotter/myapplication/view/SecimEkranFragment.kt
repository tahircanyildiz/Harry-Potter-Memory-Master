package com.harypotter.myapplication.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.harypotter.myapplication.R
import com.harypotter.myapplication.databinding.FragmentSecimEkranBinding
import com.harypotter.myapplication.util.Kart
import com.harypotter.myapplication.util.Singleton
import com.harypotter.myapplication.util.Singleton.kartList
import java.io.ByteArrayOutputStream


class SecimEkranFragment : Fragment() {
    private var _binding: FragmentSecimEkranBinding?=null
    private val binding get() = _binding!!
    private var oyuncuModu:String?=null
    private var oyuncuSeviye:String?=null
    private lateinit var firestore:FirebaseFirestore
    private var veriBilgi=false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding= FragmentSecimEkranBinding.inflate(inflater,container,false)
        val view=binding.root
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore=Firebase.firestore

        //oyun verileri bir kere çekilecek daha sonra aynı veriler kullanılabilecek
        if (veriBilgi==false){
            veriAl()
            veriBilgi=true
        }else{
            binding.progressBarSecim.visibility=View.GONE
        }


        //butonların işlevleri
        binding.buttonTekOyuncu.setOnClickListener {
            it.setBackgroundColor(requireActivity().getColor(R.color.green))
            binding.buttonCokluOyuncu.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            oyuncuModu="TekOyuncu"
        }

        binding.buttonCokluOyuncu.setOnClickListener {
            it.setBackgroundColor(requireActivity().getColor(R.color.green))
            binding.buttonTekOyuncu.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            oyuncuModu="ÇokluOyuncu"
        }

        binding.buttonKolay.setOnClickListener {
            it.setBackgroundColor(requireActivity().getColor(R.color.green))
            binding.buttonOrta.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            binding.buttonZor.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            oyuncuSeviye="kolay"
        }

        binding.buttonOrta.setOnClickListener {
            it.setBackgroundColor(requireActivity().getColor(R.color.green))
            binding.buttonKolay.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            binding.buttonZor.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            oyuncuSeviye="orta"
        }

        binding.buttonZor.setOnClickListener {
            it.setBackgroundColor(requireActivity().getColor(R.color.green))
            binding.buttonKolay.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            binding.buttonOrta.setBackgroundColor(requireActivity().getColor(R.color.purple_200))
            oyuncuSeviye="zor"
        }

        binding.buttonBasla.setOnClickListener {
            //oyuncu modu ve seviyesi seçildi ise oyuna başlatır
            if (oyuncuModu!=null && oyuncuSeviye!=null){
                if (oyuncuModu=="TekOyuncu"){
                    when(oyuncuSeviye){
                        "kolay"->{
                            val action=SecimEkranFragmentDirections.actionSecimEkranFragmentToTekliKolayFragment()
                            Navigation.findNavController(it).navigate(action)
                        }
                        "orta"->{
                            val action=SecimEkranFragmentDirections.actionSecimEkranFragmentToTekliOrtaFragment()
                            Navigation.findNavController(it).navigate(action)
                        }
                        "zor"->{
                            val action=SecimEkranFragmentDirections.actionSecimEkranFragmentToTekliZorFragment()
                            Navigation.findNavController(it).navigate(action)
                        }
                    }
                }else if (oyuncuModu=="ÇokluOyuncu"){
                    when(oyuncuSeviye){
                        "kolay"->{
                            val action=SecimEkranFragmentDirections.actionSecimEkranFragmentToCokluKolayFragment()
                            Navigation.findNavController(it).navigate(action)
                        }
                        "orta"->{
                            val action=SecimEkranFragmentDirections.actionSecimEkranFragmentToCokluOrtaFragment()
                            Navigation.findNavController(it).navigate(action)
                        }
                        "zor"->{
                            val action=SecimEkranFragmentDirections.actionSecimEkranFragmentToCokluZorFragment()
                            Navigation.findNavController(it).navigate(action)
                        }
                    }
                }
            }else{
                //oyun modu ve seviyeden birisi seçilmediği zaman
                Toast.makeText(this.requireContext(),"Oyun modunu ve seviyesini seçin!", Toast.LENGTH_SHORT).show()
            }
        }


    }

    //sunucudan verileri alır
    private fun veriAl(){
        kartList.clear()
        Toast.makeText(requireContext(),"Oyun verileri yükleniyor,lütfen bekleyin",Toast.LENGTH_SHORT).show()
        binding.progressBarSecim.visibility=View.VISIBLE
        binding.buttonBasla.isEnabled=false
        firestore.collection("Kartlar").get().addOnSuccessListener {
            if (it!=null){
                if (!it.isEmpty){
                    binding.progressBarSecim.visibility=View.GONE
                    binding.buttonBasla.isEnabled=true
                    val documents=it.documents
                    for (document in documents){
                        val base64=document.get("base64") as String
                        val ev=document.get("ev") as String
                        val evPuan=document.get("evPuan") as String
                        val kartIsim=document.get("kartIsim") as String
                        val kartPuan=document.get("kartPuan") as String
                        val kart= Kart(ev,evPuan.toInt(),kartIsim,kartPuan.toInt(),base64)
                        kartList.add(kart)
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),"Oyun dataları alınamadı,Oyunu tekrar açmayı deneyin!", Toast.LENGTH_SHORT).show()
            binding.progressBarSecim.visibility=View.GONE
            binding.buttonBasla.isEnabled=false
        }
    }



}