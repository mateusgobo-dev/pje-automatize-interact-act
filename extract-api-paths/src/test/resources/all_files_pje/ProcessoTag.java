package br.jus.pje.nucleo.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = ProcessoTag.NOME_TABELA)
@org.hibernate.annotations.GenericGenerator(name = "gen_processo_tag", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_processo_tag"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ProcessoTag implements Serializable{

	private static final long serialVersionUID = 1L;

	static final String NOME_TABELA = "tb_processo_tag";

	@Id
	@GeneratedValue(generator = "gen_processo_tag")
	@Column(name = "id_processo_tag", unique = true, nullable = false)
	private Integer idProcessoTag;

	@Column(name = "id_processo")
	private Long idProcesso;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tag")
	private TagMin tag;


	@Column(name = "id_usuario_inclusao")
	private Integer idUsuarioInclusao;

	public ProcessoTag() {
	}

	public ProcessoTag(Long idProcesso, TagMin tag,Integer idUsuario) {
		this.idProcesso = idProcesso;
		this.tag = tag;
		this.idUsuarioInclusao = idUsuario;
	}
	
	public Integer getIdProcessoTag() {
		return idProcessoTag;
	}
	public void setIdProcessoTag(Integer idProcessoTag) {
		this.idProcessoTag = idProcessoTag;
	}
	public Long getIdProcesso() {
		return idProcesso;
	}
	public void setIdProcesso(Long idProcesso) {
		this.idProcesso = idProcesso;
	}
	public TagMin getTag() {
		return tag;
	}
	public void setTag(TagMin tag) {
		this.tag = tag;
	}

	public Integer getIdUsuarioInclusao() {
		return idUsuarioInclusao;
	}

	public void setIdUsuarioInclusao(Integer idUsuarioInclusao) {
		this.idUsuarioInclusao = idUsuarioInclusao;
	}
}
