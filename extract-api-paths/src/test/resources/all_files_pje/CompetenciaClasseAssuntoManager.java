package br.jus.cnj.pje.nucleo.manager;

import java.util.List;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.CompetenciaClasseAssuntoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.controller.competencia.dto.CompetenciaClasseAssuntoDTO;
import br.jus.pje.nucleo.dto.EntityPageDTO;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(CompetenciaClasseAssuntoManager.NAME)
public class CompetenciaClasseAssuntoManager extends BaseManager<CompetenciaClasseAssunto> {
	
	public static final String NAME = "competenciaClasseAssuntoManager";
	
	@In
	private CompetenciaClasseAssuntoDAO competenciaClasseAssuntoDAO;

	@Override
	protected BaseDAO<CompetenciaClasseAssunto> getDAO() {
		return competenciaClasseAssuntoDAO;
	}
	
	public static CompetenciaClasseAssuntoManager instance() {
		return ComponentUtil.getComponent(CompetenciaClasseAssuntoManager.class);
	}
	
	public int recuperarNivelAcesso(ProcessoTrf processo, Competencia competencia) {
		return competenciaClasseAssuntoDAO.recuperarNivelAcesso(processo, competencia);
	}
	
	public boolean recuperarConfiguracaoSegredo(ProcessoTrf processo, Competencia competencia) {
		return competenciaClasseAssuntoDAO.recuperarConfiguracaoSegredo(processo, competencia);
	}

	public EntityPageDTO<CompetenciaClasseAssuntoDTO> recuperarCompetenciaClasseAssunto(Integer page, Integer size, CompetenciaClasseAssuntoDTO competenciaClasseAssuntoDto) {
		EntityPageDTO<CompetenciaClasseAssuntoDTO> response  = null;
		if(competenciaClasseAssuntoDto != null ) {
			List<CompetenciaClasseAssuntoDTO> competencias = recuperarCompetenciaClasseAssunto(competenciaClasseAssuntoDto);
			response  = new EntityPageDTO<>(false, false, page, null, size, null, competencias.size(), null);
			response.setContent(competencias);
		}
		return response;
	}
	
	public List<CompetenciaClasseAssuntoDTO> recuperarCompetenciaClasseAssunto(CompetenciaClasseAssuntoDTO competenciaClasseAssuntoDto) {
		return competenciaClasseAssuntoDAO.recuperarCompetenciaClasseAssunto(competenciaClasseAssuntoDto);
	}
	
	public void atualizarCompetenciaClasseAssunto(List <CompetenciaClasseAssuntoDTO> lista) throws PJeBusinessException {
		for(CompetenciaClasseAssuntoDTO dto: lista) {
			CompetenciaClasseAssunto cca = this.findById(dto.getIdCompetenciaClasseAssunto());
			cca.setNivelAcesso(dto.getNivelAcesso());
			cca.setSegredoSigilo(dto.isSigiloSegredo());
			this.mergeAndFlush(cca);
		}
	}
}
