package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.ClasseAplicacaoDescricaoAplicacaoClasseSuggestBean;
import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.listener.LogEventListener;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;
import br.jus.pje.nucleo.entidades.OjClasseTipoAudiencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(CompetenciaClasseAssuntoHome.NAME)
@BypassInterceptors
public class CompetenciaClasseAssuntoHome extends AbstractCompetenciaClasseAssuntoHome<CompetenciaClasseAssunto> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "competenciaClasseAssuntoHome";

	private AssuntoTrf assuntoLocalTrf;
	private ClasseJudicial classeJudicial;
	private AssuntoTrf assuntoTrf;
	private ClasseJudicial classeJudicialFilho;
	private AssuntoTrf assuntoTrfFilho;
	private List<CompetenciaClasseAssunto> listFilhos = new ArrayList<CompetenciaClasseAssunto>(0);
	private Boolean checkBox = Boolean.FALSE;
	private Date dataAtual;
	private AplicacaoClasse aplicacaoClasse;

	private ClasseAplicacaoDescricaoAplicacaoClasseSuggestBean getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean() {
		return getComponent("classeAplicacaoDescricaoAplicacaoClasseSuggest");
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (getInstance().getClasseAplicacao().getClasseJudicial() != null) {
				classeJudicial = getInstance().getClasseAplicacao().getClasseJudicial();
				getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean().setInstance(
						getInstance().getClasseAplicacao().getAplicacaoClasse());
			}
			this.assuntoLocalTrf = getInstance().getAssuntoTrf();
		}
		if (id == null) {
			ClasseAplicacao classeAplicacao = new ClasseAplicacao();
			classeJudicialFilho = classeAplicacao.getClasseJudicial();
			getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean().setInstance(classeAplicacao.getAplicacaoClasse());
			this.assuntoLocalTrf = null;
		}
	}

	public String getCJSuggest() {
		String cj = "";
		if (classeJudicialFilho != null) {
			cj = classeJudicialFilho.getClasseJudicial();
		}
		return cj;
	}

	public String getACSuggest() {
		String ac = "";
		if (getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean().getInstance() != null) {
			ac = getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean().getInstance().getAplicacaoClasse();
		}
		return ac;
	}

	public void limparACSuggest() {
		getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean().setInstance(null);
		Contexts.removeFromAllContexts("classeAplicacaoDescricaoAplicacaoClasseSuggest");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (instance.getClasseAplicacao() == null) {
			getInstance().setClasseAplicacao(getClasseAplicacao());
		}
		return super.beforePersistOrUpdate();
	}

	private ClasseAplicacao getClasseAplicacao() {
		StringBuilder sb = new StringBuilder();

		ClasseAplicacao retorno = null;

		if ((getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean().getInstance() != null)
				&& (this.classeJudicial != null)) {
			sb.append("select o from ClasseAplicacao o where o.ativo = true ");
			sb.append("and o.classeJudicial = :classeJudicial ");
			sb.append("and o.aplicacaoClasse = :aplicacaoClasse ");
			sb.append("and o.orgaoJustica = :orgaoJustica ");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("classeJudicial", classeJudicial);
			q.setParameter("aplicacaoClasse", getClasseAplicacaoDescricaoAplicacaoClasseSuggestBean().getInstance());
			q.setParameter("orgaoJustica", ParametroUtil.instance().getOrgaoJustica());

			retorno = (ClasseAplicacao) EntityUtil.getSingleResult(q);
		}

		return retorno;
	}

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("classeAplicacaoDescricaoClasseJudicialSuggest");
		Contexts.removeFromAllContexts("classeAplicacaoDescricaoAplicacaoClasseSuggest");
		((AssuntoTrfTreeHandler) getComponent("assuntoCompClassAssunTrfFormTree")).clearTree();
		((ClasseJudicialTreeHandler) getComponent("classeJudicialTree")).clearTree();
		assuntoLocalTrf = null;
		assuntoTrf = null;
		checkBox = Boolean.FALSE;
		limparChecks();
		classeJudicial = null;
		assuntoTrfFilho = null;
		classeJudicialFilho = null;
		aplicacaoClasse = null;
		super.newInstance();
	}

	@SuppressWarnings("unchecked")
	public boolean verificarExistente(Integer idAssuntoTrf, Integer idCompetencia, Integer idClasseAplicacao) {
		EntityManager em = EntityUtil.getEntityManager();
		String query = "select o from CompetenciaClasseAssunto o " + "where o.assuntoTrf.idAssuntoTrf = :idAssuntoTrf "
				+ "and o.competencia.idCompetencia = :idCompetencia "
				+ "and o.classeAplicacao.idClasseAplicacao = :idClasseAplicacao";
		Query q = em.createQuery(query);
		q.setParameter("idAssuntoTrf", idAssuntoTrf);
		q.setParameter("idCompetencia", idCompetencia);
		q.setParameter("idClasseAplicacao", idClasseAplicacao);
		
		List<CompetenciaClasseAssunto> competenciaClasseAssuntoList = (List<CompetenciaClasseAssunto>) q.getResultList();
		
		Boolean flag = Boolean.TRUE;
		if (competenciaClasseAssuntoList.size() >= 1) {
			for (int i = 0; i < competenciaClasseAssuntoList.size(); i++) {
				CompetenciaClasseAssunto cca = (CompetenciaClasseAssunto) competenciaClasseAssuntoList.get(i);
				if (cca.getDataFim() == null
						|| (cca.getDataFim() != null && getInstance().getDataInicio().before(cca.getDataFim()))) {
					flag = false;
					return flag;
				}
			}
		}
		return flag;
	}

	public Boolean isDataValida() {
		if (instance.getDataFim() != null && instance.getDataFim().after(instance.getDataInicio())) {
			return true;
		}
		if (instance.getDataFim() == null) {
			return true;
		}
		return false;
	}

	public void gravarVarios() {
		if (isDataValida()) {
			AssuntoTrfTreeHandler tree = getComponent("assuntoCompClassAssunTrfFormTree");
		 	Boolean duplicados = false;
			if (tree.getSelectedTree().size() == 0) {
				FacesMessages.instance().add(Severity.ERROR, "É preciso marcar pelo menos um assunto.");
			} else {
			 	ClasseAplicacao classeAplicacao = getClasseAplicacao();
			 	if (classeAplicacao == null) {
			 		FacesMessages.instance().clear();
			 		FacesMessages.instance().add("Selecione Aplicação Classe antes de incluir!");
			 		return;
			 	}
				for (AssuntoTrf assunto : tree.getSelectedTree()) {
					if (verificarAssuntoFilho(assunto)) {
						CompetenciaClasseAssunto cca = new CompetenciaClasseAssunto();
						cca.setClasseAplicacao(classeAplicacao);
						cca.setCompetencia(instance.getCompetencia());
						cca.setDataFim(instance.getDataFim());
						cca.setDataInicio(instance.getDataInicio());
						cca.setAssuntoTrf(assunto);
						System.out.println(assunto.getIdAssuntoTrf() + "=="
								+ instance.getCompetencia().getIdCompetencia() + "=="
								+ classeAplicacao.getIdClasseAplicacao());
						if (verificarExistente(assunto.getIdAssuntoTrf(), instance.getCompetencia().getIdCompetencia(),
								classeAplicacao.getIdClasseAplicacao())) {
							persist(cca);
							refreshGrid("competenciaPaiClasseAssuntoGrid");
							refreshGrid("competenciaClasseAssuntoGrid");
						} else {
						 	FacesMessages.instance().clear();
						 	FacesMessages.instance().add(StatusMessage.Severity.INFO,
						 			"Eventuais assuntos previamente inseridos tiveram suas datas originais mantidas.");
						 	duplicados = true;
						}
					}
				}
				if (!duplicados) {
					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "competenciaClasseAssunto_created"));
				}
				tree.clearTree();
				newInstance();
				refreshGrid("classeJudicialAtendimentoPlantaoGrid");				
			}
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"A data final tem que ser maior que a data inicial");
		}
	}

	/**
	 *  Verifica se o assunto escolhido é folha. Se for, adiciona.
	 * @param assunto
	 * @return
	 */
	public Boolean verificarAssuntoFilho(AssuntoTrf assunto) {
		if(assunto == null){
			return false;
		}
		AssuntoTrfManager assuntoTrfManager = (AssuntoTrfManager) Component.getInstance("assuntoTrfManager");
		if(!assuntoTrfManager.hasChildren(assunto) && assunto.getAssuntoTrfSuperior() != null){
			return true;
		}else{
			int cont = 0;
			AssuntoTrf aux = assunto;
			while((aux = aux.getAssuntoTrfSuperior()) != null){
				cont++;
				if(cont == 2){
					break;
				}
			}
			return cont >= 2;
		}
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
			refreshGrid("competenciaClasseAssuntoGrid");
		} catch (EntityExistsException e) {
			instance().add(StatusMessage.Severity.ERROR, "Registro já existe.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public void Pesquisa(String grid) {
		Contexts.getConversationContext().remove(grid);
	}

	public void limparTela(String obj) {
		classeJudicial = null;
		classeJudicialFilho = null;
		assuntoTrf = null;
		assuntoTrfFilho = null;
		((AssuntoTrfTreeHandler) getComponent("assuntoTrfProcessoTree")).clearTree();
		((ClasseJudicialTreeHandler) getComponent("classeJudicialTree")).clearTree();
		setAplicacaoClasse(null);
		setAssuntoLocalTrf(null);
		limparACSuggest();
		UIComponent form = ComponentUtil.getUIComponent(obj);
		ComponentUtil.clearChildren(form);
	}

	public void gravarCompetenciaPaiClasseAssunto() {
		int limitador = 0;
		int limiteRegistros = 100;
		if (listFilhos.size() >= limiteRegistros) {
			LogEventListener.disableLogForEvent();
		}
		for (int i = 0; i < listFilhos.size(); i++) {
			if (listFilhos.get(i).getClasseAplicacao() == null) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add("Selecione Aplicação Classe antes de incluir!");
				return;
			}

			CompetenciaClasseAssunto cca = new CompetenciaClasseAssunto();
			cca.setAssuntoTrf(listFilhos.get(i).getAssuntoTrf());
			cca.setClasseAplicacao(listFilhos.get(i).getClasseAplicacao());
			cca.setCompetencia(CompetenciaHome.instance().getInstance());
			cca.setDataFim(listFilhos.get(i).getDataFim());
			cca.setDataInicio(listFilhos.get(i).getDataInicio());
			EntityManager em = EntityUtil.getEntityManager();
			em.persist(cca);
			em.flush();
			limitador++;
			if (limitador > limiteRegistros) {
				EntityUtil.getEntityManager().clear();
				limitador = 0;
			}
		}
		listFilhos.clear();
		refreshGrid("competenciaPaiClasseAssuntoGrid");
		refreshGrid("competenciaClasseAssuntoGrid");
		refreshGrid("classeJudicialAtendimentoPlantaoGrid");
	}

	@Override
	public String update() {
		String persist = super.update();
		ajustarDataFimTipoAudiencia(getInstance());
		refreshGrid("competenciaClasseAssuntoGrid");
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "competenciaClasseAssunto_updated"));
		
		return persist;
	}
	
	@SuppressWarnings("unchecked")
	public void ajustarDataFimTipoAudiencia(CompetenciaClasseAssunto cca){
		StringBuilder sb = new StringBuilder();
		sb.append("select o2 from OjClasseTipoAudiencia o2 ");
		sb.append("where o2.classeJudicial in (select o3.classeAplicacao.classeJudicial from CompetenciaClasseAssunto o3 where o3.competencia=:competencia) and ");
		sb.append("(o2.dtFim = null or o2.dtFim >= now()) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("competencia", cca.getCompetencia());
		List<OjClasseTipoAudiencia> list = q.getResultList();
		
		for (OjClasseTipoAudiencia ojClasseTipoAudiencia : list) {
			sb = new StringBuilder();
			sb.append("select o from OrgaoJulgadorCompetencia o ");
			sb.append("where o.orgaoJulgador=:orgaoJulgador and ");
			sb.append("o.competencia in (select o1 from CompetenciaClasseAssunto o1 where o1.classeAplicacao.classeJudicial=:classeJudicial) ");
			q = getEntityManager().createQuery(sb.toString());
			q.setParameter("orgaoJulgador", ojClasseTipoAudiencia.getOrgaoJulgador());
			q.setParameter("classeJudicial", ojClasseTipoAudiencia.getClasseJudicial());
			List<OrgaoJulgadorCompetencia> list2 = q.getResultList();
			Date maiorDataOrgaoJulgadorCompetencia = null;
			for (OrgaoJulgadorCompetencia orgaoJulgadorCompetencia : list2) {
				if(maiorDataOrgaoJulgadorCompetencia == null || (orgaoJulgadorCompetencia.getDataFim() != null && orgaoJulgadorCompetencia.getDataFim().after(maiorDataOrgaoJulgadorCompetencia))){
					maiorDataOrgaoJulgadorCompetencia = orgaoJulgadorCompetencia.getDataFim();
				}
			}
			
			sb = new StringBuilder();
			sb.append("select o from CompetenciaClasseAssunto o ");
			sb.append("where o.classeAplicacao.classeJudicial=:classeJudicial and ");
			sb.append("o.competencia in (select o1 from OrgaoJulgadorCompetencia o1 where o1.orgaoJulgador=:orgaoJulgador) ");
			q = getEntityManager().createQuery(sb.toString());
			q.setParameter("orgaoJulgador", ojClasseTipoAudiencia.getOrgaoJulgador());
			q.setParameter("classeJudicial", ojClasseTipoAudiencia.getClasseJudicial());
			List<CompetenciaClasseAssunto> l = q.getResultList();
			Date maiorDataCompetenciaClasseAssunto = null;
			for (CompetenciaClasseAssunto competenciaClasseAssunto : l) {
				if(maiorDataCompetenciaClasseAssunto == null || (competenciaClasseAssunto.getDataFim() != null && competenciaClasseAssunto.getDataFim().after(maiorDataCompetenciaClasseAssunto))){
					maiorDataCompetenciaClasseAssunto = competenciaClasseAssunto.getDataFim();
				}
			}
			
			Date dataFim = null;
			if(maiorDataOrgaoJulgadorCompetencia != null && maiorDataCompetenciaClasseAssunto == null || maiorDataOrgaoJulgadorCompetencia == null && maiorDataCompetenciaClasseAssunto != null){
				dataFim = maiorDataOrgaoJulgadorCompetencia != null ? maiorDataOrgaoJulgadorCompetencia : maiorDataCompetenciaClasseAssunto; 
			}else if(maiorDataOrgaoJulgadorCompetencia != null && maiorDataCompetenciaClasseAssunto != null){
				dataFim = maiorDataOrgaoJulgadorCompetencia.before(maiorDataCompetenciaClasseAssunto) ? maiorDataOrgaoJulgadorCompetencia : maiorDataCompetenciaClasseAssunto;
			}else{
				dataFim = null;
			}
			
			ojClasseTipoAudiencia.setDtFim(dataFim);
			getEntityManager().merge(ojClasseTipoAudiencia);
		}
		getEntityManager().flush();
	}

	@Override
	public String remove() {
		String remove = super.remove();
		refreshGrid("competenciaClasseAssuntoGrid");
		return remove;
	}

	public void criarLista(CompetenciaClasseAssunto obj) {
		if (obj.getCheck()) {
			listFilhos.add(obj);
		} else {
			listFilhos.remove(obj);
		}
	}

	@SuppressWarnings("unchecked")
	public void checkAll(String grid) {
		GridQuery gridQuery = getComponent("competenciaPaiClasseAssuntoGrid");
		List<CompetenciaClasseAssunto> lista = new ArrayList<CompetenciaClasseAssunto>(gridQuery.getFullList());
		
		CompetenciaClasseAssunto cca = null;

		if (checkBox) {
			for (int i = 0; i < lista.size(); i++) {
				cca = lista.get(i);
				cca.setCheck(true);
				criarLista(cca);
			}
		} else {
			for (int i = 0; i < lista.size(); i++) {
				cca = lista.get(i);
				cca.setCheck(false);
				criarLista(cca);
			}
		}
	}

	private void limparChecks() {
		for (CompetenciaClasseAssunto cca : listFilhos) {
			cca.setCheck(Boolean.FALSE);
		}
	}

	public void inativar(CompetenciaClasseAssunto cca) {
		cca.setDataFim(new Date());
		getEntityManager().merge(cca);
		getEntityManager().flush();
		ajustarDataFimTipoAudiencia(cca);
		refreshGrid("competenciaClasseAssuntoGrid");
		FacesMessages.instance().add("Registro excluído com sucesso");
	}
	
	/**
	 * Método que verifica se o processo possui a classe e os assuntos associados à classe passados.
	 * O parametro deve ser passado no seguinte formato:
	 * CLASSE_JUDICIAL_1:ASSUNTO_1_DA_CLASSE,ASSUNTO_2_DA_CLASSE,ASSUNTO_3_DA_CLASSE;CLASSE_JUDICIAL_2...
	 * exemplo: "687:32,44,55;188:12,17;99" - 687, 188 e 99 neste exemplo são classes, e os números após os ":" são assuntos da respectiva classe.
	 * OBS: Os assuntos são opcionais. A classe 99 por exemplo, não possui assunto.
	 * */
	public boolean processoContemClasseAssunto(String classesAssuntos){
		ProcessoTrf procTrf = ProcessoTrfHome.instance().getInstance();
		
		String[] classeComAssuntosAgrupados = classesAssuntos.split(";");
		String[] classeOuAssuntos;
		String[] assuntosDivididos;
		
		List<ProcessoAssunto> listaProcessoAssuntos = procTrf.getProcessoAssuntoList();
		int totalAssuntosOK = 0;
		
		for (String assuntosDaClasse : classeComAssuntosAgrupados) {
			totalAssuntosOK = 0;
			classeOuAssuntos = assuntosDaClasse.split(":");
			if (classeOuAssuntos.length > 1){
				
				if (listaProcessoAssuntos.size() > 0){
					if (procTrf.getClasseJudicial().getCodClasseJudicial().equalsIgnoreCase(classeOuAssuntos[0])){
						assuntosDivididos = classeOuAssuntos[1].split(",");
						for (int i = 0; i < assuntosDivididos.length; i++) { 
							
							for(int j=0; j < listaProcessoAssuntos.size(); j++){
								if (listaProcessoAssuntos.get(j).getAssuntoTrf().getCodAssuntoTrf().equalsIgnoreCase(assuntosDivididos[i])){
									totalAssuntosOK++;
									break;
								}
							}
						}
						if (totalAssuntosOK == (assuntosDivididos.length)){ 						
							return true;
						}
					}
				}
			}
			else{
				if (procTrf.getClasseJudicial().getCodClasseJudicial().equalsIgnoreCase(classeOuAssuntos[0])){
					return true;
				}
			}
		}
		return false;
	}

	public AssuntoTrf getAssuntoLocalTrf() {
		return assuntoLocalTrf;
	}

	public void setAssuntoLocalTrf(AssuntoTrf assuntoTrf) {
		this.assuntoLocalTrf = assuntoTrf;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setAssuntoTrf(AssuntoTrf assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public AssuntoTrf getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setListFilhos(List<CompetenciaClasseAssunto> listFilhos) {
		this.listFilhos = listFilhos;
	}

	public List<CompetenciaClasseAssunto> getListFilhos() {
		return listFilhos;
	}

	public Boolean getCheckBox() {
		return checkBox;
	}

	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public void setClasseJudicialFilho(ClasseJudicial classeJudicialFilho) {
		this.classeJudicialFilho = classeJudicialFilho;
	}

	public ClasseJudicial getClasseJudicialFilho() {
		return classeJudicialFilho;
	}

	public void setAssuntoTrfFilho(AssuntoTrf assuntoTrfFilho) {
		this.assuntoTrfFilho = assuntoTrfFilho;
	}

	public AssuntoTrf getAssuntoTrfFilho() {
		return assuntoTrfFilho;
	}

	public Date getDataAtual() {
		this.dataAtual = new Date();
		return dataAtual;
	}

	public void setDataAtual(Date dataAtual) {
		this.dataAtual = dataAtual;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	public AplicacaoClasse getAplicacaoClasse() {
		return aplicacaoClasse;
	}
}