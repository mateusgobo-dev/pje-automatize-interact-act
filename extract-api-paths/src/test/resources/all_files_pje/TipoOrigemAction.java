package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ClientErrorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoOrigemRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoProcedimentoOrigemRestClient;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;
import br.jus.pje.nucleo.dto.TipoProcedimentoOrigemDTO;

@Name(TipoOrigemAction.NAME)
@Scope(ScopeType.PAGE)
public class TipoOrigemAction extends BaseRestAction<TipoOrigemDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoOrigemAction";
	
	@In(create=true)
	private TipoOrigemRestClient tipoOrigemRestClient;
	
	@In(create=true)
	private TipoProcedimentoOrigemRestClient tipoProcedimentoOrigemRestClient;
	
	private EntityRestDataModel<TipoOrigemDTO> dataModel;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	private  String homeName;
	
	private List<String> tipoProcedimentoOrigemList = new ArrayList<String>();
	
	private Map<String, TipoProcedimentoOrigemDTO> mapTipoProcedimentoOrigemDTO = new HashMap<String, TipoProcedimentoOrigemDTO>();
	
	@Create
	public void init() throws PJeRestException{
		this.searchInstance = new TipoOrigemDTO();
		instance = new TipoOrigemDTO();
		this.pesquisar();
		
		List<TipoProcedimentoOrigemDTO> list = tipoProcedimentoOrigemRestClient.getResources();
		
		for(TipoProcedimentoOrigemDTO dto : list){
			mapTipoProcedimentoOrigemDTO.put(dto.getId().toString(), dto);
		}
	}
	
	public List<TipoOrigemDTO> pesquisar(){
		this.dataModel = new EntityRestDataModel<TipoOrigemDTO>(this.page, this.facesContext, tipoOrigemRestClient, this.searchInstance);		
		return null;
	}

	public Map<String, TipoProcedimentoOrigemDTO> getMapTipoProcedimentoOrigemDTO() {
		return mapTipoProcedimentoOrigemDTO; 
	}

	public void setMapTipoProcedimentoOrigemDTO(
			Map<String, TipoProcedimentoOrigemDTO> mapTipoProcedimentoOrigemDTO) {
		this.mapTipoProcedimentoOrigemDTO = mapTipoProcedimentoOrigemDTO;
	}

	public List<String> getTipoProcedimentoOrigemList() {
		return tipoProcedimentoOrigemList;
	}

	public void setTipoProcedimentoOrigemList(
			List<String> tipoProcedimentoOrigemList) {
		this.tipoProcedimentoOrigemList = tipoProcedimentoOrigemList;
	}

	@Override
	protected BaseRestClient<TipoOrigemDTO> getRestClient() {
		return this.tipoOrigemRestClient;
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
	
	public void setTipoOrigemEdicao(TipoOrigemDTO dto){
		setInstance(dto);
		tipoProcedimentoOrigemList = new ArrayList<String>();
		for(TipoProcedimentoOrigemDTO tpo : dto.getTipoProcedimentoOrigemList()){
			tipoProcedimentoOrigemList.add(tpo.getId().toString());
		}		
	}
	
	public void salvarVinculacaoTipoProcedimento(){
		if(getInstance() != null){
			getInstance().getTipoProcedimentoOrigemList().clear();
			for(String key : tipoProcedimentoOrigemList){
				TipoProcedimentoOrigemDTO tpo = mapTipoProcedimentoOrigemDTO.get(key);
				if (tpo != null) {
					getInstance().getTipoProcedimentoOrigemList().add(tpo);
				}
			}	
		}	
		tipoProcedimentoOrigemList = new ArrayList<String>();
		
		if(getInstance().getId() != null){
			update(true);
		}else{
			persist(true);
		}
	}
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<TipoOrigemDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<TipoOrigemDTO> dataModel) {
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
