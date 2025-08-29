package br.jus.cnj.pje.nucleo.manager.migrador;

import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.migrador.OrgaoProcedimentoOriginarioLegacyDAO;
import br.jus.cnj.pje.business.dao.migrador.TipoProcedimentoOrigemLegacyDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.dto.MunicipioDTO;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoProcedimentoOriginario;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(OrgaoProcedimentoOriginarioLegacyManager.NAME)
public class OrgaoProcedimentoOriginarioLegacyManager extends BaseManager<OrgaoProcedimentoOriginario> {

	private OrgaoProcedimentoOriginarioLegacyDAO orgaoProcedimentoOriginarioDAO; 
	
	public static final String NAME = "orgaoProcedimentoOriginarioLegacyManager";

	@Override
	protected BaseDAO<OrgaoProcedimentoOriginario> getDAO() {
		return this.orgaoProcedimentoOriginarioDAO;
	}
	
	@Create
	public void init() {
		if(this.orgaoProcedimentoOriginarioDAO == null) {
			this.orgaoProcedimentoOriginarioDAO =  ComponentUtil.getComponent(OrgaoProcedimentoOriginarioLegacyDAO.NAME);
		}
	}
	
	public List<OrgaoProcedimentoOriginario> recuperarPendentesMigracao(){
		return this.orgaoProcedimentoOriginarioDAO.recuperarPendentesMigracao();
	}
	
	public boolean isPendenteMigracao() {
		List<OrgaoProcedimentoOriginario> pendentes = this.recuperarPendentesMigracao();
		
		return CollectionUtilsPje.isNotEmpty(pendentes);
	}
	
	public MunicipioDTO getMunicipioDTO(Integer idOrgaoProcedimentoOriginario) {
		MunicipioDTO dto = null;
		OrgaoProcedimentoOriginario opo = this.orgaoProcedimentoOriginarioDAO.find(idOrgaoProcedimentoOriginario);
		if (opo != null && opo.getIdMunicipio() != null) {
			MunicipioManager municipioManager = ComponentUtil.getComponent(MunicipioManager.NAME);
			
			Municipio municipio;
			try {
				municipio = municipioManager.findById(opo.getIdMunicipio());
				Estado estado = municipio.getEstado();
				
				dto = new MunicipioDTO();
				dto.setCodigoIbge(municipio.getCodigoIbge());
				dto.setMunicipio(municipio.getMunicipio());
				dto.setUf((estado != null ? estado.getCodEstado() : null));

			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
		return dto;
	}
}
