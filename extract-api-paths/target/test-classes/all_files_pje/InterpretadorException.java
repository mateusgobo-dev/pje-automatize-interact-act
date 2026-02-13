package br.com.infox.editor.interpretadorDocumento;

public class InterpretadorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String mensagem;

    public InterpretadorException(Exception ex) {
        super();
        this.setStackTrace(ex.getStackTrace());
        this.mensagem = "Erro de execução \"" + ex.getClass().getName() + " " + ex.getLocalizedMessage() + "\" acusado ao INTERPRETAR modelo";
    }
    
    public InterpretadorException(String mensagem, int linha) {
        super();
        this.mensagem = mensagem + " (linha " + linha + ")";
    }
    
    @Override
    public String getLocalizedMessage() {
        return mensagem;
    }
    
}
