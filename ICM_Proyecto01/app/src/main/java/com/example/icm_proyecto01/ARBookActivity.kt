package com.example.icm_proyecto01

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.icm_proyecto01.model.UserBook
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class ARBookActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private var userBook: UserBook? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, StepService::class.java))
        setContentView(R.layout.activity_ar_book)

        userBook = intent.getSerializableExtra("USER_BOOK") as? UserBook

        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("HiddenBooks").child(userBook!!.id).child("locationHint")
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val hint = snapshot.getValue(String::class.java)
                    if (!hint.isNullOrBlank()) {
                        Toast.makeText(this@ARBookActivity, "Pista: $hint", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Toast.makeText(this@ARBookActivity, "No se pudo cargar la pista", Toast.LENGTH_SHORT).show()
                }
            })


        if (userBook == null) {
            Toast.makeText(this, "Error: libro no cargado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
            return
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            if (plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                val anchor = hitResult.createAnchor()
                placeBookWithAR(anchor)
            }
        }
    }

    private fun placeBookWithAR(anchor: Anchor) {
        ViewRenderable.builder()
            .setView(this, R.layout.render_book_view)
            .build()
            .thenAccept { renderable ->
                val view: View = renderable.view
                val titleText = view.findViewById<TextView>(R.id.bookTitle)
                val imageView = view.findViewById<ImageView>(R.id.bookImage)
                val btnPick = view.findViewById<Button>(R.id.btnPickBook)

                titleText.text = userBook?.titulo ?: "Libro"
                Picasso.get().load(userBook?.portadaUrl)
                    .placeholder(R.drawable.default_book)
                    .into(imageView)

                btnPick.setOnClickListener {
                    recogerLibro()
                }

                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment.arSceneView.scene)

                val node = TransformableNode(arFragment.transformationSystem)
                node.renderable = renderable
                node.setParent(anchorNode)
                node.select()
            }
            .exceptionally {
                Toast.makeText(this, "Error al crear objeto AR", Toast.LENGTH_SHORT).show()
                null
            }
    }

    private fun recogerLibro() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().reference

        userBook!!.hidden = false
        userBook!!.status = "activo"
        dbRef.child("Books").child(userBook!!.id).child("hidden").setValue(false)

        dbRef.child("Users").child(uid).child("Books").child(userBook!!.id).setValue(userBook)
            .addOnSuccessListener {
                registrarLibroEncontrado(userBook!!)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar el libro", Toast.LENGTH_SHORT).show()
            }
    }


    private fun registrarLibroEncontrado(userBook: UserBook) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().reference
        val hiddenBookRef = dbRef.child("HiddenBooks").child(userBook.id)

        hiddenBookRef.child("finderUserId").setValue(currentUserId)
            .addOnSuccessListener {
                Toast.makeText(this, "Libro agregado a tu colección", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar el libro", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate()
        } else {
            Toast.makeText(this, "Se requiere permiso de cámara para AR", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
