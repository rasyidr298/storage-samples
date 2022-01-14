/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.ktfiles

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.DocumentsContract.EXTRA_INITIAL_URI
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class MainActivity : AppCompatActivity() {

    private val handleIntentActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val directoryUri = it.data?.data ?: return@registerForActivityResult
            contentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            showDirectoryContents(directoryUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val openDirectoryButton = findViewById<FloatingActionButton>(R.id.fab_open_directory)
        openDirectoryButton.setOnClickListener {
            openDirectory()
        }

        with(fabUnzip) {
            visibility = View.GONE

            setOnClickListener {
                requestPermissionReadWrite()

                if (checkPermissionReadWrite()) {
                    doUnzip()
                }
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val directoryOpen = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.let { actionBar ->
                actionBar.setDisplayHomeAsUpEnabled(directoryOpen)
                actionBar.setDisplayShowHomeEnabled(directoryOpen)
            }

            if (directoryOpen) {
                openDirectoryButton.visibility = View.GONE
            } else {
                openDirectoryButton.visibility = View.VISIBLE
            }
        }
    }

    private fun doUnzip() {

        //coba tambahin file.zip di "Android/data/com.example.android.ktfiles/files" ---> untuk tes file yg akan di unzip
        val sourceFile = File(getExternalFilesDir("")?.absolutePath, "/file.zip")

        //ini beberapa destination Uri yang aku coba
        var destination = "Android/data/com.mobile.legends/files/dragon2017/assets/"
        val uri1 = "/storage/emulated/0/$destination"
        val uri2 = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:$destination")
        val uri3 = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", "primary:$destination")

        val result = Unzip.unzip(sourceFile, uri1)
        Log.e("unzip", result)
        Toast.makeText(this, result, Toast.LENGTH_LONG).show()
    }

    private fun checkPermissionReadWrite(): Boolean {
        return EasyPermissions.hasPermissions(this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun requestPermissionReadWrite() {
        EasyPermissions.requestPermissions(
            this,"getString(R.string.acces)",
            123,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return

            contentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            showDirectoryContents(directoryUri)
        }
    }

    fun showDirectoryContents(directoryUri: Uri) {
        fabUnzip.visibility = View.VISIBLE
        supportFragmentManager.commit {
            val directoryTag = directoryUri.toString()
            val directoryFragment = DirectoryFragment.newInstance(directoryUri)
            replace(R.id.fragment_container, directoryFragment, directoryTag)
            addToBackStack(directoryTag)
        }
    }

    private fun openDirectory() {
        val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Android/data")
        val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", "primary:Android/data")
        contentResolver.persistedUriPermissions.find {
            it.uri.equals(treeUri) && it.isReadPermission
        }?.run {
            showDirectoryContents(treeUri)
        } ?: handleIntentActivityResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).putExtra(EXTRA_INITIAL_URI, uri))
    }
}

private const val OPEN_DIRECTORY_REQUEST_CODE = 0xf11e
