package com.adrianbl.traductorml

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.adrianbl.traductorml.modelo.Idioma
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity : AppCompatActivity(),TextToSpeech.OnInitListener {
    
    lateinit var ti_entrada_texto: TextInputEditText
    lateinit var ti_salida_texto_traducido: TextInputEditText
    lateinit var tv_idioma_identificado: TextView
    lateinit var btn_elegir_idioma_entrada: MaterialButton
    lateinit var btn_elegir_idioma_salida:MaterialButton
    lateinit var btn_traducir:MaterialButton
    lateinit var btn_mostrar_dialogo: ImageButton

    lateinit var btn_iniciar_entrada_audio: MaterialButton

    private var idiomasArrayList: ArrayList<Idioma>? = null
    private val REGISTROS = "Mis registros"

    private var codigo_idioma_origen = "es"
    private var titulo_idioma_origen = "Español"
    private var codigo_idioma_destino = "en"
    private var titulo_idioma_destino = "Inglés"

    private var progressDialog: ProgressDialog? = null
    private lateinit var new_progressDialog: SweetAlertDialog

    private var translatorOptions: TranslatorOptions? = null
    private var translator: Translator? = null
    private var texto_idioma_origen = ""

    private val REQUEST_CODE_SPEECH_INPUT = 100

    private var tts: TextToSpeech? = null
    private var btnSpeak: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
        app()
    }

    private fun setup() {
        this.ti_entrada_texto = findViewById(R.id.ti_entrada_texto)
        this.ti_salida_texto_traducido = findViewById(R.id.ti_salida_texto_traducido)
        this.tv_idioma_identificado = findViewById(R.id.tv_idioma_identificado)
        this.btn_elegir_idioma_entrada = findViewById(R.id.btn_elegir_idioma_entrada)
        this.btn_elegir_idioma_salida = findViewById(R.id.btn_elegir_idioma_salida)
        this.btn_traducir = findViewById(R.id.btn_traducir)

        this.btn_mostrar_dialogo = findViewById(R.id.mostrar_dialogo)
        this.btn_iniciar_entrada_audio = findViewById(R.id.btn_iniciar_entrada_audio)

        this.btnSpeak = findViewById(R.id.activar_voz)
        btnSpeak!!.isEnabled = false

        this.tts = TextToSpeech(this, this)

        this.progressDialog = ProgressDialog(this)
        this.progressDialog!!.setTitle("Espere por favor")
        this.progressDialog!!.setCanceledOnTouchOutside(false)

        this.new_progressDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        this.new_progressDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        this.new_progressDialog.setTitleText("Cargando")
        this.new_progressDialog.setCancelable(false)
    }

    private fun app() {
        idiomasDisponibles()

        ti_entrada_texto.addTextChangedListener {
            identificarIdioma()
        }

        btn_iniciar_entrada_audio.setOnClickListener {
            startVoiceInput()
        }

        btn_elegir_idioma_entrada.setOnClickListener {
            Toast.makeText(this,"Elegir idioma",Toast.LENGTH_SHORT).show()
            elegirIdiomaOrigen()
        }

        btn_elegir_idioma_salida.setOnClickListener {
            Toast.makeText(this,"Elegir idioma",Toast.LENGTH_SHORT).show()
            elegirIdiomaDestino()
        }

        btn_traducir.setOnClickListener {
            validarDatos()
        }

        btnSpeak?.setOnClickListener {
            speakOut()
        }

        btn_mostrar_dialogo.setOnClickListener {
            mostrarDialogo()
        }
    }

    private fun mostrarDialogo() {
        //Asignacion de valores
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_about, null)

        //Pasando la vista al Builder
        builder.setView(view)

        //Creando el dialog
        val dialog = builder.create()

        val btn_ok = dialog.findViewById<Button>(R.id.btn_ok)
        btn_ok?.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }

    private fun identificarIdioma() {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(ti_entrada_texto.text.toString().trim { it <= ' ' })
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Log.i(TAG, "Idioma no identificado.")
                } else {
                    Log.i(TAG, "Idioma identificado:  $languageCode")
                    tv_idioma_identificado.text = "Idioma identificado:  $languageCode"
                }
            }
            .addOnFailureListener { e ->
                // Model couldn’t be loaded or other internal error.
                Log.d(REGISTROS, "onFailure: $e")
                Toast.makeText(this, "onFailure: " + e, Toast.LENGTH_SHORT).show()
            }
    }

    private fun idiomasDisponibles(){
        idiomasArrayList = arrayListOf()
        val ListaCodigoIdioma = TranslateLanguage.getAllLanguages()

        for (codigo_lenguaje in ListaCodigoIdioma) {
            val titulo_lenguaje = Locale(codigo_lenguaje).displayLanguage

            Log.d(REGISTROS, "IdiomasDisponibles: codigo_lenguaje: "+ codigo_lenguaje)
            Log.d(REGISTROS, "IdiomasDisponibles: titulo_lenguaje: "+ titulo_lenguaje)

            val modeloIdioma = Idioma(codigo_lenguaje, titulo_lenguaje)
            idiomasArrayList!!.add(modeloIdioma)
        }
    }

    private fun elegirIdiomaOrigen() {
        val popupMenu = PopupMenu(this, btn_elegir_idioma_entrada)
        for (i in idiomasArrayList!!.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, idiomasArrayList!![i].titulo_idioma.toUpperCase())
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val posicion = item.itemId
            codigo_idioma_origen = idiomasArrayList!![posicion].codigo_idioma
            titulo_idioma_origen = idiomasArrayList!![posicion].titulo_idioma
            btn_elegir_idioma_entrada.text = titulo_idioma_origen
            ti_entrada_texto.setHint("Ingrese texto en: $titulo_idioma_origen")
            Log.d(REGISTROS, "onMenuItemClick: codigo_idioma_origen: $codigo_idioma_origen")
            Log.d(REGISTROS, "onMenuItemClick: titulo_idioma_origen: $titulo_idioma_origen")
            false
        }
    }

    private fun elegirIdiomaDestino() {
        val popupMenu = PopupMenu(this, btn_elegir_idioma_salida)
        for (i in idiomasArrayList!!.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, idiomasArrayList!![i].titulo_idioma.toUpperCase())
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val posicion = item.itemId
            codigo_idioma_destino = idiomasArrayList!![posicion].codigo_idioma
            titulo_idioma_destino = idiomasArrayList!![posicion].titulo_idioma
            btn_elegir_idioma_salida.text = titulo_idioma_destino
            Log.d(REGISTROS, "onMenuItemClick: codigo_idioma_destino: $codigo_idioma_destino")
            Log.d(REGISTROS, "onMenuItemClick: titulo_idioma_destino: $titulo_idioma_destino")
            false
        }
    }

    private fun validarDatos() {
        texto_idioma_origen = ti_entrada_texto.getText().toString().trim { it <= ' ' }
        Log.d(REGISTROS, "ValidarDatos: Texto_idioma_origen: $texto_idioma_origen")

        if (texto_idioma_origen.isEmpty()) {
            Toast.makeText(this, "Ingrese texto", Toast.LENGTH_SHORT).show()
        } else {
            traducirTexto()
        }
    }

    private fun traducirTexto(){
        //progressDialog!!.setMessage("Procesando")
        //progressDialog!!.show()

        new_progressDialog.setTitleText("Procesando")
        new_progressDialog.show()

        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(codigo_idioma_origen)
            .setTargetLanguage(codigo_idioma_destino)
            .build()

        translator = Translation.getClient(translatorOptions!!)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator!!.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                //Los paquetes de traducción se descargaron con éxito
                Log.d(REGISTROS, "onSuccess: El paquete se ha descargado con éxito")
                //progressDialog!!.setMessage("Traduciendo texto")
                new_progressDialog.setTitleText("Traduciendo texto")

                translator!!.translate(texto_idioma_origen)
                    .addOnSuccessListener { texto_traducido ->
                        //progressDialog!!.dismiss()
                        new_progressDialog.dismiss()
                        Log.d(REGISTROS, "onSuccess: texto_traducido: $texto_traducido")
                        ti_salida_texto_traducido.setText(texto_traducido)
                    }.addOnFailureListener { e ->
                        //progressDialog!!.dismiss()
                        new_progressDialog.dismiss()
                        Log.d(REGISTROS, "onFailure: $e")
                        Toast.makeText(this, "onFailure: " + e, Toast.LENGTH_SHORT).show()
                    }

            }.addOnFailureListener { e ->
                //Los paquetes no se descargaron
                //progressDialog!!.dismiss()
                new_progressDialog.dismiss()
                Log.d(REGISTROS, "onFailure: $e")
                Toast.makeText(this, "onFailure: " + e, Toast.LENGTH_SHORT).show()
            }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla algo")

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            // Manejar excepciones, si existen
            Log.d(REGISTROS, "onFailure: $e")
            Toast.makeText(this, "onFailure: " + e, Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Obtener el texto reconocido
                    val recognizedText = result?.get(0)
                    // Insertar el texto en el EditText
                    ti_entrada_texto.setText(recognizedText)
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","¡El idioma no es compatible!")
            } else {
                btnSpeak!!.isEnabled = true
            }
        }
    }
    private fun speakOut() {
        val text =  ti_salida_texto_traducido.text.toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    public override fun onDestroy() {
        // Apagar TTS cuando se destruya la actividad
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}