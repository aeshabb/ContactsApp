package ru.yadro.contactapp.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.yadro.contactapp.Contact
import ru.yadro.contactapp.IContactService
import ru.yadro.contactapp.R
import ru.yadro.contactapp.service.ContactService
import ru.yadro.contactapp.ui.adapter.ContactsAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private var contactService: IContactService? = null
    private var isBound = false

    private val PERMISSIONS_REQUEST_READ_CONTACTS = 100

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            contactService = IContactService.Stub.asInterface(binder)
            isBound = true
            loadContactsFromService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            contactService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
            bindToContactService()
        }
    }

    private fun bindToContactService() {
        val intent = Intent(this, ContactService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            bindToContactService()
        } else {
            Toast.makeText(
                this,
                "Разрешение на чтение контактов не предоставлено",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadContactsFromService() {
        Thread {
            try {
                val result = contactService?.contacts
                val contacts = result?.map { Contact(it.name, it.phone) } ?: emptyList()
                runOnUiThread {
                    adapter = ContactsAdapter(contacts)
                    recyclerView.adapter = adapter
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
