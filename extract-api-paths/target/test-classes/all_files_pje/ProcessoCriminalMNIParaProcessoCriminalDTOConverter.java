/**
 * ProcessoCriminalMNIParaProcessoCriminalDTOConverter.java
 * 
 * Data de criação: 18/01/2018
 */
package br.jus.cnj.pje.intercomunicacao.v223.converter;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.exceptions.NegocioException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.intercomunicacao.v223.beans.Endereco;
import br.jus.cnj.intercomunicacao.v223.criminal.FatoCriminal;
import br.jus.cnj.intercomunicacao.v223.criminal.OrgaoProcedimentoOrigem;
import br.jus.cnj.intercomunicacao.v223.criminal.ProcedimentoOrigem;
import br.jus.cnj.intercomunicacao.v223.criminal.Processo;
import br.jus.cnj.pje.intercomunicacao.v223.util.ConversorUtil;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.pje.nucleo.beans.criminal.TipoProcessoEnum;
import br.jus.pje.nucleo.dto.MunicipioDTO;
import br.jus.pje.nucleo.dto.OrgaoProcedimentoOriginarioDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.dto.ProcessoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Conversor de Processo (criminal mni) para ProcessoCriminalDTO.
 * 
 * @author Adriano Pamplona
 */
@Name(ProcessoCriminalMNIParaProcessoCriminalDTOConverter.NAME)
public class ProcessoCriminalMNIParaProcessoCriminalDTOConverter
		extends
		IntercomunicacaoConverterAbstrato<Processo, ProcessoCriminalDTO> {

	public static final String NAME = "processoCriminalMNIParaProcessoCriminalDTOConverter";
	
	@In
	private CepManager cepManager;
	
	/**
	 * @see ProcessoCriminalMNIParaProcessoCriminalDTOConverter#converter(Processo, ProcessoTrf)
	 */
	@Override
	@Deprecated
	public ProcessoCriminalDTO converter(Processo processo) {
		// Usar converter(Processo, ProcessoTrf)
		return null;
	}
	
	/**
	 * Converte Processo criminal para ProcessoCriminalDTO.
	 * @param processo
	 * @param processoTrf
	 * @return ProcessoCriminalDTO
	 */
	public ProcessoCriminalDTO converter(Processo processo, ProcessoTrf processoTrf) {
		ProcessoCriminalDTO resultado = null;
		
		if (isNull(processo)) {
			throw new NegocioException("Não foi possível encontrar as informações criminais do processo.");
		}

		if (isNull(processo.getFatoCriminal())) {
			throw new NegocioException("Não foi possível encontrar informações do fato criminal.");
		}
		
		resultado = new ProcessoCriminalDTO();
		ClasseJudicialManager classeJudicialManager = ComponentUtil.getComponent(ClasseJudicialManager.class); 
		if (classeJudicialManager.isClasseCriminal(processoTrf.getClasseJudicial())) { 
			resultado.setTipoProcesso(TipoProcessoEnum.CRI); 
		} else if (classeJudicialManager.isClasseInfracional(processoTrf.getClasseJudicial())) { 
			resultado.setTipoProcesso(TipoProcessoEnum.INF); 
		} 
		FatoCriminal fatoCriminal = processo.getFatoCriminal();
		Endereco enderecoFatoCriminal = fatoCriminal.getEndereco();
		if (enderecoFatoCriminal != null) {
			String cep = CepManager.formatarCep(enderecoFatoCriminal.getCep());
			resultado.setCep(cep);
			resultado.setComplemento(enderecoFatoCriminal.getComplemento());
			resultado.setNmBairro(enderecoFatoCriminal.getBairro());
			resultado.setNmLogradouro(enderecoFatoCriminal.getLogradouro());
			resultado.setNmNumero(enderecoFatoCriminal.getNumero());
			resultado.setMunicipio(obterMunicipio(enderecoFatoCriminal, cep));
		}
		resultado.setDsLatitude(fatoCriminal.getLatitude());
		resultado.setDsLongitude(fatoCriminal.getLongitude());
		resultado.setDsLocalFato(fatoCriminal.getLocal());
		resultado.setDtLocalFato(ConversorUtil.converterParaDate(fatoCriminal.getData()));
		resultado.setNrProcesso(processoTrf.getNumeroProcesso());
		resultado.setProcessoProcedimentoOrigemList(obterColecaoProcessoProcedimentoOrigemDTO(processo));
	return resultado;
	}

	/**
	 * @param processo Processo
	 * @return Lista de ProcessoProcedimentoOrigemDTO.
	 */
	private List<ProcessoProcedimentoOrigemDTO> obterColecaoProcessoProcedimentoOrigemDTO(Processo processo) {
		List<ProcessoProcedimentoOrigemDTO> resultado = new ArrayList<ProcessoProcedimentoOrigemDTO>();
		
		List<ProcedimentoOrigem> procedimentosOrigens = processo.getProcedimentosOrigens();
		for (ProcedimentoOrigem procedimentoOrigem : procedimentosOrigens) {
			OrgaoProcedimentoOrigem orgaoProcedimentoOrigem = procedimentoOrigem.getOrgaoProcedimentoOrigem();
			
			
			OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOriginarioDto = new OrgaoProcedimentoOriginarioDTO();
			orgaoProcedimentoOriginarioDto.setId(converterParaInt(orgaoProcedimentoOrigem.getId()));
			
			TipoOrigemDTO tipoOrigemDTO = null; 
			if(procedimentoOrigem != null && procedimentoOrigem.getTipoOrigem() != null){
				tipoOrigemDTO = new TipoOrigemDTO();
				tipoOrigemDTO.setId(converterParaInt(procedimentoOrigem.getTipoOrigem()));
			}
			
			TipoProcedimentoOrigemDTO tipoProcedimentoOrigemDTO = null;
			if (procedimentoOrigem != null && procedimentoOrigem.getTipo() != null) {
				tipoProcedimentoOrigemDTO = new TipoProcedimentoOrigemDTO();
				tipoProcedimentoOrigemDTO.setId(converterParaInt(procedimentoOrigem.getTipo()));
			}
			
			ProcessoProcedimentoOrigemDTO procedimentoOrigemDto = new ProcessoProcedimentoOrigemDTO();
			procedimentoOrigemDto.setAno(procedimentoOrigem.getAno());
			procedimentoOrigemDto.setAtivo(Boolean.TRUE);
			procedimentoOrigemDto.setDataInstauracao(ConversorUtil.converterParaDate(procedimentoOrigem.getDataInstauracao()));
			procedimentoOrigemDto.setDataLavratura(ConversorUtil.converterParaDate(procedimentoOrigem.getDataLavratura()));
			procedimentoOrigemDto.setNrProtocoloPolicia(procedimentoOrigem.getProtocolo());
			procedimentoOrigemDto.setOrgaoProcedimentoOriginario(orgaoProcedimentoOriginarioDto);
			procedimentoOrigemDto.setTipoOrigem(tipoOrigemDTO);
			procedimentoOrigemDto.setTipoProcedimentoOrigem(tipoProcedimentoOrigemDTO);
			procedimentoOrigemDto.setUf(procedimentoOrigem.getEstado());
			procedimentoOrigemDto.setNumero(procedimentoOrigem.getNumero());
			//procedimentoOrigemDto.setProcesso(resultado);
			
			resultado.add(procedimentoOrigemDto);
		}
		
		return resultado;
	}

	/**
	 * @param enderecoFatoCriminal Endereco
	 * @param string 
	 * @return MunicipioDTO
	 */
	private MunicipioDTO obterMunicipio(Endereco enderecoFatoCriminal, String cep) {
		MunicipioDTO resultado = new MunicipioDTO();
		String cidade = enderecoFatoCriminal.getCidade();
		MunicipioManager municipioManager = ComponentUtil.getComponent(MunicipioManager.class);
		Municipio municipioEncontrado = null;
		Cep cepEncontrado = null;
		
		if(cep != null){
			cepEncontrado = findByCep(cep);
		}
		
		if (cepEncontrado != null) {
			municipioEncontrado = cepEncontrado.getMunicipio();
		}

		if (municipioEncontrado == null && isNumeroValidoPreenchido(cidade)) {
			municipioEncontrado = municipioManager.getMunicipioByCodigoIBGE(cidade);
			if (municipioEncontrado == null) {
				throw new RuntimeException("Não foi possível encontrar o município com esse código: " + cidade
						+ ". Verifique se está correto e solicite o cadastramento.");
			}
		}
		
		if (municipioEncontrado == null) {
			if(enderecoFatoCriminal.getEstado() == null || enderecoFatoCriminal.getCidade() == null){
				throw new RuntimeException("Fato Criminal: 'Município' e 'Estado' são obrigatórios.");
			}
			municipioEncontrado = municipioManager.findByUfAndDescricao(enderecoFatoCriminal.getEstado(), enderecoFatoCriminal.getCidade());
			if (municipioEncontrado == null) {
				throw new RuntimeException(String.format("Não foi possível encontrar um município com essa descrição: %s (%s).", enderecoFatoCriminal.getCidade(), enderecoFatoCriminal.getEstado()));
			}
		}
		
		if (municipioEncontrado != null) {
			resultado.setUf(municipioEncontrado.getEstado().getCodEstado());
			resultado.setCodigoIbge(municipioEncontrado.getCodigoIbge());
			resultado.setMunicipio(municipioEncontrado.getMunicipio());
		}
		return resultado;
	}
	
	private Cep findByCep(String cep) {
		if (cep == null) {
			return null;
		}
		CepManager cepManager = ComponentUtil.getComponent(CepManager.class);
		return cepManager.findByCep(cep);
	}

}
