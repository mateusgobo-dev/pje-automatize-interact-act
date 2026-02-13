package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.List;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.TipoVoto;

public class DadosBlocoJulgamentoDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String propostaVoto;
	private Sessao sessao;
	private String nomeBloco;
	private OrgaoJulgador orgaoJulgadorRelator;
	private Boolean agruparOrgaoJulgador;
	private TipoVoto tipoVotoRelator;
	private List<ProcessoTrf> processos;
	
	public DadosBlocoJulgamentoDTO(String nomeBloco, Sessao sessao, String propostaVoto, OrgaoJulgador orgaoJulgadorRelator, List<ProcessoTrf> processosSelecionados, Boolean agruparOrgaoJulgador, TipoVoto tipoVotoRelator) {
		this.propostaVoto = propostaVoto;
		this.sessao = sessao;
		this.nomeBloco = nomeBloco;
		this.orgaoJulgadorRelator = orgaoJulgadorRelator;
		this.processos = processosSelecionados;
		this.agruparOrgaoJulgador = agruparOrgaoJulgador;
		this.setTipoVotoRelator(tipoVotoRelator);
	}

	public String getPropostaVoto() {
		return propostaVoto;
	}

	public void setPropostaVoto(String propostaVoto) {
		this.propostaVoto = propostaVoto;
	}

	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	public String getNomeBloco() {
		return nomeBloco;
	}

	public void setNomeBloco(String nomeBloco) {
		this.nomeBloco = nomeBloco;
	}

	public OrgaoJulgador getOrgaoJulgadorRelator() {
		return orgaoJulgadorRelator;
	}

	public void setOrgaoJulgadorRelator(OrgaoJulgador orgaoJulgadorRelator) {
		this.orgaoJulgadorRelator = orgaoJulgadorRelator;
	}

	public List<ProcessoTrf> getProcessos() {
		return processos;
	}

	public void setProcessos(List<ProcessoTrf> processos) {
		this.processos = processos;
	}

	public Boolean getAgruparOrgaoJulgador() {
		return agruparOrgaoJulgador;
	}

	public void setAgruparOrgaoJulgador(Boolean agruparOrgaoJulgador) {
		this.agruparOrgaoJulgador = agruparOrgaoJulgador;
	}

	public TipoVoto getTipoVotoRelator() {
		return tipoVotoRelator;
	}

	public void setTipoVotoRelator(TipoVoto tipoVotoRelator) {
		this.tipoVotoRelator = tipoVotoRelator;
	}
}