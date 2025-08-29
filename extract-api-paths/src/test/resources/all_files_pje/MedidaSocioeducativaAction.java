package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.MedidaSocioeducativaRestClient;
import br.jus.pje.nucleo.dto.MedidaSocioeducativaDTO;

@Name(MedidaSocioeducativaAction.NAME)
@Scope(ScopeType.PAGE)
public class MedidaSocioeducativaAction extends BaseRestAction<MedidaSocioeducativaDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "medidaSocioeducativaAction";
	
	@In(create=true)
	private MedidaSocioeducativaRestClient medidaSocioeducativaRestClient;
	
	private transient EntityRestDataModel<MedidaSocioeducativaDTO> dataModel;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	@Create
	public void init(){
		this.searchInstance = new MedidaSocioeducativaDTO();
		instance = new MedidaSocioeducativaDTO();
		this.pesquisar();
	}
	
	public List<MedidaSocioeducativaDTO> pesquisar(){
		this.dataModel = new EntityRestDataModel<>(this.page, this.facesContext, medidaSocioeducativaRestClient, this.searchInstance);		
		return new ArrayList<>();
	}
	
	@Override
	protected BaseRestClient<MedidaSocioeducativaDTO> getRestClient() {
		return this.medidaSocioeducativaRestClient;
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
	
	public void setMedidaSocioeducativaEdicao(MedidaSocioeducativaDTO dto){
		setInstance(dto);	
	}
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<MedidaSocioeducativaDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<MedidaSocioeducativaDTO> dataModel) {
		this.dataModel = dataModel;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

}
