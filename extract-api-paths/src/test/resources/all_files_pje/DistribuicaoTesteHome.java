package br.com.infox.trf.distribuicao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Scope(ScopeType.SESSION)
@Name("distribuicaoTesteHome")
@Deprecated
public class DistribuicaoTesteHome implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nomeCargo;
	private String nomeVara;
	private Integer qtClasse;
	private Integer qtTotal;
	private Integer qtDistribuir = 1;

	private List<VaraCargo> varaCargos;

	private String[] cargoList = { "Titular", "Substituto" };

	public void addVaraCargo() {
		VaraCargo v = new VaraCargo(nomeVara, nomeCargo, qtClasse, qtTotal);
		if (varaCargos == null) {
			varaCargos = new ArrayList<VaraCargo>();
		}
		removeVaraCargo(v);
		varaCargos.add(v);
		nomeCargo = null;
		nomeVara = null;
		qtClasse = null;
		qtTotal = null;
	}

	public void removeVaraCargo(VaraCargo varaCargo) {
		varaCargos.remove(varaCargo);
	}

	public void distribuir() {
		if (varaCargos != null && varaCargos.size() > 0) {
			SorteioVaraCargo sorteioVaraCargo = new SorteioVaraCargo();
			sorteioVaraCargo.setListVaraCargo(varaCargos);
			sorteioVaraCargo.calcPesos();
			for (int i = 0; i < qtDistribuir; i++) {
				Sorteio<VaraCargo> sorteio = new Sorteio<VaraCargo>(sorteioVaraCargo.getListVaraCargoAsElementoList());
				Elemento<VaraCargo> elementoSorteado = sorteio.sortearElemento();

				VaraCargo vara = varaCargos.get(varaCargos.indexOf(elementoSorteado.getObjeto()));
				vara.incClasse();

				sorteioVaraCargo.calcPesos();
			}
		}
	}

	public boolean isEditable() {
		return true;
	}

	public EntityManager getEntityManager() {
		return (EntityManager) Component.getInstance("entityManager");
	}

	public void limparTela() {
	}

	public void setNomeCargo(String nomeVaraCargo) {
		this.nomeCargo = nomeVaraCargo;
	}

	public String getNomeCargo() {
		return nomeCargo;
	}

	public void setQtClasse(Integer qtClasse) {
		this.qtClasse = qtClasse;
	}

	public Integer getQtClasse() {
		return qtClasse;
	}

	public void setQtTotal(Integer qtTotal) {
		this.qtTotal = qtTotal;
	}

	public Integer getQtTotal() {
		return qtTotal;
	}

	public void setVaraCargos(List<VaraCargo> varaCargos) {
		this.varaCargos = varaCargos;
	}

	public List<VaraCargo> getVaraCargos() {
		return varaCargos;
	}

	public void setQtDistribuir(Integer qtDistribuir) {
		this.qtDistribuir = qtDistribuir;
	}

	public Integer getQtDistribuir() {
		return qtDistribuir;
	}

	public void setNomeVara(String nomeVara) {
		this.nomeVara = nomeVara;
	}

	public String getNomeVara() {
		return nomeVara;
	}

	public void setCargoList(String[] cargoList) {
		this.cargoList = cargoList;
	}

	public String[] getCargoList() {
		return cargoList;
	}
}