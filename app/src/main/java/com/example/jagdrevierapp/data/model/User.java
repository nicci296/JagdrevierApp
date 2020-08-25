package com.example.jagdrevierapp.data.model;

public class User {

    /**
     * **************23.08.20 Nico *******************************************
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * Attribute, Constructor, Getter und Setter implementiert.
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private String mail;
    private String nick;
    private boolean paechter;
    private String registered;


    public User() {
    }

    public User(String mail, String nick, boolean paechter, String registered) {
        this.mail = mail;
        this.nick = nick;
        this.paechter = paechter;
        this.registered = registered;


    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public boolean isPaechter() {
        return paechter;
    }

    public void setPaechter(boolean paechter) {
        this.paechter = paechter;
    }

    public String getRegistered() { return registered; }

    public void setRegistered(String registered) { this.registered = registered; }
}
