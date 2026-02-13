package br.jus.cnj.pje.view;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.TipoRecursoRestClient;
import br.jus.pje.nucleo.dto.TipoRecursoDTO;

@Name(TipoRecursoAction.NAME)
@Scope(ScopeType.PAGE)
public class TipoRecursoAction extends BaseRestAction<TipoRecursoDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoRecursoAction";

	@In(create = true)
	private TipoRecursoRestClient tipoRecursoRestClient;

	private EntityRestDataModel<TipoRecursoDTO> dataModel;

	private Integer pageSize = 10;

	private Integer currentPage = 0;

	private String tab;

	private String homeName;

	@Create
	public void init() throws PJeException {
		this.searchInstance = new TipoRecursoDTO();
		instance = new TipoRecursoDTO();
		this.pesquisar();
	}

	public List<TipoRecursoDTO> pesquisar() {
		this.dataModel = new EntityRestDataModel<TipoRecursoDTO>(this.page, this.facesContext, tipoRecursoRestClient,
				this.searchInstance);
		return null;
	}

	@Override
	protected BaseRestClient<TipoRecursoDTO> getRestClient() {
		return this.tipoRecursoRestClient;
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

	public void setTipoRecursoEdicao(TipoRecursoDTO dto) {
		setInstance(dto);
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public EntityRestDataModel<TipoRecursoDTO> getDataModel() {
		return dataModel;
	}

	public void setDataModel(EntityRestDataModel<TipoRecursoDTO> dataModel) {
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
