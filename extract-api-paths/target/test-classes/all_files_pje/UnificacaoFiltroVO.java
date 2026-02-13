package br.jus.pje.nucleo.entidades;

import java.util.Date;

import br.jus.pje.nucleo.enums.TipoPessoaEnum;


/**
 * classe criada para facilitar a manutençao e controle de objetos envolvidos no filtro da tela unificacao
 * @author luiz.mendes
 *
 */
public class UnificacaoFiltroVO {
	
	private int identificadorBuscaPessoa = NUMERO_IDENTIFICADOR_CPF;
	private String nomeSearch = null;
	private String idPessoaSearch = null;
	private String CPFSearch = null;
	private String CNPJSearch = null;
	private String CNPJOVSearch = null;
	private TipoPessoaEnum inTipoPessoaSearch = null;
	private String nomeAlternativoSearch = null;
	private Date dataNascimentoSearch = null;
	private Date dataAberturaSearch = null;
	private boolean buscaNascimento = Boolean.TRUE;
	private PessoaJuridica orgaoVinculacaoSearch = null;
	
	private TipoPessoaEnum inTipoPessoaPrincipalInicial = null;

	private static final int NUMERO_IDENTIFICADOR_CPF = 1;
	private static final int NUMERO_IDENTIFICADOR_CNPJ = 2;
	
	public UnificacaoFiltroVO() {}

	public UnificacaoFiltroVO(TipoPessoaEnum tipoRealPessoaPrincipal) {
		this.inTipoPessoaSearch = tipoRealPessoaPrincipal;
	}

	public int getIdentificadorBuscaPessoa() {
		return identificadorBuscaPessoa;
	}

	public void setIdentificadorBuscaPessoa(int identificadorBuscaPessoa) {
		this.identificadorBuscaPessoa = identificadorBuscaPessoa;
	}

	/**
	 * CPF da pessoa secundaria para search
	 * @return
	 */
	public String getCPFSearch() {
		return CPFSearch;
	}

	public void setCPFSearch(String cPFSearch) {
		CPFSearch = cPFSearch;
	}

	/**
	 * CNPJ da pessoa secundaria para search
	 * @return
	 */
	public String getCNPJSearch() {
		return CNPJSearch;
	}

	public void setCNPJSearch(String cNPJSearch) {
		CNPJSearch = cNPJSearch;
	}

	/**
	 * CNPJ do orgao de vinculacao da pessoa secundaria para search
	 * @return
	 */
	public String getCNPJOVSearch() {
		return CNPJOVSearch;
	}

	public void setCNPJOVSearch(String cNPJOVSearch) {
		CNPJOVSearch = cNPJOVSearch;
	}

	/**
	 * nome alternativo da pessoa secundaria para search
	 * @return
	 */
	public String getNomeAlternativo() {
		return nomeAlternativoSearch;
	}

	public void setNomeAlternativo(String nomeAlternativo) {
		this.nomeAlternativoSearch = nomeAlternativo;
	}

	/**
	 * data de nascimento da pessoa secundaria para search
	 * @return
	 */
	public Date getDataNascimento() {
		return dataNascimentoSearch;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimentoSearch = dataNascimento;
	}

	/**
	 * data de abertura da pessoa secudaria (juridica) para search
	 * @return
	 */
	public Date getDataAbertura() {
		return dataAberturaSearch;
	}

	public void setDataAbertura(Date dataAbertura) {
		this.dataAberturaSearch = dataAbertura;
	}

	public boolean isBuscaNascimento() {
		return buscaNascimento;
	}

	public void setBuscaNascimento(boolean buscaNascimento) {
		this.buscaNascimento = buscaNascimento;
	}

	/**
	 * pessoa juridica de vinculacao da pessoa autoridade para search
	 * @return
	 */
	public PessoaJuridica getOrgaoVinculacao() {
		return orgaoVinculacaoSearch;
	}

	public void setOrgaoVinculacao(PessoaJuridica orgaoVinculacao) {
		this.orgaoVinculacaoSearch = orgaoVinculacao;
	}

