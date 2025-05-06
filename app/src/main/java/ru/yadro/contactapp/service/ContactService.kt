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
import ru.yadro.contactapp.IDeleteCallback

class ContactService : Service() {

    private val binder = object : IContactService.Stub() {

        override fun getContacts(): MutableList<Contact> {
            val contacts = mutableListOf<Contact>()

            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
            ) return contacts

            val resolver = contentResolver
            val cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                ),
                null, null, null
            )

            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

                while (it.moveToNext()) {
                    val name = it.getString(nameIdx) ?: continue
                    val phone = it.getString(numberIdx) ?: continue
                    val contactId = it.getString(idIdx) ?: continue

                    if (isSimContact(contactId)) continue

                    contacts.add(Contact(name, phone))
                }
            }

            return contacts
        }

        override fun deleteDuplicateContacts(callback: IDeleteCallback?) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                callback?.onError("Нет разрешения на изменение контактов")
                return
            }

            try {
                val resolver = contentResolver
                val allContacts = getContacts()

                val grouped = allContacts.groupBy {
                    val normalizedName = it.name.trim().lowercase()
                    val normalizedPhone = normalizePhoneNumber(it.phone)
                    "$normalizedName|$normalizedPhone"
                }

                val deletedIds = mutableSetOf<String>()
                var deletedCount = 0

                for ((_, duplicates) in grouped) {
                    if (duplicates.size > 1) {
                        val contactsToDelete = duplicates.drop(1)

                        for (contact in contactsToDelete) {
                            val normalizedPhone = normalizePhoneNumber(contact.phone)

                            val cursor = resolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID),
                                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?",
                                arrayOf(contact.name),
                                null
                            )

                            cursor?.use {
                                while (it.moveToNext()) {
                                    val contactId = it.getString(
                                        it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                                    )

                                    if (deletedIds.contains(contactId)) continue
                                    if (isSimContact(contactId)) continue

                                    val phoneCursor = resolver.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                        arrayOf(contactId),
                                        null
                                    )

                                    var matched = false
                                    phoneCursor?.use { pc ->
                                        while (pc.moveToNext()) {
                                            val actualNumber = pc.getString(
                                                pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                            )
                                            if (normalizePhoneNumber(actualNumber) == normalizedPhone) {
                                                matched = true
                                                break
                                            }
                                        }
                                    }

                                    if (matched) {
                                        resolver.delete(
                                            ContactsContract.RawContacts.CONTENT_URI,
                                            "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                                            arrayOf(contactId)
                                        )
                                        deletedIds.add(contactId)
                                        deletedCount++
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                if (deletedCount > 0) {
                    callback?.onSuccess("Удалено $deletedCount повторяющихся контактов")
                } else {
                    callback?.onSuccess("Повторяющиеся контакты не найдены")
                }

            } catch (e: Exception) {
                callback?.onError("Ошибка при удалении дубликатов: ${e.message}")
            }
        }
    }

    private fun normalizePhoneNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }

        return when {
            digits.length == 11 && digits.startsWith("8") -> "7" + digits.substring(1)
            digits.length == 11 && digits.startsWith("7") -> digits
            digits.length == 10 -> "7$digits"
            else -> digits
        }
    }

    private fun isSimContact(contactId: String): Boolean {
        val resolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts.ACCOUNT_TYPE),
            "${ContactsContract.RawContacts.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val accountType = it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE)
                )
                return accountType?.lowercase()?.contains("sim") == true || accountType?.lowercase()?.contains("sd") == true
            }
        }
        return false
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
