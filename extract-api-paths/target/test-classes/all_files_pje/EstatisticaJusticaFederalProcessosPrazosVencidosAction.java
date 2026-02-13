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
import br.com.infox.cliente.component.tree.ClasseJudicialTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.EventoTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.EstatisticaProcessosPrazoVencidoBean;
import br.com.infox.pje.list.EstatisticaJusticaFederalProcessosPrazosVencidosList;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Classe action controladora do listView de /EstatisticaProcessoJusticaFederal/
 * ProcessosComPrazoVencido/
 * @author Laércio
 *
 */
@Name(value=EstatisticaJusticaFederalProcessosPrazosVencidosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaJusticaFederalProcessosPrazosVencidosAction implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "estatisticaJusticaFederalProcessosPrazosVencidosAction";
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcessoJusticaFederal/ProcessosComPrazoVencido/processosPrazosVencidosTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "ProcessosPrazosVencidos.xls";

	@In
	private ProcessoTrfManager processoTrfManager;
	@In
	private RelatorioLogManager relatorioLogManager;
	
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private ClasseJudicial classeJudicial;
	private List<ClasseJudicial> classeJudicialList = new ArrayList<ClasseJudicial>();
	private Evento evento;
	private List<Evento> eventoList = new ArrayList<Evento>();
	private String token;
	
	private EstatisticaJusticaFederalProcessosPrazosVencidosList estatisticaJusticaFederalProcessosPrazosVencidosList = 
		new EstatisticaJusticaFederalProcessosPrazosVencidosList();
	
	public static EstatisticaJusticaFederalProcessosPrazosVencidosAction instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
	private List<EstatisticaProcessosPrazoVencidoBean> getPrazosVencidosList(){
		return estatisticaJusticaFederalProcessosPrazosVencidosList.getResultList();
	}
	
	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta 
	 * retorne registros
	 * @param registros total de registros da consulta
	 */
	public void exportarProcessosPrazosVencidosXLS(){
		try {
			if(getPrazosVencidosList().size() > 0){
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
     * @param dirNomeTemplate Caminho com nome do template excel 
     * @param nomeArqDown Nome usado para download do arquivo  
     * @param nomeListaTemplate Nome da lista usada dentro do template
     * @param lista Lista com os dados a serem exportados
	 * @throws ExcelExportException 
     */
	public void exportarXLS(String dirNomeTemplate, String nomeArqDown) throws ExcelExportException {
		String urlTemplate = new Util().getContextRealPath() + dirNomeTemplate;
		ExcelExportUtil.downloadXLS(urlTemplate, beanExportarXLS(), nomeArqDown);
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<Map<String,Object>> getPrazosVencidosMapList(){
		List<Map<String, Object>> mapList = new ArrayList<Map<String,Object>>();
		for(EstatisticaProcessosPrazoVencidoBean bean: getPrazosVencidosList()){
			Map<String, Object> map = new HashedMap();
			map.put("processo", bean.getProcesso().toString());
			map.put("classe", bean.getClasse());
			map.put("fase", bean.getFase());
			map.put("dataExpiracao", bean.getDataExpiracao());
			map.put("diasVencido", bean.getDiasVencido());
			map.put("autorXreu", getAutorReu(bean.getProcessoTrf()));
			mapList.add(map);
		}
		return mapList;
	}
	
	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("processosPrazosVencidosBeanList", getPrazosVencidosMapList());
		map.put("titulo", Messages.instance().get("estatisticaProcessosComPrazoVencido.relatorio"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("total", getPrazosVencidosMapList().size());
		map.put("secao", secaoJudiciaria.getSecaoJudiciaria().toUpperCase());
		map.put("vara",orgaoJulgador);
		map.put("juiz", getJuizFederal());
		map.put("diretor", getDiretorVara());
		map.put("classe", getClasseJudicial() != null ? getClasseJudicial() : "");
		map.put("fase", getEvento() != null ? getEvento() : "");
		return map;
	}	
	
	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * @param registros quantidade de registros da lista
	 */
	public void gravarLogRelatorio(){
        if(getPrazosVencidosList().size() > 0){ 
        	relatorioLogManager.persist("Estatística de Processos com Prazos Vencidos", Authenticator.getUsuarioLogado());
        }	
	}
	
	public void createToken(){
		try {
			if(secaoJudiciaria.getUrlAplicacao() != null && !secaoJudiciaria.getUrlAplicacao().isEmpty()){
				token = TokenManager.instance().getRemoteToken(secaoJudiciaria.getUrlAplicacao()) ;
			}else{
				FacesMessages.instance().add(Severity.ERROR, MessageFormat.format(
						"URL da aplicação não está definida para a seção escolhida: {0}",
						secaoJudiciaria.getSecaoJudiciaria()));
			}
		} catch (IOException e) {
			String msgErro = MessageFormat.format(
					"URL do Webservice não esta acessivel no estado {0}: {1}",
					secaoJudiciaria.getSecaoJudiciaria(), e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, msgErro);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void validarToken(){
		TokenManager.instance().validateToken(token);
	}
	
	public SecaoJudiciaria getSecaoJudiciaria() {
		return secaoJudiciaria;
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

	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
	
	public List<ClasseJudicial> getClasseJudicialList() {
		classeJudicialList.clear();
		ClasseJudicialTreeHandler tree = ComponentUtil.getComponent("classeJudicialTree");
		for (ClasseJudicial classe : tree.getSelectedTree()) {
			classeJudicialList.add(classe);
		}
		if(classeJudicialList.isEmpty() && classeJudicial != null){
			classeJudicialList.add(classeJudicial);
		}		
		return classeJudicialList;
	}
	
	public void setClasseJudicialList(List<ClasseJudicial> classeJudicialList) {
		this.classeJudicialList = classeJudicialList;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public List<Evento> getEventoList() {
		eventoList.clear();
		EventoTreeHandler tree = ComponentUtil.getComponent("eventoTree");
		for (Evento evento : tree.getSelectedTree()) {
			eventoList.add(evento);
		}
		if(eventoList.isEmpty() && evento != null){
			eventoList.add(evento);
		}
		return eventoList;
	}

	public void setEventoList(List<Evento> eventoList) {
		this.eventoList = eventoList;
	}	

	/**
	 * Traz o nome do Diretor da Vara selecionada (Orgão Julgador)
	 * @return String com o nome
	 */
	public String getDiretorVara(){
		Usuario diretorVara = processoTrfManager.getDiretorVara(orgaoJulgador);
		return diretorVara != null ? diretorVara.getNome() : "";
	}

	/**
	 * Traz o nome do Juiz Federal da Vara selecionada (Orgão Julgador)
	 * @return String com o nome
	 */	
	public String getJuizFederal(){
		Usuario juizFederal = processoTrfManager.getJuizFederal(orgaoJulgador);
		return juizFederal != null ? juizFederal.getNome() : "";
	}	
	
	/**
	 * Traz o nome do 1º Autor x 1º Réu
	 * @return String com o nome
	 */	
	public String getAutorReu(ProcessoTrf processoTrf){
		String ar = processoTrfManager.primeiroAutorXprimeiroReu(processoTrf);
		return ar;
	}

	public void limparFiltros(){
		setOrgaoJulgador(null);
		setClasseJudicial(null);
		setEvento(null);
	}
	
	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}	
	
}