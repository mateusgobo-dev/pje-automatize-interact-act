package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.List;

public class ConteudoInformacaoCriminalDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private ParteDTO parte;
	private List<IncidenciaPenalDTO> incidenciasPenais;
	private List<PrisaoDTO> prisoes;
	private List<SolturaDTO> solturas;
	private List<FugaDTO> fugas;

	public ConteudoInformacaoCriminalDTO() {
		super();
	}

	public ConteudoInformacaoCriminalDTO(ParteDTO parte, List<IncidenciaPenalDTO> incidenciasPenais,
			List<PrisaoDTO> prisoes, List<SolturaDTO> solturas, List<FugaDTO> fugas) {
		super();
		this.parte = parte;
		this.incidenciasPenais = incidenciasPenais;
		this.prisoes = prisoes;
		this.solturas = solturas;
		this.fugas = fugas;
	}

	public ParteDTO getParte() {
		return parte;
	}

	public void setParte(ParteDTO parte) {
		this.parte = parte;
	}

	public List<IncidenciaPenalDTO> getIncidenciasPenais() {
		return incidenciasPenais;
	}

	public void setIncidenciasPenais(List<IncidenciaPenalDTO> incidenciasPenais) {
		this.incidenciasPenais = incidenciasPenais;
	}

	public List<PrisaoDTO> getPrisoes() {
		return prisoes;
	}

	public void setPrisoes(List<PrisaoDTO> prisoes) {
		this.prisoes = prisoes;
	}

	public List<SolturaDTO> getSolturas() {
		return solturas;
	}

	public void setSolturas(List<SolturaDTO> solturas) {
		this.solturas = solturas;
	}

	public List<FugaDTO> getFugas() {
		return fugas;
	}

	public void setFugas(List<FugaDTO> fugas) {
		this.fugas = fugas;
	}

}
