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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import br.jus.pje.nucleo.enums.TipoAtuacaoMagistradoEnum;

@Entity
@javax.persistence.Cacheable(true)
@Cache(region = "FechamentoPautaSessaoJulgamento", usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = SessaoPautaProcessoComposicao.TABLE_NAME)
@org.hibernate.annotations.GenericGenerator(name = "gen_sessao_pauta_proc_comp", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_sessao_pauta_proc_comp"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class SessaoPautaProcessoComposicao implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<SessaoPautaProcessoComposicao,Integer> {

	private static final long serialVersionUID = 1;

	public static final String TABLE_NAME = "tb_sessao_pauta_proc_comp";

	private Integer idSessaoPautaProcessoComposicao;

	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	
	private Boolean presente;	

	private Boolean impedidoSuspeicao;
	private Boolean definidoPorUsuario = Boolean.FALSE;

	private PessoaMagistrado magistradoPresente;
	private OrgaoJulgador orgaoJulgador;
	private TipoAtuacaoMagistradoEnum tipoAtuacaoMagistrado;
	private OrgaoJulgadorCargo cargoAtuacao;
	private Integer ordemVotacao;


	public SessaoPautaProcessoComposicao() {}
	
	@Id
	@GeneratedValue(generator="gen_sessao_pauta_proc_comp", strategy=GenerationType.SEQUENCE)	
	@Column(name="id_sessao_pauta_proc_comp")
	public Integer getIdSessaoPautaProcessoComposicao() {
		return idSessaoPautaProcessoComposicao;
	}

	public void setIdSessaoPautaProcessoComposicao(Integer idSessaoPautaProcessoComposicao) {
		this.idSessaoPautaProcessoComposicao = idSessaoPautaProcessoComposicao;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_sessao_pauta_proc_trf", nullable= false)
	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}
	
	public void setSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		this.sessaoPautaProcessoTrf = sessaoPautaProcessoTrf;
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
	
	@Column(name = "num_sppcomp_ordem_votacao")
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
		SessaoPautaProcessoComposicao other = (SessaoPautaProcessoComposicao) obj;
		if (getIdSessaoPautaProcessoComposicao() != other.getIdSessaoPautaProcessoComposicao()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdSessaoPautaProcessoComposicao();
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
	
	@Override
	@javax.persistence.Transient
	public Class<? extends SessaoPautaProcessoComposicao> getEntityClass() {
		return SessaoPautaProcessoComposicao.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdSessaoPautaProcessoComposicao();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
