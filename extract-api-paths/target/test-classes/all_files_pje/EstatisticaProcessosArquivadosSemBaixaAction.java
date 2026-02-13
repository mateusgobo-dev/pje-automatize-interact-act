package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.securitytoken.TokenManager;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaProcessosArquivadosSemBaixaBean;
import br.com.infox.pje.list.EstatisticaProcessosArquivadosSemBaixaList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;

/**
 * Classe action controladora do listView de
 * /EstatisticaProcessoJusticaFederal/ProcessosArquivadosSemBaixa/
 * 
 * @author Silas Álvares
 * 
 */
@Name(value = EstatisticaProcessosArquivadosSemBaixaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcessosArquivadosSemBaixaAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessosArquivadosSemBaixaAction";

	private static final String TEMPLATE_SINTETICO_XLS_PATH = "/EstatisticaProcessoJusticaFederal/ProcessosArquivadosSemBaixa/processosArquivadosSemBaixaTemplate.xls";
	private static final String DOWNLOAD_SINTETICO_XLS_NAME = "ProcessosArquivadosSemBaixaSintetico.xls";

	private EstatisticaProcessosArquivadosSemBaixaList processosArquivadosSemBaixaList = new EstatisticaProcessosArquivadosSemBaixaList();
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private Date dataInicio;
	private Date dataFim;
	private Boolean sintetico = true;
	private Pessoa entidade;
	private boolean arquivados = false;
	private boolean arquivamento = true;
	private boolean suspensao = true;
	private boolean remessas = true;
	private List<EstatisticaProcessosArquivadosSemBaixaBean> processosArquivadosSemBaixaBeanList;
	private long totalGeral;
	private String token;

	@In
	private ProcessoTrfManager processoTrfManager;
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	@In
	private RelatorioLogManager relatorioLogManager;

	public List<EstatisticaProcessosArquivadosSemBaixaBean> getEstatisticaProcessosArquivadosSemBaixaList() {
		if (!arquivamento && !suspensao && !remessas) {
			totalGeral = 0;
			return null;
		} else {
			return buildEstatisticaProcessosArquivadosSemBaixaBean();
		}
	}

	public List<EstatisticaProcessosArquivadosSemBaixaBean> buildEstatisticaProcessosArquivadosSemBaixaBean() {
		SimpleDateFormat formatter = new SimpleDateFormat("DD/mm/yyyy");
		getProcessosArquivadosSemBaixaList().setArquivados(arquivados);
		getProcessosArquivadosSemBaixaList().setArquivamento(arquivamento);
		getProcessosArquivadosSemBaixaList().setSuspensao(suspensao);
		getProcessosArquivadosSemBaixaList().setRemessas(remessas);

		List<EstatisticaProcessosArquivadosSemBaixaBean> processosArquivadosSemBaixaList = getProcessosArquivadosSemBaixaList()
				.getResultList();
		totalGeral = 0;
		for (EstatisticaProcessosArquivadosSemBaixaBean bean : processosArquivadosSemBaixaList) {
			bean.setEntidade(getEntidadeProcesso(bean.getProcessoTrf()));
			bean.setPartes(processoTrfManager.primeiroAutorXprimeiroReu(bean.getProcessoTrf()));
			formatter.applyPattern("dd/MM/yyyy");
			bean.setDataEventoStr(formatter.format(bean.getDataEvento()));
			if (bean.getDataDesarquivamento() != null) {
				formatter.applyPattern("dd/MM/yyyy HH:mm");
				bean.setDataDesarquivamentoStr(formatter.format(bean.getDataDesarquivamento()));
			}
			totalGeral += 1;
		}
		processosArquivadosSemBaixaBeanList = processosArquivadosSemBaixaList;
		return processosArquivadosSemBaixaList;
	}

	public String getEntidadeProcesso(ProcessoTrf processoTrf) {
		StringBuilder sb = new StringBuilder();
		List<ProcessoParte> processoParteList = processoTrf.getProcessoParteList();
		for (ProcessoParte processoParte : processoParteList) {
			if (processoParte.getPessoa().getAtraiCompetencia()) {
				if (sb.length() != 0) {
					sb.append(" / ");
				}
				sb.append(processoParte.getPessoa().toString());
			}
		}

		return sb.toString();
	}

	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta
	 * retorne registros
	 * 
	 * @param registros
	 *            total de registros da consulta
	 */
	public void exportarPautaAudienciaXLS() {
		try {
			if (getEstatisticaProcessosArquivadosSemBaixaList().size() > 0) {
				exportarXLS(TEMPLATE_SINTETICO_XLS_PATH, DOWNLOAD_SINTETICO_XLS_NAME);
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (ExcelExportException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Método que exporta lits em planilhas do excel
	 * 
	 * @param dirNomeTemplate
	 *            Caminho com nome do template excel
	 * @param nomeArqDown
	 *            Nome usado para download do arquivo
	 * @param nomeListaTemplate
	 *            Nome da lista usada dentro do template
	 * @param lista
	 *            Lista com os dados a serem exportados
	 * @throws ExcelExportException
	 */
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + dirNomeTemplate;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), nomeArqDown);
	}

	private Map<String, Object> beanExportarXLS() {
		String arquidosExcel = "Não";
		String arquivamentoExcel = "Não";
		String suspensaoExcel = "Não";
		String remessasExcel = "Não";
		Map<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
		map.put("titulo", Messages.instance().get("estatisticaProcessosArquivadosSemBaixa.relatorioTitulo"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("local", getOrgaoJulgador());
		map.put("dataInicio", formatador.format(getDataInicio()));
		map.put("dataFim", formatador.format(getDataFim()));
		map.put("sintetico", getSintetico());
		map.put("processosArquivadosSemBaixaBeanList", getProcessosArquivadosSemBaixaBeanList());
		map.put("totalGeral", getTotalGeral());
		if (getArquivados()) {
			arquidosExcel = "Sim";
		}
		if (isArquivamento()) {
			arquivamentoExcel = "Sim";
		}
		if (isSuspensao()) {
			suspensaoExcel = "Sim";
		}
		if (isRemessas()) {
			remessasExcel = "Sim";
		}
		map.put("arquidosExcel", arquidosExcel);
		map.put("arquivamentoExcel", arquivamentoExcel);
		map.put("suspensaoExcel", suspensaoExcel);
		map.put("remessasExcel", remessasExcel);

		return map;
	}

	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 */
	public void gravarLogRelatorio() {
		if (getEstatisticaProcessosArquivadosSemBaixaList() != null
				&& getEstatisticaProcessosArquivadosSemBaixaList().size() > 0) {
			relatorioLogManager.persist("Estatística de Processos Arquivados sem Baixa",
					Authenticator.getUsuarioLogado());
		}
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

	/**
	 * Retorna todas as varas de uma determinada seção, caso o usuário tenha OJ
	 * retorna o dele.
	 * 
	 * @return
	 */
	public List<OrgaoJulgador> listOrgaoJulgadorItems() {
		List<OrgaoJulgador> items = new ArrayList<OrgaoJulgador>();
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			items.add(Authenticator.getOrgaoJulgadorAtual());
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
			return items;
		} else if (secaoJudiciaria != null) {
			for (OrgaoJulgador s : estatisticaProcessoJusticaFederalManager
					.buscaListaOrgaoJulgador(getSecaoJudiciaria().getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}

	public void limparLista() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secaoJudiciaria = null;
		}
		orgaoJulgador = null;
		dataInicio = null;
		dataFim = null;
		sintetico = true;
		arquivados = false;
		arquivamento = true;
		suspensao = true;
		remessas = true;
	}

	public void limparOpcao() {
		if (sintetico) {
			entidade = null;
		}
	}

	public void limparFases() {
		arquivamento = true;
		remessas = true;
		suspensao = true;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
			secaoJudiciaria = (SecaoJudiciaria) si.getSingleResult();
		}
		return secaoJudiciaria;
	}

	public EstatisticaProcessosArquivadosSemBaixaList getProcessosArquivadosSemBaixaList() {
		return processosArquivadosSemBaixaList;
	}

	public void setProcessosArquivadosSemBaixaList(
			EstatisticaProcessosArquivadosSemBaixaList processosArquivadosSemBaixaList) {
		this.processosArquivadosSemBaixaList = processosArquivadosSemBaixaList;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
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

	public Boolean getSintetico() {
		return sintetico;
	}

	public void setSintetico(Boolean sintetico) {
		this.sintetico = sintetico;
	}

	public Pessoa getEntidade() {
		return entidade;
	}

	public void setEntidade(Pessoa entidade) {
		this.entidade = entidade;
	}

	public boolean getArquivados() {
		return arquivados;
	}

	public void setArquivados(boolean arquivados) {
		this.arquivados = arquivados;
	}

	public boolean isArquivamento() {
		return arquivamento;
	}

	public void setArquivamento(boolean arquivamento) {
		this.arquivamento = arquivamento;
	}

	public boolean isSuspensao() {
		return suspensao;
	}

	public void setSuspensao(boolean suspensao) {
		this.suspensao = suspensao;
	}

	public boolean isRemessas() {
		return remessas;
	}

	public void setRemessas(boolean remessas) {
		this.remessas = remessas;
	}

	public List<EstatisticaProcessosArquivadosSemBaixaBean> getProcessosArquivadosSemBaixaBeanList() {
		return processosArquivadosSemBaixaBeanList;
	}

	public void setProcessosArquivadosSemBaixaBeanList(
			List<EstatisticaProcessosArquivadosSemBaixaBean> processosArquivadosSemBaixaBeanList) {
		this.processosArquivadosSemBaixaBeanList = processosArquivadosSemBaixaBeanList;
	}

	public long getTotalGeral() {
		return totalGeral;
	}

	public void setTotalGeral(long totalGeral) {
		this.totalGeral = totalGeral;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}