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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

/**
 * @author cristof
 * 
 */
@Entity
@Table(name="tb_autoridade_afetada")
@org.hibernate.annotations.GenericGenerator(name = "gen_autoridade_afetada", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_autoridade_afetada"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class AutoridadeAfetada implements Serializable, br.jus.pje.nucleo.entidades.IEntidade<AutoridadeAfetada,Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2904446952070687489L;

	private Integer idAutoridadeAfetada;
	
	private DimensaoFuncional dimensaoFuncional;

	private PessoaAutoridade autoridade;

	private ProcessoParteParticipacaoEnum polo;

	private AssociacaoDimensaoPessoalEnum tipoRestricao;

	@Id
	@GeneratedValue(generator = "gen_autoridade_afetada")
	@Column(name = "id_autoridade_afetada", unique = true, nullable = false)
	public Integer getIdAutoridadeAfetada() {
		return idAutoridadeAfetada;
	}

	public void setIdAutoridadeAfetada(Integer idAutoridadeAfetada) {
		this.idAutoridadeAfetada = idAutoridadeAfetada;
	}
	
	@ManyToOne
	@JoinColumn(name = "id_dimensao_funcional")
	public DimensaoFuncional getDimensaoFuncional() {
		return dimensaoFuncional;
	}

	public void setDimensaoFuncional(DimensaoFuncional dimensaoFuncional) {
		this.dimensaoFuncional = dimensaoFuncional;
	}

	@ManyToOne
	@JoinColumn(name = "id_pessoa_autoridade")
	public PessoaAutoridade getAutoridade() {
		return autoridade;
	}

	public void setAutoridade(PessoaAutoridade autoridade) {
		this.autoridade = autoridade;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "in_participacao")
	public ProcessoParteParticipacaoEnum getPolo() {
		return polo;
	}

	public void setPolo(ProcessoParteParticipacaoEnum polo) {
		this.polo = polo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "tp_associacao")
	public AssociacaoDimensaoPessoalEnum getTipoRestricao() {
		return tipoRestricao;
	}

	public void setTipoRestricao(AssociacaoDimensaoPessoalEnum tipoRestricao) {
		this.tipoRestricao = tipoRestricao;
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends AutoridadeAfetada> getEntityClass() {
		return AutoridadeAfetada.class;
	}

	@Override
	@javax.persistence.Transient
	public Integer getEntityIdObject() {
		return getIdAutoridadeAfetada();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
