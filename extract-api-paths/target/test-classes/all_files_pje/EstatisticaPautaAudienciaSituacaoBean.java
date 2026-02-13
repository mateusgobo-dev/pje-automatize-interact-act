package br.com.infox.pje.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.enums.StatusAudienciaEnum;

public class EstatisticaPautaAudienciaSituacaoBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5728776131890710561L;
	private StatusAudienciaEnum statusAudienciaEnum;
	private List<EstatisticaPautaAudienciaBean> estatisticaPautaAudienciaBeanList;

	public EstatisticaPautaAudienciaSituacaoBean() {
		estatisticaPautaAudienciaBeanList = new ArrayList<EstatisticaPautaAudienciaBean>();
	}

	public StatusAudienciaEnum getStatusAudienciaEnum() {
		return statusAudienciaEnum;
	}

	public void setStatusAudienciaEnum(StatusAudienciaEnum statusAudienciaEnum) {
		this.statusAudienciaEnum = statusAudienciaEnum;
	}

	public List<EstatisticaPautaAudienciaBean> getEstatisticaPautaAudienciaBeanList() {
		return estatisticaPautaAudienciaBeanList;
	}

	public void setEstatisticaPautaAudienciaBeanList(
			List<EstatisticaPautaAudienciaBean> estatisticaPautaAudienciaBeanList) {
		this.estatisticaPautaAudienciaBeanList = estatisticaPautaAudienciaBeanList;
	}
}
