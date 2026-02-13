package br.com.infox.editor.interpretadorDocumento;

/**
    * Classe que o tratamento de exceção na geração de texto baseado nos modelos segundo a linguagem formal.
    * <p>
    * @see      InterpretadorDocumentos
    * @author   Marcos Scapin, Francis Tscheliski
*/
public class LinguagemFormalException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String mensagem;

    public LinguagemFormalException(Exception ex) {
        super();
        this.setStackTrace(ex.getStackTrace());
        mensagem = ex.getLocalizedMessage();
    }
    
    @Override
    /**
     * Se forem encontrados erros durante a geração de texto baseado nos modelos segundo a linguagem formal, 
     * o detalhe do erro pode ser obtido por esse método.
     * <p>
     * @return  String contendo o detalhamento do erro encontrado
     * @see     verificarSintaxe()
    */
    public String getLocalizedMessage() {
        return mensagem;
    }
    
}
