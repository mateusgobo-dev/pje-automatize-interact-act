package br.com.infox.trf.distribuicao;

import java.util.ArrayList;
import java.util.List;

public class SorteioVaraCargo {

	List<VaraCargo> listVaraCargo;

	public SorteioVaraCargo() {
		listVaraCargo = new ArrayList<VaraCargo>();
	}

	public void addVaraCargo(VaraCargo varaCargo) {
		listVaraCargo.add(varaCargo);
	}

	public List<VaraCargo> getListVaraCargo() {
		return listVaraCargo;
	}

	public void setListVaraCargo(List<VaraCargo> listVaraCargo) {
		this.listVaraCargo = listVaraCargo;
	}

	public void calcPesos() {
		int totalClasse = 0;
		int total = 0;
		for (VaraCargo varaCargo : listVaraCargo) {
			totalClasse += varaCargo.getQuantidadeClasse();
			total += varaCargo.getQuantidadeTotal();
		}
		if (total == 0) {
			for (VaraCargo varaCargo : listVaraCargo) {
				varaCargo.setPeso(1);
			}
		} else if (totalClasse == 0) {
			for (VaraCargo varaCargo : listVaraCargo) {
				varaCargo.setPeso(varaCargo.getQuantidadeTotal());
			}
		} else {
			float peso = ((float) total / listVaraCargo.size()) / (totalClasse / listVaraCargo.size());
			for (VaraCargo varaCargo : listVaraCargo) {
				varaCargo.setPeso(varaCargo.getQuantidadeClasse() * peso + varaCargo.getQuantidadeTotal());
			}
		}
	}

	public List<Elemento<VaraCargo>> getListVaraCargoAsElementoList() {
		List<Elemento<VaraCargo>> elementos = new ArrayList<Elemento<VaraCargo>>();
		for (VaraCargo v : listVaraCargo) {
			Elemento<VaraCargo> elemento = new Elemento<VaraCargo>(v, (int) v.getPeso(), v.getQuantidadeTotal());
			elementos.add(elemento);
		}
		return elementos;
	}

}