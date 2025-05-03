package ru.yadro.contactapp.data

import android.content.Context
import android.provider.ContactsContract
import ru.yadro.contactapp.domain.model.Contact

class ContactRepository(private val context: Context) {

    fun getContacts(): List<Contact> {
        val contactList = mutableListOf<Contact>()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                contactList.add(Contact(name))
            }
        }

        return contactList
    }
}
