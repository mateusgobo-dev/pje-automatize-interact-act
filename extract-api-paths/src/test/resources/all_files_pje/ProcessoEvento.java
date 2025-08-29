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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.entidades.lancadormovimento.ComplementoSegmentado;

@Entity
@Table(name = ProcessoEvento.TABLE_NAME)
@IndexedEntity(id="idProcessoEvento",
	value="movimentacoes",
	mappings={
		@Mapping(beanPath="processo.idProcesso", mappedPath="processo"),
		@Mapping(beanPath="evento.codEvento", mappedPath="codigo"),
		@Mapping(beanPath="dataAtualizacao", mappedPath="data"),
		@Mapping(beanPath="textoFinalExterno", mappedPath="descricao"),
		@Mapping(beanPath="textoFinalInterno", mappedPath="textoInterno"),
		@Mapping(beanPath="visibilidadeExterna", mappedPath="visivel")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_evento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_evento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoEvento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ProcessoEvento,Integer> {

	public static final String TABLE_NAME = "tb_processo_evento";
	private static final long serialVersionUID = 1L;

	private int idProcessoEvento;
	private Processo processo;
	private ProcessoDocumento processoDocumento;
	private Evento evento;
	private Usuario usuario;
	private Date dataAtualizacao;
	private String descricaoEvento;
	private Long idJbpmTask;
	private Long idProcessInstance;
	private Tarefa tarefa;
	private String nomeUsuario;
	private String cpfUsuario;
	private String cnpjUsuario;
	private boolean processado = false;
	private boolean verificadoProcessado = false;
	// ProcessoEvento que excluiu esta linha de movimentação processual
	private ProcessoEvento processoEventoExcludente;
	private Boolean visibilidadeExterna;
	private String observacao;
	
	// Propriedades vindas de MovimentoProcesso
	private String textoFinalInterno;
	private String textoFinalExterno;
	private String textoParametrizado;
	
	private Boolean ativo = Boolean.TRUE;
	
	private List<ComplementoSegmentado> complementoSegmentadoList = new ArrayList<ComplementoSegmentado>();


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_evento_excludente", nullable = true)
	public ProcessoEvento getProcessoEventoExcludente() {
		return processoEventoExcludente;
	}

	public void setProcessoEventoExcludente(ProcessoEvento processoEventoExcludente) {
		this.processoEventoExcludente = processoEventoExcludente;
	}

	@Id
	@GeneratedValue(generator = "gen_processo_evento", strategy=GenerationType.SEQUENCE)
	@Column(name = "id_processo_evento", unique = true, nullable = false)
	public int getIdProcessoEvento() {
		return idProcessoEvento;
	}

	public void setIdProcessoEvento(int idProcessoEvento) {
		this.idProcessoEvento = idProcessoEvento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo", nullable = false)
	@NotNull
	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_documento")
	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento", nullable = false)
	@NotNull
	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_atualizacao")
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	/**
	 * @return the descricaoEvento
	 */
	@Column(name = "ds_processo_evento")
	public String getDescricaoEvento() {
		return descricaoEvento;
	}

	/**
	 * @param descricaoEvento
	 *            the descricaoEvento to set
	 */
	public void setDescricaoEvento(String descricaoEvento) {
		this.descricaoEvento = descricaoEvento;
	}

	@Transient
	public String getDescricao() {
		return (descricaoEvento != null && !descricaoEvento.isEmpty()) ? descricaoEvento : evento.getEvento();
	}

	@Column(name = "id_jbpm_task")
	public Long getIdJbpmTask() {
		return idJbpmTask;
	}

	public void setIdJbpmTask(Long idJbpmTask) {
		this.idJbpmTask = idJbpmTask;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarefa")
	public Tarefa getTarefa() {
		return tarefa;
	}

	public void setTarefa(Tarefa tarefa) {
		this.tarefa = tarefa;
	}

	@Column(name = "id_process_instance")
	public Long getIdProcessInstance() {
		return idProcessInstance;
	}

	public void setIdProcessInstance(Long idProcessInstance) {
		this.idProcessInstance = idProcessInstance;
	}

	@Column(name = "ds_nome_usuario", length = 100)
	@Length(max = 100)
	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	@Column(name = "ds_cpf_usuario", length = 50)
	@Length(max = 50)
	public String getCpfUsuario() {
		return cpfUsuario;
	}

	public void setCpfUsuario(String cpfUsuario) {
		this.cpfUsuario = cpfUsuario;
	}

	@Column(name = "ds_cnpj_usuario", length = 50)
	@Length(max = 50)
	public String getCnpjUsuario() {
		return cnpjUsuario;
	}

	public void setCnpjUsuario(String cnpjUsuario) {
		this.cnpjUsuario = cnpjUsuario;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idProcessoEvento;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ProcessoEvento))
			return false;
		ProcessoEvento other = (ProcessoEvento) obj;
		if (getIdProcessoEvento() != other.getIdProcessoEvento())
			return false;
		return true;
	}

	public void setProcessado(boolean processado) {
		this.processado = processado;
	}

	@Column(name = "in_processado", nullable = false)
	public boolean isProcessado() {
		return processado;
	}

	public void setVerificadoProcessado(boolean verificadoProcessado) {
		this.verificadoProcessado = verificadoProcessado;
	}

	@Column(name = "in_verificado_processado", nullable = false)
	public boolean isVerificadoProcessado() {
		return verificadoProcessado;
	}

	@Column(name = "in_visibilidade_externa")
	public Boolean getVisibilidadeExterna() {
		return visibilidadeExterna;
	}

	public void setVisibilidadeExterna(Boolean visibilidadeExterna) {
		this.visibilidadeExterna = visibilidadeExterna;
	}
	
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name="ds_observacao")
	public String getObservacao() {
		return observacao;
	}
	
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Transient
	public String getTextoFinal() {
		return (getTextoFinal(Boolean.TRUE));
	}

	/**
	 * Retorna o texto final do movimento. Utilizado pelo lançador de
	 * movimentos.
	 * 
	 * @author Guilherme Bispo
	 * @return Texto final do movimento.
	 */
	@Override
	public String toString() {
		return getTextoFinal();
	}
	
	@SuppressWarnings("deprecation")
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "movimentoProcesso")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<ComplementoSegmentado> getComplementoSegmentadoList() {
		return complementoSegmentadoList;
	}

	public void setComplementoSegmentadoList(List<ComplementoSegmentado> complementoSegmentadoList) {
		this.complementoSegmentadoList = complementoSegmentadoList;
	}

	@Column(name = "ds_texto_parametrizado")
	public String getTextoParametrizado() {
		return textoParametrizado;
	}

	public void setTextoParametrizado(String textoParametrizado) {
		this.textoParametrizado = textoParametrizado;
	}

	@Column(name = "ds_texto_final_interno")
	public String getTextoFinalInterno() {
		return textoFinalInterno;
	}

	@Column(name = "ds_texto_final_externo")
	public String getTextoFinalExterno() {
		return textoFinalExterno;
	}

	public void setTextoFinalInterno(String textoFinal) {
		this.textoFinalInterno = textoFinal;
	}

	public void setTextoFinalExterno(String textoFinal) {
		this.textoFinalExterno = textoFinal;
	}

	@Transient
	public String getTextoFinal(Boolean isVisibilidadeExterna) {
		return isVisibilidadeExterna ? getTextoFinalExterno() : getTextoFinalInterno();
	}
	
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ProcessoEvento> getEntityClass() {
		return ProcessoEvento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdProcessoEvento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
