package br.jus.cnj.pje.view;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.criminal.UnidadePrisionalRestClient;
import br.jus.pje.nucleo.dto.UnidadePrisionalDTO;
import br.jus.pje.nucleo.entidades.Cep;

@Name(UnidadePrisionalAction.NAME)
@Scope(ScopeType.PAGE)
public class UnidadePrisionalAction extends BaseRestAction<UnidadePrisionalDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "unidadePrisionalAction";
	
	@In(create=true)
	private UnidadePrisionalRestClient unidadePrisionalRestClient;
	
	private EntityRestDataModel<UnidadePrisionalDTO> dataModel;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	private  String homeName;
			
	@Create
	public void init(){
		this.searchInstance = new UnidadePrisionalDTO();
		instance = new UnidadePrisionalDTO();
		this.pesquisar();
	}
	
	public List<UnidadePrisionalDTO> pesquisar(){
		this.dataModel = new EntityRestDataModel<UnidadePrisionalDTO>(this.page, this.facesContext, unidadePrisionalRestClient, this.searchInstance);		
		return null;
	}
	
	private void setCep(){
		CepService cepService = ComponentUtil.getComponent("cepService");
		if (getInstance().getNrCep() != null) {
			Cep cep = cepService.findByCodigo(getInstance().getNrCep());
			if(cep != null){
				CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
				cepSuggestBean.setInstance(cep);
			}
		}
	}	
	
	public void atualizarDadosEndereco(){
		CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
		
		Cep cep = cepSuggestBean.getInstance();
		
		UnidadePrisionalDTO unidadePrisionalDTO = getInstance();
		
		unidadePrisionalDTO.getMunicipio().setUf(cep.getMunicipio().getEstado().getCodEstado());
		unidadePrisionalDTO.getMunicipio().setMunicipio(cep.getMunicipio().getMunicipio());
		unidadePrisionalDTO.getMunicipio().setCodigoIbge(cep.getMunicipio().getCodigoIbge());
		unidadePrisionalDTO.setNrCep(cep.getNumeroCep());
		unidadePrisionalDTO.setNmLogradouro(cep.getNomeLogradouro());
		unidadePrisionalDTO.setNmBairro(cep.getNomeBairro());
		
	}
	@Override
	protected BaseRestClient<UnidadePrisionalDTO> getRestClient() {
		return this.unidadePrisionalRestClient;
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
	
	public void setUnidadePrisionalEdicao(UnidadePrisionalDTO dto){
		setInstance(dto);	
		setCep();
	}
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<UnidadePrisionalDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<UnidadePrisionalDTO> dataModel) {
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
