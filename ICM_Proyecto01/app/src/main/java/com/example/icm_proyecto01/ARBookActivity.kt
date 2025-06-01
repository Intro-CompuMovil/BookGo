package com.example.icm_proyecto01

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Color as SceneColor
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ARBookActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_book)

        // Verificar permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
            return
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        // Esperar a que el plano esté detectado y colocar objeto
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                val anchor = hitResult.createAnchor()
                placeObject(anchor)
            }
        }
    }

    private fun placeObject(anchor: Anchor) {
        MaterialFactory.makeOpaqueWithColor(this, SceneColor(Color.RED))
            .thenAccept { material ->
                val cube = ShapeFactory.makeCube(
                    com.google.ar.sceneform.math.Vector3(0.1f, 0.1f, 0.1f),
                    com.google.ar.sceneform.math.Vector3(0f, 0.05f, 0f),
                    material
                )
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment.arSceneView.scene)

                val node = TransformableNode(arFragment.transformationSystem)
                node.renderable = cube
                node.setParent(anchorNode)
                node.select()
            }
            .exceptionally {
                Toast.makeText(this, "No se pudo crear el objeto AR", Toast.LENGTH_SHORT).show()
                null
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
