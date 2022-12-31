package com.harypotter.myapplication.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.harypotter.myapplication.R
import com.harypotter.myapplication.databinding.FragmentCokluZorBinding
import com.harypotter.myapplication.databinding.FragmentTekliKolayBinding
import com.harypotter.myapplication.util.Kart
import com.harypotter.myapplication.util.MemoryCard
import com.harypotter.myapplication.util.Singleton



class CokluZorFragment : Fragment() {
    private var _binding: FragmentCokluZorBinding?=null
    private val binding get() = _binding!!

    private lateinit var sayici: CountDownTimer         //Sayaç
    private var mediaPlayer: MediaPlayer?=null          //Müzik oynatıcı
    private lateinit var buttons:List<ImageView>        //imageView listesi
    private var kartlar=ArrayList<Kart>()               //imageView kartlara dağılan liste
    private lateinit var cards:List<MemoryCard>         //kartların kontrol listesi
    private var indexOfSingleSelectedCard:Int?=null     //kart açıcı kontrol değişkeni
    private var countMatch:Int=0                        //kaç eşmeşme olduğunu sayar
    private var kalanSure:Long=45                       //kalan süre değişkeni
    private var puan1:Double=0.0                        //puan1 değişkeni
    private var puan2:Double=0.0                        //puan2 değişkeni
    private var siraOyuncu=1                        //oyuncu sırası

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding= FragmentCokluZorBinding.inflate(inflater,container,false)
        val view=binding.root
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        //fragment destroy edilirken sayici ve media player kapanır
        _binding=null
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer=null
        sayici.cancel()
    }
    override fun onResume() {
        super.onResume()
        //fragment çalışmaya devam ederken ana müzik tekrarlı çalacak
        mediaPlayer=MediaPlayer.create(this.requireContext(), R.raw.anamuzik)
        mediaPlayer?.start()
        mediaPlayer?.isLooping=true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        muzikCalar()        //müziğin sesini kapatıp açma fonksiyonu
        kartDagitim()       //firebase den gelen listeyi imageView lara dağıtma fonksiyonu
        buttonInit()        //imageView listesinin fonksiyonu
        geriSayim()         //geri sayımı çalıştıran fonksiyon

        when(siraOyuncu){
            1->binding.textOyunSirasi.text="1.Oyuncunun sırası"
            2->binding.textOyunSirasi.text="2.Oyuncunun sırası"
        }

        //imageVİew a yüklenen kartlar ile aynı sirada cards listesi oluşturulur
        cards=buttons.indices.map { index->
            MemoryCard(kartlar[index])
        }

        val notDefteri=ArrayList<String>()      //not defterinde verileri göstermek için liste

        //bütün imageViewlara tıklanınca işlem yapmak için forEach içinde işlem yapıldı
        buttons.forEachIndexed { index, imageView ->
            Log.d("NotDefteri","imageView${index+1}=${kartlar[index].kartIsim}")    //kartların logcat takibi
            notDefteri.add("imageView${index+1}=${kartlar[index].kartIsim}")
            imageView.setOnClickListener {
                updateModels(index)     //en fazla 2 kart açılmasını sağlayan  fonksiyon
                updateViews()           //kartın ön yüzünü ya da arka yüzünü açan fonksiyon
            }
        }

        val notDefterMetin=notDefteri.joinToString("\n")    //listeye alınan kart bilgileri stringe çevrildi
        //image ve kart bilgileri alertdialogda gösterilecek
        binding.buttonNotDefterCZ.setOnClickListener {
            val alert=AlertDialog.Builder(requireContext())
            alert.setMessage(notDefterMetin)
            alert.show()
        }


    }



    private fun geriSayim() {
        sayici=object :CountDownTimer(60000,1000){
            override fun onTick(p0: Long) {
                binding.textViewSureTZ.text="Süre: ${p0/1000}"
                kalanSure=p0/1000
            }

            override fun onFinish() {
                mesajSureBitti()
                sureBittiMuzik()
                buttons.forEach {imageView ->
                    imageView.isEnabled=false
                }
            }

        }.start()
    }

    private fun finish() {
        mesajKazandin()
        zaferMuzik()
        sayici.cancel()
        buttons.forEach {imageView ->
            imageView.isEnabled=false
        }
    }

    private fun updateViews() {
        cards.forEachIndexed { index, card ->
            val imageView=buttons[index]
            if (card.isMatched){
                imageView.alpha=0.1f
            }
            if (card.isFaceUp){
                val bitmap=base64toBitmap(card.identifier.base64)       //base64 ü bitmap a çevirir
                imageView.setImageBitmap(bitmap)
            }else{
                imageView.setImageDrawable(requireContext().getDrawable(R.drawable.kartarka))
            }
        }
    }

    private fun base64toBitmap(base64:String): Bitmap {
        val decodeString= Base64.decode(base64, Base64.DEFAULT)
        val decodeByte= BitmapFactory.decodeByteArray(decodeString,0,decodeString.size)
        return decodeByte
    }

    private fun updateModels(index:Int) {
        val card=cards[index]
        if (card.isFaceUp){
            Toast.makeText(requireContext(),"Geçersiz hamle!", Toast.LENGTH_SHORT).show()
            return
        }
        if (indexOfSingleSelectedCard==null){
            restoreCards()                          // kartlar eşleşmediyse iki kartı da kapat
            indexOfSingleSelectedCard=index
        }else{
            checkForMatch(indexOfSingleSelectedCard!!,index)       //eşleşme var mı kontrol et
            indexOfSingleSelectedCard=null
        }


        card.isFaceUp=!card.isFaceUp
    }

    private fun restoreCards() {
        for (card in cards){
            if (!card.isMatched){
                card.isFaceUp=false
            }
        }
    }

    private fun checkForMatch(position1:Int,position2: Int) {
        if (cards[position1].identifier.kartIsim==cards[position2].identifier.kartIsim){
            // kartlar eşleşti
            cards[position1].isMatched=true
            cards[position2].isMatched=true
            countMatch=countMatch+1

            //ekrana kart bilgilerini anlık getir
            val kartinAdi=cards[position1].identifier.kartIsim
            val kartinPuani=cards[position1].identifier.kartPuan
            val kartinEvi=cards[position1].identifier.ev
            Toast.makeText(requireContext(),"${kartinAdi},(Puan:${kartinPuani},Ev:${kartinEvi})",Toast.LENGTH_SHORT).show()

            when(siraOyuncu){
                1->{
                    //1. oyuncu puanEkle
                    val kartPuan=cards[position1].identifier.kartPuan
                    val evPuan=cards[position1].identifier.evPuan
                    puan1=puan1+(2*kartPuan*evPuan)
                    binding.textPuan1.text="1.Oyuncu: ${puan1.toInt()} puan"
                }
                2->{
                    //2. oyuncu puanEkle
                    val kartPuan=cards[position1].identifier.kartPuan
                    val evPuan=cards[position1].identifier.evPuan
                    puan2=puan2+(2*kartPuan*evPuan)
                    binding.textPuan2.text="2.Oyuncu: ${puan2.toInt()} puan"
                }
            }
            //eşleşme müziği çal
            if (countMatch==18){
                finish()           //18  eşleşmede oyun biter
            }else if (countMatch>0 && countMatch<18){
                eslesmeMuzik()      //18 eşleşmeden küçükse hepsinde esleşme müziği çal
            }

        }else{
            //kartlar eşleşmedi
            //hangi oyuncudaydı
            val kart1Puan=cards[position1].identifier.kartPuan
            val kart2Puan=cards[position2].identifier.kartPuan
            val ev1Puan=cards[position1].identifier.evPuan
            val ev2Puan=cards[position2].identifier.evPuan
            val kart1Ev=cards[position1].identifier.ev
            val kart2Ev=cards[position2].identifier.ev

            when(siraOyuncu){
                1->{
                    siraOyuncu=2
                    binding.textOyunSirasi.text="${siraOyuncu}.Oyuncunun sırası"
                    //1. oyuncudan puanÇıkar
                    if (kart1Ev==kart2Ev){
                        puan1=puan1-((kart1Puan+kart2Puan)/ev1Puan)
                        binding.textPuan1.text="1.Oyuncu: ${puan1.toInt()} puan"
                    }else{
                        puan1=puan1-((kart1Puan+kart2Puan)/2*ev1Puan*ev2Puan)
                        binding.textPuan1.text="1.Oyuncu: ${puan1.toInt()} puan"
                    }
                }
                2->{
                    siraOyuncu=1
                    binding.textOyunSirasi.text="${siraOyuncu}.Oyuncunun sırası"
                    //2. oyuncudan puanÇıkar
                    if (kart1Ev==kart2Ev){
                        puan2=puan2-((kart1Puan+kart2Puan)/ev1Puan)
                        binding.textPuan2.text="2.Oyuncu: ${puan2.toInt()} puan"
                    }else{
                        puan2=puan2-((kart1Puan+kart2Puan)/2*ev1Puan*ev2Puan)
                        binding.textPuan2.text="2.Oyuncu: ${puan2.toInt()} puan"
                    }
                }
            }

        }
    }

    private fun muzikCalar() {
        binding.music.setOnClickListener {
            if (mediaPlayer!!.isPlaying){
                mediaPlayer?.pause()
                binding.music.setImageDrawable(requireContext().getDrawable(R.drawable.musicoff))
            }else{
                mediaPlayer=MediaPlayer.create(this.requireContext(), R.raw.anamuzik)
                mediaPlayer?.start()
                mediaPlayer?.isLooping=true
                binding.music.setImageDrawable(requireContext().getDrawable(R.drawable.musicon))
            }
        }
    }

    private fun sureBittiMuzik() {
        if (mediaPlayer!!.isPlaying){
            mediaPlayer!!.stop()
            mediaPlayer=null
            mediaPlayer=MediaPlayer.create(requireContext(),R.raw.surebitti)
            mediaPlayer?.start()
        }
    }

    private fun eslesmeMuzik() {
        if (mediaPlayer!!.isPlaying){
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer=null
            mediaPlayer=MediaPlayer.create(requireContext(),R.raw.kartbulundu)
            mediaPlayer?.start()
        }

        //eşleşme müziği bitince tekrar ana müziği çal
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer=MediaPlayer.create(this.requireContext(), R.raw.anamuzik)
            mediaPlayer?.start()
            mediaPlayer?.isLooping=true
        }
    }

    private fun zaferMuzik() {
        if (mediaPlayer!!.isPlaying){
            mediaPlayer!!.stop()
            mediaPlayer=null
            mediaPlayer=MediaPlayer.create(requireContext(),R.raw.kazandin)
            mediaPlayer?.start()
        }
    }

    private fun buttonInit() {
        buttons= listOf(binding.imageKart1TZ,binding.imageKart2TZ,binding.imageKart3TZ,binding.imageKart4TZ,binding.imageKart5TZ,
            binding.imageKart6TZ,binding.imageKart7TZ,binding.imageKart8TZ,binding.imageKart9TZ,binding.imageKart10TZ,binding.imageKart11TZ,
            binding.imageKart12TZ,binding.imageKart13TZ,binding.imageKart14TZ,binding.imageKart15TZ,binding.imageKart16TZ,binding.imageKart17TZ,
            binding.imageKart18TZ,binding.imageKart19TZ,binding.imageKart20TZ,binding.imageKart21TZ,binding.imageKart22TZ,binding.imageKart23TZ,
            binding.imageKart24TZ,binding.imageKart25TZ,binding.imageKart26TZ,binding.imageKart27TZ,binding.imageKart28TZ,binding.imageKart29TZ,
            binding.imageKart30TZ,binding.imageKart31TZ,binding.imageKart32TZ,binding.imageKart33TZ,binding.imageKart34TZ,binding.imageKart35TZ,
            binding.imageKart36TZ)
    }

    private fun kartDagitim() {
        Singleton.kartList.shuffle()
        kartlar.clear()

        val kartListGryfinndor= Singleton.kartList.filter { it.ev=="GRYFFİNDOR" }
        val kartListSlytherin= Singleton.kartList.filter { it.ev=="SLYTHERİN" }
        val kartListRavenclaw= Singleton.kartList.filter { it.ev=="RAVENCLAW" }
        val kartlistHufflepuff= Singleton.kartList.filter { it.ev=="HUFFLEPUFF" }
        kartListGryfinndor.shuffled()
        kartListSlytherin.shuffled()
        kartListRavenclaw.shuffled()
        kartlistHufflepuff.shuffled()

        for (x in 0..4){
            kartlar.add(kartListGryfinndor[x])
        }

        for (x in 0..4){
            kartlar.add(kartListSlytherin[x])
        }

        for (x in 0..3){
            kartlar.add(kartListRavenclaw[x])
        }

        for (x in 0..3){
            kartlar.add(kartlistHufflepuff[x])
        }

        kartlar.addAll(kartlar)
        kartlar.shuffle()
    }

    private fun mesajKazandin(){
        var kazanan=""
        if (puan1>puan2){
            kazanan="1.Oyuncu Kazandı"
        }else if (puan2>puan1){
            kazanan="2.Oyuncu Kazandı"
        }
        val builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle(kazanan)
        builder.setMessage("1.Oyuncu:${puan1.toInt()} puan"+"\n"+ "2.Oyuncu:${puan2.toInt()} puan")
        builder.show()
    }

    private fun mesajSureBitti(){
        val builder = AlertDialog.Builder(this.requireContext())
        builder.setTitle("Süre Bitti")
        builder.setMessage("1.Oyuncu:${puan1.toInt()} puan"+"\n"+ "2.Oyuncu:${puan2.toInt()} puan")
        builder.show()
    }




}