package ru.yadro.contactapp.model

import ru.yadro.contactapp.Contact

sealed class ContactListItem {
    data class Header(val letter: String) : ContactListItem()
    data class ContactItem(val contact: Contact) : ContactListItem()
}
