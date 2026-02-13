package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.entidades.TipoParteConfiguracao;
import br.jus.pje.nucleo.enums.ClasseJudicialInicialEnum;

@Name(ClasseJudicialHome.NAME)
@BypassInterceptors
public class ClasseJudicialHome extends AbstractClasseJudicialHome<ClasseJudicial> {

	private static final long serialVersionUID = 7779457816200091235L;
	
	public static final String NAME = "classeJudicialHome";
	private static final LogProvider log = Logging.getLogProvider(AbstractHome.class);
	private SearchTree2GridList<ClasseJudicial> searchTree2GridList;
	private ClasseJudicial classeJudicialPai;

	public static ClasseJudicialHome instance() {
		return ComponentUtil.getComponent(ClasseJudicialHome.NAME, ScopeType.CONVERSATION);
	}

	public ClasseJudicial getClasseJudicialPai() {
		return classeJudicialPai;
	}

	public void setClasseJudicialPai(ClasseJudicial classeJudicialPai) {
		this.classeJudicialPai = classeJudicialPai;
	}

	public ClasseJudicialInicialEnum[] getClasseJudicialInicialEnumValues() {
		return ClasseJudicialInicialEnum.values();
	}
	
	@Create
	public void create() {
		super.create();
		this.iniciaSearchTab();
	}

	public void iniciaSearchTab() {
		ClasseJudicial searchBean = getComponent("classeJudicialSearch");
		searchBean.setPadraoSgt(null);
	}
	
	@Override
	public void onClickSearchTab() {
		super.onClickSearchTab();
		this.iniciaSearchTab();
	}

	@Override
	protected ClasseJudicial createInstance() {
		instance = super.createInstance();
		instance.setClasseJudicialPai(new ClasseJudicial());
		return instance;
	}

	@Override
	public void newInstance() {
		getInstance().setAtivo(Boolean.TRUE);
		getInstance().setPadraoSgt(Boolean.FALSE);
		Contexts.removeFromAllContexts("poloAtivoSuggest");
		Contexts.removeFromAllContexts("poloPassivoSuggest");
		limparTrees();
		refreshGrid("classeJudicialGrid");
		super.newInstance();
	}

	private void limparTrees() {
		ClasseJudicialTreeHandler ret1 = getComponent("classeJudicialSearchTree", ScopeType.CONVERSATION);
		ClasseJudicialTreeHandler ret2 = getComponent("classeJudicialFormTree", ScopeType.CONVERSATION);
		ret1.clearTree();
		ret2.clearTree();
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
		classeJudicialPai = getInstance().getClasseJudicialPai();
	}

	@Override
	public String persist() {
		ClasseJudicial classeJudicial = getInstance();
		classeJudicial.setClasseJudicialCompleto(classeJudicial.getClasseJudicial());
		String ret = super.persist();
		if (ret != null) {
			adicionarTipoParte(classeJudicial, "ADVOGADO");
			limparTrees();
			EntityUtil.flush(getEntityManager());
		}
		return ret;
	}
	
