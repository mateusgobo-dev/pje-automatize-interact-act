package br.jus.cnj.pje.nucleo;

public class PJeRestException extends Exception {

	private static final long serialVersionUID = -1;

	private String code;
	private String mensagem;

	public PJeRestException(Throwable e){
		super(e);
	}
	
	public PJeRestException(String code, String mensagem){
		super(mensagem);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
	
	@Override
	public String getMessage() {
		if(mensagem != null) {
			return "Código: "+this.code+"/n"+
				   "Mensagem: "+this.mensagem;
		}
		return super.getMessage();
	}
	
	

}
