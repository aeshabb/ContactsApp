package ru.yadro.contactapp;

import ru.yadro.contactapp.Contact;
import ru.yadro.contactapp.IDeleteCallback;

interface IContactService {
    List<Contact> getContacts();
    void deleteDuplicateContacts(in IDeleteCallback callback);
}
