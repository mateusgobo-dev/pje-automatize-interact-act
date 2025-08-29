package br.com.jt.pje.bean;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.jt.enums.ResultadoVotacaoEnum;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoPessoa;

@Name(SearchProcessosPautaBean.NAME)
@Scope(ScopeType.CONVERSATION)
public class SearchProcessosPautaBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5656580798527599516L;

	public static final String NAME = "searchProcessosPautaBean";

	private String numeroProcesso;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private TipoPessoa tipoPessoa;
	private String numeroCPF;
	private String numeroCNPJ;
	private boolean cpf;
	private TipoInclusaoEnum tipoInclusao;
	private Estado ufOab;
	private String numeroOab;
	private String letraOab;
	private String nomeParte;
	private String nomeAdvogadoProcurador;
	private ResultadoVotacaoEnum resultadoVotacao;
	
	public void clearSearchFields(){
		setOrgaoJulgador(null);
		setClasseJudicial(null);
		setAssuntoTrf(null);
		setNumeroCPF(null);
		setNumeroCNPJ(null);
		setCpf(false);
		setTipoPessoa(null);
		setNomeParte(null);
		setNumeroProcesso(null);
		setTipoInclusao(null);
		setNumeroOab(null);
		setLetraOab(null);
		setUfOab(null);
		setNomeAdvogadoProcurador(null);
		setResultadoVotacao(null);
	}
	
	public void clearCpfCnpj(){
		setNumeroCPF(null);
		setNumeroCNPJ(null);
	}
	
	/*
	 * items
	 */
	
	public ResultadoVotacaoEnum[] getResultadoVotacaoEnumValues(){
		return ResultadoVotacaoEnum.values();
	}
	
	public TipoInclusaoEnum[] getTipoInclusaoEnumValues(){
		TipoInclusaoEnum[] tipo = new TipoInclusaoEnum[4];
		tipo[0] = TipoInclusaoEnum.PA;
		tipo[1] = TipoInclusaoEnum.RE;
		tipo[2] = TipoInclusaoEnum.ME;
		tipo[3] = TipoInclusaoEnum.MS;
		return tipo;
	}
	
	/*
	 * get e set
	 */
	
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setNumeroCPF(String numeroCPF) {
		this.numeroCPF = numeroCPF;
	}

	public String getNumeroCPF() {
		return numeroCPF;
	}

	public void setNumeroCNPJ(String numeroCNPJ) {
		this.numeroCNPJ = numeroCNPJ;
	}

	public String getNumeroCNPJ() {
		return numeroCNPJ;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}
	
	public void setTipoInclusao(TipoInclusaoEnum tipoInclusao) {
		this.tipoInclusao = tipoInclusao;
	}

	public TipoInclusaoEnum getTipoInclusao() {
		return tipoInclusao;
	}

	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public boolean isCpf() {
		return cpf;
	}

	public String getNumeroOab() {
		return numeroOab;
	}

	public void setNumeroOab(String numeroOab) {
		this.numeroOab = numeroOab;
	}

	public String getLetraOab() {
		return letraOab;
	}

	public void setLetraOab(String letraOab) {
		this.letraOab = letraOab;
	}

	public Estado getUfOab() {
		return ufOab;
	}

	public void setUfOab(Estado ufOab) {
		this.ufOab = ufOab;
	}
	
	public void setNomeAdvogadoProcurador(String nomeAdvogadoProcurador) {
		this.nomeAdvogadoProcurador = nomeAdvogadoProcurador;
	}

	public String getNomeAdvogadoProcurador() {
		return nomeAdvogadoProcurador;
	}
	
	public void setResultadoVotacao(ResultadoVotacaoEnum resultadoVotacao) {
		this.resultadoVotacao = resultadoVotacao;
	}

	public ResultadoVotacaoEnum getResultadoVotacao() {
		return resultadoVotacao;
	}
	
}