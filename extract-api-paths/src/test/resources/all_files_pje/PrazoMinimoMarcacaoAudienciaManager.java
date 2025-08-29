package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PrazoMinimoMarcacaoAudienciaDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PrazoMinimoMarcacaoAudiencia;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name(PrazoMinimoMarcacaoAudienciaManager.NAME)
public class PrazoMinimoMarcacaoAudienciaManager extends BaseManager<PrazoMinimoMarcacaoAudiencia> {
	
	public static final String NAME = "prazoMinimoMarcacaoAudienciaManager";
	
	@In
	private PrazoMinimoMarcacaoAudienciaDAO prazoMinimoMarcacaoAudienciaDAO;

	@Override
	protected BaseDAO<PrazoMinimoMarcacaoAudiencia> getDAO() {
		return prazoMinimoMarcacaoAudienciaDAO;
	}
	
	public List<PrazoMinimoMarcacaoAudiencia> getPrazoMinimoMarcacaoAudienciaList(OrgaoJulgador orgaoJulgador){
		return prazoMinimoMarcacaoAudienciaDAO.getPrazoMinimoMarcacaoAudienciaList(orgaoJulgador);
	}
	
	public Integer getPrazoMinimoMarcacaoAudienciaPorTipo(OrgaoJulgador orgaoJulgador, TipoAudiencia tipoAudiencia){
		return prazoMinimoMarcacaoAudienciaDAO.getPrazoMinimoMarcacaoAudienciaPorTipo(orgaoJulgador, tipoAudiencia);
	}

}
