package br.com.infox.editor.interpretadorDocumento;

public class NomeVariavelInvalidoException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String mensagem;

    public NomeVariavelInvalidoException(String nome) {
        super();
        this.mensagem = "Nome de variável \"" + nome + "\" inválido. Não pode conter \".\".";
    }
    
    @Override
    public String getLocalizedMessage() {
        return mensagem;
    }
}