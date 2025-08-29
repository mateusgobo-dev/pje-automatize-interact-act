package br.com.infox.editor.interpretadorDocumento;

public class AnalisadorLexicoException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	String mensagem;

    public AnalisadorLexicoException(Exception ex, int linha) {
        super();
        this.setStackTrace(ex.getStackTrace());
        this.mensagem = "Erro de execução \"" + ex.getClass().getName() + " " + ex.getLocalizedMessage() + "\" acusado ao analisar a LINHA " + linha + " do modelo";
    }
    
    public AnalisadorLexicoException(String mensagem, int linha) {
        super();
        this.mensagem = "Erro \"" + mensagem + "\" de compilação do modelo na linha " + linha;
    }
    
    @Override
    public String getLocalizedMessage() {
        return mensagem;
    }
    
}
