package ru.yadro.contactapp;

interface IDeleteCallback {
    void onSuccess(String message);
    void onError(String error);
}
