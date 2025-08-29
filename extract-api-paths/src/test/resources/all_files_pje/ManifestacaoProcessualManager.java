package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ManifestacaoProcessualDAO;
import br.jus.pje.nucleo.entidades.ManifestacaoProcessual;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name(ManifestacaoProcessualManager.NAME)
public class ManifestacaoProcessualManager extends BaseManager<ManifestacaoProcessual> {
	public static final String NAME = "manifestacaoProcessualManager";

	@In
	private ManifestacaoProcessualDAO manifestacaoProcessualDAO;
	
	@Override
	protected ManifestacaoProcessualDAO getDAO() {
		return manifestacaoProcessualDAO;
	}
	
	/**
	 * @return instância de ManifestacaoProcessualManager
	 */
	public static ManifestacaoProcessualManager instance(){
		return ComponentUtil.getComponent(ManifestacaoProcessualManager.class);
	}
	
	public ManifestacaoProcessual buscaSemWsdl(ProcessoTrf processoTrf) {
		return manifestacaoProcessualDAO.buscaSemWsdl(processoTrf);
	}
	
	public ManifestacaoProcessual buscaUltimoEntregue(ProcessoTrf processoTrf) {
		return manifestacaoProcessualDAO.buscaUltimoEntregue(processoTrf);
	}
	
	public List<ManifestacaoProcessual> findByProcessoTrf(ProcessoTrf processoTrf) {
		return manifestacaoProcessualDAO.findByProcessoTrf(processoTrf);
	}
}
