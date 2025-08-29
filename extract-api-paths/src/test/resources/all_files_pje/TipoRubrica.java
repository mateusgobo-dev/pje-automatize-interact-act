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

import java.util.ArrayList;
import java.util.List;

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
import javax.validation.constraints.Size;

import org.hibernate.annotations.ForeignKey;

import br.jus.pje.jt.enums.CredorExplicitoEnum;
import br.jus.pje.jt.enums.OrdemCreditoEnum;
import br.jus.pje.jt.enums.TipoCredorEnum;

/**
 * @author Rodrigo Cartaxo / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class TipoRubrica
 * @description Classe que representa um tipo de rubrica.
 */

@Entity
@Table(name = "tb_tipo_rubrica")
@org.hibernate.annotations.GenericGenerator(name = "gen_tipo_rubrica", strategy = "br.jus.pje.nucleo.entidades.util.SequencePooledGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence", value = "sq_tb_tipo_rubrica"),
		@org.hibernate.annotations.Parameter(name = "allocationSize", value = "-1")})
public class TipoRubrica implements java.io.Serializable, br.jus.pje.nucleo.entidades.IEntidade<TipoRubrica,Long> {

	private static final long serialVersionUID = 1L;

	public static final String COD_VALOR_PRINCIPAL = "1";
	public static final String COD_FGTS_CTA_VINCULADA = "2";
	public static final String COD_JUROS = "3";
	public static final String COD_EDITAIS = "5";
	public static final String COD_INSS_RECLAMANTE = "6";
	public static final String COD_INSS_RECLAMADO = "7";
	public static final String COD_CUSTAS = "8";
	public static final String COD_EMOLUMENTOS = "9";
	public static final String COD_IMPOSTO_RENDA = "10";
	public static final String COD_OUTROS = "19";

	private Long id;
	private String codigo;
	private String descricao;
	private CategoriaRubrica categoriaRubrica;
	private List<CredorRubrica> credorRubricaList = new ArrayList<CredorRubrica>(0);
	private List<EspecialidadeRubrica> especialidadeRubricaList = new ArrayList<EspecialidadeRubrica>(0);

	@Id
	@GeneratedValue(generator = "gen_tipo_rubrica")
	@Column(name = "id_tipo_rubrica", unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "cd_codigo")
	@Size(max = 3, min = 1)
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Column(name = "ds_descricao")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_categoria_rubrica", nullable = false)
	@ForeignKey(name = "fk_tb_tipo_rubr_tb_cat_rubr")
	public CategoriaRubrica getCategoriaRubrica() {
		return categoriaRubrica;
	}

	public void setCategoriaRubrica(CategoriaRubrica categoriaRubrica) {
		this.categoriaRubrica = categoriaRubrica;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "tipoRubrica")
	public List<CredorRubrica> getCredorRubricaList() {
		return credorRubricaList;
	}

	public void setCredorRubricaList(List<CredorRubrica> credorRubricaList) {
		this.credorRubricaList = credorRubricaList;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tipoRubrica")
	public List<EspecialidadeRubrica> getEspecialidadeRubricaList() {
		return especialidadeRubricaList;
	}

	public void setEspecialidadeRubricaList(List<EspecialidadeRubrica> especialidadeRubricaList) {
		this.especialidadeRubricaList = especialidadeRubricaList;
	}

	/**
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @category PJE-JT
	 * @since 1.4.2
	 * @created 29/09/2011
	 * 
	 * @return the exigeCredor
	 */
	@Transient
	public boolean getExigeCredor() {
		return (this.getExigibilidadeCredor() == CredorExplicitoEnum.O);
	}

	@Transient
	public CredorExplicitoEnum getExigibilidadeCredor() {
		Boolean possuiCredorUniao = false;
		Boolean possuiCredorExplicito = false;
		for (CredorRubrica credorRubrica : this.credorRubricaList) {
			if (credorRubrica.getTipoCredor() == TipoCredorEnum.U /* União */) {
				possuiCredorUniao = true;
				if (credorRubrica.getOrdemCredito() == OrdemCreditoEnum.S) {
					return CredorExplicitoEnum.F;
				}
			}
			if (credorRubrica.getTipoCredor() != TipoCredorEnum.U /* não é União */) {
				possuiCredorExplicito = true;
			}
		}
		if (possuiCredorUniao && !possuiCredorExplicito)
			return CredorExplicitoEnum.P;

		if (!possuiCredorUniao && possuiCredorExplicito)
			return CredorExplicitoEnum.O;

		if (possuiCredorUniao && possuiCredorExplicito)
			return CredorExplicitoEnum.F;

		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TipoRubrica))
			return false;
		TipoRubrica other = (TipoRubrica) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	/**
	 * @return Os tipo de credores formatados.
	 */
	@Transient
	public String getTipoCredorFormatado() {
		StringBuilder tipoCredores = new StringBuilder();
		int qtd = credorRubricaList.size();

		for (int i = 0; i < qtd; i++) {
			CredorRubrica cr = credorRubricaList.get(i);
			String separador = (i == qtd - 2 ? " e/ou " : ", ");
			if (i == qtd - 1)
				separador = "";

			if (!cr.getTipoCredor().equals(TipoCredorEnum.U)) {
				tipoCredores.append(cr.getTipoCredor().getLabel());
				tipoCredores.append(separador);
			}
		}

		return tipoCredores.toString();

	}
	
	@Override
	@javax.persistence.Transient
	public Class<? extends TipoRubrica> getEntityClass() {
		return TipoRubrica.class;
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
