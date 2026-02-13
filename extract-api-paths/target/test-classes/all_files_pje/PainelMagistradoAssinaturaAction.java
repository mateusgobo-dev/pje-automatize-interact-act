package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;



@Name(PainelMagistradoAssinaturaAction.NAME)
@Scope(ScopeType.EVENT)
public class PainelMagistradoAssinaturaAction implements ArquivoAssinadoUploader {
	
	public static final String  NAME  = "painelMagistradoAssinaturaAction";
	public static final String  MAPA_ASSINATURAS = "pje:assinatura:mapa";
	

	@SuppressWarnings("unchecked")
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		List<ArquivoAssinadoHash> assinaturasSessao = null;
		if(Contexts.getSessionContext().get(MAPA_ASSINATURAS) != null){
			assinaturasSessao = (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get(MAPA_ASSINATURAS);
		}
		else{
			assinaturasSessao = new ArrayList<ArquivoAssinadoHash>();
		}
		assinaturasSessao.add(arquivoAssinadoHash);
		Contexts.getSessionContext().set(MAPA_ASSINATURAS, assinaturasSessao);
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	@SuppressWarnings("unchecked")
	public static List<ArquivoAssinadoHash> getAssinaturas(){
		if(Contexts.getSessionContext().get(MAPA_ASSINATURAS) != null){
			return  (List<ArquivoAssinadoHash>)Contexts.getSessionContext().get(MAPA_ASSINATURAS);
		}
		return null;
	}
}

