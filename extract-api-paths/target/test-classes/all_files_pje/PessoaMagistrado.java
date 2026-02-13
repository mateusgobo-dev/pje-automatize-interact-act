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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Entity
@Table(name = PessoaMagistrado.TABLE_NAME)
@SecondaryTables({ @SecondaryTable(name = "tb_usuario_login", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_usuario", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_usuario", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa", referencedColumnName = "id") }),
	@SecondaryTable(name = "tb_pessoa_fisica", pkJoinColumns = { @PrimaryKeyJoinColumn(name = "id_pessoa_fisica", referencedColumnName = "id") }) })
public class PessoaMagistrado extends PessoaFisicaEspecializada implements java.io.Serializable {

	public static final String TABLE_NAME = "tb_pessoa_magistrado";
	private static final long serialVersionUID = 1L;
	private String matricula;
	private Date dataPosse;
	private Boolean magistradoAtivo;

	private List<EstatisticaProcessoJusticaFederal> estatisticaProcessoJusticaFederalList = new ArrayList<EstatisticaProcessoJusticaFederal>(
			0);

	public PessoaMagistrado() {
		setInTipoPessoa(TipoPessoaEnum.F);
	}

	@Column(name = "nr_matricula", length = 15, nullable=false)
	@Length(max = 15)
	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_posse")
	public Date getDataPosse() {
		return dataPosse;
	}

	public void setDataPosse(Date dataPosse) {
		this.dataPosse = dataPosse;
	}

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "pessoaMagistrado")
	public List<EstatisticaProcessoJusticaFederal> getEstatisticaProcessoJusticaFederalList() {
		return estatisticaProcessoJusticaFederalList;
	}

	public void setEstatisticaProcessoJusticaFederalList(
			List<EstatisticaProcessoJusticaFederal> estatisticaProcessoJusticaFederalList) {
		this.estatisticaProcessoJusticaFederalList = estatisticaProcessoJusticaFederalList;
	}

	@Transient
	public Boolean getMagistradoAtivo() {
		
		if(this.magistradoAtivo == null){
			this.magistradoAtivo = (this.getPessoa().getEspecializacoes() & PessoaFisica.MAG) == PessoaFisica.MAG;
		}else{
			return this.magistradoAtivo;
		}
		return magistradoAtivo;		
	}
	
	public void setMagistradoAtivo(Boolean magistradoAtivo) {
		this.magistradoAtivo = magistradoAtivo;
	}
	
	@Override
	public String toString() {
		return super.getNome();
	}

	@Transient
	@Override
	public Class<? extends PessoaFisicaEspecializada> getEntityClass() {
		return PessoaMagistrado.class;
	}
}