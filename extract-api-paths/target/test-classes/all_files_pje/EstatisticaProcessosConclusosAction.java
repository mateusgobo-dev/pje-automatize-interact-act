package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.securitytoken.TokenManager;
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaProcessosConclusosBean;
import br.com.infox.pje.bean.EstatisticaProcessosConclusosTipoConclusaoBean;
import br.com.infox.pje.bean.EstatisticaProcessosSubListaTipoConclusosBean;
import br.com.infox.pje.list.EstatisticaProcessosConclusosList;
import br.com.infox.pje.list.EstatisticaProcessosConclusosTipoConclusaoList;
import br.com.infox.pje.list.EstatisticaProcessosSubListaTipoConclusosList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(value = EstatisticaProcessosConclusosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosConclusosAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7587884610878402286L;

	public static final String NAME = "estatisticaProcessosConclusosAction";

	private EstatisticaProcessosConclusosList estatisticaProcessosConclusosList = new EstatisticaProcessosConclusosList();
	private EstatisticaProcessosConclusosTipoConclusaoList estatisticaProcessosConclusosTipoConclusaoList = new EstatisticaProcessosConclusosTipoConclusaoList();
	private EstatisticaProcessosSubListaTipoConclusosList estatisticaProcessosSubListaTipoConclusosList = new EstatisticaProcessosSubListaTipoConclusosList();
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcessoJusticaFederal/ProcessosConclusos/estatisticaProcessosConclusos.xls";
	private static final String DOWNLOAD_XLS_NAME = "ProcessosConclusos.xls";

	@In
	private RelatorioLogManager relatorioLogManager;
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	@In
	private ProcessoTrfManager processoTrfManager;
	@In
	private transient OrgaoJulgadorManager orgaoJulgadorManager;

	private List<EstatisticaProcessosConclusosBean> estatisticaBeanList = new ArrayList<EstatisticaProcessosConclusosBean>();
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private Usuario juiz;
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
	private String dataInicioStr;
	private String dataFimStr;
	private Date dataInicio;
	private Date dataFim;
	private PessoaMagistrado pessoaMagistrado;
	private String codEvento;
	private Evento motivo;
	private String token;
	private Integer totalGeralConclusao = 0;
	private Map<String, Integer> mapTotalTipoConclusao = new HashMap<String, Integer>();
	private List<String> totalProcessosTipoConclusao = new ArrayList<String>();;

	public List<EstatisticaProcessosConclusosBean> estatisticaProcessosConclusosList() {
		totalProcessosTipoConclusao.clear();
		estatisticaBeanList = buildEstatisticaProcessosConclusosList();
		setCodEvento(null);
		return estatisticaBeanList;
	}

	/**
	 * Método que recebe uma data e transforma para "yyyy-MM-dd"
	 * 
	 * @param data
	 * @return
	 */
	public String formatarAnoMes(Date data) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(data);
	}

	private List<EstatisticaProcessosConclusosBean> buildEstatisticaProcessosConclusosList() {
		if (dataInicio != null) {
			dataInicioStr = formatarAnoMes(dataInicio);
			dataFimStr = formatarAnoMes(dataFim);
		}
		setPessoaMagistrado(null);
		List<EstatisticaProcessosConclusosBean> juizList = getEstatisticaProcessosConclusosList().getResultList();
		Integer temp = 0;
		totalGeralConclusao = 0;
		mapTotalTipoConclusao = new HashMap<String, Integer>();
		for (EstatisticaProcessosConclusosBean juizBean : juizList) {
			// Setando o filtro por juiz do
			// EstatisticaProcessosConclusosTipoConclusaoList
			setPessoaMagistrado(juizBean.getPessoaMagistrado());
			setCodEvento(null);
			List<EstatisticaProcessosConclusosTipoConclusaoBean> tipoConclusaoBeanList = getEstatisticaProcessosConclusosTipoConclusaoList()
					.getResultList();
			juizBean.setEstatisticaProcessosConclusosTipoConclusaoBeanList(tipoConclusaoBeanList);
			for (int i = 0; i < tipoConclusaoBeanList.size(); i++) {
				EstatisticaProcessosConclusosTipoConclusaoBean tipoConclusaoBean = tipoConclusaoBeanList.get(i);
				// Setando o filtro por tipo de conclusão do
				// EstatisticaProcessosConclusosTipoConclusaoList
				if (!tipoConclusaoBean.getCodEvento().equals(getCodEvento())) {
					setCodEvento(tipoConclusaoBean.getCodEvento());
					List<EstatisticaProcessosSubListaTipoConclusosBean> subListaTipoConclusaoList = getEstatisticaProcessosSubListaTipoConclusosList()
							.getResultList();
					for (EstatisticaProcessosSubListaTipoConclusosBean bean : subListaTipoConclusaoList) {
						bean.setAutorXreu(processoTrfManager.primeiroAutorXprimeiroReu(bean.getProcessoTrf()));
						totalGeralConclusao++;
					}
					tipoConclusaoBean.setEstatisticaProcessosSubListaTipoConclusosBean(subListaTipoConclusaoList);
				}
				tipoConclusaoBean.setDsEvento(retornaEvento(tipoConclusaoBean.getCodEvento()));

				temp = mapTotalTipoConclusao.get(tipoConclusaoBean.getDsEvento());
				if (temp == null) {
					temp = 0;
				}
				temp += tipoConclusaoBean.getQtdProcessos();
				mapTotalTipoConclusao.put(tipoConclusaoBean.getDsEvento(), temp);
			}
		}
		return juizList;
	}

	public void exportarEstatisticaProcessosConclusosXLS() {
		try {
			if (estatisticaBeanList.size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, DOWNLOAD_XLS_NAME);
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		}
	}

	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + dirNomeTemplate;
		ExcelExportUtil util = new ExcelExportUtil(urlTemplate, nomeArqDown);
		util.setBean(beanExportarXLS());
		util.download();
	}

	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("processosConclusosBean", estatisticaBeanList);
		map.put("titulo", Messages.instance().get("RELATÓRIO DE PROCESSOS CONCLUSOS ATÉ HOJE"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("secaoJudiciaria", getSecaoJudiciaria());
		map.put("orgaoJulgador", getOrgaoJulgador());
		for (Entry<String, Integer> entry : mapTotalTipoConclusao.entrySet()) {
			totalProcessosTipoConclusao.add("Total de Processos Conclusos para " + entry.getKey() + "    "
					+ Integer.toString(entry.getValue()));
		}
		map.put("totalProcessosTipoConclusao", totalProcessosTipoConclusao);
		map.put("totalGeralConclusao", "Total de Processos Conclusos    " + totalGeralConclusao);
		if (dataInicio != null && dataFim != null) {
			map.put("dataInicio", new SimpleDateFormat("dd/MM/yyyy").format(dataInicio));
			map.put("dataFim", new SimpleDateFormat("dd/MM/yyyy").format(dataFim));
		}
		map.put("classe", getClasseJudicial() != null ? getClasseJudicial() : "");
		map.put("juiz", getJuiz() != null ? getJuiz() : "");
		map.put("motivo", getMotivo() != null ? getMotivo() : "");
		return map;
	}

	/**
	 * Retorna todas as varas de uma determinada seção, caso o usauário tenha OJ
	 * retorna o dele.
	 * 
	 * @return
	 */
	public List<OrgaoJulgador> listOrgaoJulgadorItems() {
		List<OrgaoJulgador> items = new ArrayList<OrgaoJulgador>();
		if (ParametroUtil.instance().isPrimeiroGrau() && (Authenticator.getOrgaoJulgadorAtual() != null)) {
			items.add(Authenticator.getOrgaoJulgadorAtual());
			return items;
		} else {
			for (OrgaoJulgador s : estatisticaProcessoJusticaFederalManager
					.buscaListaOrgaoJulgador(getSecaoJudiciaria().getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}

	/**
	 * Retorna todas os juizes de uma determinada vara.
	 * 
	 * @return
	 */
	public List<Usuario> listJuizByOrgaoJulgadorItems() {
		List<Usuario> items = new ArrayList<Usuario>();
		for (Usuario u : orgaoJulgadorManager.buscaJuizesOJ(getOrgaoJulgador())) {
			items.add(u);
		}
		return items;
	}

	public String retornaEvento(final String codigoEvento) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ep.evento from Evento ep ");
		sb.append("where ep.codEvento = :evento");

		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("evento", codigoEvento);
		return query.getSingleResult().toString();
	}

	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * 
	 * @param registros
	 *            quantidade de registros da lista
	 */
	public void gravarLogRelatorio() {
		estatisticaBeanList = null;
		if (estatisticaProcessosConclusosList() != null) {
			relatorioLogManager.persist("Estatística de Processos Conclusos", Authenticator.getUsuarioLogado());
		}
	}

	/**
	 * Traz o nome do Diretor da Vara selecionada (Orgão Julgador)
	 * 
	 * @return String com o nome
	 */
	public String getDiretorVara() {
		Usuario diretorVara = processoTrfManager.getDiretorVara(orgaoJulgador);
		return diretorVara != null ? diretorVara.getNome() : "";
	}

	/**
	 * Traz o nome do Juiz Federal da Vara selecionada (Orgão Julgador)
	 * 
	 * @return String com o nome
	 */
	public String getJuizFederal() {
		Usuario juizFederal = processoTrfManager.getJuizFederal(orgaoJulgador);
		return juizFederal != null ? juizFederal.getNome() : "";
	}

	public void limparFiltros() {
		dataFim = null;
		dataInicio = null;
		dataFimStr = null;
		dataInicioStr = null;
		orgaoJulgador = null;
		classeJudicial = null;
		juiz = null;
		motivo = null;
		codEvento = null;
	}

	public void createToken() {
		try {
			if (secaoJudiciaria.getUrlAplicacao() != null && !secaoJudiciaria.getUrlAplicacao().isEmpty()) {
				token = TokenManager.instance().getRemoteToken(secaoJudiciaria.getUrlAplicacao());
			} else {
				FacesMessages.instance().add(
						Severity.ERROR,
						MessageFormat.format("URL da aplicação não está definida para a seção escolhida: {0}",
								secaoJudiciaria.getSecaoJudiciaria()));
			}
		} catch (IOException e) {
			String msgErro = MessageFormat.format("URL do Webservice não esta acessivel no estado {0}: {1}",
					secaoJudiciaria.getSecaoJudiciaria(), e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void validarToken() {
		TokenManager.instance().validateToken(token);
	}

	/*
	 * Inicio - Getters and Setters
	 */
	public void setEstatisticaProcessosConclusosList(EstatisticaProcessosConclusosList estatisticaProcessosConclusosList) {
		this.estatisticaProcessosConclusosList = estatisticaProcessosConclusosList;
	}

	public EstatisticaProcessosConclusosList getEstatisticaProcessosConclusosList() {
		return estatisticaProcessosConclusosList;
	}

	public EstatisticaProcessosConclusosTipoConclusaoList getEstatisticaProcessosConclusosTipoConclusaoList() {
		return estatisticaProcessosConclusosTipoConclusaoList;
	}

	public void setEstatisticaProcessosConclusosTipoConclusaoList(
			EstatisticaProcessosConclusosTipoConclusaoList estatisticaProcessosConclusosTipoConclusaoList) {
		this.estatisticaProcessosConclusosTipoConclusaoList = estatisticaProcessosConclusosTipoConclusaoList;
	}

	public EstatisticaProcessosSubListaTipoConclusosList getEstatisticaProcessosSubListaTipoConclusosList() {
		return estatisticaProcessosSubListaTipoConclusosList;
	}

	public void setEstatisticaProcessosSubListaTipoConclusosList(
			EstatisticaProcessosSubListaTipoConclusosList estatisticaProcessosSubListaTipoConclusosList) {
		this.estatisticaProcessosSubListaTipoConclusosList = estatisticaProcessosSubListaTipoConclusosList;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
			secaoJudiciaria = (SecaoJudiciaria) si.getSingleResult();
		}
		return secaoJudiciaria;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		if (ParametroUtil.instance().isPrimeiroGrau() && (Authenticator.getOrgaoJulgadorAtual() != null)) {
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public String getCodEvento() {
		return codEvento;
	}

	public void setCodEvento(String codEvento) {
		this.codEvento = codEvento;
	}

	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}

	public List<ClasseJudicial> getClasseJudicialList() {
		classeJudicialList.clear();
		ClasseJudicialTreeHandler tree = ComponentUtil.getComponent("classeJudicialTree");
		for (ClasseJudicial classe : tree.getSelectedTree()) {
			classeJudicialList.add(classe);
		}
		return classeJudicialList;
	}

	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public String getDataInicioStr() {
		return dataInicioStr;
	}

	public void setDataInicioStr(String dataInicioStr) {
		this.dataInicioStr = dataInicioStr;
	}

	public String getDataFimStr() {
		return dataFimStr;
	}

	public void setDataFimStr(String dataFimStr) {
		this.dataFimStr = dataFimStr;
	}

	public Usuario getJuiz() {
		return juiz;
	}

	public void setJuiz(Usuario juiz) {
		this.juiz = juiz;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public List<EstatisticaProcessosConclusosBean> getEstatisticaBeanList() {
		return estatisticaBeanList;
	}

	public void setEstatisticaBeanList(List<EstatisticaProcessosConclusosBean> estatisticaBeanList) {
		this.estatisticaBeanList = estatisticaBeanList;
	}

	public Evento getMotivo() {
		return motivo;
	}

	public void setMotivo(Evento motivo) {
		this.motivo = motivo;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public Integer getTotalGeralConclusao() {
		return totalGeralConclusao;
	}

	public void setTotalGeralConclusao(Integer totalGeralConclusao) {
		this.totalGeralConclusao = totalGeralConclusao;
	}

	public void setMapTotalTipoConclusao(Map<String, Integer> mapTotalTipoConclusao) {
		this.mapTotalTipoConclusao = mapTotalTipoConclusao;
	}

	public Map<String, Integer> getMapTotalTipoConclusao() {
		return mapTotalTipoConclusao;
	}

	public List<String> getTipoConclusaoList() {
		return new ArrayList<String>(mapTotalTipoConclusao.keySet());
	}

	public List<String> getTotalProcessosTipoConclusao() {
		return totalProcessosTipoConclusao;
	}

	public void setTotalProcessosTipoConclusao(List<String> totalProcessosTipoConclusao) {
		this.totalProcessosTipoConclusao = totalProcessosTipoConclusao;
	}

}