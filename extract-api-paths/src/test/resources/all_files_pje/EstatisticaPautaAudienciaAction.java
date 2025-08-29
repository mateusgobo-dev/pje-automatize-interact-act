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
import br.com.infox.pje.bean.EstatisticaPautaAudienciaBean;
import br.com.infox.pje.bean.EstatisticaPautaAudienciaSituacaoBean;
import br.com.infox.pje.list.EstatisticaPautaAudienciaList;
import br.com.infox.pje.manager.EstatisticaProcessoJusticaFederalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.manager.RelatorioLogManager;
import br.com.itx.component.SelectItemsQuery;
import br.com.itx.component.Util;
import br.com.itx.exception.ExcelExportException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.ExcelExportUtil;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.SecaoJudiciaria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;

/**
 * Classe action controladora do listView de /EstatisticaProcessoJusticaFederal/PautaAudiencia/
 * @author Luiz Carlos Menezes
 *
 */
@Name(value=EstatisticaPautaAudienciaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstatisticaPautaAudienciaAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaPautaAudienciaAction";
	
	private static final String TEMPLATE_XLS_PATH = "/EstatisticaProcessoJusticaFederal/PautaAudiencia/pautaAudienciaTemplate.xls";
	private static final String DOWNLOAD_XLS_NAME = "EstatisticaPautaAudiencia.xls";
	private static final String TEMPLATE_SITUACAO_XLS_PATH = "/EstatisticaProcessoJusticaFederal/PautaAudiencia/pautaAudienciaSituacaoTemplate.xls";
	private static final String DOWNLOAD_SITUACAO_XLS_NAME = "EstatisticaPautaAudienciaSituacao.xls";

	private EstatisticaPautaAudienciaList pautaAudienciaList = new EstatisticaPautaAudienciaList();
	private List<EstatisticaPautaAudienciaBean> estatisticaPautaAudienciaBeanList;
	private List<EstatisticaPautaAudienciaSituacaoBean> estatisticaPautaAudienciaSituacaoBeanList;
	private SecaoJudiciaria secaoJudiciaria;
	private OrgaoJulgador orgaoJulgador;
	private Pessoa juiz;
	private StatusAudienciaEnum statusAudienciaEnum;
	private Date dataInicio;
	private Date dataFim;
	private String dataInicioFormatada;
	private String dataFimFormatada;
	private boolean agruparSituacao;
	private long totalNumeroDepoimento;
	private String assuntoProcessoTrf;
	private long totalGeral;
	private String token;
	
	@In
	private ProcessoTrfManager processoTrfManager;
	@In
	private EstatisticaProcessoJusticaFederalManager estatisticaProcessoJusticaFederalManager;	
	@In
	private RelatorioLogManager relatorioLogManager;
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;	
	
	
	public List<EstatisticaPautaAudienciaBean> getEstatisticaPautaAudienciaList(){
		return buildEstatisticaPautaAudienciaBean();
	}
	
	public List<EstatisticaPautaAudienciaSituacaoBean> getEstatisticaPautaAudienciaSituacaoList() {
		return buildEstatisticaPautaAudienciaSituacaoBean();
	}

	private List<EstatisticaPautaAudienciaBean> buildEstatisticaPautaAudienciaBean(){
		List<EstatisticaPautaAudienciaBean> estatisticaPautaAudienciaList = getPautaAudienciaList().getResultList();
		if(estatisticaPautaAudienciaBeanList == null){
    		estatisticaPautaAudienciaBeanList = new ArrayList<EstatisticaPautaAudienciaBean>();
    	}
    	
    	totalGeral = 0;
		for (EstatisticaPautaAudienciaBean bean : estatisticaPautaAudienciaList) {
    		bean.setAutorXreu(processoTrfManager.primeiroAutorXprimeiroReu(bean.getProcessoTrf()));
    		totalGeral += bean.getTotalNumeroDepoimento();
    	}
		this.estatisticaPautaAudienciaBeanList = estatisticaPautaAudienciaList;
    	return estatisticaPautaAudienciaList;
    }
	
	public List<EstatisticaPautaAudienciaSituacaoBean> buildEstatisticaPautaAudienciaSituacaoBean() {
		List<EstatisticaPautaAudienciaSituacaoBean> estatisticaPautaAudienciaSituacaoList = new ArrayList<EstatisticaPautaAudienciaSituacaoBean>(); 
		List<EstatisticaPautaAudienciaBean> estatisticaPautaAudienciaList = buildEstatisticaPautaAudienciaBean();
		EstatisticaPautaAudienciaSituacaoBean estatisticaPautaAudienciaSituacaoBean;
		
		for (StatusAudienciaEnum statusAudiencia : StatusAudienciaEnum.values()) {
			estatisticaPautaAudienciaSituacaoBean = new EstatisticaPautaAudienciaSituacaoBean();
			estatisticaPautaAudienciaSituacaoBean.setStatusAudienciaEnum(statusAudiencia);
			for (EstatisticaPautaAudienciaBean estatisticaPautaAudiencia : estatisticaPautaAudienciaList) {
				if (estatisticaPautaAudiencia.getStatusAudienciaEnum() == statusAudiencia) {
					estatisticaPautaAudienciaSituacaoBean.getEstatisticaPautaAudienciaBeanList().add(estatisticaPautaAudiencia);
				}
			}
			if (estatisticaPautaAudienciaSituacaoBean.getEstatisticaPautaAudienciaBeanList().size() != 0) {
				estatisticaPautaAudienciaSituacaoList.add(estatisticaPautaAudienciaSituacaoBean);
			}
		}
		this.estatisticaPautaAudienciaSituacaoBeanList = estatisticaPautaAudienciaSituacaoList;
		return this.estatisticaPautaAudienciaSituacaoBeanList;
	}
	
	/**
	 * Método que grava o log das consultas de relarório caso a consulta retorne
	 * registros.
	 * @param registros quantidade de registros da lista
	 */
	public void gravarLogRelatorio(){
		estatisticaPautaAudienciaBeanList = null;
        if(getEstatisticaPautaAudienciaList().size() > 0){ 
        	relatorioLogManager.persist("Relatório de Pauta de Audiências", Authenticator.getUsuarioLogado());
        }	
	}
	
	public static EstatisticaPautaAudienciaAction instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
	public void setSecaoJudiciaria(SecaoJudiciaria secaoJudiciaria) {
		this.secaoJudiciaria = secaoJudiciaria;
	}

	public SecaoJudiciaria getSecaoJudiciaria() {
		if(ParametroUtil.instance().isPrimeiroGrau()){
			SelectItemsQuery si = ComponentUtil.getComponent("secaoJudiciariaItems");
			secaoJudiciaria = (SecaoJudiciaria) si.getSingleResult();
		}
		return secaoJudiciaria;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		if (orgaoJulgador == null) {
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		return orgaoJulgador;
	}

	/**
	 * Método que retorna o nome do Juiz para parecer no PDF e Excel.
	 * @return
	 */
	public String retornaJuiz(){
		if (getJuiz() != null) {
			return getJuiz().getNome();
		}
		return "";
	}
	
	
	/**
	 * Método que retorna a descrição da Situação para parecer no PDF e Excel.
	 * @return
	 */
	public String retornaStatusAudienciaEnum(){
		if (getStatusAudienciaEnum() != null) {
			return getStatusAudienciaEnum().getLabel();
		}
		return "";
	}
	
	/**
	 * Método que exporta o resultado da consulta para excel, caso a consulta 
	 * retorne registros
	 * @param registros total de registros da consulta
	 */
	public void exportarPautaAudienciaXLS(){
		try {
			if(getEstatisticaPautaAudienciaList().size() > 0){
			  exportarXLS(TEMPLATE_XLS_PATH, DOWNLOAD_XLS_NAME);
			} else {
				FacesMessages.instance().add(Severity.INFO, "Não há dados para exportar!");
			}
		} catch (ExcelExportException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao exportar arquivo." + e.getMessage());
			e.printStackTrace();
		} 
	}
	
	public void limparFiltros(){
		setJuiz(null);
		setStatusAudienciaEnum(null);
		setOrgaoJulgador(null);
		setDataFim(null);
		setDataInicio(null);
		setAgruparSituacao(false);
	}
	
	/**
	 * Método que recebe uma data e transforma para "yyyy-MM-dd"
	 * @param data
	 * @return
	 */
	public String formatarAnoMes(Date data) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(data);
	}
	
	/**
	 * Método que exporta o resultado da consulta agrupada por situação para excel, caso a consulta 
	 * retorne registros
	 * @param registros total de registros da consulta
	 */
	public void exportarPautaAudienciaSituacaoXLS() {
		try {
			if(getEstatisticaPautaAudienciaSituacaoList().size() > 0){
			  exportarXLS(TEMPLATE_SITUACAO_XLS_PATH, DOWNLOAD_SITUACAO_XLS_NAME);
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

	private Map<String, Object> beanExportarXLS() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("titulo", Messages.instance().get("estatisticaPautaAudiencia.relatorio"));
		map.put("subNomeSistema", ParametroUtil.getParametro("nomeSecaoJudiciaria").toUpperCase());
		map.put("nomeSistema", ParametroUtil.getParametro("nomeSistema"));
		map.put("local", getOrgaoJulgador());
		map.put("dataInicio",getDataInicioFormatada());
		map.put("dataFim", getDataFimFormatada());
		map.put("totalGeral", getTotalGeral());
		map.put("juiz", retornaJuiz());
		map.put("situacao", retornaStatusAudienciaEnum());
		map.put("secao", getSecaoJudiciaria().getSecaoJudiciaria().toUpperCase());
		
		if (agruparSituacao) {
			map.put("estatisticaPautaAudienciaSituacaoBeanList", getEstatisticaPautaAudienciaSituacaoList());
			map.put("agrupar", "Sim");
		} else {
			map.put("estatisticaPautaAudienciaBeanList", getEstatisticaPautaAudienciaList());
			map.put("agrupar", "Não");
		}
		return map;
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
	
	/**
	 * Retorna todas as varas de uma determinada seção, caso o usuário tenha OJ retorna o dele.
	 * @return
	 */
	public List<OrgaoJulgador> listOrgaoJulgadorItems() {
		
		List<OrgaoJulgador> items = new ArrayList<OrgaoJulgador>();
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			items.add(Authenticator.getOrgaoJulgadorAtual());
			return items;
		} else if(secaoJudiciaria != null) {
			for(OrgaoJulgador s : estatisticaProcessoJusticaFederalManager.buscaListaOrgaoJulgador(getSecaoJudiciaria().getCdSecaoJudiciaria())) {
				if (s != null) {
					items.add(s);
				}
			}
		}
		return items;
	}
	
	/**
	 * Retorna todos os juizes.
	 * @return
	 */
	public List<Usuario> listJuizPorOrgaoJulgadorItems() {
		List<Usuario> items = new ArrayList<Usuario>();
		for(Usuario u : usuarioLocalizacaoMagistradoServidorManager.juizes()) {
			items.add(u);
		}
		return items;
	}	
	
	public void setPautaAudienciaList(EstatisticaPautaAudienciaList pautaAudienciaList) {
		this.pautaAudienciaList = pautaAudienciaList;
	}

	public EstatisticaPautaAudienciaList getPautaAudienciaList() {
		return pautaAudienciaList;
	}

	public void setJuiz(Pessoa juiz) {
		this.juiz = juiz;
	}

	public Pessoa getJuiz() {
		return juiz;
	}

	public void setStatusAudienciaEnum(StatusAudienciaEnum statusAudienciaEnum) {
		this.statusAudienciaEnum = statusAudienciaEnum;
	}

	public StatusAudienciaEnum getStatusAudienciaEnum() {
		return statusAudienciaEnum;
	}
	
	public StatusAudienciaEnum[] getStatusAudienciaEnumValues() {
		return StatusAudienciaEnum.values();
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setAgruparSituacao(boolean agruparSituacao) {
		this.agruparSituacao = agruparSituacao;
	}

	public boolean getAgruparSituacao() {
		return agruparSituacao;
	}

	public void setTotalNumeroDepoimento(long totalNumeroDepoimento) {
		this.totalNumeroDepoimento = totalNumeroDepoimento;
	}

	public long getTotalNumeroDepoimento() {
		return totalNumeroDepoimento;
	}

	public String getAssuntoProcessoTrf() {
		return assuntoProcessoTrf;
	}

	public void setAssuntoProcessoTrf(String assuntoProcessoTrf) {
		this.assuntoProcessoTrf = assuntoProcessoTrf;
	}

	public long getTotalGeral() {
		return totalGeral;
	}

	public void setTotalGeral(long totalGeral) {
		this.totalGeral = totalGeral;
	}

	public List<EstatisticaPautaAudienciaBean> getEstatisticaPautaAudienciaBeanList() {
		return estatisticaPautaAudienciaBeanList;
	}

	public void setEstatisticaPautaAudienciaBeanList(
			List<EstatisticaPautaAudienciaBean> estatisticaPautaAudienciaBeanList) {
		this.estatisticaPautaAudienciaBeanList = estatisticaPautaAudienciaBeanList;
	}

	public List<EstatisticaPautaAudienciaSituacaoBean> getEstatisticaPautaAudienciaSituacaoBeanList() {
		return estatisticaPautaAudienciaSituacaoBeanList;
	}

	public void setEstatisticaPautaAudienciaSituacaoBeanList(
			List<EstatisticaPautaAudienciaSituacaoBean> estatisticaPautaAudienciaSituacaoBeanList) {
		this.estatisticaPautaAudienciaSituacaoBeanList = estatisticaPautaAudienciaSituacaoBeanList;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public String getDataInicioFormatada() {
		dataInicioFormatada = formatarAnoMes(getDataInicio());
		return dataInicioFormatada;
	}

	public void setDataInicioFormatada(String dataInicioFormatada) {
		this.dataInicioFormatada = dataInicioFormatada;
	}

	public String getDataFimFormatada() {
		dataFimFormatada = formatarAnoMes(getDataFim());
		return dataFimFormatada;
	}

	public void setDataFimFormatada(String dataFimFormatada) {
		this.dataFimFormatada = dataFimFormatada;
	}
	
}