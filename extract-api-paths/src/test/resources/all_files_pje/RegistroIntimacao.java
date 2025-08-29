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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.TipoResultadoAvisoRecebimentoEnum;

@Entity
@Table(name = RegistroIntimacao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_registro_intimacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_registro_intimacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class RegistroIntimacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<RegistroIntimacao,Integer> {

	public static final String TABLE_NAME = "tb_registro_intimacao";
	private static final long serialVersionUID = 1L;

	private int id;
	private String numeroAvisoRecebimento;
	private ProcessoParteExpediente processoParteExpediente;
	private Date data;
	private TipoResultadoAvisoRecebimentoEnum resultado;

	private ProcessoDocumento processoDocumento;

	public RegistroIntimacao() {
	}

	@Id
	@GeneratedValue(generator = "gen_registro_intimacao")
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "nr_aviso_recebimento", nullable = false, length = 30)
	@NotNull
	@Length(max = 100)
	public String getNumeroAvisoRecebimento() {
		return this.numeroAvisoRecebimento;
	}

	public void setNumeroAvisoRecebimento(String numeroAvisoRecebimento) {
		this.numeroAvisoRecebimento = numeroAvisoRecebimento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_parte_expediente", nullable = false)
	@NotNull
	public ProcessoParteExpediente getProcessoParteExpediente() {
		return processoParteExpediente;
	}

	public void setProcessoParteExpediente(ProcessoParteExpediente processoParteExpediente) {
		this.processoParteExpediente = processoParteExpediente;
	}

	@Column(name = "dt_registro", nullable = false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento", nullable = false)
	@NotNull
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@Column(name = "in_resultado", length = 1)
	@Enumerated(EnumType.STRING)
	@NotNull
	public TipoResultadoAvisoRecebimentoEnum getResultado() {
		return resultado;
	}

	public void setResultado(TipoResultadoAvisoRecebimentoEnum resultado) {
		this.resultado = resultado;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends RegistroIntimacao> getEntityClass() {
		return RegistroIntimacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getId());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
