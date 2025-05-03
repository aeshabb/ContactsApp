package ru.yadro.contactapp.domain.usecase

import ru.yadro.contactapp.data.ContactRepository
import ru.yadro.contactapp.domain.model.Contact

class GetContactsUseCase(private val repository: ContactRepository) {
    operator fun invoke(): List<Contact> = repository.getContacts()
}
