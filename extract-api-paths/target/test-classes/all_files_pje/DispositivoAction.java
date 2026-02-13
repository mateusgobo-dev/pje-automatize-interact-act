package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.DispositivoRestClient;
import br.jus.cnj.pje.webservice.client.NormaRestClient;
import br.jus.cnj.pje.webservice.client.TipoDispositivoRestClient;
import br.jus.pje.nucleo.dto.DispositivoDTO;
import br.jus.pje.nucleo.dto.NormaDTO;
import br.jus.pje.nucleo.dto.TipoDispositivoDTO;

@Name(DispositivoAction.NAME)
@Scope(ScopeType.PAGE)
public class DispositivoAction extends BaseRestAction<DispositivoDTO> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "dispositivoAction";
	
	private static final int TIPO_D_ARTIGO = 1;
	private static final int TIPO_D_INCISO = 2;
	private static final int TIPO_D_PARAGRAFO = 3;
	private static final int TIPO_D_ALINEA = 4;

	@In(create = true)
	private DispositivoRestClient dispositivoRestClient;
	
	@In(create = true)
	private TipoDispositivoRestClient tipoDispositivoRestClient;
	
	@In(create = true)
	private NormaRestClient normaRestClient;

	private EntityRestDataModel<DispositivoDTO> dataModel;
	
	private List<TipoDispositivoDTO> listTipoDispositivoDto;
	
	private List<NormaDTO> listNormaDto;
	
	private List<DispositivoDTO> listDispositivosPaiDto;

	private Integer pageSize = 10;

	private Integer currentPage = 0;

	private String tab;

	private String homeName;
	
	private Integer idTipoDispositivo;
	private Integer idNorma;
	private Integer idTipoDispositivoPai;
	private Integer idDispositivoPai;
	
	private Integer idNormaArvore;
	private TreeNode<DispositivoDTO> noRaiz;
	private Boolean noSelecionado;
	private boolean arvoreVazia;

	@Create
	public void init() throws PJeRestException {
		listTipoDispositivoDto = tipoDispositivoRestClient.getResources();
		listNormaDto = normaRestClient.getResources();
		this.searchInstance = new DispositivoDTO();
		instance = new DispositivoDTO();
		this.pesquisar();
	}

	public List<DispositivoDTO> pesquisar() {
		this.dataModel = new EntityRestDataModel<DispositivoDTO>(this.page, this.facesContext, dispositivoRestClient,
				this.searchInstance);
		return null;
	}

	@Override
	protected BaseRestClient<DispositivoDTO> getRestClient() {
		return this.dispositivoRestClient;
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

	public void setDispositivoEdicao(DispositivoDTO dto) {
		idNorma = dto.getNorma().getId();
		idTipoDispositivo = dto.getTipoDispositivo().getId();
		if (dto.getDispositivoPai() != null) {
			idTipoDispositivoPai = dto.getDispositivoPai().getTipoDispositivo().getId();
			idDispositivoPai = dto.getDispositivoPai().getId();
		} else {
			idTipoDispositivoPai = null;
			idDispositivoPai = null;
		}
		setInstance(dto);
		filtrarDispositivosPai();
	}
	
	public void atualizarNorma(){
		NormaDTO norma = null;
		for(NormaDTO _norma : listNormaDto){
			if(idNorma.equals(_norma.getId())){
				norma = _norma;
				break;
			}
		}
		getInstance().setNorma(norma);
	}
	
	public void atualizarTipoDispositivo(){
		TipoDispositivoDTO tipoDispositivo = null;
		for(TipoDispositivoDTO _tipoDispositivo : listTipoDispositivoDto){
			if(idTipoDispositivo.equals(_tipoDispositivo.getId())){
				tipoDispositivo = _tipoDispositivo;
				break;
			}
		}
		getInstance().setTipoDispositivo(tipoDispositivo);
	}
	
	public void atualizarDispositivoPai(){
		if (idDispositivoPai != null) {
			DispositivoDTO dispositivo = null;
			for(DispositivoDTO _dispositivo : listDispositivosPaiDto){
				if(idDispositivoPai.equals(_dispositivo.getId())){
					dispositivo = _dispositivo;
					break;
				}
			}
			getInstance().setDispositivoPai(dispositivo);
		} else {
			getInstance().setDispositivoPai(null);
		}
	}
	
	@Override
	public void persist(boolean newInstance) {
		atualizarNorma();
		atualizarTipoDispositivo();
		atualizarDispositivoPai();
		atualizarSimbolo();
		atualizarIdentificador();
		super.persist(newInstance);
	}
	
	@Override
	public void update(boolean newInstance) {
		atualizarNorma();
		atualizarTipoDispositivo();
		atualizarDispositivoPai();
		atualizarSimbolo();
		atualizarIdentificador();
		super.update(newInstance);
	}

	private void atualizarIdentificador() {
		if (!isIdentificadorVazio()) {
			return;
		}
		switch (getInstance().getTipoDispositivo().getId()) {
		case TIPO_D_ARTIGO:
			getInstance().setIdentificador(extrairIdentificadorNumerico());
			break;
		case TIPO_D_INCISO:
			getInstance().setIdentificador(extrairIdentificadorNaoNumerico());
			break;
		case TIPO_D_PARAGRAFO:
			getInstance().setIdentificador(extrairParagrafo());
			break;
		case TIPO_D_ALINEA:
			getInstance().setIdentificador(extrairIdentificadorNaoNumerico());
			break;
		default:
			getInstance().setIdentificador("");
		}
	}
	
	private void atualizarSimbolo() {
		switch (getInstance().getTipoDispositivo().getId()) {
		case TIPO_D_ARTIGO:
			getInstance().setSimbolo("Art.");
			break;
		case TIPO_D_INCISO:
			getInstance().setSimbolo(null);
			break;
		case TIPO_D_PARAGRAFO:
			getInstance().setSimbolo("§");
			break;
		case TIPO_D_ALINEA:
			getInstance().setSimbolo(null);
			break;
		default:
			getInstance().setSimbolo("");
		}
	}
	
	private boolean isIdentificadorVazio() {
		return getInstance().getIdentificador() == null || getInstance().getIdentificador().isEmpty();
	}

	private String extrairIdentificadorNumerico() {
		String texto = getInstance().getTexto().trim();
		StringBuilder artigo = new StringBuilder();
		boolean emAnalise = false;
		for (int i=0; i<texto.length(); i++) {
			char caracter = texto.charAt(i);
			if (Character.isDigit(caracter)) {
				emAnalise = true;
				artigo.append(caracter);
			} else if (emAnalise) {
				break;
			}
		}
		return artigo.toString();
	}
	
	private String extrairIdentificadorNaoNumerico() {
		String texto = getInstance().getTexto().trim();
		StringBuilder artigo = new StringBuilder();
		boolean emAnalise = false;
		for (int i=0; i<texto.length(); i++) {
			char caracter = texto.charAt(i);
			if (Character.isLetter(caracter)) {
				emAnalise = true;
				artigo.append(caracter);
			} else if (emAnalise) {
				break;
			}
		}
		return artigo.toString();
	}
	
	private String extrairParagrafo() {
		String texto = getInstance().getTexto().trim().toLowerCase();
		if (texto.replace("á", "a").replace("ú", "u").startsWith("paragrafo unico")) {
			return "Parágrafo único";
		} else {
			return extrairIdentificadorNumerico();
		}
	}
	
	public void filtrarDispositivosPai() {
		listDispositivosPaiDto = (idNorma != null) ? 
				(idTipoDispositivoPai != null) ? dispositivoRestClient.recuperarDispositivosPorIdNormaIdTipoDispositivo(idNorma, idTipoDispositivoPai) :
				dispositivoRestClient.recuperarDispositivosPorIdNorma(idNorma) : null;
		if (getInstance().getId() != null && listDispositivosPaiDto != null) {
			for (Iterator<DispositivoDTO> iterator = listDispositivosPaiDto.iterator(); iterator.hasNext();) {
				DispositivoDTO dispositivoDTO = iterator.next();
				if (dispositivoDTO.getId().equals(getInstance().getId())) {
					iterator.remove();
					break;
				}
			}
		}
	}
	
	@Override
	public void onClickFormTab() {
		super.onClickFormTab();
		idTipoDispositivo = null;
		idNorma = null;
		idTipoDispositivoPai = null;
		idDispositivoPai = null;
		listDispositivosPaiDto = new ArrayList<>();
	}
	
	public void criarArvoreDispositivos() {
		noSelecionado = false;
		noRaiz = new TreeNodeImpl<>();
		arvoreVazia = false;
		if (idNormaArvore != null) {
			List<DispositivoDTO> dispositivos = dispositivoRestClient.recuperarDispositivosPorIdNorma(idNormaArvore);
			if (dispositivos != null) {
				for (DispositivoDTO dispositivoDTO : dispositivos) {
					if (dispositivoDTO.getDispositivoPai() == null) {
						TreeNodeImpl<DispositivoDTO> no = new TreeNodeImpl<>();
						no.setData(dispositivoDTO);
						noRaiz.addChild(dispositivoDTO.getId(), no);
						adicionarNoArvore(no, dispositivoDTO, dispositivos);
					}
				}
			} else {
				arvoreVazia = true;
			}
		}
	}
	
	private void adicionarNoArvore(TreeNodeImpl<DispositivoDTO> noPai, DispositivoDTO dispositivoPai, List<DispositivoDTO> dispositivos) {
		for (DispositivoDTO dispositivoDTO : dispositivos) {
			if (dispositivoDTO.getDispositivoPai() != null 
					&& dispositivoDTO.getDispositivoPai().getId().equals(dispositivoPai.getId())) {
				TreeNodeImpl<DispositivoDTO> noFilho = new TreeNodeImpl<>();
				noFilho.setData(dispositivoDTO);
				noPai.addChild(dispositivoDTO.getId(), noFilho);
				adicionarNoArvore(noFilho, dispositivoDTO, dispositivos);
			}
		}
	}

	public void selecionarNo(NodeSelectedEvent event) {
		noSelecionado = true;
        HtmlTree tree = (HtmlTree) event.getComponent();
        DispositivoDTO dispositivo = (DispositivoDTO) tree.getRowData();
        setDispositivoEdicao(dispositivo);
        setTab("form");
    }
	
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public EntityRestDataModel<DispositivoDTO> getDataModel() {
		return dataModel;
	}

	public void setDataModel(EntityRestDataModel<DispositivoDTO> dataModel) {
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

	public List<TipoDispositivoDTO> getListTipoDispositivoDto() {
		return listTipoDispositivoDto;
	}

	public void setListTipoDispositivoDto(List<TipoDispositivoDTO> listTipoDispositivoDto) {
		this.listTipoDispositivoDto = listTipoDispositivoDto;
	}

	public List<NormaDTO> getListNormaDto() {
		return listNormaDto;
	}

	public void setListNormaDto(List<NormaDTO> listNormaDto) {
		this.listNormaDto = listNormaDto;
	}

	public Integer getIdTipoDispositivo() {
		return idTipoDispositivo;
	}

	public void setIdTipoDispositivo(Integer idTipoDispositivo) {
		this.idTipoDispositivo = idTipoDispositivo;
	}

	public Integer getIdNorma() {
		return idNorma;
	}

	public void setIdNorma(Integer idNorma) {
		this.idNorma = idNorma;
	}
	
	public Integer getIdDispositivoPai() {
		return idDispositivoPai;
	}

	public void setIdDispositivoPai(Integer idDispositivoPai) {
		this.idDispositivoPai = idDispositivoPai;
	}
	
	public List<DispositivoDTO> getListDispositivosPaiDto() {
		return listDispositivosPaiDto;
	}

	public void setListDispositivosPaiDto(List<DispositivoDTO> listDispositivosPaiDto) {
		this.listDispositivosPaiDto = listDispositivosPaiDto;
	}
	
	public Integer getIdTipoDispositivoPai() {
		return idTipoDispositivoPai;
	}

	public void setIdTipoDispositivoPai(Integer idTipoDispositivoPai) {
		this.idTipoDispositivoPai = idTipoDispositivoPai;
	}
	
	public Integer getIdNormaArvore() {
		return idNormaArvore;
	}

	public void setIdNormaArvore(Integer idNormaArvore) {
		this.idNormaArvore = idNormaArvore;
	}

	public TreeNode<DispositivoDTO> getNoRaiz() {
		return noRaiz;
	}

	public void setNoRaiz(TreeNode<DispositivoDTO> noRaiz) {
		this.noRaiz = noRaiz;
	}

	public Boolean getNoSelecionado() {
		return noSelecionado;
	}

	public void setNoSelecionado(Boolean noSelecionado) {
		this.noSelecionado = noSelecionado;
	}

	public boolean isArvoreVazia() {
		return arvoreVazia;
	}

	public void setArvoreVazia(boolean arvoreVazia) {
		this.arvoreVazia = arvoreVazia;
	}

}
