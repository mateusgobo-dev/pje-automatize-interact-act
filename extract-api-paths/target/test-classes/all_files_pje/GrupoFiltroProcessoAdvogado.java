package br.com.infox.cliente.bean;

import java.util.ArrayList;
import java.util.List;

import br.com.infox.cliente.component.NumeroProcesso;

public class GrupoFiltroProcessoAdvogado {

	private List<NumeroProcesso> numeroProcessoList = new ArrayList<NumeroProcesso>(0);
	private List<ConsultaOrgaoJulgadorCaixa> orgaoJulgadorCaixaList = new ArrayList<ConsultaOrgaoJulgadorCaixa>(0);
	private Boolean andOperator;

	public void addFiltroOrgaoJulgadorCaixa() {
		orgaoJulgadorCaixaList.add(new ConsultaOrgaoJulgadorCaixa());
	}

	public void removeFiltroOrgaoJulgadorCaixa(ConsultaOrgaoJulgadorCaixa consultaOrgaoJulgadorCaixa) {
		orgaoJulgadorCaixaList.remove(consultaOrgaoJulgadorCaixa);
	}

	public List<ConsultaOrgaoJulgadorCaixa> getOrgaoJulgadorCaixaList() {
		if (orgaoJulgadorCaixaList.size() == 0)
			addFiltroOrgaoJulgadorCaixa();
		return orgaoJulgadorCaixaList;
	}

	public void addFiltroNumeroProcesso() {
		numeroProcessoList.add(new NumeroProcesso());
	}

	public void removeFiltroNumeroProcesso(NumeroProcesso numeroProcesso) {
		numeroProcessoList.remove(numeroProcesso);
	}

	public List<NumeroProcesso> getNumeroProcessoList() {
		if (numeroProcessoList.size() == 0)
			addFiltroNumeroProcesso();
		return numeroProcessoList;
	}

	public void setAndOperator(Boolean andOperator) {
		this.andOperator = andOperator;
	}

	public Boolean isAndOperator() {
		return andOperator;
	}
}
