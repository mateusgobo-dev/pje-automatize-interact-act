package br.com.infox.cliente.entity.search;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;

@Name(ProcessoAudienciaSearch.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ProcessoAudienciaSearch extends ProcessoAudiencia {

	public static final String NAME = "processoAudienciaSearch";
	private static final long serialVersionUID = 1L;

	private OrgaoJulgador orgaoJulgador;
	private String nomeConciliador;
	private String nomeRealizador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private Integer numeroSequencia;
	private Integer numeroDigitoVerificador;
	private Integer ano;
	private Integer numeroOrigemProcesso;
	private String nomeSalaAudiencia;

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getNomeConciliador() {
		return nomeConciliador;
	}

	public void setNomeConciliador(String nomeConciliador) {
		this.nomeConciliador = nomeConciliador;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getNumeroOrigemProcesso() {
		return numeroOrigemProcesso;
	}

	public void setNumeroOrigemProcesso(Integer numeroOrigemProcesso) {
		this.numeroOrigemProcesso = numeroOrigemProcesso;
	}

	public String getNomeRealizador() {
		return nomeRealizador;
	}

	public void setNomeRealizador(String nomeRealizador) {
		this.nomeRealizador = nomeRealizador;
	}

	public String getNomeSalaAudiencia() {
		return nomeSalaAudiencia;
	}

	public void setNomeSalaAudiencia(String nomeSalaAudiencia) {
		this.nomeSalaAudiencia = nomeSalaAudiencia;
	}

}