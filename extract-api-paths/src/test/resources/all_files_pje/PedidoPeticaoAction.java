package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(PedidoPeticaoAction.NAME)
@Scope(ScopeType.EVENT)
public class PedidoPeticaoAction extends BaseAction<ProcessoDocumento> {
	
	private static final long serialVersionUID = 2685060657995380691L;

	public static final String NAME = "pedidoPeticaoAction";
		
	private EntityDataModel<ProcessoDocumento> processoDocumentoDataModel ;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	
	/**
	 * Variável destinada a armazenar o identificador do componente rich:tab selecionado.
	 */
	@RequestParameter(value="selectedTab")
	private String selectedTab;

	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}
	
	@Create
	public void init() {
		String numeroOrgaoJustica = ComponentUtil.getComponent(ParametroService.class).valueOf("numeroOrgaoJustica");
		if (numeroOrgaoJustica != null) {
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
		DataRetriever<ProcessoDocumento> dataRetriever = new PedidoPeticaoRetriever(ComponentUtil.getComponent(ProcessoDocumentoManager.class));
		this.processoDocumentoDataModel = new EntityDataModel<ProcessoDocumento>(ProcessoDocumento.class, this.facesContext, dataRetriever);
	}

	/**
	 * Método responsável por atribuir os filtros ao {@link EntityDataModel}.
	 */
	public void pesquisar() {
		try {
			this.processoDocumentoDataModel.setCriterias(getCriteriosTelaPesquisa());
		} catch (NoSuchFieldException e) {
			facesMessages.add(Severity.ERROR, "Há erro na definição da consulta: " + e.getLocalizedMessage());
		}
	}
	
	private List<Criteria> getCriteriosTelaPesquisa() {
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		if (numeroSequencia != null && numeroSequencia > 0) {
			criterios.add(Criteria.equals("processoTrf.numeroSequencia", numeroSequencia));
		}
		if (digitoVerificador != null && digitoVerificador > 0) {
			criterios.add(Criteria.equals("processoTrf.numeroDigitoVerificador", digitoVerificador));
		}
		if (ano != null && ano > 0) {
			criterios.add(Criteria.equals("processoTrf.ano", ano));
		}
		if (numeroOrigem != null && numeroOrigem > 0) {
			criterios.add(Criteria.equals("processoTrf.numeroOrigem", numeroOrigem));
		}
		if (StringUtils.isNotBlank(ramoJustica) && StringUtils.isNotBlank(respectivoTribunal)) {
			criterios.add(Criteria.equals("processoTrf.numeroOrgaoJustica", Integer.parseInt(ramoJustica + respectivoTribunal)));
		}
		return criterios;
	}
	
	/**
	 * Classe interna responsável pela pesquisa de ProcessoDocumento
	 * Essa classe implementa {@link DataRetriever}, para que seja possivel navegar entre os resultados de uma pesquisa (true pagination)
	 */
	private class PedidoPeticaoRetriever implements DataRetriever<ProcessoDocumento> {
		private static final long QTD_LISTA_VAZIA = 0;
		private ProcessoDocumentoManager manager;
		
		public PedidoPeticaoRetriever(ProcessoDocumentoManager processoDocumentoManager) {
			this.manager = processoDocumentoManager;
		}
		
		@Override
		public Object getId(ProcessoDocumento obj) {
			return manager.getId(obj);
		}

		@Override
		public ProcessoDocumento findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<ProcessoDocumento> list(Search search) {
			try {
				incluirUsuarioLogado(search);
				search.addOrder("o.dataJuntada", Order.DESC);
				search.addOrder("o.dataInclusao", Order.DESC);
				
				return manager.list(search);
			} catch (Exception e){
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar recuperar os registros: " + e.getLocalizedMessage());
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			try {
				incluirUsuarioLogado(search);
				return manager.count(search);
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar recuperar o número de registros: " + e.getLocalizedMessage());
				return QTD_LISTA_VAZIA;
			}
		}
		
		private void incluirUsuarioLogado(Search search) throws NoSuchFieldException {
			search.addCriteria(Criteria.equals("usuarioInclusao.idUsuario", Authenticator.getIdUsuarioLogado()));
		}
	}
	
	@Override
	protected BaseManager<ProcessoDocumento> getManager() {
		return ComponentUtil.getComponent(ProcessoDocumentoManager.class);
	}

	@Override
	public EntityDataModel<ProcessoDocumento> getModel() {
		return this.processoDocumentoDataModel;
	}
	
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}
	
	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}
	
	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}
	
	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}
	
	public Integer getAno() {
		return ano;
	}
	
	public void setAno(Integer ano) {
		this.ano = ano;
	}
	
	public String getRamoJustica() {
		return ramoJustica;
	}
	
	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}
	
	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}
	
	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}
	
	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}
	
}
