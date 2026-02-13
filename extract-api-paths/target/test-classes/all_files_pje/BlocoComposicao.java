/**
 * pje-comum
 * Copyright (C) 2009-2015 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;

@Entity
@Table(name = BlocoComposicao.TABLE_NAME)
@SequenceGenerator(allocationSize = 1, name="gen_bloco_composicao", sequenceName="sq_tb_bloco_composicao")
public class BlocoComposicao implements java.io.Serializable {

	private static final long serialVersionUID = 1;

	public static final String TABLE_NAME = "tb_bloco_composicao";

	private Integer idBlocoComposicao;

	private BlocoJulgamento bloco;
	
	private Boolean presente;	

	private Boolean impedidoSuspeicao;
	private Boolean definidoPorUsuario = Boolean.FALSE;

	private PessoaMagistrado magistradoPresente;
	private OrgaoJulgador orgaoJulgador;
	private TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado;
	private OrgaoJulgadorCargo cargoAtuacao;
	private Integer ordemVotacao;


	public BlocoComposicao() {}
	
	@Id
	@GeneratedValue(generator="gen_bloco_composicao", strategy=GenerationType.SEQUENCE)	
	@Column(name="id_bloco_composicao")
	public Integer getIdBlocoComposicao() {
		return idBlocoComposicao;
	}

	public void setIdBlocoComposicao(Integer idBlocoComposicao) {
		this.idBlocoComposicao = idBlocoComposicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_bloco_julgamento", nullable= false)
	public BlocoJulgamento getBloco() {
		return bloco;
	}
	
	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}
	
	@Column(name = "in_presente", nullable=false)
	public Boolean getPresente() {
		return presente;
	}

	public void setPresente(Boolean presente) {
		this.presente = presente;
	}
	
	@Column(name = "in_impedimento_suspeicao", nullable=false)
	public Boolean getImpedidoSuspeicao() {
		return impedidoSuspeicao;
	}

	public void setImpedidoSuspeicao(Boolean impedidoSuspeicao) {
		this.impedidoSuspeicao = impedidoSuspeicao;
	}
	
	@Column(name = "in_definido_por_usuario", nullable=false)
	public Boolean getDefinidoPorUsuario() {
		return definidoPorUsuario;
	}

	public void setDefinidoPorUsuario(Boolean definidoPorUsuario) {
		this.definidoPorUsuario = definidoPorUsuario;
	}

	/**
	 * Recupera o magistrado referente a esse item de composição de julgamento.
	 * @return o magistrado em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_magistrado_presente", nullable = true)
	public PessoaMagistrado getMagistradoPresente() {
		return magistradoPresente;
	}

	/**
	 * Atribui o magistrado referente a esse item de composição de julgamento.
	 * @param magistrado o magistrado em questão.
	 */
	public void setMagistradoPresente(PessoaMagistrado magistradoPresente) {
		this.magistradoPresente = magistradoPresente;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_orgao_julgador", nullable=false)
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	/**
	 * Recupera o tipo de atuação do magistrado nessa composição. 
	 * @return o tipo de atuação em questão. 
	 */
	@Column(name = "tp_atuacao_magistrado", length = 5, nullable = true)
	@Enumerated(EnumType.STRING)
	public TipoAtuacaoMagistradoEnum getTipoAtuacaoMagistrado() {
		return tipoAtuacaoMagistrado;
	}

	/**
	 * Atribui o tipo de atuação do magistrado nessa composição.
	 * @param tipoAtuacaoMagistrado o tipo em questão.
	 */
	public void setTipoAtuacaoMagistrado(TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado) {
		this.tipoAtuacaoMagistrado = tipoAtuacaoMagistrado;
	}
	
	/**
	 * Recupera o cargo de atuação do magistrado nessa composição.
	 * @return o cargo de atuação em questão.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cargo_atuacao", nullable=false)
	public OrgaoJulgadorCargo getCargoAtuacao() {
		return cargoAtuacao;
	}

	/**
	 * Atribui o cargo de atuação do magistrado nessa composição.
	 * @param cargoAtuacao o cargo em questão.
	 */
	public void setCargoAtuacao(OrgaoJulgadorCargo cargoAtuacao) {
		this.cargoAtuacao = cargoAtuacao;
	}
	
	@Column(name = "num_ordem_votacao")
	public Integer getOrdemVotacao() {
		return ordemVotacao;
	}

	public void setOrdemVotacao(Integer ordemVotacao) {
		this.ordemVotacao = ordemVotacao;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SessaoPautaProcessoComposicao)) {
			return false;
		}
		BlocoComposicao other = (BlocoComposicao) obj;
		if (getIdBlocoComposicao() != other.getIdBlocoComposicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdBlocoComposicao();
		return result;
	}
	
	@Transient
	public String getInformacoesDetalhadasParticipante(){
		String informacoesDetalhadas = null;
		if( this.getMagistradoPresente() != null ) { 
			informacoesDetalhadas = this.getMagistradoPresente().getNome();
			if (!this.tipoAtuacaoMagistrado.equals(TipoAtuacaoMagistradoEnum.VOGAL)){
				informacoesDetalhadas += " ("+this.tipoAtuacaoMagistrado.getLabel()+")";
			}
		}
		return informacoesDetalhadas;
	}

}