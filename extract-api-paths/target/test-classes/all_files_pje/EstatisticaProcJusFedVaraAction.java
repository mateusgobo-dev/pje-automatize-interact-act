package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
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
import br.com.infox.pje.bean.EstatisticaProcessoEntidadesVaraBean;
import br.com.infox.pje.list.EstatisticaProcJusFedVaraList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;

/**
 * Classe action controladora do listView de
 * /EstatisticaProcessoJusticaFederal/EstatisticaEntidadesVara/
 * 
 * @author Luiz Carlos Menezes
 * 
 */
@Name(value = EstatisticaProcJusFedVaraAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaProcJusFedVaraAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7400729924359525352L;

	public static final String NAME = "estatisticaProcJusFedVaraAction";

	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcessoJusticaFederal/EstatisticaEntidadesVara/estatisticaProcJusFedVara.xls";
	private static final String DOWNLOAD_XLS_NAME = "EstatisticaEntidadesVara.xls";

	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;
	@In
	private RelatorioLogManager relatorioLogManager;

	private EstatisticaProcJusFedVaraList procJusFedVaraList = new EstatisticaProcJusFedVaraList();
	private List<EstatisticaProcessoEntidadesVaraBean> estatisticaProcessoEntidadesVaraBeanList;
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private Pessoa pessoa;
	private long totalVara;
	private String token;
	
	private List<EstatisticaProcessoEntidadesVaraBean> getEstatisticaEntidadesVaraList() {
		return procJusFedVaraList.getResultList();
	}

	public List<EstatisticaProcessoEntidadesVaraBean> estatisticaProcessoEntidadesVaraBean() {
		if (estatisticaProcessoEntidadesVaraBeanList == null) {
			estatisticaProcessoEntidadesVaraBeanList = new ArrayList<EstatisticaProcessoEntidadesVaraBean>();
			return getProcJusFedVaraList().getResultList();
		}
		totalVara = estatisticaProcessoJusticaFederalManager.qtdProcessosVara(secaoJudiciaria.getCdSecaoJudiciaria(),
				orgaoJulgador, pessoa);
		return getProcJusFedVaraList().getResultList();
	}

	public void setProcJusFedVaraList(EstatisticaProcJusFedVaraList procJusFedVaraList) {
		this.procJusFedVaraList = procJusFedVaraList;
	}

	public EstatisticaProcJusFedVaraList getProcJusFedVaraList() {
		return procJusFedVaraList;
	}

	public static EstatisticaProcJusFedVaraAction instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * 
	 * @param registros
	 *            quantidade de registros da lista
	 */
	public void gravarLogRelatorio() {
		estatisticaProcessoEntidadesVaraBeanList = null;
		if (estatisticaProcessoEntidadesVaraBean().size() > 0) {
			relatorioLogManager.persist("Estatística de Entidades por Vara", Authenticator.getUsuarioLogado());
			if ((getOrgaoJulgador() == null) && (Authenticator.getOrgaoJulgadorAtual() != null)) {
				setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
			}
		}
	}

	/*
	 * Retorna o codigo da seção caso esteja no primeiro grau.
	 */
	public String secaoJudiciariaPrimeiroGrau() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			return ParametroUtil.instance().getSecao();
		}
		return null;
	}

	/*
	 * Retorna o orgão julgador atual caso esteja no primeiro grau.
	 */
	public OrgaoJulgador orgaoJulgadorAtualPrimeiroGrau() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			return Authenticator.getOrgaoJulgadorAtual();
		}
		return null;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		// [PJEII-1082] Tiago Zanon
		// impedir que ele altere a seção judiciária caso ela esteja setada
		// evitar exceção ao tentar usar getSingleResult() quando houver mais de 1 resultado 
		if (secaoJudiciaria == null) {
			if (ParametroUtil.instance().isPrimeiroGrau()) {
				SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaRelatorioEntidadesVaraItems");
				if (si.getResultCount() > 1) {
					secaoJudiciaria = (SecaoJudiciaria) si.getResultList().get(0);
				}
				else if (si.getResultCount() == 1) {
					secaoJudiciaria = (SecaoJudiciaria) si.getSingleResult();
				}
			}
		}
		return secaoJudiciaria;
	}

	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	
	/**
	 * Sobrecarga de {@link #setPessoa(Pessoa)} em razão de PJEII-2726.
	 * 
	 * @param pessoa a pessoa especializada a ser atribuída.
	 */
	public void setPessoa(PessoaFisicaEspecializada pessoa){
		setPessoa(pessoa != null ? pessoa.getPessoa() : (Pessoa) null);
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setTotalVara(long totalVara) {
		this.totalVara = totalVara;
	}

	public long getTotalVara() {
		return totalVara;
	}

	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta
	 * retorne registros
	 * 
	 * @param registros
	 *            total de registros da consulta
	 */
	public void exportarEntidadesVaraXLS() {
		try {
			if (getEstatisticaEntidadesVaraList().size() > 0) {
				exportarXLS(TEMPLATE_XLS_PATH, DOWNLOAD_XLS_NAME);
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

	@SuppressWarnings({ "unchecked" })
	private List<Map<String, Object>> getEstatisticaEntidadesVaraMapList() {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		for (EstatisticaProcessoEntidadesVaraBean bean : getEstatisticaEntidadesVaraList()) {
			Map<String, Object> map = new HashedMap();
			map.put("entidade", bean.getEntidade());
			map.put("totalProcEntidade", bean.getTotalProcEntidade());
			mapList.add(map);
		}
		return mapList;
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
			return items;
		} else if (secaoJudiciaria != null) {
			// [PJEII-1082] Tiago Zanon
			// como o IF acima faz referência direta a secaoJudiciaria, não há motivo para usar getSecaoJudiciaria() abaixo.
			for (OrgaoJulgador s : estatisticaProcessoJusticaFederalManager.buscaListaOrgaoJulgador(secaoJudiciaria.getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}
	
	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("estatisticaProcessoEntidadesVaraBeanList", getEstatisticaEntidadesVaraMapList());
		map.put("titulo", Messages.instance().get("estatisticaEntidadesPorVara.relatorioTitulo"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("secaoJudiciaria", "Seção Judiciária de " + getSecaoJudiciaria().getSecaoJudiciaria());
		map.put("total", totalVara);
		map.put("secao", secaoJudiciaria);
		map.put("vara", orgaoJulgador);
		if (getPessoa() != null) {
			map.put("entidade", getPessoa());
		}
		return map;
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

	public void limparFiltros() {
		setOrgaoJulgador(null);
		setPessoa((Pessoa) null);
	}

	public void validarToken() {
		TokenManager.instance().validateToken(token);
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}
}