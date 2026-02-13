package br.jus.pje.nucleo.entidades;

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

import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;

@Entity
@Table(name = "tb_tipo_proc_doc_meio_comunicacao")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_doc_meio_comunicacao", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_doc_meio_comunicacao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoProcessoDocumentoMeioComunicacao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoProcessoDocumentoMeioComunicacao,Integer> {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private ExpedicaoExpedienteEnum meioComunicacao;
	

	public TipoProcessoDocumentoMeioComunicacao() {
	}

	public TipoProcessoDocumentoMeioComunicacao(Integer id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(generator = "gen_tipo_doc_meio_comunicacao")
	@Column(name = "id_tb_tipo_doc_meio_comunicacao", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@NotNull
	@Column(name = "in_meio_expedicao_expediente", length = 1)
	@Enumerated(EnumType.STRING)
	public ExpedicaoExpedienteEnum getMeioComunicacao() {
		return meioComunicacao;
	}

	public void setMeioComunicacao(ExpedicaoExpedienteEnum meioComunicacao) {
		this.meioComunicacao = meioComunicacao;
	}

	@NotNull
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@JoinColumn(name = "id_tipo_processo_documento")
	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoProcessoDocumentoMeioComunicacao> getEntityClass() {
		return TipoProcessoDocumentoMeioComunicacao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
