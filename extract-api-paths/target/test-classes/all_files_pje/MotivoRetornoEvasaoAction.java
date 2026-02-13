package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.MotivoRetornoEvasaoRestClient;
import br.jus.pje.nucleo.dto.MedidaSocioeducativaDTO;
import br.jus.pje.nucleo.dto.MotivoRetornoEvasaoDTO;

@Name(MotivoRetornoEvasaoAction.NAME)
@Scope(ScopeType.PAGE)
public class MotivoRetornoEvasaoAction extends BaseRestAction<MotivoRetornoEvasaoDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "motivoRetornoEvasaoAction";
	
	@In(create=true)
	private MotivoRetornoEvasaoRestClient motivoRetornoEvasaoRestClient;
	
	private transient EntityRestDataModel<MotivoRetornoEvasaoDTO> dataModel;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	@Create
	public void init(){
		this.searchInstance = new MotivoRetornoEvasaoDTO();
		instance = new MotivoRetornoEvasaoDTO();
		this.pesquisar();
	}
	
	public List<MedidaSocioeducativaDTO> pesquisar(){
		this.dataModel = new EntityRestDataModel<>(this.page, this.facesContext, motivoRetornoEvasaoRestClient, this.searchInstance);		
		return new ArrayList<>();
	}
	
	@Override
	protected BaseRestClient<MotivoRetornoEvasaoDTO> getRestClient() {
		return this.motivoRetornoEvasaoRestClient;
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
	
	public void setMotivoRetornoEvasaoEdicao(MotivoRetornoEvasaoDTO dto){
		setInstance(dto);	
	}
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<MotivoRetornoEvasaoDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<MotivoRetornoEvasaoDTO> dataModel) {
		this.dataModel = dataModel;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

}
