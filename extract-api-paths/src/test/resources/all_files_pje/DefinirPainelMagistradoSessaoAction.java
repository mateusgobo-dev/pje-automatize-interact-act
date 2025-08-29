package br.jus.cnj.pje.view;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import br.com.infox.cliente.util.ParametroUtil;

@Name(DefinirPainelMagistradoSessaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class DefinirPainelMagistradoSessaoAction {
	
	public static final String  NAME  = "definirPainelMagistradoSessaoAction";
	
	public static final String PAINEL_V1 = "/Painel/painel_usuario/sessao_v1.xhtml";
	public static final String PAINEL_V2 = "/Painel/painel_usuario/sessao_v2.xhtml";
	
	private String urlPainelMagistradoSessao;
	
	@RequestParameter("idsess_")
	private Integer idSessao;
	
	@Create
	public void init(){
		this.defineQualPainelSeraUtilizado();
	}
	
	void defineQualPainelSeraUtilizado() {
		if(ParametroUtil.instance().isUsarNovoPainelMagistradoSessao()) {
			this.setUrlPainelMagistradoSessao(PAINEL_V2);
		}else {
			this.setUrlPainelMagistradoSessao(PAINEL_V1);
		}
	}
	
	// getters e setters 
	public String getUrlPainelMagistradoSessao() {
		return urlPainelMagistradoSessao;
	}

	public void setUrlPainelMagistradoSessao(String urlPainelMagistradoSessao) {
		this.urlPainelMagistradoSessao = urlPainelMagistradoSessao;
	}

	public Integer getIdSessao() {
		return idSessao;
	}

	public void setIdSessao(Integer idSessao) {
		this.idSessao = idSessao;
	}
}
