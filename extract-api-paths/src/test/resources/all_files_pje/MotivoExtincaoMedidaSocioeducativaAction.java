package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.MotivoExtincaoMedidaSocioeducativaRestClient;
import br.jus.pje.nucleo.dto.MedidaSocioeducativaDTO;
import br.jus.pje.nucleo.dto.MotivoExtincaoMedidaSocioeducativaDTO;

@Name(MotivoExtincaoMedidaSocioeducativaAction.NAME)
@Scope(ScopeType.PAGE)
public class MotivoExtincaoMedidaSocioeducativaAction extends BaseRestAction<MotivoExtincaoMedidaSocioeducativaDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "motivoExtincaoMedidaSocioeducativaAction";
	
	@In(create=true)
	private MotivoExtincaoMedidaSocioeducativaRestClient motivoExtincaoMedidaSocioeducativaRestClient;
	
	private transient EntityRestDataModel<MotivoExtincaoMedidaSocioeducativaDTO> dataModel;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	@Create
	public void init(){
		this.searchInstance = new MotivoExtincaoMedidaSocioeducativaDTO();
		instance = new MotivoExtincaoMedidaSocioeducativaDTO();
		this.pesquisar();
	}
	
	public List<MedidaSocioeducativaDTO> pesquisar(){
		this.dataModel = new EntityRestDataModel<>(this.page, this.facesContext, motivoExtincaoMedidaSocioeducativaRestClient, this.searchInstance);		
		return new ArrayList<>();
	}
	
	@Override
	protected BaseRestClient<MotivoExtincaoMedidaSocioeducativaDTO> getRestClient() {
		return this.motivoExtincaoMedidaSocioeducativaRestClient;
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
	
	public void setMotivoExtincaoMedidaSocioeducativaEdicao(MotivoExtincaoMedidaSocioeducativaDTO dto){
		setInstance(dto);	
	}
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<MotivoExtincaoMedidaSocioeducativaDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<MotivoExtincaoMedidaSocioeducativaDTO> dataModel) {
		this.dataModel = dataModel;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

}
