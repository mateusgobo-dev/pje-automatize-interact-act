package br.jus.cnj.pje.webservice.json;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class InformacaoUsuarioSessao {
	
	private Integer idOrgaoJulgador;
	private Integer idOrgaoJulgadorColegiado;
	private boolean isServidorExclusivoOJC;
	private Integer idOrgaoJulgadorCargo; 
	private Integer idUsuario; 
	private Integer idLocalizacaoFisica;
	private List<Integer> idsLocalizacoesFisicasFilhas;
	private Integer idLocalizacaoModelo;
	private Integer idPapel;
	private Boolean papelNaoFiltravel;
	private Boolean visualizaSigiloso;
	private Integer nivelAcessoSigilo;
	private String nomeUsuario;
	private List<Integer> idsOrgaoJulgadorCargoVisibilidade;
	private Boolean cargoAuxiliar = Boolean.FALSE;
	private Integer idUsuarioLocalizacaoMagistradoServidor;
	private String login;
	private String descricaoOrgaoJulgador;
	private String descricaoOrgaoJulgadorColegiado;
	private String descricaoLocalizacao;
	private String descricaoPapel;
	
	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}
	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
		this.atualizaSeServidorExclusivoOJC();
	}
	public Integer getIdOrgaoJulgadorColegiado() {
		return idOrgaoJulgadorColegiado;
	}
	public void setIdOrgaoJulgadorColegiado(Integer idOrgaoJulgadorColegiado) {
		this.idOrgaoJulgadorColegiado = idOrgaoJulgadorColegiado;
		this.atualizaSeServidorExclusivoOJC();
	}

	private void atualizaSeServidorExclusivoOJC() {
		this.setServidorExclusivoOJC(this.getIdOrgaoJulgador() == null && this.getIdOrgaoJulgadorColegiado() != null);
	}
	
	public boolean isServidorExclusivoOJC() {
		return isServidorExclusivoOJC;
	}
	public void setServidorExclusivoOJC(boolean isServidorExclusivoOJC) {
		this.isServidorExclusivoOJC = isServidorExclusivoOJC;
	}
	public Integer getIdOrgaoJulgadorCargo() {
		return idOrgaoJulgadorCargo;
	}
	public void setIdOrgaoJulgadorCargo(Integer idOrgaoJulgadorCargo) {
		this.idOrgaoJulgadorCargo = idOrgaoJulgadorCargo;
	}
	public Integer getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}
	public List<Integer> getIdsLocalizacoesFisicasFilhas() {
		return idsLocalizacoesFisicasFilhas;
	}
	public void setIdsLocalizacoesFisicasFilhas(List<Integer> idsLocalizacoesFisicasFilhas) {
		this.idsLocalizacoesFisicasFilhas = idsLocalizacoesFisicasFilhas;
	}
	public Integer getIdLocalizacaoFisica() {
		return idLocalizacaoFisica;
	}
	public void setIdLocalizacaoFisica(Integer idLocalizacaoFisica) {
		this.idLocalizacaoFisica = idLocalizacaoFisica;
	}
	public Integer getIdLocalizacaoModelo() {
		return idLocalizacaoModelo;
	}
	public void setIdLocalizacaoModelo(Integer idLocalizacaoModelo) {
		this.idLocalizacaoModelo = idLocalizacaoModelo;
	}
	public Integer getIdPapel() {
		return idPapel;
	}
	public void setIdPapel(Integer idPapel) {
		this.idPapel = idPapel;
	}
	public Boolean getVisualizaSigiloso() {
		return visualizaSigiloso;
	}
	public void setVisualizaSigiloso(Boolean visualizaSigiloso) {
		this.visualizaSigiloso = visualizaSigiloso;
	}
	
	public Integer getNivelAcessoSigilo() {
		return nivelAcessoSigilo;
	}
	public void setNivelAcessoSigilo(Integer nivelAcessoSigilo) {
		this.nivelAcessoSigilo = nivelAcessoSigilo;
	}
	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public List<Integer> getIdsOrgaoJulgadorCargoVisibilidade() {
		return idsOrgaoJulgadorCargoVisibilidade;
	}

	public void setIdsOrgaoJulgadorCargoVisibilidade(List<Integer> idsOrgaoJulgadorCargoVisibilidade) {
		this.idsOrgaoJulgadorCargoVisibilidade = idsOrgaoJulgadorCargoVisibilidade;
	}

	public Boolean getCargoAuxiliar() {
		return cargoAuxiliar;
	}

	public void setCargoAuxiliar(Boolean cargoAuxiliar) {
		this.cargoAuxiliar = cargoAuxiliar;
	}

	public Integer getIdUsuarioLocalizacaoMagistradoServidor() {
		return idUsuarioLocalizacaoMagistradoServidor;
	}

	public void setIdUsuarioLocalizacaoMagistradoServidor(Integer idUsuarioLocalizacaoMagistradoServidor) {
		this.idUsuarioLocalizacaoMagistradoServidor = idUsuarioLocalizacaoMagistradoServidor;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
	
	public Boolean getPapelNaoFiltravel() {
		return papelNaoFiltravel;
	}
	
	public void setPapelNaoFiltravel(Boolean papelNaoFiltravel) {
		this.papelNaoFiltravel = papelNaoFiltravel;
	}
	
	public String getDescricaoOrgaoJulgador() {
		return descricaoOrgaoJulgador;
	}
	
	public void setDescricaoOrgaoJulgador(String descricaoOrgaoJulgador) {
		this.descricaoOrgaoJulgador = descricaoOrgaoJulgador;
	}
	
	public String getDescricaoOrgaoJulgadorColegiado() {
		return descricaoOrgaoJulgadorColegiado;
	}
	
	public void setDescricaoOrgaoJulgadorColegiado(String descricaoOrgaoJulgadorColegiado) {
		this.descricaoOrgaoJulgadorColegiado = descricaoOrgaoJulgadorColegiado;
	}
	
	public String getDescricaoLocalizacao() {
		return descricaoLocalizacao;
	}
	
	public void setDescricaoLocalizacao(String descricaoLocalizacao) {
		this.descricaoLocalizacao = descricaoLocalizacao;
	}
	
	public String getDescricaoPapel() {
		return descricaoPapel;
	}

	public void setDescricaoPapel(String descricaoPapel) {
		this.descricaoPapel = descricaoPapel;
	}

}