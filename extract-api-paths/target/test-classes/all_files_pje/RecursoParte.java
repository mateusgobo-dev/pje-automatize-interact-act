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
package br.jus.pje.jt.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import br.jus.pje.nucleo.entidades.ProcessoParte;

@Entity
@Table(name = RecursoParte.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_recurso_parte", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_recurso_parte"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RecursoParte implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RecursoParte,Integer> {

	public static final String TABLE_NAME = "tb_recurso_parte";
	private static final long serialVersionUID = 1L;

	private int idRecursoParte;
	private AssistenteAdmissibilidadeRecurso assistenteAdmissibilidadeRecurso;
	private ProcessoParte processoParte;
	

	@Id
	@GeneratedValue(generator = "gen_recurso_parte")
	@Column(name = "id_recurso_parte", columnDefinition = "integer", nullable = false, unique = true)
	@NotNull
	public int getIdRecursoParte() {
		return idRecursoParte;
	}

	public void setIdRecursoParte(int idRecursoParte) {
		this.idRecursoParte = idRecursoParte;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_assis_admis_rec")
	public AssistenteAdmissibilidadeRecurso getAssistenteAdmissibilidadeRecurso() {
		return assistenteAdmissibilidadeRecurso;
	}

	public void setAssistenteAdmissibilidadeRecurso(AssistenteAdmissibilidadeRecurso assistenteAdmissibilidadeRecurso) {
		this.assistenteAdmissibilidadeRecurso = assistenteAdmissibilidadeRecurso;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte")
	public ProcessoParte getProcessoParte() {
		return processoParte;
	}

	public void setProcessoParte(ProcessoParte processoParte) {
		this.processoParte = processoParte;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RecursoParte> getEntityClass() {
		return RecursoParte.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdRecursoParte());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
