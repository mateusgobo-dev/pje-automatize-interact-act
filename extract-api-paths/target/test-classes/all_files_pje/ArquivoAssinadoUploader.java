package br.jus.cnj.pje.interfaces;

import javax.servlet.http.HttpServletRequest;

import br.jus.cnj.pje.vo.ArquivoAssinadoHash;

public interface ArquivoAssinadoUploader {

	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception;
	public String getActionName();
	
}
