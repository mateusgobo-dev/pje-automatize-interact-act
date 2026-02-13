package br.jus.csjt.pje.commons.exception;

/**
 * Esta classe dispara exceções relacionada a consistências de dados necessárias
 * para o intercâmbio de dados entre AUD e PJE.
 * 
 * Códigos de Exceção e respectivas descrições (Tais exceções impedem o
 * prosseguimento da troca de dados): -1 PROCESSO_INEXISTENTE: O Processo não
 * existe no PJE. (Não pertence, não inciou no PJE.) -2 AUDIENCIA_INEXISTENTE: A
 * Audiência não existe no PJE. (Não pertence, não é de um processo originado no
 * PJE.) -3 REGISTRO_CONSOLIDADO: Registro de importação já consolidado no PJE.
 * A data de consolidação está preenchida, os dados da tabela de importação já
 * foram copiados para as tabelas do sistema e a respectiva ata já foi assinada
 * pelo magistrado.
 * 
 * @author Bernardo A. Gouvea
 * @since 1.4.3
 * @see
 * @category PJE-JT
 * 
 * */

public class IntegracaoAudException extends Exception {

	private static final long serialVersionUID = 1L;

	private Integer codeException = new Integer(0);

	public static final String PROCESSO_INEXISTENTE = "O Processo não existe no PJE.";
	public static final String AUDIENCIA_INEXISTENTE = "A Audiência não existe no PJE.";
	public static final String REGISTRO_VALIDADO = "Registro de importação já validado no PJE.";
	public static final String INSERCAO_SOMENTE_CONCILIACAO = "A inserção de uma Audiência em pauta diretamente pelo AUD só será admitida em caso de Conciliação.";

	public IntegracaoAudException() {
		super();
	}

	public IntegracaoAudException(String message, Throwable cause) {
		super(message, cause);
	}

	public IntegracaoAudException(String message) {
		super(message);
		if (message.equals(PROCESSO_INEXISTENTE)) {
			setCodeException(-1);
		} else if (message.equals(AUDIENCIA_INEXISTENTE)) {
			setCodeException(-2);
		} else if (message.equals(REGISTRO_VALIDADO)) {
			setCodeException(-3);
		} else if (message.equals(INSERCAO_SOMENTE_CONCILIACAO)) {
			setCodeException(-4);
		}
	}

	public IntegracaoAudException(Throwable cause) {
		super(cause);
	}

	public Integer getCodeException() {
		return codeException;
	}

	public void setCodeException(Integer codeException) {
		this.codeException = codeException;
	}

}
