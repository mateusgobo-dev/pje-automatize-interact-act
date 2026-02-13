package br.jus.cnj.pje.view;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoProcedimentoOrigemRestClient;
import br.jus.pje.nucleo.dto.TipoProcedimentoOrigemDTO;

@Name(TipoProcedimentoOrigemAction.NAME)
@Scope(ScopeType.PAGE)
public class TipoProcedimentoOrigemAction extends BaseRestAction<TipoProcedimentoOrigemDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoProcedimentoOrigemAction";
	
	@In(create=true)
	private TipoProcedimentoOrigemRestClient tipoProcedimentoOrigemRestClient;
	
	private EntityRestDataModel<TipoProcedimentoOrigemDTO> dataModel;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	private  String homeName;
			
	@Create
	public void init(){
		this.searchInstance = new TipoProcedimentoOrigemDTO();
		instance = new TipoProcedimentoOrigemDTO();
		this.pesquisar();
	}
	
	public List<TipoProcedimentoOrigemDTO> pesquisar(){
		this.dataModel = new EntityRestDataModel<TipoProcedimentoOrigemDTO>(this.page, this.facesContext, tipoProcedimentoOrigemRestClient, this.searchInstance);		
		return null;
	}

	@Override
	protected BaseRestClient<TipoProcedimentoOrigemDTO> getRestClient() {
		return this.tipoProcedimentoOrigemRestClient;
	}

	@Override
	public Integer getPageSize() {
		return this.pageSize;
	}
	
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public Integer getCurrentPage() {
		return this.currentPage ;
	}
	
	public void setTipoProcedimentoOrigemEdicao(TipoProcedimentoOrigemDTO dto){
		setInstance(dto);	
	}
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<TipoProcedimentoOrigemDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<TipoProcedimentoOrigemDTO> dataModel) {
		this.dataModel = dataModel;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public String getHomeName() {
		return homeName;
	}

	public void setHomeName(String homeName) {
		this.homeName = homeName;
	}
	
}
