package br.com.just.peticao.inicial.test.factory;

public class Peticao {
    private Long id;
    private String nome;
    private Peticao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public static Peticao instanceOf(Long id, String nome) {
        return new Peticao(id, nome);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
