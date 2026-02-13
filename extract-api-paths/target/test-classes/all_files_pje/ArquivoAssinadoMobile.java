package br.jus.cnj.pje.view;

import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.extensao.AssinadorA1;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.auxiliar.ResultadoAssinatura;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.util.StringUtil;


@Name(ArquivoAssinadoMobile.NAME)
@Scope(ScopeType.CONVERSATION)
public class ArquivoAssinadoMobile implements Serializable {

private static final long serialVersionUID = 1L;
	
	public static final String NAME = "arquivoAssinadoMobile";
	
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;
	
	@In(create = true, required = false)
	private AssinadorA1 assinadorA1;
	
	
	private String action;
	private String urlDocsField;
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	public String getUrlDocsField() {
		return urlDocsField;
	}

	public void setUrlDocsField(String urlDocsField) {
		this.urlDocsField = urlDocsField;
	}

	public void doAssinaArquivo() {
		
		try {
			if (StringUtil.isNullOrEmpty(getAction())) {
				throw new Exception("O nome da action no foi fornecido!");
			}
			
			Object component = Component.getInstance(getAction());
			
			if (component == null) {
				throw new Exception("No foi possvel recuperar a action: " + action + " na conversao!");
			}
			
			if (!(component instanceof ArquivoAssinadoUploader)) {
				throw new Exception("A action: " + action + " no implementa a interface ArquivoAssinadoUploader!");
			}
			
			ArquivoAssinadoUploader uploader = (ArquivoAssinadoUploader) component;
			
			if (StringUtil.isNullOrEmpty(getUrlDocsField())) {
				throw new Exception("A id com o documento a ser assinado no foi fornecido!");
			}
			
			if ( assinadorA1==null ) {
				throw new Exception("Não foi encontrado nenhum conector para assinatura do documento");
			}
			
			for (String url: getUrlDocsField().split(",")) {
				String params[] = url.split("&");
				
				String id = null;
				String codIni = null;
				String md5 = null;
				
				for (String param: params) {
					if ( param.startsWith("id") ) {
						id = param.replace("id=", "");
					} else if ( param.startsWith("codIni") ) {
						codIni = param.replace("codIni=", "");
					} else if ( param.startsWith("md5") ) {
						md5 = param.replace("md5=", "");
					}
				}
				
				if ( md5==null ) {
					throw new Exception("URl de documentos inválida");
				}
				
				try {
					ResultadoAssinatura res = assinadorA1.assinarHash(md5);
					
					ArquivoAssinadoHash arquivo = new ArquivoAssinadoHash();
					
					arquivo.setId(id);
					arquivo.setCodIni(codIni);
					arquivo.setHash(md5);
					arquivo.setAssinatura(res.getAssinatura());
					arquivo.setCadeiaCertificado(res.getCadeiaCertificado());
					
					uploader.doUploadArquivoAssinado(null, arquivo);
				} catch (PontoExtensaoException e) {
					e.printStackTrace();
					throw new Exception("Erro ao utilizar conector para assinatura do documento", e);
				}
			}
			
			
			
			
			this.ajaxDataUtil.sucesso();
		}
		catch (Exception e) {
			this.ajaxDataUtil.erro();
		}
	}
	
	
}
