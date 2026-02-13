package br.jus.cnj.pje.webservice.api;

import java.util.List;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto.AudienciaDTO;
import br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto.SalaDTO;
import br.jus.cnj.pje.webservice.controller.modalAudienciaLote.dto.TipoAudienciaDTO;

public interface IAudienciaService {
	
	public List<TipoAudienciaDTO> obterTiposAudiencia();

	public List<SalaDTO> obterSalasAudiencias(Integer idTipoAudiencia, List<Integer> idsOrgaoJulgador);

	public Integer obterTempoAudiencia(Integer idTipoAudiencia, List<Integer> idsOrgaoJulgador);

	public AudienciaDTO designarAudiencia(Long idTarefa, Long idProcesso, Integer idTipoAudiencia, 
			Integer idSalaAudiencia, Integer duracao, Long dataInicio) throws PJeBusinessException;

}
