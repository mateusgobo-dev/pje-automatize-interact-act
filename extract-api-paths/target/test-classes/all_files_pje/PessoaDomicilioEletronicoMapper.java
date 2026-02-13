package br.jus.cnj.pje.webservice.client.domicilioeletronico.dto;

import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.dto.domicilioeletronico.PessoaDomicilioEletronicoDTO;
import br.jus.pje.nucleo.entidades.LotePessoasDomicilioEletronico;
import br.jus.pje.nucleo.entidades.PessoaDomicilioEletronico;

public class PessoaDomicilioEletronicoMapper {

	/**
	 * Construtor.
	 */
	private PessoaDomicilioEletronicoMapper() {
		//Construtor
	}
	
	public static List<PessoaDomicilioEletronico> readValue(List<PessoaDomicilioEletronicoDTO> dtos) {
		return dtos.stream().map(PessoaDomicilioEletronicoMapper::readValue).collect(toList());
	}

	public static PessoaDomicilioEletronico readValue(PessoaDomicilioEletronicoDTO dto, PessoaDomicilioEletronico pessoaParaMapear, LotePessoasDomicilioEletronico lote) {
		PessoaDomicilioEletronico pessoaMapeada = readValue(dto, lote);

		if (pessoaParaMapear != null) {
			pessoaMapeada.setId(pessoaParaMapear.getId());
		}

		return pessoaMapeada;
	}

	public static PessoaDomicilioEletronico readValue(PessoaDomicilioEletronicoDTO dto, LotePessoasDomicilioEletronico lote) {
		PessoaDomicilioEletronico pessoaMapeada = readValue(dto);
		pessoaMapeada.setLote(lote);
		return pessoaMapeada;
	}

	public static PessoaDomicilioEletronico readValue(PessoaDomicilioEletronicoDTO dto) {
		PessoaDomicilioEletronico pessoa = new PessoaDomicilioEletronico();
		pessoa.setNumeroDocumento(InscricaoMFUtil.acrescentaMascaraMF(dto.getDocumento()));
		pessoa.setHabilitado(dto.isHabilitado());
		pessoa.setTipoDocumento(dto.getTipoDocumento());
		pessoa.setDataAtualizacao(new Date());
		pessoa.setPessoaJuridicaDireitoPublico(dto.isPessoaJuridicaDireitoPublico());
		return pessoa;
	}

}