	/**
	 * tipo de pessoa enum para busca - inicialmente é igual ao da pessoa principal
	 * @return
	 */
	public TipoPessoaEnum getInTipoPessoaSearch() {
		return inTipoPessoaSearch;
	}

	public void setInTipoPessoaSearch(TipoPessoaEnum inTipoPessoa) {
		this.inTipoPessoaSearch = inTipoPessoa;
		if(this.inTipoPessoaPrincipalInicial == null) {
			this.inTipoPessoaPrincipalInicial = inTipoPessoa;
		}
	}
	
	/**
	 * metodo Get que retorna uma String baseada no tipoPessoaEnum selecionado no filtro.
	 * utilizado no filtro de pessoas secundarias para unificacao
	 * @return String contendo uma das seguintes letras:
	 * T - todos
	 * A - ente ou autoridade
	 * J - pessoa juridica
	 * F - pessoa fisica
	 */
	public String getInTipoPessoaFiltro() {
		if(getInTipoPessoaSearch() == null) {
			return "T";
		} else if (getInTipoPessoaSearch().equals(TipoPessoaEnum.A)){
			return "A";
		} else if(getInTipoPessoaSearch().equals(TipoPessoaEnum.J)) {
			return "J";
		} else if(getInTipoPessoaSearch().equals(TipoPessoaEnum.F)) {
			return "F";
		}else {
			return "T";
		}
	}

	public void setInTipoPessoaFiltro(String inTipoPessoa) {
		if(inTipoPessoa.equalsIgnoreCase("A")){
			this.inTipoPessoaSearch = TipoPessoaEnum.A;
		}else if(inTipoPessoa.equalsIgnoreCase("J")){
			this.inTipoPessoaSearch = TipoPessoaEnum.J;
		}else if(inTipoPessoa.equalsIgnoreCase("F")){
			this.inTipoPessoaSearch = TipoPessoaEnum.F;
		} else {
			this.inTipoPessoaSearch = null;
		}
	}

	/**
	 * nome da pessoa secundaria para search
	 * @return
	 */
	public String getNome() {
		return nomeSearch;
	}

	public void setNome(String nome) {
		this.nomeSearch = nome;
	}

	/**
	 * id da pessoa secundaria para search
	 * @return
	 */
	public String getIdPessoa() {
		return idPessoaSearch;
	}

	public void setIdPessoa(String idPessoa) {
		this.idPessoaSearch = idPessoa;
	}
	
	/**
	 * metodo para limpar o campo oposto ao escolhido (data nascimento / data abertura), evitando assim a entrada do mesmo campo na query
	 */
	public void limparCamposDataAberturaDataNascimentoAlternados(){
		if(buscaNascimento) {
			this.dataAberturaSearch = null;
		} else {
			this.dataNascimentoSearch = null;
		}
	}

	/**
	 * metodo responsavel por limpar os campos de CNPJSearch, CNPJOVSearch e/ou CPFSearch, dependendo do identificadorBuscaPessoa
	 */
	public void limparCampoCpfCnpj() {
		if(identificadorBuscaPessoa == NUMERO_IDENTIFICADOR_CPF) {
			CNPJSearch = null;
			CNPJOVSearch = null;
		} else if(identificadorBuscaPessoa == NUMERO_IDENTIFICADOR_CNPJ){
			CPFSearch = null;
			CNPJOVSearch = null;
		} else {
			CPFSearch = null;
			CNPJSearch = null;
		}
	}

	/**
	 * metodo responsavel por limpar todos os campos do filtro.
	 */
	public void limparTodosCampos() {
		identificadorBuscaPessoa = NUMERO_IDENTIFICADOR_CPF;
		nomeSearch = null;
		idPessoaSearch = null;
		CPFSearch = null;
		CNPJSearch = null;
		CNPJOVSearch = null;
		inTipoPessoaSearch = inTipoPessoaPrincipalInicial;
		nomeAlternativoSearch = null;
		dataNascimentoSearch = null;
		dataAberturaSearch = null;
		buscaNascimento = Boolean.TRUE;
		orgaoVinculacaoSearch = null;
	}
}