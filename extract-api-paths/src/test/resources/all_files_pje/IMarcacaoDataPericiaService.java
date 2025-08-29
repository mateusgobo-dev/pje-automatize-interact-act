package br.jus.cnj.pje.webservice;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.controller.modalPericiaLote.dto.PericiaDTO;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;

public interface IMarcacaoDataPericiaService extends Serializable{
	
	Date obterDataMarcacaoDisponivel(Especialidade especialidade, PessoaPerito perito, 
			Date dataPesquisa, List<Integer> idLotePericiasMarcadas) throws PJeBusinessException;
	
	Date obterDataMarcacaoDisponivel(Especialidade especialidade, PessoaPerito perito, 
			Date dataPesquisa) throws PJeBusinessException;
	
	PericiaDTO designarPericia(Long idTarefa, Long idProcesso, Integer idEspecialidade, Integer idPerito, 
			Double valor, Long dataInicio) throws PJeBusinessException;

}
