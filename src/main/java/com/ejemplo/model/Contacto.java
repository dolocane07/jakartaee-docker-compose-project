package com.ejemplo.model;

public class Contacto {
    private int ideCon;
    private String nomCon;
    private int tlfCon;

    public Contacto() {
    }

    public Contacto(int ideCon, String nomCon, int tlfCon) {
        this.ideCon = ideCon;
        this.nomCon = nomCon;
        this.tlfCon = tlfCon;
    }

    public int getIdeCon() {
        return ideCon;
    }

    public void setIdeCon(int ideCon) {
        this.ideCon = ideCon;
    }

    public String getNomCon() {
        return nomCon;
    }

    public void setNomCon(String nomCon) {
        this.nomCon = nomCon;
    }

    public int getTlfCon() {
        return tlfCon;
    }

    public void setTlfCon(int tlfCon) {
        this.tlfCon = tlfCon;
    }
}
