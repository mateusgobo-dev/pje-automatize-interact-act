package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import br.com.infox.pje.bean.EstatisticaPermissaoSegredoJusticaBean;
import br.com.infox.pje.bean.PermissaoSegredoJusticaListBean;
import br.com.infox.pje.list.EstatisticaPermissaoSegredoJusticaList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

/**
 * Classe action controladora do listView de /EstatisticaProcesso/JusticaFederal
 * PermissaoSegredoJustica/
 * 
 * @author thiago
 * 
 */
@Name(value = EstatisticaJusticaFederalPermissaoSegredoJusticaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaJusticaFederalPermissaoSegredoJusticaAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6913233668452152920L;

	public static final String NAME = "estatisticaJusticaFederalPermissaoSegredoJusticaAction";

	private EstatisticaPermissaoSegredoJusticaList permissaoSegredoJusticaList = new EstatisticaPermissaoSegredoJusticaList();
	private static final String TEMPLATE_XLS_TP01_PATH = "/EstatisticaProcessoJusticaFederal/PermissaoSegredoJustica/processosPermissaoSegredoJusticaTemplateTP01.xls";
	private static final String DOWNLOAD_XLS_TP01_NAME = "ProcessosPermissaoSegredoJusticaTP01.xls";
	private static final String TEMPLATE_XLS_TP02_PATH = "/EstatisticaProcessoJusticaFederal/PermissaoSegredoJustica/processosPermissaoSegredoJusticaTemplateTP02.xls";
	private static final String DOWNLOAD_XLS_TP02_NAME = "ProcessosPermissaoSegredoJusticaTP02.xls";
	private static final String TEMPLATE_XLS_TP03_PATH = "/EstatisticaProcessoJusticaFederal/PermissaoSegredoJustica/processosPermissaoSegredoJusticaTemplateTP03.xls";
	private static final String DOWNLOAD_XLS_TP03_NAME = "ProcessosPermissaoSegredoJusticaTP03.xls";

	private SecaoJudiciaria secao;
	private OrgaoJulgador orgaoJulgador;
	private UsuarioLogin usuario;
	private Processo processo;
	private List<EstatisticaPermissaoSegredoJusticaBean> estatisticaBeanList;
	private Boolean existeUsuario = Boolean.FALSE;
	private Boolean showUsuario = Boolean.TRUE;
	private Boolean showProcesso = Boolean.TRUE;
	private Integer totalProcessoVara = 0;
	private String token;

	@In
	private RelatorioLogManager relatorioLogManager;
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;

	public static EstatisticaJusticaFederalPermissaoSegredoJusticaAction instance() {
		return ComponentUtil.getComponent(NAME);
	}

	public Integer getTotalProcessoVara() {
		return totalProcessoVara;
	}

	public void setTotalProcessoVara(Integer totalProcessoVara) {
		this.totalProcessoVara = totalProcessoVara;
	}

	public List<EstatisticaPermissaoSegredoJusticaBean> estatisticaPermissaoSegredoJustica() {
		if (processo != null) {
			return estatisticaPermissaoSegredoJusticaByProcesso();
		} else {
			return estatisticaPermissaoSegredoJusticaByRequiredFieldsorUsuario();
		}
	}

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaPermissaoSegredoJusticaBean.java
	 * 
	 * @return lista com os dados dos Processos com Segredo de Justiça.
	 */
	public List<EstatisticaPermissaoSegredoJusticaBean> estatisticaPermissaoSegredoJusticaByRequiredFieldsorUsuario() {
		if (estatisticaBeanList == null) {
			estatisticaBeanList = new ArrayList<EstatisticaPermissaoSegredoJusticaBean>();
			List<Object[]> resultList = getPermissaoSegredoJusticaList().getResultList();
			resultList = invertList(resultList);
			if (resultList != null && resultList.size() > 0) {
				EstatisticaPermissaoSegredoJusticaBean epsjb = new EstatisticaPermissaoSegredoJusticaBean();
				estatisticaBeanList.add(epsjb);

				for (Object[] obj : resultList) {
					if (epsjb.getVara() == null) {
						epsjb.setVara(obj[1].toString());
					}

					Integer totalProcessosvara = epsjb.getTotalProcessosVara();
					epsjb.setTotalProcessosVara(totalProcessosvara == null ? 1 : totalProcessosvara + 1);
					this.totalProcessoVara = epsjb.getTotalProcessosVara();

					PermissaoSegredoJusticaListBean psjlb = new PermissaoSegredoJusticaListBean();
					psjlb.setProcesso(obj[0].toString());
					psjlb.setClasseJudicial(obj[2].toString());

					if (usuario != null) {
						psjlb.setSegredoJustica(existsSegredoJustica((ProcessoTrf) obj[0], usuario.getIdUsuario()));
						psjlb.setTextoSigiloso(existsTextoSigiloso((ProcessoTrf) obj[0], usuario.getIdUsuario()));
					}
					epsjb.getSegredoJusticaListBean().add(psjlb);
				}
			}
			estatisticaBeanList = invertList(estatisticaBeanList);
		}
		return estatisticaBeanList;
	}

	/**
	 * Obtem o relatório da estatística convertendo a lista proveniente do banco
	 * para uma lista no esquema da exibição necessária para a tela utilizando o
	 * EstatisticaPermissaoSegredoJusticaBean.java
	 * 
	 * @return lista com os dados dos Processos com Segredo de Justiça filtrando
	 *         por Processo.
	 */
	public List<EstatisticaPermissaoSegredoJusticaBean> estatisticaPermissaoSegredoJusticaByProcesso() {
		if (estatisticaBeanList == null) {
			estatisticaBeanList = new ArrayList<EstatisticaPermissaoSegredoJusticaBean>();
			List<Object[]> resultList = getPermissaoSegredoJusticaList().getResultList();
			resultList = invertList(resultList);
			if (resultList != null && resultList.size() > 0) {
				EstatisticaPermissaoSegredoJusticaBean epsjb = new EstatisticaPermissaoSegredoJusticaBean();
				estatisticaBeanList.add(epsjb);

				for (Object[] obj : resultList) {
					if (epsjb.getVara() == null) {
						epsjb.setVara(obj[1].toString());
					}

					List<UsuarioLogin> usuarios = listUsuario((ProcessoTrf) obj[0]);
					if (usuarios != null && usuarios.size() > 0) {
						epsjb.setUsuarios(usuarios);
						epsjb.setTotalUsuarios(usuarios.size());
						setExisteUsuario(true);
					} else {
						UsuarioLogin usu = new UsuarioLogin();
						usu.setNome("Não existe usuário.");
						epsjb.getUsuarios().add(usu);
					}

				}
			}
			estatisticaBeanList = invertList(estatisticaBeanList);
		}
		return estatisticaBeanList;
	}

	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta
	 * retorne registros
	 * 
	 * @param registros
	 *            total de registros da consulta
	 */
	public void exportarRelatorioXLS() {
		try {
			if (estatisticaBeanList.size() > 0) {
				if (usuario != null) {
					exportarXLS(TEMPLATE_XLS_TP02_PATH, DOWNLOAD_XLS_TP02_NAME);
				} else if (processo != null) {
					exportarXLS(TEMPLATE_XLS_TP03_PATH, DOWNLOAD_XLS_TP03_NAME);
				} else {
					exportarXLS(TEMPLATE_XLS_TP01_PATH, DOWNLOAD_XLS_TP01_NAME);
				}
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
		Map<String, Object> map = new HashMap<String, Object>();
		if (usuario != null) {
			map.put("usuario", "Usuário: " + usuario.getNome());
		}
		if (processo != null) {
			map.put("processo", "Processo: " + processo.getNumeroProcesso());
		}
		map.put("permissaoSegredoJusticaBeanList", estatisticaPermissaoSegredoJustica());
		map.put("titulo", Messages.instance().get("estatisticaJusticaFederalPermissaoSegredoJustica.relatorio"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("secaoJudiciaria", "Seção Judiciária de " + secao.getSecaoJudiciaria());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("vara", orgaoJulgador);
		return map;
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
		if (estatisticaPermissaoSegredoJustica().size() > 0) {
			relatorioLogManager.persist("Estatística de Permissão para Segredo de Justiça",
					Authenticator.getUsuarioLogado());
		}
	}

	/**
	 * Inverte a ordem da lista informada. Último vira primeiro e primeiro vira
	 * último.
	 * 
	 * @param <T>
	 * @param resultList
	 * @return
	 */
	private <T> List<T> invertList(List<T> resultList) {
		List<T> listaInvertida = new ArrayList<T>();
		for (int i = (resultList.size() - 1); i >= 0; i--) {
			listaInvertida.add(resultList.get(i));
		}
		return listaInvertida;
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
		} else if (secao != null) {
			for (OrgaoJulgador s : estatisticaProcessoJusticaFederalManager.buscaListaOrgaoJulgador(getSecao()
					.getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}

	public List<UsuarioLogin> listUsuario(ProcessoTrf processo) {
		List<UsuarioLogin> usuariosList = new ArrayList<UsuarioLogin>();
		for (UsuarioLogin usu : estatisticaProcessoJusticaFederalManager.buscaUsuariosVisibilidadeSegredo(processo)) {
			if (usu != null) {
				usuariosList.add(usu);
			}
		}
		return usuariosList;
	}

	public boolean existsSegredoJustica(ProcessoTrf processo, int idUsuario) {
		Processo proc = estatisticaProcessoJusticaFederalManager.buscaProcessoSegredoJustica(processo, idUsuario);
		if (proc != null) {
			return true;
		}
		return false;
	}

	public boolean existsTextoSigiloso(ProcessoTrf processo, int idUsuario) {
		Processo proc = estatisticaProcessoJusticaFederalManager.buscaProcessoTextoSigiloso(processo, idUsuario);
		if (proc != null) {
			return true;
		}
		return false;
	}

	public void createToken() {
		try {
			if (secao.getUrlAplicacao() != null && !secao.getUrlAplicacao().isEmpty()) {
				token = TokenManager.instance().getRemoteToken(secao.getUrlAplicacao());
			} else {
				FacesMessages.instance().add(
						Severity.ERROR,
						MessageFormat.format("URL da aplicação não está definida para a seção escolhida: {0}",
								secao.getSecaoJudiciaria()));
			}
		} catch (IOException e) {
			String msgErro = MessageFormat.format("URL do Webservice não esta acessivel no estado {0}: {1}",
					secao.getSecaoJudiciaria(), e.getMessage());
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
	public EstatisticaPermissaoSegredoJusticaList getPermissaoSegredoJusticaList() {
		return permissaoSegredoJusticaList;
	}

	public void setPermissaoSegredoJusticaList(EstatisticaPermissaoSegredoJusticaList permissaoSegredoJusticaList) {
		this.permissaoSegredoJusticaList = permissaoSegredoJusticaList;
	}

	public SecaoJudiciaria getSecao() {
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
			secao = (SecaoJudiciaria) si.getSingleResult();
		}
		return secao;
	}

	public void setSecao(SecaoJudiciaria secao) {
		this.secao = secao;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public UsuarioLogin getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioLogin usuario) {
		this.usuario = usuario;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public Boolean getExisteUsuario() {
		return existeUsuario;
	}

	public void setExisteUsuario(Boolean existeUsuario) {
		this.existeUsuario = existeUsuario;
	}

	public Boolean getShowUsuario() {
		return showUsuario;
	}

	public void setShowUsuario(Boolean showUsuario) {
		this.showUsuario = showUsuario;
	}

	public Boolean getShowProcesso() {
		return showProcesso;
	}

	public void setShowProcesso(Boolean showProcesso) {
		this.showProcesso = showProcesso;
	}

	public void limparFiltros() {
		if (!ParametroUtil.instance().isPrimeiroGrau()) {
			secao = null;
		}
		showUsuario = true;
		showProcesso = true;
		orgaoJulgador = null;
		estatisticaBeanList = null;
		usuario = null;
		processo = null;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}