package br.com.infox.editor.interpretadorDocumento;

public class TipoValorInvalidoException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String mensagem;

    public TipoValorInvalidoException(Class classe) {
        super();
        this.mensagem = "Tipo de variável " + classe.getName() + " inválido. Os tipos aceitos são: Boolean, Date, Integer, Double, String, List e Map.";
    }
    
    @Override
    public String getLocalizedMessage() {
        return mensagem;
    }
}