package br.jus.pje.nucleo.dto;

public class VisaoGeralTarefaLocalizacaoDTO {
	
	private Integer idUsuarioLocalizacaoMagistradoServidor;

	private Integer idOrgaoJulgadorColegiado;
	private String descricaoOrgaoJulgadorColegiado;
	
	private Integer idOrgaoJulgador;
	private String descricaoOrgaoJulgador;
	
	private Integer totalPendencias = 0;
	
	
	
	public Integer getIdUsuarioLocalizacaoMagistradoServidor() {
		return idUsuarioLocalizacaoMagistradoServidor;
	}

	public void setIdUsuarioLocalizacaoMagistradoServidor(Integer idUsuarioLocalizacaoMagistradoServidor) {
		this.idUsuarioLocalizacaoMagistradoServidor = idUsuarioLocalizacaoMagistradoServidor;
	}

	public Integer getIdOrgaoJulgadorColegiado() {
		return idOrgaoJulgadorColegiado;
	}

	public void setIdOrgaoJulgadorColegiado(Integer idOrgaoJulgadorColegiado) {
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
	}

	public String getDescricaoOrgaoJulgadorColegiado() {
		return descricaoOrgaoJulgadorColegiado;
	}

	public void setDescricaoOrgaoJulgadorColegiado(String descricaoOrgaoJulgadorColegiado) {
		this.descricaoOrgaoJulgadorColegiado = descricaoOrgaoJulgadorColegiado;
	}

	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	public String getDescricaoOrgaoJulgador() {
		return descricaoOrgaoJulgador;
	}

	public void setDescricaoOrgaoJulgador(String descricaoOrgaoJulgador) {
		this.descricaoOrgaoJulgador = descricaoOrgaoJulgador;
	}

	public Integer getTotalPendencias() {
		return totalPendencias;
	}

	public void setTotalPendencias(Integer totalPendencias) {
		this.totalPendencias = totalPendencias;
	}
	
	
	

}
