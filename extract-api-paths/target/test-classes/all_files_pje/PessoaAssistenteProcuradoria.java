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
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.PessoaAssistenteProcuradorEnum;

@Entity
@Table(name = PessoaAssistenteProcuradoria.TABLE_NAME)
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaAssistenteProcuradoria extends PessoaFisicaEspecializada implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pess_assistente_procurd";
	private static final long serialVersionUID = 1L;

	private String numeroMatricula;
	private Estado idUfOab;
	private String numeroOab;
	private String letraOab;
	private PessoaAssistenteProcuradorEnum inTipoInscricaoOab = PessoaAssistenteProcuradorEnum.A;
	private Date dataExpedicaoOab;
	private Date dataCadastro;
	private Boolean assistenteProcuradoriaAtivo;

	public PessoaAssistenteProcuradoria() {
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_cadastro", nullable = false)
	@NotNull
	public Date getDataCadastro() {
		return this.dataCadastro;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_expedicao_oab")
	public Date getDataExpedicaoOab() {
		return this.dataExpedicaoOab;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_uf_oab")
	public Estado getIdUfOab() {
		return this.idUfOab;
	}

	@Column(name = "in_tipo_inscricao_oab", length = 1)
	@Enumerated(EnumType.STRING)
//	@Type(type = "br.jus.pje.nucleo.enums.PessoaAssistenteProcuradorType")
	public PessoaAssistenteProcuradorEnum getInTipoInscricaoOab() {
		return this.inTipoInscricaoOab;
	}

	@Column(name = "ds_letra_oab", length = 1)
	@Length(max = 1)
	public String getLetraOab() {
		return this.letraOab;
	}

	@Column(name = "nr_matricula", length = 15)
	@Length(max = 15)
	public String getNumeroMatricula() {
		return this.numeroMatricula;
	}

	@Column(name = "nr_oab", length = 15)
	@Length(max = 15)
	public String getNumeroOab() {
		return this.numeroOab;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public void setDataExpedicaoOab(Date dataExpedicaoOab) {
		this.dataExpedicaoOab = dataExpedicaoOab;
	}

	public void setIdUfOab(Estado idUfOab) {
		this.idUfOab = idUfOab;
	}

	public void setInTipoInscricaoOab(PessoaAssistenteProcuradorEnum inTipoInscricaoOab) {
		this.inTipoInscricaoOab = inTipoInscricaoOab;
	}

	public void setLetraOab(String letraOab) {
		this.letraOab = letraOab;
	}

	public void setNumeroMatricula(String numeroMatricula) {
		this.numeroMatricula = numeroMatricula;
	}

	public void setNumeroOab(String numeroOab) {
		this.numeroOab = numeroOab;
	}
	
	@Transient
	public Boolean getAssistenteProcuradoriaAtivo() {
		if(this.assistenteProcuradoriaAtivo == null){
			this.assistenteProcuradoriaAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.ASP) == PessoaFisica.ASP;
		}else{
			return this.assistenteProcuradoriaAtivo;
		}
		return assistenteProcuradoriaAtivo;
	}
	
	public void setAssistenteProcuradoriaAtivo(Boolean assistenteProcuradoriaAtivo) {
		this.assistenteProcuradoriaAtivo = assistenteProcuradoriaAtivo;
	}	

	@Transient
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaAssistenteProcuradoria.class;
	}
}