	/**
	 * Método responsável por adicionar uma ou mais tipos de parte à classe judicial.
	 * 
	 * @param classeJudicial Classe Judicial
	 * @param descricaoTipoPartes Descrição do tipo de parte
	 */
	private void adicionarTipoParte(ClasseJudicial classeJudicial, String... descricaoTipoPartes) {
		if (classeJudicial != null && descricaoTipoPartes != null) {  // Validação dos parâmetros de entrada.
			TipoParteManager tipoParteManager = getComponent(TipoParteManager.NAME);
			for (int i = 0; i < descricaoTipoPartes.length; i++) {
				try {
					List<TipoParte> tipoParteListFound = tipoParteManager.findByNomeParticipacao(descricaoTipoPartes[i]);
					if (tipoParteListFound != null && tipoParteListFound.size() > 0) {
						TipoParte tipoParteFound = tipoParteListFound.get(0);
						TipoParteConfiguracao tipoParteConfiguracao = new TipoParteConfiguracao();
						tipoParteConfiguracao.setPoloAtivo(true);
						tipoParteConfiguracao.setTipoParte(tipoParteFound);
						tipoParteConfiguracao.setTipoPessoaFisica(true);
						tipoParteConfiguracao.setTipoPessoaJuridica(false);
						tipoParteConfiguracao.setOab(true);
						getEntityManager().persist(tipoParteConfiguracao);
						
						
						TipoParteConfigClJudicial tipoParteConfigClJudicial = new TipoParteConfigClJudicial();
						tipoParteConfigClJudicial.setClasseJudicial(classeJudicial);
						tipoParteConfigClJudicial.setTipoParteConfiguracao(tipoParteConfiguracao);
						getEntityManager().persist(tipoParteConfigClJudicial);
						
					}
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
	}

	public String inactiveRecursive(ClasseJudicial classeJudicial) {
		
		if(existeAssociacaoAtivaRecursive(classeJudicial)){			
			FacesMessages.instance().add(Severity.ERROR,"Não é possível inativar esta classe, pois ela está sendo utilizada por um tipo de audiência de algum órgão julgador");
			return "";
		}
		
		if (classeJudicial.getClasseJudicialList().size() > 0) {
			inativarFilhos(classeJudicial);
		}
		
		classeJudicial.setAtivo(Boolean.FALSE);
				
		String ret = super.update();
		
		if (ret != null && ret != "") {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, super.getInactiveSuccess());
		}
		
		limparTrees();
		refreshGrid("classeJudicialGrid");
		return ret;
	}

	private void inativarFilhos(ClasseJudicial classeJudicial) {
		classeJudicial.setAtivo(Boolean.FALSE);
		getEntityManager().merge(classeJudicial);
		
		Integer quantidadeFilhos = classeJudicial.getClasseJudicialList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(classeJudicial.getClasseJudicialList().get(i));
		}
	}
	
	private boolean existeAssociacaoAtiva(ClasseJudicial classeJudicial){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from OjClasseTipoAudiencia o ");
		sb.append("where o.classeJudicial=:classeJudicial ");
		sb.append("and (o.dtFim = null or o.dtFim >= now()) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("classeJudicial", classeJudicial);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}		
	}
	
	private boolean existeAssociacaoAtivaRecursive(ClasseJudicial classeJudicial){
		boolean result = false;
		if(existeAssociacaoAtiva(classeJudicial)){
			return true;
		}
		if(classeJudicial.getClasseJudicialList().size()>0){
			result = existeAssociacaoAtivaFilhos(classeJudicial);
		}
		return result;
	}
	
	private boolean existeAssociacaoAtivaFilhos(ClasseJudicial classeJudicial){
		if(existeAssociacaoAtiva(classeJudicial)){
			return true;
		}
		Integer quantidadeFilhos = classeJudicial.getClasseJudicialList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			if(existeAssociacaoAtivaFilhos(classeJudicial.getClasseJudicialList().get(i))){
				return true;
			}
		}
		return false;
	}

	@Override
	public String update() {
		verificaListas();
		/*
		 * Se o registro estiver como inativo na hora do update, todos os seus
		 * filhos serão inativados
		 */
		if (!getInstance().getAtivo()) {
			inactiveRecursive(getInstance());
			return "updated";
		} else {
			String ret = super.update();
			if (ret != null) {
				limparTrees();
			} else {
				getInstance();
			}
			return ret;
		}
	}

	private void verificaListas() {
		/*
		 * Verifica se o pai atual e o pai selecionado são diferentes de nulo e
		 * se os dois são diferentes um do outro e remove o registro da lista do
		 * pai atual e insere na lista do pai selecionado.
		 */
		if ((getInstance().getClasseJudicialPai() != null)
				&& (classeJudicialPai != null)
				&& (!getInstance().getClasseJudicialPai().getClasseJudicial()
						.equals(classeJudicialPai.getClasseJudicial()))) {

			getInstance().getClasseJudicialPai().getClasseJudicialList().remove(getInstance());
			classeJudicialPai.getClasseJudicialList().add(getInstance());
			classeJudicialPai.getClasseJudicialList();
		}
		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getClasseJudicialPai() != null) && (classeJudicialPai == null)) {
			getInstance().getClasseJudicialPai().getClasseJudicialList().remove(getInstance());
		}
		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getClasseJudicialPai() == null) && (classeJudicialPai != null)) {
			classeJudicialPai.getClasseJudicialList().add(getInstance());
			getInstance().setClasseJudicialPai(classeJudicialPai);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		
		double valorPesoClasseJudicialMin = Double.parseDouble(ParametroUtil.getFromContext(
				Parametros.PESO_MINIMO_CLASSE_JUDICIAL, true));
		
		double valorPesoClasseJudicialMax = Double.parseDouble(ParametroUtil.getFromContext(
				Parametros.PESO_MAXIMO_CLASSE_JUDICIAL, true));
		
		valorPesoClasseJudicialMin = obterPesoMinimoSeClasseJudicialIncidental(valorPesoClasseJudicialMin); 
		
		boolean pesoValido = false;
		if(instance.getValorPeso() != null){
			pesoValido = instance.getValorPeso() >= valorPesoClasseJudicialMin 
						&& instance.getValorPeso() <= valorPesoClasseJudicialMax;
		}
		
		if (!pesoValido) {
			FacesMessages.instance()
					.addFromResourceBundle(StatusMessage.Severity.ERROR, "classeJudicial.erro.peso.fora.minimo.maximo", 
														valorPesoClasseJudicialMin, valorPesoClasseJudicialMax);
			return false;
		}
		boolean controlaValorCausa = getInstance().getControlaValorCausa();
		if(controlaValorCausa){
			if(getInstance().getPisoValorCausa() == null){
				getInstance().setPisoValorCausa(0.0);
			}
			if(getInstance().getPisoValorCausa().compareTo(0.0) >= 0 
					&& (getInstance().getTetoValorCausa() != null	&& getInstance().getPisoValorCausa().compareTo(getInstance().getTetoValorCausa()) >= 0)){
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Os limites do valor da causa devem ser positivos e o valor máximo não pode ser inferior ao mínimo.");
				return false;
			}
		}else{
			getInstance().setPisoValorCausa(null);
			getInstance().setTetoValorCausa(null);
		}

		if (classeJudicialPai != null) {
			classeJudicialPai.setJusPostulandi(null);
		}
		
		if (getInstance().getHabilitarMascaraProcessoReferencia() == null) {
			getInstance().setHabilitarMascaraProcessoReferencia(Boolean.FALSE);
		}

		getInstance().setClasseJudicialPai(classeJudicialPai);
		return super.beforePersistOrUpdate();
	}

	/**
	 *  Método que obtém o valor do Peso Mínimo para Classe Judicial Incidental
	 *  
	 *  @param	valorPesoClasseJudicialMin
	 *  @return Caso a classe judicial seja incidental, então procura pelo parâmetro de valor mínimo para este tipo de 
	 *  		classe. Caso não encontrado, mantém o valor do parâmetro encontrado anteriormente para classes judiciais
	 */
	private double obterPesoMinimoSeClasseJudicialIncidental(double valorPesoClasseJudicialMin) {
		
		if (Boolean.TRUE == instance.getIncidental()) {
			String pesoMinimoClasJudIncidental = ParametroUtil
										.getFromContext(Parametros.PESO_MINIMO_CLASSE_JUDICIAL_INCIDENTAL, false);
			
			if (StringUtils.isNotBlank(pesoMinimoClasJudIncidental)) {
				valorPesoClasseJudicialMin = Double.parseDouble(pesoMinimoClasJudIncidental);	
			}
		}
		return valorPesoClasseJudicialMin;
	}

	public String persistAndNext() {
		String outcome = null;
		try {
			outcome = persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		if (outcome != null) {
			if (!outcome.equals("")) {
				ClasseJudicial me = getInstance();
				newInstance();
				getInstance().setClasseJudicialPai(me);
				EntityUtil.flush();
				classeJudicialPai = getInstance().getClasseJudicialPai();
				getInstance().setClasseJudicialPai(classeJudicialPai);
			}
		}
		return outcome;
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			classeJudicialPai = getInstance().getClasseJudicialPai();
		}
		if (id == null) {
			classeJudicialPai = null;
		}
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			classeJudicialPai = getInstance().getClasseJudicialPai();
		} else {
			classeJudicialPai = null;

			/*
			 * PJE-JT:Ricardo Scholz e Sérgio Pacheco : PJE-103 - 2011-09-08
			 * Alteracoes feitas pela JT
			 */
			Boolean Jt = (Boolean) Component.getInstance("justicaTrabalho");
			getInstance().setJusPostulandi(Jt);
			/*
			 * PJE-JT:Fim
			 */

		}
		super.onClickFormTab();
	}

	public SearchTree2GridList<ClasseJudicial> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			ClasseJudicial searchBean = getComponent("classeJudicialSearch");
			ClasseJudicialTreeHandler treeHandler = getComponent("classeJudicialSearchTree");
			searchTree2GridList = new SearchTree2GridList<ClasseJudicial>(searchBean, treeHandler);
			String[] filterName = { "ativo", "classeJudicial", "classeJudicialSigla", "codClasseOutro",
					"codClasseJudicial", "classeJudicialPai", "natureza", "norma", "leiArtigo", "lei", "inicial",
					"recursal", "incidental", "fluxoStr", "classeJudicialGlossario" };
			searchTree2GridList.setFilterName(filterName);
			searchTree2GridList.setGrid((GridQuery) getComponent("classeJudicialGrid"));
		}
		return searchTree2GridList;
	}

	public void pesoChangedInGrid() {
		// correção do bug do Hibernate
		EntityUtil.flush();
	}
	
	public Boolean isClasseCriminal() {
		Boolean ret = Boolean.FALSE;
		
		ClasseJudicialManager manager = ComponentUtil.getComponent(ClasseJudicialManager.NAME);
		
		ret = manager.isClasseCriminal(this.instance);
		
		return ret;
	}
	
	public Boolean isClasseInfracional() {
		Boolean ret;
		
		ClasseJudicialManager manager = ComponentUtil.getComponent(ClasseJudicialManager.NAME);
		ret =  manager.isClasseInfracional(this.instance);
		
		return ret;
	}
	
	public Boolean isClasseCriminalOuInfracional() {
		return isClasseCriminal() || isClasseInfracional();
	}

}