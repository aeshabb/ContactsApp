package ru.yadro.contactapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import ru.yadro.contactapp.data.ContactRepository
import ru.yadro.contactapp.domain.usecase.GetContactsUseCase
import ru.yadro.contactapp.ui.theme.ContactAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var getContactsUseCase: GetContactsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getContactsUseCase = GetContactsUseCase(ContactRepository(this))

        checkPermissionsAndShowContacts()
    }

    private fun checkPermissionsAndShowContacts() {
        val readGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (readGranted && writeGranted) {
            showContactsUI()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] ?: false

        if (readGranted && writeGranted) {
            showContactsUI()
        } else {
            Toast.makeText(this, "Разрешения не предоставлены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContactsUI() {
        val contacts = getContactsUseCase()
        setContent {
            ContactAppTheme {
                ContactsScreen(contacts)
            }
        }
    }
}
