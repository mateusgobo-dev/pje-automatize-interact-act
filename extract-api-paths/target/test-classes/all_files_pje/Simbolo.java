package br.com.infox.editor.interpretadorDocumento;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author marcosscapin
 */
public class Simbolo {
    
    private String lexema;
    private String token;
    private String tipo;
    private String valor;

    public Simbolo(String lexema, String token) {
        this.lexema = lexema;
        this.token = token;
        this.tipo = null;
        this.valor = "";
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
    
}
