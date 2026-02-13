package br.jus.cnj.pje.nucleo;

import java.util.ArrayList;
import java.util.List;

/**
 * Exceção destinada a permitir o repasse de informações de exceções entre as camadas do sistema PJe.
 * 
 * @author cristof
 */
public class PJeRuntimeException extends RuntimeException{

	private static final long serialVersionUID = 7259121188493613505L;
	public static String PJE_DEFAULT_ERROR_MSG = "pje.default.error.msg";

	private String code;
	private Object[] params;

	public PJeRuntimeException(){
		super();
	}

	public PJeRuntimeException(Throwable e){
		super(e);
		setCode(PJE_DEFAULT_ERROR_MSG);
		List<Object> listParams = new ArrayList<Object>();
		listParams.add(e);
		setParams(listParams.toArray());
	}

	public PJeRuntimeException(String code, Throwable t, Object... params){
		super(code,t);
		setCode(code);
		setParams(params);
	}
	
	public PJeRuntimeException(String code){
		setCode(code);
		setParams((Object[]) null);
	}

	public String getCode(){
		return code;
	}

	public void setCode(String code){
		this.code = code;
	}

	public Object[] getParams(){
		return params;
	}

	public void setParams(Object... params){
		this.params = params;
	}

	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder(super.toString());

		sb.append(", Code: ");
		sb.append(code);
		sb.append(", Message: ");
		sb.append(getMessage());
		sb.append(", Params: ");

		int countParams = 0;

		if (params != null && params.length > 0){
			for (Object param : params){
				++countParams;

				if (param instanceof Exception)
					sb.append(((Exception) param).getMessage());
				else
					sb.append(param);

				if (countParams < params.length) {
					sb.append(", ");
				}
			}
		}

		return sb.toString();
	}

}
