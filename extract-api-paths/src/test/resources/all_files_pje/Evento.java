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
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;

/**
 * Entidade representativa dos tipos de movimentação passíveis de lançamento em um dado processo.
 * 
 */
@Entity
@javax.persistence.Cacheable(true)
@Table(name = Evento.TABLE_NAME)
@AttributeOverrides({
	@AttributeOverride(name="code", column=@Column(name = "cd_evento", insertable=false, updatable=false)),
	@AttributeOverride(name="description", column=@Column(name = "ds_evento", insertable=false, updatable=false)),
	@AttributeOverride(name="pathDescription", column=@Column(name = "ds_caminho_completo", unique=true))
})
@AssociationOverrides({
	@AssociationOverride(name="parent", joinColumns={@JoinColumn(name = "id_evento_superior", insertable=false, updatable=false)})
})
@org.hibernate.annotations.GenericGenerator(name = "gen_evento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_evento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class Evento extends HierarchicEntity<Evento>{

	public static final String TABLE_NAME = "tb_evento";
	private static final long serialVersionUID = 1L;

	private int idEvento;
	private String evento;
	private String observacao;
	private Boolean ativo = Boolean.TRUE;
	private Evento eventoSuperior;
	private Status status;
	private String caminhoCompleto;
	// Vindos de EventoProcessual
	private String codEvento;
	private String codEventoOutro;
	private String complemento;
	private String movimento;
	private Boolean complementar;
	private String norma;
	private String descricaoLei;
	private String leiArtigo;
	private Boolean segredoJustica = Boolean.FALSE;
	private Boolean visibilidadeExterna = Boolean.FALSE;
	private String glossario;
	private String eventoCompleto;
	private Boolean permiteLancarLote = Boolean.TRUE;
	private Boolean flagEletronico;
	private Boolean flagPapel;
	
	private Integer nivel;
	private Integer faixaInferior;
	private Integer faixaSuperior;
	private Boolean padraoSgt = Boolean.FALSE;
	private String motivoInativacao;

	private List<AplicacaoClasseEvento> aplicacaoClasseEventoList = new ArrayList<AplicacaoClasseEvento>(0);
	private List<TipoEvento> tipoEventoList = new ArrayList<TipoEvento>(0);
	private List<AplicacaoMovimento> aplicacaoMovimentoList = new ArrayList<AplicacaoMovimento>();
	// Fim de eventoprocessual

	private List<Evento> eventoList = new ArrayList<Evento>(0);
	private List<Evento> eventoAtivoList = new ArrayList<Evento>(0);

	private List<TipoProcessoDocumento> tipoProcessoDocumentoList = new ArrayList<TipoProcessoDocumento>(0);
	
	private List<TipoInformacaoCriminalRelevante> tipoICRList = new ArrayList<TipoInformacaoCriminalRelevante>(0);

	private List<EventoAgrupamento> eventoAgrupamentoList = new ArrayList<EventoAgrupamento>(0);
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.entidades.RecursiveAbstractEntity#getId()
	 */
	@Override
	@Transient
	public Integer getId() {
		return new Integer(idEvento);
	}
	
	/**
	 * Recupera o identificador do tipo de movimentação.
	 * 
	 * @return o identificador.
	 */
	@Id
	@GeneratedValue(generator = "gen_evento")
	@Column(name = "id_evento", unique = true, nullable = false)
	public int getIdEvento() {
		return this.idEvento;
	}

	/**
	 * Atribui a este tipo de movimentação um identificador. Não deve ser utilizado em 
	 * razão da adoção da estratégia de auto-geração.
	 * 
	 * @param idEvento o identificador a ser atribuído.
	 */
	public void setIdEvento(int idEvento) {
		this.idEvento = idEvento;
	}
	
	/**
	 * Recupera a descrição deste tipo de movimentação.
	 * 
	 * @return a descrição completa
	 */
	@Column(name = "ds_evento", nullable = false, length = 2000, unique = true)
	@NotNull
	@Length(max = 2000)
	public String getEvento() {
		return this.evento;
	}

	/**
	 * Atribui a este tipo de movimentação uma descrição.
	 * 
	 * @param evento a descrição a ser atribuída
	 */
	public void setEvento(String evento) {
		this.evento = evento;
		if(getDescription() == null || !getDescription().equals(evento)){
			setDescription(evento);
		}
	}

	/**
	 * Recupera um texto elucidativo eventualmente associado a este tipo de movimentação.
	 * 
	 * @return o texto elucidativo
	 */
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_observacao")
	public String getObservacao() {
		return this.observacao;
	}

	/**
	 * Atriibui a este tipo de movimentação um texto elucidativo.
	 * 
	 * @param observacao o texto elucidativo a ser atribuído.
	 */
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	/**
	 * Indica se este tipo de movimentação está ativa para utilização no sistema.
	 * 
	 * @return true, se estiver ativa.
	 */
	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	/**
	 * Permite indicar que um tipo de movimentação está ativo ou inativo no sistema.
	 * 
	 * @param ativo a indicação quanto ao tipo de movimentação estar ativo (true) ou inativo (false).
	 */
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	/**
	 * Recupera, se existente, o tipo de movimentação imediatamente superior na árvore
	 * dos tipos de movimentação.
	 * 
	 * @return o tipo de movimentação imediatamente superior ou nulo, se o tipo for uma
	 * raiz.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_evento_superior")
	public Evento getEventoSuperior() {
		return this.eventoSuperior;
	}

	/**
	 * Atribui a este tipo de movimentação um tipo hierarquicamente superior.
	 * 
	 * @param eventoSuperior o tipo de movimentação ao qual este tipo de movimentação será
	 * vinculado como filho.
	 */
	public void setEventoSuperior(Evento eventoSuperior) {
		this.eventoSuperior = eventoSuperior;
	}

	/**
	 * Recupera uma lista de tipos de movimentação diretamente vinculados a este tipo de movimentação
	 * como filhos.
	 * 
	 * @return a lista de tipos de movimentação filhos.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "eventoSuperior")
	public List<Evento> getEventoList() {
		return this.eventoList;
	}

	/**
	 * Atribui a este tipo de movimentação uma lista de tipos como filhos.
	 * Não deve ser invocado diretamente em razão da implementação JPA realizada
	 * pelo Hibernate 3.3.
	 * 
	 * @param eventoList a lista a ser atribuída.
	 */
	public void setEventoList(List<Evento> eventoList) {
		this.eventoList = eventoList;
	}

	/**
	 * Recupera a lista completa dos tipos de movimentação tidos como filhos do tipo indicado.
	 * 
	 * @return a lista de todos os tipos de movimentação filhos deste tipo.
	 * 
	 */
	@Transient
	public List<Evento> getEventoListCompleto() {
		return getEventoListCompleto(this, new ArrayList<Evento>());
	}

	/**
	 * Método recursivo destinado a permitir a identificação e carga de 
	 * todos os tipos de movimentação filhas deste tipo.
	 * 
	 * @param filho o tipo de movimentação a ser pesquisado.
	 * @param eventos a lista de tipos já identificados.
	 * @return a lista de todos os tipos de movimentação que são filhos do parâmetro dado.
	 */
	private List<Evento> getEventoListCompleto(Evento filho, List<Evento> eventos) {
		List<Evento> filhos = filho.getEventoList();
		eventos.add(filho);
		for (Evento evento : filhos) {
			getEventoListCompleto(evento, eventos);
		}
		return eventos;
	}

	@Override
	public String toString() {
		return this.evento + " (" + this.codEvento + ")";
	}

	/**
	 * Recupera a lista de tipos de documentos que estão diretamente associados a este tipo de movimentação.
	 * 
	 * @return a lista de tipos de documentos associados a este tipo de movimentação.
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_tipo_evento", joinColumns = { @JoinColumn(name = "id_evento", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_tipo_processo_documento", nullable = false, updatable = false) })
	public List<TipoProcessoDocumento> getTipoProcessoDocumentoList() {
		return tipoProcessoDocumentoList;
	}
	
	/**
	 * Atribui a este tipo de movimentação uma lista de tipos de documentos diretamente associados.
	 * Não deve ser utilizado diretamente em razão da implementação JPA do Hibernate 3.3.
	 * 
	 * @param tipoProcessoDocumentoList a lista a ser atribuída
	 */
	public void setTipoProcessoDocumentoList(List<TipoProcessoDocumento> tipoProcessoDocumentoList) {
		this.tipoProcessoDocumentoList = tipoProcessoDocumentoList;
	}

	/**
	 * Recupera a lista de tipos de informação criminal relevante a ser associada com este tipo de movimentação.
	 * 
	 * @return a lista de tipos de informação criminal relevante associada.
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	@JoinTable(name = "tb_tipo_icr_evento", joinColumns = { @JoinColumn(name = "id_evento", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "id_tipo_icr", nullable = false, updatable = false) })
	public List<TipoInformacaoCriminalRelevante> getTipoICRList() {
		return tipoICRList;
	}
	
	/**
	 * Atribui a este tipo de movimentação uma lista de tipos de informação criminal relevante associados.
	 * Não deve ser utilizado diretamente em razão da implementação JPA do Hibernate 3.3.
	 *  
	 * @param tipoICRList a lista a ser atribuída 
	 */
	public void setTipoICRList(List<TipoInformacaoCriminalRelevante> tipoICRList) {
		this.tipoICRList = tipoICRList;
	}

	/**
	 * Recupera a lista de agrupamentos de tipos de movimentação associadas a este tipo.
	 * 
	 * @return a lista de agrupamentos associada
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "evento")
	public List<EventoAgrupamento> getEventoAgrupamentoList() {
		return eventoAgrupamentoList;
	}

	/**
	 * Atribui a este tipo de movimentação uma lista de agrupamentos associados.
	 * Não deve ser utilizado diretamente em razão da implementação JPA do Hibernate 3.3.
	 * 
	 * @param eventoAgrupamentoList a lista a ser atribuída
	 */
	public void setEventoAgrupamentoList(List<EventoAgrupamento> eventoAgrupamentoList) {
		this.eventoAgrupamentoList = eventoAgrupamentoList;
	}

	/**
	 * Recupera a situação deste tipo de movimentação (se ativo e/ou mensurável).
	 * 
	 * @return a situação deste tipo de movimentação
	 * @see Status
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_status")
	public Status getStatus() {
		return status;
	}

	/**
	 * Atribui a este tipo de movimentação uma situação.
	 * 
	 * @param status a situação a ser atribuída
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Recupera o caminho completo deste tipo de movimentação, a partir da raiz.
	 * 
	 * @return o caminho completo
	 */
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_caminho_completo", insertable=false, updatable=false)
	public String getCaminhoCompleto() {
		return caminhoCompleto;
	}

	/**
	 * Atribui a este tipo de movimentação um caminho completo.
	 * 
	 * @param caminhoCompleto o caminho a ser atribuído.
	 */
	public void setCaminhoCompleto(String caminhoCompleto) {
		this.caminhoCompleto = caminhoCompleto;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Evento)) {
			return false;
		}
		Evento other = (Evento) obj;
		if (getIdEvento() != other.getIdEvento()) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdEvento();
		return result;
	}
	
	/**
	 * Cria, em tempo de execução, o caminho completo deste tipo de movimentação.
	 * 
	 * @return o caminho completo criado em tempo de execução.
	 */
	@Transient
	public String generateEventoCompleto() {
		Evento eventoSuperior = this.eventoSuperior;
		if (eventoSuperior == null) {
			return evento;
		}
		List<Evento> list = getListEventoAtePai();
		StringBuilder sb = new StringBuilder();
		for (int i = list.size() - 1; i >= 0; i--) {
			if (sb.length() > 0) {
				sb.append("|");
			}
			sb.append(list.get(i));
		}
		sb.append("|");
		sb.append(evento);
		return sb.toString();
	}
	
	/**
	 * Recupera a lista de tipos de movimentações superiores na hierarquia, excluindo-se
	 * este e na ordem ramo->raiz
	 * 
	 * @return a lista de tipos de movimentações superiores a este
	 */
	@Transient
	public List<Evento> getListEventoAtePai() {
		List<Evento> list = new ArrayList<Evento>();
		Evento eventoSuperior = this.eventoSuperior;
		while (eventoSuperior != null) {
			list.add(eventoSuperior);
			eventoSuperior = eventoSuperior.getEventoSuperior();
		}
		return list;
	}
	
	@Transient
	public List<Evento> getEventoAtivoList() {
		if(eventoAtivoList.size() == 0) {
			for(Evento evento : this.eventoList) {
				if(evento.getAtivo()) {
					eventoAtivoList.add(evento);
				}
			}
		}
		return eventoAtivoList;
	}
	
	// Vindo de EventoProcessual
	/**
	 * Recupera o código do tipo de movimentação, preferencialmente o nacional.
	 * 
	 * @return o código do tipo de movimentação
	 */
	@Column(name = "cd_evento", length = 30, nullable = false, unique = true)
	@NotNull
	@Length(max = 30)
	public String getCodEvento() {
		return this.codEvento;
	}

	/**
	 * Atribui a este tipo de movimentação um código.
	 *  
	 * @param codEvento o código a ser atribuído
	 */
	public void setCodEvento(String codEvento) {
		this.codEvento = codEvento;
	}

	/**
	 * Recupera o código alternativo deste tipo de movimentação.
	 * 
	 * @return o código alternativo deste tipo de movimentação.
	 */
	@Column(name = "cd_evento_outro", length = 30)
	@Length(max = 30)
	public String getCodEventoOutro() {
		return this.codEventoOutro;
	}

	/**
	 * Atribui a este tipo de movimentação um código alternativo de identificação.
	 * 
	 * @param codEventoOutro o código alternativo a ser atribuído
	 */
	public void setCodEventoOutro(String codEventoOutro) {
		this.codEventoOutro = codEventoOutro;
	}

	/**
	 * Recupera o complemento padrão deste tipo de movimentação.
	 * 
	 * @return o complemento padrão deste tipo de movimentação
	 */
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_complemento")
	public String getComplemento() {
		return this.complemento;
	}

	/**
	 * Atribui a este tipo de movimentação um complemento padrão.
	 * 
	 * @param complemento o complemento padrão a ser atribuído
	 */
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	/**
	 * Recupera o texto final padrão a ser exibido pelo sistema para uma dada movimentação.
	 * 
	 * Substituído pelo atributo da classe ProcessoEvento.textoFinal, que contempla
	 * o movimento com os complementos preenchidos pelo usuário.
	 * 
	 * @return texto do movimento(sem os complementos).
	 * @author David/Zanon
	 * @since 1.2.0
	 * @see ProcessoEvento#getTextoFinal
	 */
	@Column(name = "ds_movimento", length = 2000)
	@Length(max = 2000)
	@Deprecated
	public String getMovimento() {
		return this.movimento;
	}

	/**
	 * Atribui a este tipo de movimentação um texto final padrão para exibição pública.
	 * Deve ser preterido em favor de {@link ProcessoEvento#getTextoFinal()}
	 * 
	 * @param movimento o texto final a ser atribuído
	 */
	@Deprecated
	public void setMovimento(String movimento) {
		this.movimento = movimento;
	}

	/**
	 * Indica se este tipo de movimentação é complementar a algum outro.
	 * 
	 * @return true, se for complementar.
	 */
	@Column(name = "in_complementar")
	public Boolean getComplementar() {
		return this.complementar;
	}

	/**
	 * Permite marcar este tipo de movimentação como complementar de outro.
	 * 
	 * @param complementar true, para marcar como complementar
	 */
	public void setComplementar(Boolean complementar) {
		this.complementar = complementar;
	}

	/**
	 * Recupera a norma legal que teria dado causa ao surgimento deste tipo de movimentação.
	 * 
	 * @return a norma legal
	 */
	@Column(name = "ds_norma", length = 100)
	@Length(max = 100)
	public String getNorma() {
		return norma;
	}

	/**
	 * Atribui a este tipo de movimentação uma norma legal como justificadora de sua criação.
	 * 
	 * @param norma a norma justificadora
	 */
	public void setNorma(String norma) {
		this.norma = norma;
	}

	/**
	 * Recupera a lei que teria dado causa à criação deste tipo de movimentação.
	 * 
	 * @return a lei justificadora
	 */
	@Column(name = "ds_lei", length = 100)
	@Length(max = 100)
	public String getDescricaoLei() {
		return descricaoLei;
	}

	/**
	 * Atribui a este tipo de movimentação uma lei justificadora de sua criação.
	 * 
	 * @param descricaoLei a lei justificadora.
	 */
	public void setDescricaoLei(String descricaoLei) {
		this.descricaoLei = descricaoLei;
	}

	/**
	 * Recupera o artigo de lei que teria dado causa à criação deste tipo de movimentação.
	 * 
	 * @return o artigo de lei justificador
	 */
	@Column(name = "ds_lei_artigo", length = 100)
	@Length(max = 100)
	public String getLeiArtigo() {
		return leiArtigo;
	}

	/**
	 * Atribui a este tipo de movimentação um artigo de lei justificador de sua criação.
	 * 
	 * @param leiArtigo o artigo justificador.
	 */
	public void setLeiArtigo(String leiArtigo) {
		this.leiArtigo = leiArtigo;
	}

	/**
	 * Recupera marca indicativa do nível de sigilo padrão a ser adotado por movimentações
	 * deste tipo.
	 * 
	 * @return true, se a movimentação derivada dever ser, por padrão, sigilosa.
	 */
	@Column(name = "in_segredo_justica")
	public Boolean getSegredoJustica() {
		return segredoJustica;
	}

	/**
	 * Permite indicar o o nível de sigilo padrão a ser adotado por movimentações
	 * deste tipo.
	 * 
	 * @param segredoJustica true, se a movimentação derivada dever ser, por padrão, sigilosa.
	 */
	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}
	
	/**
	 * Recupera marca indicativa de que movimentações deste tipo podem ou não ser lançadas
	 * em lotes.
	 * 
	 * @return true, se as movimentações derivadas deste tipo puderem ser lançadas em lote
	 */
	@Column(name = "in_permite_lancar_lote", nullable = false)
	@NotNull
	public Boolean getPermiteLancarLote() {
		return this.permiteLancarLote;
	}

	/**
	 * Permite indicar que as movimentações derivadas deste tipo podem ou não ser lançadas em lotes.
	 * 
	 * @param permiteLancarLote true, se as movimentações derivadas deste tipo puderem ser lançadas em lote
	 */
	public void setPermiteLancarLote(Boolean permiteLancarLote) {
		this.permiteLancarLote = permiteLancarLote;
	}

	/**
	 * Recupera marca indicativa de que movimentações derivadas deste tipo devem ou não, por padrão, ser
	 * visíveis externamente.
	 *  
	 * @return true, se a movimentação derivada deve ser visível externamente por padrão.
	 */
	@Column(name = "in_visibilidade_externa")
	public Boolean getVisibilidadeExterna() {
		return visibilidadeExterna;
	}

	/**
	 * Permite indicar que movimentações derivadas deste tipo devem ou não, por padrão, ser
	 * visíveis externamente.
	 * 
	 * @param visibilidadeExterna true, se a movimentação derivada deve ser visível externamente por padrão.
	 */
	public void setVisibilidadeExterna(Boolean visibilidadeExterna) {
		this.visibilidadeExterna = visibilidadeExterna;
	}

	/**
	 * Recupera a lista de situações em que este tipo de movimentação é aplicável.
	 * 
	 * @return a lista de situações aplicáveis
	 * @see AplicacaoClasseEvento
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "evento")
	public List<AplicacaoClasseEvento> getAplicacaoClasseEventoList() {
		return aplicacaoClasseEventoList;
	}

	/**
	 * Atribui a este tipo de movimentação uma lista de situações em que ele é aplicável.
	 * Não deve ser utilizado diretamente em razão da implementação JPA do Hibernate 3.3.
	 *  
	 * @param aplicacaoClasseEventoList a lista de situações
	 */
	public void setAplicacaoClasseEventoList(List<AplicacaoClasseEvento> aplicacaoClasseEventoList) {
		this.aplicacaoClasseEventoList = aplicacaoClasseEventoList;
	}

	/**
	 * Recupera a lista de vinculações entre tipos de movimentações e tipos de documentos vinculados a este tipo.
	 * Depreciado por {@link #getTipoProcessoDocumentoList()} recuperar a lista de forma mais eficiente.
	 *   
	 * @return a lista de vinculações
	 * @see #getTipoProcessoDocumentoList()
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "evento")
	@Deprecated
	public List<TipoEvento> getTipoEventoList() {
		return tipoEventoList;
	}

	/**
	 * Atribui a este tipo de movimentação uma lista de vinculações com tipos de documentos.
	 * Depreciado por {@link #setTipoProcessoDocumentoList()} estar mapeado de forma mais eficiente.
	 *   
	 * @return a lista de vinculações
	 * @see #setTipoProcessoDocumentoList()
	 */
	@Deprecated
	public void setTipoEventoList(List<TipoEvento> tipoEventoList) {
		this.tipoEventoList = tipoEventoList;
	}

	/**
	 * Recupera o texto elucidativo da utilização deste tipo de movimentação.
	 * 
	 * @return o texto elucidativo da utilização deste tipo de movimentação
	 */
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_glossario")
	public String getGlossario() {
		return glossario;
	}

	/**
	 * Atribui a este tipo de movimentação um texto elucidativo quanto a sua utilização.
	 * 
	 * @param glossario o texto elucidativo
	 */
	public void setGlossario(String glossario) {
		this.glossario = glossario;
	}

	/**
	 * Recupera a descrição completa, em texto, deste tipo de movimentação.
	 * 
	 * @return a descrição completa
	 */
	@Lob
	@Basic(fetch=FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "ds_evento_completo")
	public String getEventoCompleto() {
		return eventoCompleto;
	}

	/**
	 * Atribui a este tipo de movimentação uma descrição completa.
	 * 
	 * @param eventoCompleto a descrição completa a ser atribuída
	 */
	public void setEventoCompleto(String eventoCompleto) {
		this.eventoCompleto = eventoCompleto;
	}

	/**
	 * Recupera marca indicativa de que este tipo de movimentação somente
	 * deve ser utilizado em processos com tramitação eletrônica.
	 * 
	 * @return true, se o tipo for exclusivo para processos com tramitação eletrônica
	 */
	@Column(name = "in_eletronico")
	public Boolean getFlagEletronico() {
		return flagEletronico;
	}

	/**
	 * Permite indicar que este tipo de movimentação somente deve ser utilizado em 
	 * processo com tramitação eletrônica.
	 * 
	 * @param flagEletronico true, se o tipo for exclusivo para processos com tramitação eletrônica
	 */
	public void setFlagEletronico(Boolean flagEletronico) {
		this.flagEletronico = flagEletronico;
	}

	/**
	 * Recupera marca indicativa de que este tipo de movimentação somente
	 * deve ser utilizado em processos com tramitação em papel.
	 * 
	 * @return true, se o tipo for exclusivo para processos com tramitação em papel
	 */
	@Column(name = "in_papel")
	public Boolean getFlagPapel() {
		return flagPapel;
	}

	/**
	 * Permite indicar que este tipo de movimentação somente deve ser utilizado em 
	 * processo com tramitação em papel.
	 * 
	 * @param flagPapel true, se o tipo for exclusivo para processos com tramitação em papel
	 */
	public void setFlagPapel(Boolean flagPapel) {
		this.flagPapel = flagPapel;
	}
	
	@Column(name="nr_nivel", nullable=true)
	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	@Column(name="nr_faixa_inferior", nullable=true)
	public Integer getFaixaInferior() {
		return faixaInferior;
	}

	public void setFaixaInferior(Integer faixaInferior) {
		this.faixaInferior = faixaInferior;
	}

	@Column(name="nr_faixa_superior", nullable=true)
	public Integer getFaixaSuperior() {
		return faixaSuperior;
	}

	public void setFaixaSuperior(Integer faixaSuperior) {
		this.faixaSuperior = faixaSuperior;
	}
	
	@Column(name="in_padrao_sgt", nullable=false)
	public Boolean getPadraoSgt() {
		return padraoSgt;
	}

	public void setPadraoSgt(Boolean padraoSgt) {
		this.padraoSgt = padraoSgt;
	}

	@Column(name="ds_motivo_inativacao", nullable=true)
	public String getMotivoInativacao() {
		return motivoInativacao;
	}

	public void setMotivoInativacao(String motivoInativacao) {
		this.motivoInativacao = motivoInativacao;
	}

	/**
	 * Recupera a lista de combinações de aplicação de tipos de movimentos, tipos de complementos e complementos
	 * concretos vinculados a este tipo de movimentação.
	 * 
	 * @return a lista de combinações possíveis
	 * @see AplicacaoMovimento
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY, mappedBy = "eventoProcessual")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	public List<AplicacaoMovimento> getAplicacaoMovimentoList() {
		return aplicacaoMovimentoList;
	}

	/**
	 * Atribui a este tipo de movimentação uma lista de combinações de complementos, descrições e aplicabilidades.
	 * Não deve ser invocado diretamente em razão da implementação JPA do Hibernate 3.3.
	 * 
	 * @param aplicacaoMovimentoList a lista a ser atribuída
	 */
	public void setAplicacaoMovimentoList(List<AplicacaoMovimento> aplicacaoMovimentoList) {
		this.aplicacaoMovimentoList = aplicacaoMovimentoList;
	}
	
	/**
	 * Indica se um dado complemento exige complementos.
	 * 
	 * @return true, se houver pelo menos uma {@link AplicacaoMovimento} que exija complemento
	 */
	@Transient
    public boolean getHaComplemento() {
		boolean retorno = false;
		if (getAplicacaoMovimentoList() != null) {
			for (AplicacaoMovimento aplicacaoMovimento : getAplicacaoMovimentoList()) {
				if (aplicacaoMovimento.getAplicacaoComplementoList() != null
						&& aplicacaoMovimento.getAplicacaoComplementoList().size() > 0) {
					retorno = true;
				}
			}
		}
		return retorno;
	}
	// Fim de vindo de EventoProcessual
	
}
