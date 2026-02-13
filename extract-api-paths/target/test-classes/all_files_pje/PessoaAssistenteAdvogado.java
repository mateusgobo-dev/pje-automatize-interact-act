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

import br.jus.pje.nucleo.enums.PessoaAssistenteAdvogadoEnum;

@Entity
@Table(name = PessoaAssistenteAdvogado.TABLE_NAME)
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaAssistenteAdvogado extends PessoaFisicaEspecializada implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_assistente_adv";
	private static final long serialVersionUID = 1L;

	private Estado ufOAB;
	private String numeroOAB;
	private String letraOAB;
	private PessoaAssistenteAdvogadoEnum tipoInscricaoOAB;
	private Date dataExpedicaoOAB;
	private Date dataCadastro;
	private Boolean assistenteAdvogadoAtivo;

	public PessoaAssistenteAdvogado() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_uf_oab")
	public Estado getUfOAB() {
		return ufOAB;
	}

	public void setUfOAB(Estado ufOAB) {
		this.ufOAB = ufOAB;
	}

	@Column(name = "nr_oab", length = 15)
	@Length(max = 15)
	public String getNumeroOAB() {
		return numeroOAB;
	}

	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	@Column(name = "ds_letra_oab", length = 1)
	@Length(max = 1)
	public String getLetraOAB() {
		return letraOAB;
	}

	public void setLetraOAB(String letraOAB) {
		this.letraOAB = letraOAB != null ? letraOAB.toUpperCase() : null;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expedicao_oab")
	public Date getDataExpedicaoOAB() {
		return this.dataExpedicaoOAB;
	}

	public void setDataExpedicaoOAB(Date dataExpedicaoOAB) {
		this.dataExpedicaoOAB = dataExpedicaoOAB;
	}

	@Column(name = "in_tipo_inscricao_oab", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.PessoaAssistenteAdvogadoType")
	public PessoaAssistenteAdvogadoEnum getTipoInscricaoOAB() {
		return tipoInscricaoOAB;
	}

	public void setTipoInscricaoOAB(PessoaAssistenteAdvogadoEnum tipoInscricaoOAB) {
		this.tipoInscricaoOAB = tipoInscricaoOAB;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro")
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
	
	@Transient
	public Boolean getAssistenteAdvogadoAtivo() {
		if(this.assistenteAdvogadoAtivo == null){
			this.assistenteAdvogadoAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.ASA) == PessoaFisica.ASA;
		}else{
			return this.assistenteAdvogadoAtivo;
		}
		return assistenteAdvogadoAtivo;
	}
	
	public void setAssistenteAdvogadoAtivo(Boolean assistenteAdvogadoAtivo) {
		this.assistenteAdvogadoAtivo = assistenteAdvogadoAtivo;
	}

	@Transient
	public String getOabFormatado() {
		if (getNumeroOAB() == null) {
			return null;
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
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaAssistenteAdvogado.class;
	}
}