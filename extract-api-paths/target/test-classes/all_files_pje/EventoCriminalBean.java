package br.jus.pje.nucleo.beans.criminal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class EventoCriminalBean extends BaseBean {

	private static final long serialVersionUID = 1L;

	protected Date data;
	protected TipoEventoCriminal tipoInformacaoCriminal;
	protected List<IncidenciaPenalBean> tipificacoes = new ArrayList<IncidenciaPenalBean>(0);
	protected String observacao;
	protected String textoFinal;
	protected Boolean ativo;

	public EventoCriminalBean(String id) {
		super(id);
	}
	
	public EventoCriminalBean() {
		super();
	}

	public EventoCriminalBean(List<IncidenciaPenalBean> incidenciasPenais) {
		super();
		this.tipificacoes = incidenciasPenais;
	}

	public EventoCriminalBean(Date data, TipoEventoCriminal tipoInformacaoCriminal,
			List<IncidenciaPenalBean> tipificacoes, Boolean ativo) {
		this.data = data;
		this.tipoInformacaoCriminal = tipoInformacaoCriminal;
		this.tipificacoes = tipificacoes;
		this.ativo = ativo;
	}

	public List<IncidenciaPenalBean> getTipificacoes() {
		return tipificacoes;
	}

	public void setTipificacoes(List<IncidenciaPenalBean> tipificacoes) {
		this.tipificacoes = tipificacoes;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public String getTextoFinal() {
		return textoFinal;
	}

	public void setTextoFinal(String textoFinal) {
		this.textoFinal = textoFinal;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
	
	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	public TipoEventoCriminal getTipoInformacaoCriminal() {
		return tipoInformacaoCriminal;
	}

	public void setTipoInformacaoCriminal(TipoEventoCriminal tipoInformacaoCriminal) {
		this.tipoInformacaoCriminal = tipoInformacaoCriminal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ativo == null) ? 0 : ativo.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((tipificacoes == null) ? 0 : tipificacoes.hashCode());
		result = prime * result + ((tipoInformacaoCriminal == null) ? 0 : tipoInformacaoCriminal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventoCriminalBean other = (EventoCriminalBean) obj;
		if (ativo == null) {
			if (other.ativo != null)
				return false;
		} else if (!ativo.equals(other.ativo))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (tipificacoes == null) {
			if (other.tipificacoes != null)
				return false;
		} else if (!tipificacoes.equals(other.tipificacoes))
			return false;
		if (tipoInformacaoCriminal == null) {
			if (other.tipoInformacaoCriminal != null)
				return false;
		} else if (!tipoInformacaoCriminal.equals(other.tipoInformacaoCriminal))
			return false;
		return true;
	}

}
