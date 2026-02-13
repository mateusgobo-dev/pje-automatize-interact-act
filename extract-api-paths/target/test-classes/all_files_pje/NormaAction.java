package br.jus.cnj.pje.view;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.NormaRestClient;
import br.jus.cnj.pje.webservice.client.TipoNormaRestClient;
import br.jus.pje.nucleo.dto.NormaDTO;
import br.jus.pje.nucleo.dto.TipoNormaDTO;

@Name(NormaAction.NAME)
@Scope(ScopeType.PAGE)
public class NormaAction extends BaseRestAction<NormaDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "normaAction";

	@In(create = true)
	private NormaRestClient normaRestClient;
	
	@In(create = true)
	private TipoNormaRestClient tipoNormaRestClient;

	private EntityRestDataModel<NormaDTO> dataModel;
	
	private List<TipoNormaDTO> listTipoNormaDto;

	private Integer pageSize = 10;

	private Integer currentPage = 0;

	private String tab;

	private String homeName;
	
	private Integer idTipoNorma;

	@Create
	public void init() throws PJeRestException {
		listTipoNormaDto = tipoNormaRestClient.getResources();
		this.searchInstance = new NormaDTO();
		instance = new NormaDTO();
		this.pesquisar();
	}

	public List<NormaDTO> pesquisar() {
		if (searchInstance.getSigla() != null && searchInstance.getSigla().trim().isEmpty()) {
			searchInstance.setSigla(null);
		}
		this.dataModel = new EntityRestDataModel<NormaDTO>(this.page, this.facesContext, normaRestClient,
				this.searchInstance);
		return null;
	}

	@Override
	protected BaseRestClient<NormaDTO> getRestClient() {
		return this.normaRestClient;
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
		return this.currentPage;
	}

	public void setNormaEdicao(NormaDTO dto) {
		idTipoNorma = dto.getTipoNorma().getId();
		setInstance(dto);
	}
	
	public void atualizarTipoNorma(){
		TipoNormaDTO tipoNorma = null;
		for(TipoNormaDTO tNorma : listTipoNormaDto){
			if(idTipoNorma.equals(tNorma.getId())){
				tipoNorma = tNorma;
				break;
			}
		}
		getInstance().setTipoNorma(tipoNorma);
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public EntityRestDataModel<NormaDTO> getDataModel() {
		return dataModel;
	}

	public void setDataModel(EntityRestDataModel<NormaDTO> dataModel) {
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

	public List<TipoNormaDTO> getListTipoNormaDto() {
		return listTipoNormaDto;
	}

	public void setListTipoNormaDto(List<TipoNormaDTO> listTipoNormaDto) {
		this.listTipoNormaDto = listTipoNormaDto;
	}

	public Integer getIdTipoNorma() {
		return idTipoNorma;
	}

	public void setIdTipoNorma(Integer idTipoNorma) {
		this.idTipoNorma = idTipoNorma;
	}

	@Override
	public void persist(boolean newInstance) {
		atualizarTipoNorma();
		super.persist(newInstance);
	}
	
	@Override
	public void update(boolean newInstance) {
		atualizarTipoNorma();
		super.update(newInstance);
	}

}
