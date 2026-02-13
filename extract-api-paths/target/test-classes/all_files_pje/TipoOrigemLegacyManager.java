package br.jus.cnj.pje.nucleo.manager.migrador;

import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.migrador.TipoOrigemLegacyDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;
import br.jus.pje.nucleo.entidades.TipoOrigem;

@Name(TipoOrigemLegacyManager.NAME)
public class TipoOrigemLegacyManager extends BaseManager<TipoOrigem> {

	private TipoOrigemLegacyDAO tipoOrigemDAO; 
	
	public static final String NAME = "tipoOrigemLegacyManager";

	@Override
	protected BaseDAO<TipoOrigem> getDAO() {
		return this.tipoOrigemDAO;
	}
	
	@Create
	public void init() {
		if(this.tipoOrigemDAO == null) {
			this.tipoOrigemDAO =  ComponentUtil.getComponent(TipoOrigemLegacyDAO.NAME);
		}
	}
	
	public List<TipoOrigem> recuperarPendentesMigracao(){
		return this.tipoOrigemDAO.recuperarPendentesMigracao();
	}
	
	public boolean isPendenteMigracao() {
		List<TipoOrigem> pendentes = this.recuperarPendentesMigracao();
		
		return CollectionUtilsPje.isNotEmpty(pendentes);
	}
	
	public TipoOrigemDTO converteEmTipoOrigemNacional(Integer idTipoOrigem) {
		TipoOrigemDTO tipoOrigemNacional = null;
		TipoOrigem tipoOrigem = this.tipoOrigemDAO.find(idTipoOrigem);
		if(tipoOrigem != null && tipoOrigem.getCodigoNacional() != null) {
			tipoOrigemNacional = new TipoOrigemDTO(
					tipoOrigem.getCodigoNacional(),
					tipoOrigem.getDsTipoOrigem(),
					tipoOrigem.getInObrigatorioNumeroOrigem(),
					tipoOrigem.getAtivo());
		}
		return tipoOrigemNacional;
		
	}
}
