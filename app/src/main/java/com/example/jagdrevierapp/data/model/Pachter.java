package com.example.jagdrevierapp.data.model;

import java.util.ArrayList;

//Modellklasse zum Anlegen einer Pächter-Collection im Firestore
public class Pachter {
    private String mail;

    public Pachter () {}

    public Pachter (String mail) {
        this.mail = mail;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

}