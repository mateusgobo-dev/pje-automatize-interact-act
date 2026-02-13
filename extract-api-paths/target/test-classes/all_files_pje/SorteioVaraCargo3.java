package br.com.infox.trf.distribuicao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SorteioVaraCargo3 {

	List<VaraCargo> varaCargoList;

	public SorteioVaraCargo3() {
		varaCargoList = new ArrayList<VaraCargo>();
	}

	public void addVaraCargo(VaraCargo varaCargo) {
		varaCargoList.add(varaCargo);
	}

	public List<VaraCargo> getListVaraCargo() {
		return varaCargoList;
	}

	public void setListVaraCargo(List<VaraCargo> listVaraCargo) {
		this.varaCargoList = listVaraCargo;
	}

	public void calcPesos() {
		int totalClasse = 0;
		int total = 0;
		Map<String, Integer> varaTotalMap = getTotalVaraMap();
		for (VaraCargo varaCargo : varaCargoList) {
			totalClasse += varaCargo.getQuantidadeClasse();
			total += varaCargo.getQuantidadeTotal();
		}
		if (total == 0) {
			for (VaraCargo varaCargo : varaCargoList) {
				varaCargo.setPeso(1);
			}
		} else if (totalClasse == 0) {
			for (VaraCargo varaCargo : varaCargoList) {
				varaCargo.setPeso(varaCargo.getQuantidadeTotal() + varaTotalMap.get(varaCargo.getNomeVara()));
			}
		} else {
			float peso = ((float) total / varaCargoList.size()) / (totalClasse / varaCargoList.size());
			for (VaraCargo varaCargo : varaCargoList) {
				varaCargo.setPeso(varaCargo.getQuantidadeClasse() * peso + varaCargo.getQuantidadeTotal()
						+ varaTotalMap.get(varaCargo.getNomeVara()));
			}
		}
	}

	public List<Elemento<VaraCargo>> getListVaraCargoAsElementoList() {
		List<Elemento<VaraCargo>> elementos = new ArrayList<Elemento<VaraCargo>>();
		Map<String, Integer> varaTotalMap = getTotalVaraMap();
		for (VaraCargo v : varaCargoList) {
			Elemento<VaraCargo> elemento = new Elemento<VaraCargo>(v, (int) v.getPeso(), varaTotalMap.get(v
					.getNomeVara()));
			elementos.add(elemento);
		}
		return elementos;
	}

	public static Map<String, Integer> getTotalVaraMap(List<VaraCargo> varaCargoList) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (VaraCargo varaCargo : varaCargoList) {
			String nomeVara = varaCargo.getNomeVara();
			Integer totalVara = map.get(nomeVara);
			map.put(nomeVara, varaCargo.getQuantidadeTotal() + (totalVara == null ? 0 : totalVara));
		}
		return map;
	}

	public Map<String, Integer> getTotalVaraMap() {
		return getTotalVaraMap(varaCargoList);
	}

}