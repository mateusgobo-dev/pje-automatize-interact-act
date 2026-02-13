package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.component.tree.ClasseJudicialTipoCertidaoTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ClasseJudicialTipoCertidao;
import br.jus.pje.nucleo.enums.APTEnum;

@Name(ClasseJudicialTipoCertidaoHome.NAME)
@BypassInterceptors
public class ClasseJudicialTipoCertidaoHome extends AbstractClasseJudicialTipoCertidaoHome<ClasseJudicialTipoCertidao> {

	private static final long serialVersionUID = 1L;

	private Boolean visualizarTree = Boolean.FALSE;
	private ClasseJudicial classeJudicialTree;
	private APTEnum polo;

	private ClasseJudicial classeJudicial;
	public static final String NAME = "classeJudicialTipoCertidaoHome";

	public static ClasseJudicialTipoCertidaoHome instance() {
		return ComponentUtil.getComponent(ClasseJudicialTipoCertidaoHome.NAME);
	}

	// private TipoCertidaoSuggestBean getTipoCertidaoSuggestBean(){
	// return getComponent("tipoCertidaoSuggest");
	// }

	@Override
	public void newInstance() {
		setVisualizarTree(Boolean.FALSE);
		super.newInstance();
	}

	public void setVisualizarTree(Boolean visualizarTree) {
		this.visualizarTree = visualizarTree;
	}

	public Boolean getVisualizarTree() {
		return visualizarTree;
	}

	public void setClasseJudicialTree(ClasseJudicial classeJudicialTree) {
		this.classeJudicialTree = classeJudicialTree;
	}

	public ClasseJudicial getClasseJudicialTree() {
		return classeJudicialTree;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	@Override
	public String persist() {
		if (classeJudicial == null) {
			return null;
		}
		getInstance().setClasseJudicial(classeJudicial);
		String persist = super.persist();
		refreshGrid("classeJudicialTipoCertidaoGrid");
		newInstance();
		setClasseJudicial(null);
		return persist;
	}

	@Override
	public String remove(ClasseJudicialTipoCertidao obj) {
		String ret = super.remove(obj);
		setVisualizarTree(Boolean.FALSE);
		return ret;
	}

	public void visualizarTree(ClasseJudicialTipoCertidao obj) {
		setVisualizarTree(Boolean.TRUE);

		ClasseJudicialTipoCertidaoTreeHandler tree = getComponent("classeJudicialTipoCertidaoTree");
		tree.clearTree();

		classeJudicialTree = obj.getClasseJudicial();
	}

	public void setPolo(APTEnum polo) {
		this.polo = polo;
	}

	public APTEnum getPolo() {
		return polo;
	}

}