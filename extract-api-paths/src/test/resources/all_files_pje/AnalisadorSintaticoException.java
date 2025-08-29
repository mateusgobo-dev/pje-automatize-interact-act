package br.com.infox.editor.interpretadorDocumento;

public class AnalisadorSintaticoException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String mensagem;

    public AnalisadorSintaticoException(Exception ex) {
        super();
        this.setStackTrace(ex.getStackTrace());
        this.mensagem = "Erro de execução \"" + ex.getClass().getName() + " " + ex.getLocalizedMessage() + "\" acusado ao analisar a SINTAXE do modelo";
    }
    
    public AnalisadorSintaticoException(String mensagem, int linha) {
        super();
        this.mensagem = mensagem + " (linha " + linha + ")";
    }
    
    @Override
    public String getLocalizedMessage() {
        return mensagem;
    }
    
}
