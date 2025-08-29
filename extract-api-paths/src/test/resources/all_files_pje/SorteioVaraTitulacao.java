package br.com.infox.trf.distribuicao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;

public class SorteioVaraTitulacao {

	List<VaraTitulacao> varaCargoList;

	public SorteioVaraTitulacao() {
		varaCargoList = new ArrayList<VaraTitulacao>();
	}

	public void addVaraCargo(VaraTitulacao varaCargo) {
		varaCargoList.add(varaCargo);
	}

	public List<VaraTitulacao> getListVaraCargo() {
		return varaCargoList;
	}

	public void setListVaraCargo(List<VaraTitulacao> listVaraCargo) {
		this.varaCargoList = listVaraCargo;
	}

	public void calcPesos() {
		int totalClasse = 0;
		int total = 0;
		Map<OrgaoJulgador, Integer> varaTotalMap = getTotalVaraMap();
		for (VaraTitulacao varaCargo : varaCargoList) {
			totalClasse += varaCargo.getQuantidadeClasse();
			total += varaCargo.getQuantidadeTotal();
		}
		if (total == 0) {
			for (VaraTitulacao varaCargo : varaCargoList) {
				varaCargo.setPeso(1);
			}
		} else if (totalClasse == 0) {
			for (VaraTitulacao varaCargo : varaCargoList) {
				varaCargo.setPeso(varaCargo.getQuantidadeTotal() + varaTotalMap.get(varaCargo.getOrgaoJulgador()));
			}
		} else {
			float peso = ((float) total / varaCargoList.size()) / (totalClasse / varaCargoList.size());
			for (VaraTitulacao varaCargo : varaCargoList) {
				varaCargo.setPeso(varaCargo.getQuantidadeClasse() * peso
						+ varaTotalMap.get(varaCargo.getOrgaoJulgador()));
			}
		}
	}

	public List<Elemento<VaraTitulacao>> getListVaraCargoAsElementoList() {
		List<Elemento<VaraTitulacao>> elementos = new ArrayList<Elemento<VaraTitulacao>>();
		for (VaraTitulacao v : varaCargoList) {
			Elemento<VaraTitulacao> elemento = new Elemento<VaraTitulacao>(v, (int) v.getPeso(), v.getQuantidadeTotal());
			elementos.add(elemento);
		}
		return elementos;
	}

	public static Map<OrgaoJulgador, Integer> getTotalVaraMap(List<VaraTitulacao> varaCargoList) {
		Map<OrgaoJulgador, Integer> map = new HashMap<OrgaoJulgador, Integer>();
		for (VaraTitulacao varaCargo : varaCargoList) {
			OrgaoJulgador nomeVara = varaCargo.getOrgaoJulgador();
			Integer totalVara = map.get(nomeVara);
			map.put(nomeVara, varaCargo.getQuantidadeTotal() + (totalVara == null ? 0 : totalVara));
		}
		return map;
	}

	public Map<OrgaoJulgador, Integer> getTotalVaraMap() {
		return getTotalVaraMap(varaCargoList);
	}

}