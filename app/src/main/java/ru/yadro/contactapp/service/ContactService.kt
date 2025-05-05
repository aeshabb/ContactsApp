package ru.yadro.contactapp.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import ru.yadro.contactapp.Contact
import ru.yadro.contactapp.IContactService

class ContactService : Service() {

    private val binder = object : IContactService.Stub() {
        override fun getContacts(): MutableList<Contact> {
            val contacts = mutableListOf<Contact>()

            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                return contacts
            }

            val contentResolver = applicationContext.contentResolver
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
            )

            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val name = it.getString(nameIdx) ?: continue
                    val phone = it.getString(numberIdx) ?: continue
                    contacts.add(Contact(name, phone))
                }
            }

            return contacts
        }

    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
