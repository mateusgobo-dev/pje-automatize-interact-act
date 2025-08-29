package br.jus.cnj.pje.nucleo.manager.migrador;

import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.migrador.TipoOrigemLegacyDAO;
import br.jus.cnj.pje.business.dao.migrador.TipoProcedimentoOrigemLegacyDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.TipoProcedimentoOrigem;

@Name(TipoProcedimentoOrigemLegacyManager.NAME)
public class TipoProcedimentoOrigemLegacyManager extends BaseManager<TipoProcedimentoOrigem> {

	private TipoProcedimentoOrigemLegacyDAO tipoProcedimentoOrigemDAO; 
	
	public static final String NAME = "tipoProcedimentoOrigemLegacyManager";

	@Override
	protected BaseDAO<TipoProcedimentoOrigem> getDAO() {
		return this.tipoProcedimentoOrigemDAO;
	}
	
	@Create
	public void init() {
		if(this.tipoProcedimentoOrigemDAO == null) {
			this.tipoProcedimentoOrigemDAO =  ComponentUtil.getComponent(TipoProcedimentoOrigemLegacyDAO.NAME);
		}
	}

	public List<TipoProcedimentoOrigem> recuperarPendentesMigracao(){
		return this.tipoProcedimentoOrigemDAO.recuperarPendentesMigracao();
	}
	
	public boolean isPendenteMigracao() {
		List<TipoProcedimentoOrigem> pendentes = this.recuperarPendentesMigracao();
		
		return CollectionUtilsPje.isNotEmpty(pendentes);
	}
}
