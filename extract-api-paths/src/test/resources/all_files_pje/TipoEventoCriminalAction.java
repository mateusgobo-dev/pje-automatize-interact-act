package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.TipoEventoCriminalRestClient;
import br.jus.pje.nucleo.beans.criminal.TipoProcessoEnum;
import br.jus.pje.nucleo.dto.TipoEventoCriminalDTO;

@Name(TipoEventoCriminalAction.NAME)
@Scope(ScopeType.PAGE)
public class TipoEventoCriminalAction extends BaseRestAction<TipoEventoCriminalDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoEventoCriminalAction";
	
	@In(create=true)
	private TipoEventoCriminalRestClient tipoEventoCriminalRestClient;
	
	private transient EntityRestDataModel<TipoEventoCriminalDTO> dataModel;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	@Create
	public void init(){
		this.searchInstance = new TipoEventoCriminalDTO();
		instance = new TipoEventoCriminalDTO();
		this.pesquisar();
	}
	
	public List<TipoEventoCriminalDTO> pesquisar(){
		this.dataModel = new EntityRestDataModel<>(this.page, this.facesContext, tipoEventoCriminalRestClient, this.searchInstance);		
		return new ArrayList<>();
	}
	
	@Override
	protected BaseRestClient<TipoEventoCriminalDTO> getRestClient() {
		return this.tipoEventoCriminalRestClient;
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
	
	public void setTipoEventoCriminalEdicao(TipoEventoCriminalDTO dto){
		setInstance(dto);	
	}
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<TipoEventoCriminalDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<TipoEventoCriminalDTO> dataModel) {
		this.dataModel = dataModel;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}
	
	public List<TipoProcessoEnum> getTiposProcessoItems() {
		return Arrays.asList(TipoProcessoEnum.values());
	}

}
