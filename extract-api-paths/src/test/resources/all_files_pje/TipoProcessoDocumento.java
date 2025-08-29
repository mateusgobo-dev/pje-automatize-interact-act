/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence a  União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.anotacoes.IndexedEntity;
import br.jus.pje.nucleo.anotacoes.Mapping;
import br.jus.pje.nucleo.enums.TipoComunicacaoEnum;
import br.jus.pje.nucleo.enums.TipoDocumentoEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteEnum;
import br.jus.pje.nucleo.enums.VisibilidadeEnum;

@Entity
@javax.persistence.Cacheable(true)
@Table(name = "tb_tipo_processo_documento")
@Inheritance(strategy = InheritanceType.JOINED)
@IndexedEntity(id="idTipoProcessoDocumento", value="tipodocumento",
	mappings={
		@Mapping(beanPath="tipoProcessoDocumento", mappedPath="tipo"),
		@Mapping(beanPath="codigoDocumento", mappedPath="codigo")
})
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_processo_documento", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_processo_documento"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoProcessoDocumento implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoProcessoDocumento,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer idTipoProcessoDocumento;
	private String tipoProcessoDocumento;
	private String codigoDocumento;
	private Boolean ativo;
	private Boolean publico;
	private TipoDocumentoEnum inTipoDocumento;
	private Boolean sistema;
	private Boolean anexar;
	private Boolean numera = Boolean.FALSE;
	private String tipoProcessoDocumentoObservacao;
	private String mascara;
	private VisibilidadeEnum visibilidade;
	private Agrupamento agrupamento;
	
	private Fluxo fluxo;
	private String variavelFluxo;
	
	private Boolean possuiCustas;

	/**
	 * @deprecated esse campo Não é mais utilizado no cadastro dos tipos de documento
	 */
	@Deprecated
	private TipoComunicacaoEnum inTipoComunicacao;
	/**
	 * @deprecated esse campo não é mais utilizado no cadastro dos tipos de documento
	 */
	@Deprecated
	private TipoExpedienteEnum inTipoExpediente;
	private Boolean notificaAdvogado;
	private Boolean notificaParte;
	private Integer codigoMateria;

	private Boolean documentoAtoProferido = Boolean.TRUE;

	private List<TipoProcessoDocumentoPapel> papeis = new ArrayList<>(0);
	private boolean pesquisavel;
	private Boolean exibeJuntadaDocumento;

	public TipoProcessoDocumento() {
	}
	
	public TipoProcessoDocumento(Integer idTipoProcessoDocumento, String tipoProcessoDocumento) {
		super();
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_processo_documento")
	@Column(name = "id_tipo_processo_documento", unique = true, nullable = false)
	public Integer getIdTipoProcessoDocumento() {
		return this.idTipoProcessoDocumento;
	}

	public void setIdTipoProcessoDocumento(Integer idTipoProcessoDocumento) {
		this.idTipoProcessoDocumento = idTipoProcessoDocumento;
	}

	@Column(name = "ds_tipo_processo_documento", nullable = false, length = 100)
	@NotNull
	@Length(max = 100)
	public String getTipoProcessoDocumento() {
		return this.tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(String tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	@Transient
	public String getTipoProcessoDocumentoComId(){
		return getTipoProcessoDocumento()+" ("+getIdTipoProcessoDocumento()+")";
	}

	@Column(name = "cd_documento", length = 30, nullable=false)
	@Length(max = 30)
	@NotNull
	public String getCodigoDocumento() {
		return this.codigoDocumento;
	}

	public void setCodigoDocumento(String codigoDocumento) {
		this.codigoDocumento = codigoDocumento;
	}

	@Column(name = "in_publico", nullable = false)
	@NotNull
	public Boolean getPublico() {
		return this.publico;
	}

	public void setPublico(Boolean publico) {
		this.publico = publico;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "in_tipo_documento", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoDocumentoEnum getInTipoDocumento() {
		return this.inTipoDocumento;
	}

	public void setInTipoDocumento(TipoDocumentoEnum inTipoDocumento) {
		this.inTipoDocumento = inTipoDocumento;
	}

	@Column(name = "tp_visibilidade", length = 1)
	@Enumerated(EnumType.STRING)
	public VisibilidadeEnum getVisibilidade() {
		return this.visibilidade;
	}

	public void setVisibilidade(VisibilidadeEnum visibilidade) {
		this.visibilidade = visibilidade;
	}

	@Column(name = "in_anexar")
	public Boolean getAnexar() {
		return this.anexar;
	}

	public void setAnexar(Boolean anexar) {
		this.anexar = anexar;
	}

	@Column(name = "in_numera")
	public Boolean getNumera() {
		return this.numera;
	}

	public void setNumera(Boolean numera) {
		this.numera = numera;
	}

	@Column(name = "ds_tp_proc_dcumento_observacao", length = 200)
	@Length(max = 200)
	public String getTipoProcessoDocumentoObservacao() {
		return this.tipoProcessoDocumentoObservacao;
	}

	public void setTipoProcessoDocumentoObservacao(String tipoProcessoDocumentoObservacao) {
		this.tipoProcessoDocumentoObservacao = tipoProcessoDocumentoObservacao;
	}

	@Column(name = "ds_mascara", length = 100)
	@Length(max = 100)
	public String getMascara() {
		return this.mascara;
	}

	public void setMascara(String mascara) {
		this.mascara = mascara;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_agrupamento")
	public Agrupamento getAgrupamento() {
		return agrupamento;
	}

	public void setAgrupamento(Agrupamento agrupamento) {
		this.agrupamento = agrupamento;
	}

	@Column(name = "in_sistema")
	public Boolean getSistema() {
		return sistema;
	}

	public void setSistema(Boolean sistema) {
		this.sistema = sistema;
	}

	/**
	 * @deprecated esse campo não é mais utilizado no cadastro dos tipos de documento
	 */
	@Deprecated 
	@Column(name = "in_tipo_comunicacao", length = 1)
	@Enumerated(EnumType.STRING)
	public TipoComunicacaoEnum getInTipoComunicacao() {
		return this.inTipoComunicacao;
	}

	/**
	 * @deprecated esse campo não é mais utilizado no cadastro dos tipos de documento
	 */
	@Deprecated 
	public void setInTipoComunicacao(TipoComunicacaoEnum inTipoComunicacao) {
		this.inTipoComunicacao = inTipoComunicacao;
	}

	/**
	 * @deprecated esse campo não é mais utilizado no cadastro dos tipos de documento
	 */
	@Column(name = "in_tipo_expediente", length = 1)
	@Enumerated(EnumType.STRING)
	@Deprecated 
	public TipoExpedienteEnum getInTipoExpediente() {
		return this.inTipoExpediente;
	}

	/**
	 * @param inTipoExpediente
	 * @deprecated esse campo não é mais utilizado no cadastro dos tipos de documento
	 */
	@Deprecated 
	public void setInTipoExpediente(TipoExpedienteEnum inTipoExpediente) {
		this.inTipoExpediente = inTipoExpediente;
	}

	/**
	 * Obtém a lista de {@link TipoProcessoDocumentoPapel} que pode atuar com
	 * este documento.
	 * 
	 * @return os relacionamentos desse documento com os papeis que podem
	 *         utilizar
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tipoProcessoDocumento")
	public List<TipoProcessoDocumentoPapel> getPapeis() {
		return papeis;
	}

	/**
	 * Atribui uma lista de papeis a este {@link TipoProcessoDocumento}. Não
	 * deve ser utilizada em razão da implementação JPA do Hibernate, devendo
	 * ser feita uma chamada ao método {@link TipoProcessoDocumento#getPapeis()}
	 * e operar a lista a partir dessa chamada.
	 * 
	 * @param papeis
	 *            os relacionamentos a serem atribuнdos
	 */
	public void setPapeis(List<TipoProcessoDocumentoPapel> papeis) {
		this.papeis = papeis;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAtivo() == null) ? 0 : getAtivo().hashCode());
		result = prime * result + getIdTipoProcessoDocumento();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(TipoProcessoDocumento.class.isAssignableFrom(obj.getClass())))
			return false;
		TipoProcessoDocumento other = (TipoProcessoDocumento) obj;
		if (getAtivo() == null) {
			if (other.getAtivo() != null)
				return false;
		} else if (!getAtivo().equals(other.getAtivo()))
			return false;

        if(getIdTipoProcessoDocumento() == null && other.getIdTipoProcessoDocumento() != null){
                return false;
        }else if(other.getIdTipoProcessoDocumento() == null && getIdTipoProcessoDocumento() != null){
                return false;
        }else if (getIdTipoProcessoDocumento().intValue() != other.getIdTipoProcessoDocumento().intValue()){
			return false;
        }

		return true;
	}

	@Override
	public String toString() {
		return tipoProcessoDocumento;
	}

	@Column(name = "nr_codigo_materia")
	public Integer getCodigoMateria() {
		return codigoMateria;
	}

	public void setCodigoMateria(Integer codigoMateria) {
		this.codigoMateria = codigoMateria;
	}

	@Column(name = "in_notifica_advogado")
	public Boolean getNotificaAdvogado() {
		return notificaAdvogado;
	}

	public void setNotificaAdvogado(Boolean notificaAdvogado) {
		this.notificaAdvogado = notificaAdvogado;
	}

	@Column(name = "in_notifica_parte")
	public Boolean getNotificaParte() {
		return notificaParte;
	}

	public void setNotificaParte(Boolean notificaParte) {
		this.notificaParte = notificaParte;
	}
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@JoinColumn(name="id_fluxo")
	public Fluxo getFluxo() {
		return fluxo;
	}
	
	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}
	
	/**
	 * Recupera o nome da variável de fluxo a ser definida quando iniciado fluxo associado 
	 * a petição com este tipo de documento.
	 * A variável será definida com o valor verdadeiro e o efeito disso é que, definida a variável, o sistema
	 * deixará de disparar novos fluxos quando já houver fluxo com esta variável definida.
	 * 
	 * @return o nome da variável.
	 */
	@Basic(optional=true)
	@Column(name = "ds_variavel_fluxo", length = 100)
	@Length(max = 100)
	public String getVariavelFluxo() {
		return variavelFluxo;
	}

	/**
	 * Atribui a este tipo de documento o nome de variável de fluxo a ser definida nos fluxos
	 * deflagrados em razão de petição deste tipo. 
	 * A variável será definida com o valor verdadeiro e o efeito disso é que, definida a variável, o sistema
	 * deixará de disparar novos fluxos quando já houver fluxo com esta variável definida.
	 * 
	 * @param variavelFluxo o nome da variável do fluxo
	 */
	public void setVariavelFluxo(String variavelFluxo) {
		this.variavelFluxo = variavelFluxo;
	}


	@Column(name = "in_ato_proferido")
	@NotNull
	public Boolean getDocumentoAtoProferido() {
		return documentoAtoProferido;
	}

	public void setDocumentoAtoProferido(Boolean documentoAtoProferido) {
		this.documentoAtoProferido = documentoAtoProferido;
	}
	
	/**
	 * @return the pesquisavel
	 */
	@Column(name = "in_pesquisavel")
	@NotNull
	public boolean isPesquisavel() {
		return pesquisavel;
	}

	/**
	 * @param pesquisavel the pesquisavel to set
	 */
	public void setPesquisavel(boolean pesquisavel) {
		this.pesquisavel = pesquisavel;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoProcessoDocumento> getEntityClass() {
		return TipoProcessoDocumento.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdTipoProcessoDocumento());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

	@Column(name = "in_possui_custas")
	@NotNull
	public Boolean getPossuiCustas() {
		return possuiCustas;
	}

	public void setPossuiCustas(Boolean possuiCustas) {
		this.possuiCustas = possuiCustas;
	}

	@Column(name = "in_exibe_juntada_documento")
	@NotNull
	public Boolean getExibeJuntadaDocumento() {
		return exibeJuntadaDocumento;
	}

	public void setExibeJuntadaDocumento(Boolean exibeJuntadaDocumento) {
		this.exibeJuntadaDocumento = exibeJuntadaDocumento;
	}
		

	
}
