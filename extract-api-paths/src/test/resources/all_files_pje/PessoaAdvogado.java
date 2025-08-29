/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Entidade representativa de um advogado no sistema, assim entendido como 
 * a pessoa física que tem capacidade postulatória, ou seja, que pode ajuizar
 * processos e neles peticionar sem restrições.
 * 
 * @author infox
 *
 */
@Entity
@Table(name = PessoaAdvogado.TABLE_NAME)
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaAdvogado extends PessoaFisicaEspecializada implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_advogado";
	private static final long serialVersionUID = 1L;

	private Estado ufOAB;
	private String numeroOAB;
	private String letraOAB;
	private Date dataExpedicaoOAB;
	private PessoaAdvogadoTipoInscricaoEnum tipoInscricao;
	private Date dataCadastro;
	private Date dataPosse;
	private Boolean incluirProcessoPush;
	private Boolean advogadoAtivo; 

	/**
	 * Construtor padrão.
	 */
	public PessoaAdvogado() {
	}

	/**
	 * Recupera a unidade federativa a que está vinculada a inscrição deste
	 * advogado na Ordem dos Advogados do Brasil.
	 * 
	 * @return a unidade federativa vinculada
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_uf_oab")
	public Estado getUfOAB() {
		return ufOAB;
	}

	/**
	 * Atribui a este advogado um unidade federativa como sendo a vinculada a 
	 * sua inscrição na Ordem dos Advogados do Brasil.
	 * 
	 * @param ufOAB a unidade federativa a vincular.
	 */
	public void setUfOAB(Estado ufOAB) {
		this.ufOAB = ufOAB;
	}

	/**
	 * Recupera o número de inscrição do advogado na Ordem dos Advogados do Brasil.
	 * 
	 * @return o número de inscrição
	 */
	@Column(name = "nr_oab", length = 15)
	@Length(max = 15)
	public String getNumeroOAB() {
		return numeroOAB;
	}

	/**
	 * Atribui a este advogado um número de inscrição na Ordem dos Advogados do Brasil.
	 * 
	 * @param numeroOAB o número de inscrição
	 */
	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	/**
	 * Recupera a letra identificadora da inscrição deste advogado na
	 * Ordem dos Advogados do Brasil.
	 * 
	 * @return a letra identificadora
	 */
	@Column(name = "ds_letra_oab", length = 1)
	@Length(max = 1)
	public String getLetraOAB() {
		return letraOAB;
	}

	/**
	 * Atribui a este advogado uma letra identificadora do tipo de sua inscrição na
	 * Ordem dos Advogados do Brasil.
	 * 
	 * @param letraOAB a letra identificadora
	 */
	public void setLetraOAB(String letraOAB) {
		this.letraOAB = letraOAB != null ? letraOAB.toUpperCase() : null;
	}

	/**
	 * Recupera a data de expedição da inscrição deste advogado na 
	 * Ordem dos Advogados do Brasil.
	 * 
	 * @return a data de expedição da inscrição
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expedicao_oab")
	public Date getDataExpedicaoOAB() {
		return this.dataExpedicaoOAB;
	}

	/**
	 * Atribui uma data como sendo a de expedição da inscrição deste advogado
	 * na Ordem dos Advogados do Brasil.
	 * 
	 * @param dataExpedicaoOAB a data de expedição.
	 */
	public void setDataExpedicaoOAB(Date dataExpedicaoOAB) {
		this.dataExpedicaoOAB = dataExpedicaoOAB;
	}

	/**
	 * Recupera o tipo de inscrição deste advogado na Ordem dos Advogados do Brasil.
	 * 
	 * @return o tipo de inscrição
	 * @see PessoaAdvogadoTipoInscricaoEnum
	 */
	@Column(name = "in_tipo_inscricao", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.PessoaAdvogadoTipoInscricaoType")
	public PessoaAdvogadoTipoInscricaoEnum getTipoInscricao() {
		return tipoInscricao;
	}

	/**
	 * Atribui um tipo de inscrição à do advogado.
	 * 
	 * @param tipoInscricao o tipo de inscrição
	 */
	public void setTipoInscricao(PessoaAdvogadoTipoInscricaoEnum tipoInscricao) {
		this.tipoInscricao = tipoInscricao;
	}

	/**
	 * Recupera a data de cadastro deste advogado no sistema.
	 * 
	 * @return a data do cadastro.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro")
	public Date getDataCadastro() {
		return dataCadastro;
	}

	/**
	 * Atribui uma data como sendo a do cadastro deste advogado no sistema.
	 * 
	 * @param dataCadastro a data a ser atribuída
	 */
	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_posse")
	public Date getDataPosse() {
		return dataPosse;
	}

	public void setDataPosse(Date dataPosse) {
		this.dataPosse = dataPosse;
	}

	/**
	 * Recupera a inscrição do advogado na Ordem dos Advogados do Brasil
	 * no formato UFNNNNNN-L.
	 * 
	 * @return a inscrição formatada, ou uma String vazia.
	 */
	@Transient
	public String getOabFormatado() {
		if (getNumeroOAB() == null) {
			return "";
		}
		StringBuilder bf = new StringBuilder();
		if (getUfOAB() != null) {
			bf.append(getUfOAB().getCodEstado());
		}
		bf.append(getNumeroOAB());
		if (getLetraOAB() != null && getLetraOAB().trim().length() > 0) {
			bf.append("-" + getLetraOAB());
		}
		return bf.toString();
	}
	
	@Transient
	public Boolean getAdvogadoAtivo() {
		if(this.advogadoAtivo == null){
			this.advogadoAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.ADV) == PessoaFisica.ADV;
		}else{
			return this.advogadoAtivo;
		}
		return advogadoAtivo;
	}
	
	public void setAdvogadoAtivo(Boolean advogadoAtivo) {
		this.advogadoAtivo = advogadoAtivo;
	}	

	/* (non-Javadoc)
	 * @see br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada#getPessoaStr()
	 */
	@Override
	@Transient
	public String getPessoaStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(getNome() == null || getNome().isEmpty() ? "" : "Advogado: " + getNome().toUpperCase());
		sb.append(getOabFormatado() == null || getOabFormatado().isEmpty() ? "" : "\n" + "OAB: " + getOabFormatado());
		return sb.toString();
	}

	/**
	 * Recupera indicação quanto à característica de os processos vinculados a este
	 * advogado deverem ou não ser incluídos automaticamente no serviço de atualização por
	 * email (push).
	 * 
	 * @return true, se a inclusão deve ser automática.
	 */
	@Column(name = "in_incluir_processo_push")
	public Boolean getIncluirProcessoPush() {
		return incluirProcessoPush;
	}

	/**
	 * Permite indicar se este advogado deve ter os processos a ele vinculados 
	 * incluídos automaticamente no serviço de atualização por email (push).
	 * 
	 * @param incluirProcessoPush a indicação quanto à intenção de incluir no serviço
	 */
	public void setIncluirProcessoPush(Boolean incluirProcessoPush) {
		this.incluirProcessoPush = incluirProcessoPush;
	}
	
	/** 
	 * PJEII-18039. Exibe a OAB do advogado junto com o nome e CPF. 
	 * 
	 * Nome Completo - OAB XXXXX - CPF: XXX.XXX.XXX-XX (ADVOGADO)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		if (getDocumentoCpfCnpj() != null)
			return super.toString().concat(" - OAB ").concat(getOabFormatado()).
        		concat(" - CPF: ").concat(StringUtil.formartCpf(getDocumentoCpfCnpj())).
        		concat(" (").concat("ADVOGADO").concat(")");
		else 
			return super.toString();
	}

	public String toString(String nome) {
		if (getDocumentoCpfCnpj() != null)
			return nome.concat(" - OAB ").concat(getOabFormatado()).concat(" - CPF: ")
					.concat(StringUtil.formartCpf(getDocumentoCpfCnpj())).concat(" (").concat("ADVOGADO").concat(")");
		else
			return nome;
	}

	@Transient
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaAdvogado.class;
	}
}