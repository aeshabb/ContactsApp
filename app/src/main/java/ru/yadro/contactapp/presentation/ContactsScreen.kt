package ru.yadro.contactapp.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.yadro.contactapp.domain.model.Contact

@Composable
fun ContactsScreen(contacts: List<Contact>) {
    LazyColumn {
        items(contacts) { contact ->
            Text(
                text = contact.name,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
