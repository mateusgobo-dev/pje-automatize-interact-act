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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import br.jus.pje.nucleo.enums.TipoCriterioEnum;

@Entity
@Table(name = "tb_criterio_filtro")
@SequenceGenerator(allocationSize = 1, name = "gen_criterio_filtro", sequenceName = "sq_tb_criterio_filtro")
public class CriterioFiltro implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "gen_criterio_filtro")
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "in_tipo_criterio", length = 100)
	@Enumerated(EnumType.STRING)
	private TipoCriterioEnum tipoCriterio;
	
	@Column(name = "ds_valor_criterio", length = 100)
	private String valorCriterio;
	
	@Column(name = "ds_texto_criterio", length = 255)
	private String textoCriterio;

	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="id_filtro")
    private Filtro filtro;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TipoCriterioEnum getTipoCriterio() {
		return tipoCriterio;
	}

	public void setTipoCriterio(TipoCriterioEnum tipoCriterio) {
		this.tipoCriterio = tipoCriterio;
	}

	public String getValorCriterio() {
		return valorCriterio;
	}

	public void setValorCriterio(String valorCriterio) {
		this.valorCriterio = valorCriterio;
	}
	
	public String getTextoCriterio() {
		return textoCriterio;
	}
	
	public void setTextoCriterio(String textoCriterio) {
		this.textoCriterio = textoCriterio;
	}

	public Filtro getFiltro() {
		return filtro;
	}
	
	public void setFiltro(Filtro filtro) {
		this.filtro = filtro;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filtro == null) ? 0 : filtro.hashCode());
		result = prime * result + ((tipoCriterio == null) ? 0 : tipoCriterio.hashCode());
		result = prime * result + ((valorCriterio == null) ? 0 : valorCriterio.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CriterioFiltro other = (CriterioFiltro) obj;
		if (filtro == null) {
			if (other.filtro != null)
				return false;
		} else if (!filtro.equals(other.filtro))
			return false;
		if (tipoCriterio != other.tipoCriterio)
			return false;
		if (valorCriterio == null) {
			if (other.valorCriterio != null)
				return false;
		} else if (!valorCriterio.equals(other.valorCriterio))
			return false;
		return true;
	}
}
