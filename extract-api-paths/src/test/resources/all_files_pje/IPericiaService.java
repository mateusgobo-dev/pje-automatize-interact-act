package br.jus.cnj.pje.webservice;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.vo.DesignarPericia;
import br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto.EspecilidadeDTO;
import br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto.PericiaDTO;
import br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto.PeritoDTO;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;

public interface IPericiaService extends Serializable {

	List<EspecilidadeDTO> obterEspecialidadesAtiva();

	List<PeritoDTO> obterPeritosAtivo(Integer idEspecialidade, List<Integer> idsOrgaoJulgador);

	Date obterDataMarcacaoDisponivel(Especialidade especialidade, PessoaPerito perito, Date data) throws PJeBusinessException;
	
	PericiaDTO designarPericia(Long idTarefa, Long idProcesso, Integer idEspecialidade, Integer idPerito, Double valor, Long dataInicio) throws PJeBusinessException;
	
	List<DesignarPericia> obterHorariosPerito(Especialidade especialidade, PessoaPerito perito, Calendar dataPesquisa, boolean isSomenteHorarioFuturo) throws PJeBusinessException;
	
	DesignarPericia designarPericia(DesignarPericia disponibilidadeHorarioPerito);
	
}
