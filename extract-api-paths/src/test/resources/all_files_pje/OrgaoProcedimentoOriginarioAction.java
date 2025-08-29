package br.jus.cnj.pje.view;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.criminal.OrgaoProcedimentoOriginarioRestClient;
import br.jus.cnj.pje.webservice.client.criminal.TipoOrigemRestClient;
import br.jus.pje.nucleo.dto.OrgaoProcedimentoOriginarioDTO;
import br.jus.pje.nucleo.dto.TipoOrigemDTO;
import br.jus.pje.nucleo.entidades.Cep;

@Name(OrgaoProcedimentoOriginarioAction.NAME)
@Scope(ScopeType.PAGE)
public class OrgaoProcedimentoOriginarioAction extends BaseRestAction<OrgaoProcedimentoOriginarioDTO>{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "orgaoProcedimentoOriginarioAction";
	
	@In(create=true)
	private OrgaoProcedimentoOriginarioRestClient orgaoProcedimentoOriginarioRestClient;	
	
	@In(create=true)
	private TipoOrigemRestClient tipoOrigemRestClient;
	
	private EntityRestDataModel<OrgaoProcedimentoOriginarioDTO> dataModel;
	
	private List<TipoOrigemDTO> listTipoOrigemDto;

	private Integer idTipoOrigem;
	
	private Integer pageSize = 10;
	
	private Integer currentPage = 0;
	
	private String tab;
	
	private  String homeName;
			
	@Create
	public void init() throws PJeRestException{
		listTipoOrigemDto = tipoOrigemRestClient.getResources();
		this.searchInstance = new OrgaoProcedimentoOriginarioDTO();
		instance = new OrgaoProcedimentoOriginarioDTO();
		this.pesquisar();
	}
	
	public void pesquisar(){
		this.dataModel = new EntityRestDataModel<OrgaoProcedimentoOriginarioDTO>(this.page, this.facesContext, orgaoProcedimentoOriginarioRestClient, this.searchInstance);		
	}

	@Override
	protected BaseRestClient<OrgaoProcedimentoOriginarioDTO> getRestClient() {
		return this.orgaoProcedimentoOriginarioRestClient;
	}

	public void atualizarDadosEndereco(){
		CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
		
		Cep cep = cepSuggestBean.getInstance();
		
		OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOriginarioDTO = getInstance();
		
		orgaoProcedimentoOriginarioDTO.getMunicipio().setUf(cep.getMunicipio().getEstado().getCodEstado());
		orgaoProcedimentoOriginarioDTO.getMunicipio().setMunicipio(cep.getMunicipio().getMunicipio());
		orgaoProcedimentoOriginarioDTO.getMunicipio().setCodigoIbge(cep.getMunicipio().getCodigoIbge());
		orgaoProcedimentoOriginarioDTO.setCep(cep.getNumeroCep());
		orgaoProcedimentoOriginarioDTO.setNmLogradouro(cep.getNomeLogradouro());
		orgaoProcedimentoOriginarioDTO.setNmBairro(cep.getNomeBairro());
		orgaoProcedimentoOriginarioDTO.setNmComplemento(cep.getComplemento());
		orgaoProcedimentoOriginarioDTO.setNmNumero(cep.getComplemento());
		
	}
	
	public void setOrgaoProcedimentoOriginarioEdicao(OrgaoProcedimentoOriginarioDTO dto){
		idTipoOrigem = dto.getTipoOrigem().getId();
		setInstance(dto);	
		salvarCep();	
	}
	
	public void atualizaTipoOrigem(){
		TipoOrigemDTO tipoOrigem= null;
		
		for(TipoOrigemDTO tOrigem :listTipoOrigemDto){
			if(idTipoOrigem.equals(tOrigem.getId())){
				tipoOrigem = tOrigem;
				break;
			}
		}
		getInstance().setTipoOrigem(tipoOrigem);
	}
	
	private void salvarCep(){
		CepService cepService = ComponentUtil.getComponent("cepService"); 
		if (getInstance().getCep() != null) {
			Cep cep = cepService.findByCodigo(getInstance().getCep());
			if(cep != null){
				CepSuggestBean cepSuggestBean = ComponentUtil.getComponent("cepSuggest");
				cepSuggestBean.setInstance(cep);
			}
		}
	}	
	
	@Override
	public void persist(boolean newInstance) {
		atualizaTipoOrigem();
		super.persist(newInstance);
	}
	
	@Override
	public void update(boolean newInstance) {
		atualizaTipoOrigem();
		super.update(newInstance);	
	};
	
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
	
	
	public List<TipoOrigemDTO> getListTipoOrigemDto() {
		return listTipoOrigemDto;
	}

	public void setListTipoOrigemDto(List<TipoOrigemDTO> listTipoOrigemDto) {
		this.listTipoOrigemDto = listTipoOrigemDto;
	}
	
	public Integer getIdTipoOrigem() {
		return idTipoOrigem;
	}

	public void setIdTipoOrigem(Integer idTipoOrigem) {
		this.idTipoOrigem = idTipoOrigem;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	
	public EntityRestDataModel<OrgaoProcedimentoOriginarioDTO> getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(EntityRestDataModel<OrgaoProcedimentoOriginarioDTO> dataModel) {
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
