package br.jus.pje.nucleo.entidades;


import java.io.Serializable;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Length;

import br.jus.pje.nucleo.enums.SituacaoPublicacaoLiberacaoEnum;
import br.jus.pje.nucleo.enums.TipoDecisaoPublicacaoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoPublicacaoEnum;

@Entity
@Table(name = LiberacaoPublicacaoDecisao.TABLE_NAME)
public class LiberacaoPublicacaoDecisao implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<LiberacaoPublicacaoDecisao,Integer>{
	
	private static final long serialVersionUID = 4286274848334655479L;

	public static final String TABLE_NAME = "tb_liberacao_publicacao_decisao";

	private int idLiberacaoPublicacaoDecisao;
	private TipoPublicacaoEnum tipoPublicacao;
	private TipoDecisaoPublicacaoEnum tipoDecisaoPublicacao;
	private SituacaoPublicacaoLiberacaoEnum situacaoPublicacaoLiberacao;
	private Date dataSessao;
	private Date dataPrazoLegal;
	private Date dataPublicacao;
	private Date dataCriacao;
	private Date dataDocPublicado;
	private String numeroProcesso;
	private ProcessoDocumento processoDocumento;
	private Sessao sessao;
	private TipoPrazoEnum tipoPrazo;
	private Integer prazoLegal;
	private boolean selecionado;
	
	public LiberacaoPublicacaoDecisao() {
 	}
 	
	public LiberacaoPublicacaoDecisao(int idLiberacaoPublicacaoDecisao, String numeroProcesso) {
		this.idLiberacaoPublicacaoDecisao = idLiberacaoPublicacaoDecisao;
		this.numeroProcesso = numeroProcesso;
	}
	
 	public LiberacaoPublicacaoDecisao(Date dataSessao, Date dataPublicao, String numeroProcesso, ProcessoDocumento processoDocumento, Sessao sessao) {
 		this.dataSessao = dataSessao;
 		this.dataPublicacao = dataPublicao;
 		this.numeroProcesso = numeroProcesso;
 		this.processoDocumento = processoDocumento;
 		this.sessao = sessao;
 	}
	
	@org.hibernate.annotations.GenericGenerator(name = "generator", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_liberacao_publicacao_decisao"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id_liberacao_publicacao_decisao", unique = true, nullable = false, updatable = false)
	public int getIdLiberacaoPublicacaoDecisao() {
		return idLiberacaoPublicacaoDecisao;
	}

	public void setIdLiberacaoPublicacaoDecisao(int idLiberacaoPublicacaoDecisao) {
		this.idLiberacaoPublicacaoDecisao = idLiberacaoPublicacaoDecisao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_sessao")
	public Date getDataSessao() {
		return dataSessao;
	}

	public void setDataSessao(Date dataSessao) {
		this.dataSessao = dataSessao;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_criacao", nullable = false)
	public Date getDataCriacao() {
		return dataCriacao;
	}
	
	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_documento_publicado")
	public Date getDataDocPublicado() {
		return dataDocPublicado;
	}

	public void setDataDocPublicado(Date dataDocPublicado) {
		this.dataDocPublicado = dataDocPublicado;
	}

	@Column(name = "nr_processo", length = 30)
	@Length(max = 30)
	public String getNumeroProcesso() {
		return numeroProcesso;
	}


	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
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
	@JoinColumn(name = "id_sessao", nullable = true)
	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}
	
	@Column(name = "tipo_prazo", nullable = true)
	@Enumerated(EnumType.STRING)
	public TipoPrazoEnum getTipoPrazo(){
		return tipoPrazo;
	}

	public void setTipoPrazo(TipoPrazoEnum tipoPrazo){
		this.tipoPrazo = tipoPrazo;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_prazo_legal", nullable = true)
	public Date getDataPrazoLegal() {
		return dataPrazoLegal;
	}

	public void setDataPrazoLegal(Date dataPrazoLegal) {
		this.dataPrazoLegal = dataPrazoLegal;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dt_publicacao", nullable = true)
	public Date getDataPublicacao() {
		return dataPublicacao;
	}

	public void setDataPublicacao(Date dataPublicacao) {
		this.dataPublicacao = dataPublicacao;
	}
	
	@Column(name = "tipo_publicacao", nullable = false)
	@Enumerated(EnumType.ORDINAL)
	public TipoPublicacaoEnum getTipoPublicacao() {
		return tipoPublicacao;
	}

	public void setTipoPublicacao(TipoPublicacaoEnum tipoPublicacao) {
		this.tipoPublicacao = tipoPublicacao;
	}
	
	@Column(name = "tipo_decisao_publicacao", nullable = true)
	@Enumerated(EnumType.ORDINAL)
	public TipoDecisaoPublicacaoEnum getTipoDecisaoPublicacao() {
		return tipoDecisaoPublicacao;
	}

	public void setTipoDecisaoPublicacao(TipoDecisaoPublicacaoEnum tipoDecisaoPublicacao) {
		this.tipoDecisaoPublicacao = tipoDecisaoPublicacao;
	}
	
	@Column(name = "situacao_publicacao_liberacao", nullable = false)
	@Enumerated(EnumType.ORDINAL)
	public SituacaoPublicacaoLiberacaoEnum getSituacaoPublicacaoLiberacao() {
		return situacaoPublicacaoLiberacao;
	}

	public void setSituacaoPublicacaoLiberacao(SituacaoPublicacaoLiberacaoEnum situacaoPublicacaoLiberacao) {
		this.situacaoPublicacaoLiberacao = situacaoPublicacaoLiberacao;
	}

	@Column(name = "prazo_legal", nullable = true)
	public Integer getPrazoLegal() {
		return prazoLegal;
	}

	public void setPrazoLegal(Integer prazoLegal) {
		this.prazoLegal = prazoLegal;
	}

	@Transient
	public boolean isSelecionado() {
		return selecionado;
	}

	public void setSelecionado(boolean selecionado) {
		this.selecionado = selecionado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idLiberacaoPublicacaoDecisao;
		result = prime * result + ((numeroProcesso == null) ? 0 : numeroProcesso.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		LiberacaoPublicacaoDecisao other = (LiberacaoPublicacaoDecisao) obj;
		if (idLiberacaoPublicacaoDecisao != other.idLiberacaoPublicacaoDecisao)
			return false;
		if (numeroProcesso == null) {
			if (other.numeroProcesso != null)
				return false;
		} else if (!numeroProcesso.equals(other.numeroProcesso))
			return false;
		return true;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends LiberacaoPublicacaoDecisao> getEntityClass() {
		return LiberacaoPublicacaoDecisao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return Integer.valueOf(getIdLiberacaoPublicacaoDecisao());
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
