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
package br.jus.pje.jt.entidades;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ForeignKey;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class ObrigacaoPagar
 * @description Classe que representa uma obrigacao de pagar. Ela eh composta
 *              por obrigacoes atomicas de cada credor com cada devedor.
 */

@Entity
@Table(name = "tb_obrigacao_pagar")
@org.hibernate.annotations.GenericGenerator(name = "gen_ogrigacao_pagar", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_ogrigacao_pagar"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class ObrigacaoPagar implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<ObrigacaoPagar,Long> {

	private static final long serialVersionUID = 1L;

	private Long id;
	private ProcessoJT processoJT;
	private GrupoEdicao grupoEdicao;
	private List<Rubrica> rubricaList = new ArrayList<Rubrica>(0);
	private List<ObrigacaoAtomica> obrigacaoAtomicaList = new ArrayList<ObrigacaoAtomica>(0);
	private Boolean ativo;
	private Boolean homologado = Boolean.FALSE;

	public ObrigacaoPagar() {
		this.ativo = true;
	}

	@Id
	@GeneratedValue(generator = "gen_ogrigacao_pagar")
	@Column(name = "id_obrigacao_pagar", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_processo_jt", nullable = false)
	@ForeignKey(name = "fk_tb_obrig_pagar_tb_proc_jt")
	public ProcessoJT getProcessoJT() {
		return processoJT;
	}

	public void setProcessoJT(ProcessoJT processoJT) {
		this.processoJT = processoJT;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "obrigacaoPagar", cascade = { CascadeType.PERSIST,
			CascadeType.REFRESH })
	public List<Rubrica> getRubricaList() {
		return rubricaList;
	}

	public void setRubricaList(List<Rubrica> rubricaList) {
		this.rubricaList = rubricaList;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "id_grupo_edicao", nullable = false)
	@ForeignKey(name = "fk_tb_obrig_pagar_tb_grupo")
	public GrupoEdicao getGrupoEdicao() {
		return grupoEdicao;
	}

	public void setGrupoEdicao(GrupoEdicao grupoEdicao) {
		this.grupoEdicao = grupoEdicao;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "obrigacaoPagar", cascade = { CascadeType.PERSIST,
			CascadeType.REFRESH })
	public List<ObrigacaoAtomica> getObrigacaoAtomicaList() {
		return obrigacaoAtomicaList;
	}

	public void setObrigacaoAtomicaList(List<ObrigacaoAtomica> obrigacaoAtomicaList) {
		this.obrigacaoAtomicaList = obrigacaoAtomicaList;
	}

	@Column(name = "in_ativo", nullable = false)
	@NotNull
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean b) {
		ativo = b;
	}

	@Column(name = "in_homologado")
	public Boolean getHomologado() {
		return homologado;
	}

	public void setHomologado(Boolean homologado) {
		this.homologado = homologado;
	}

	@Transient
	public BigDecimal getTotal() {

		BigDecimal total = new BigDecimal("0");

		for (Rubrica rubrica : this.rubricaList) {
			total = total.add(rubrica.getValor());
		}

		return total;
	}

	/**
	 * Retorna os nomes dos credores pertencentes a obrigacao de pagar. Os nomes
	 * sao separados por quebra de linha.
	 * 
	 * @author rodrigo / sergio
	 */
	@Transient
	public String getNomesCredores() {

		StringBuilder nomes = new StringBuilder();

		for (ObrigacaoAtomica obrigacaoAtomica : obrigacaoAtomicaList) {
			Credor credor = obrigacaoAtomica.getCredor();

			if (nomes.indexOf(credor.getNome()) == -1) {
				if (nomes.length() != 0) {
					nomes.append("<br/>");
				}

				nomes.append(credor.getNome());
			}
		}
		return nomes.toString();
	}

	/**
	 * Retorna os numeros do beneficio de ordem dos devedores pertencentes a
	 * obrigacao de pagar. Os numeros sao separados por quebra de linha.
	 * 
	 * @author rodrigo / sergio
	 */
	@Transient
	public String getBODevedores() {

		StringBuilder beneficiosOrdem = new StringBuilder();
		StringBuilder nomes = new StringBuilder();

		for (ObrigacaoAtomica obrigacaoAtomica : obrigacaoAtomicaList) {
			Devedor devedor = obrigacaoAtomica.getDevedor();

			if (nomes.indexOf(devedor.getNome()) == -1) {
				if (beneficiosOrdem.length() != 0) {
					beneficiosOrdem.append("<br/>");
					nomes.append("<br/>");
				}

				beneficiosOrdem.append(devedor.getBeneficioOrdem());
				nomes.append(devedor.getNome());
			}
		}
		return beneficiosOrdem.toString();
	}

	/**
	 * Retorna os nomes dos devedores pertencentes a obrigacao de pagar. Os
	 * nomes sao separados por quebra de linha.
	 * 
	 * @author rodrigo / sergio
	 */
	@Transient
	public String getNomesDevedores() {

		StringBuilder nomes = new StringBuilder();

		for (ObrigacaoAtomica obrigacaoAtomica : obrigacaoAtomicaList) {
			Devedor devedor = obrigacaoAtomica.getDevedor();

			if (nomes.indexOf(devedor.getNome()) == -1) {
				if (nomes.length() != 0) {
					nomes.append("<br/>");
				}

				nomes.append(devedor.getNome());
			}
		}
		return nomes.toString();
	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends ObrigacaoPagar> getEntityClass() {
		return ObrigacaoPagar.class;
	}

	@Override
	@javax.persistence.Transient
	public Long getEntityIdObject() {
		return getId();
	}

	@Override
	@javax.persistence.Transient
	public boolean isLoggable() {
		return true;
	}

}
