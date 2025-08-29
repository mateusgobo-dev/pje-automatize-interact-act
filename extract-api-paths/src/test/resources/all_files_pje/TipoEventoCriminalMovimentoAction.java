package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.ClientErrorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.component.Util;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRestException;
import br.jus.cnj.pje.webservice.client.BaseRestClient;
import br.jus.cnj.pje.webservice.client.TipoEventoCriminalMovimentoRestClient;
import br.jus.cnj.pje.webservice.client.TipoEventoCriminalRestClient;
import br.jus.pje.nucleo.dto.TipoEventoCriminalDTO;
import br.jus.pje.nucleo.dto.TipoEventoCriminalMovimentoDTO;
import br.jus.pje.nucleo.entidades.Evento;

@Name(TipoEventoCriminalMovimentoAction.NAME)
@Scope(ScopeType.PAGE)
public class TipoEventoCriminalMovimentoAction extends BaseRestAction<TipoEventoCriminalMovimentoDTO> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "tipoEventoCriminalMovimentoAction";
	
	@In(create = true)
	private TipoEventoCriminalMovimentoRestClient tipoEventoCriminalMovimentoRestClient;
	
	@In(create = true)
	private TipoEventoCriminalRestClient tipoEventoCriminalRestClient;
	
	private Integer pageSize = 10;

	private Integer currentPage = 0;

	private TipoEventoCriminalDTO dtoPesquisa;
	
	private Evento evento;
	
	private List<TipoEventoCriminalDTO> tiposEventoCriminal;
	
	private List<TipoEventoCriminalDTO> tiposEventoCriminalDisponiveis = new ArrayList<>();
	private List<TipoEventoCriminalDTO> tiposEventoCriminalSelecionados = new ArrayList<>();
	private List<TipoEventoCriminalDTO> tiposEventoCriminalSelecao = new ArrayList<>();

	private List<TipoEventoCriminalMovimentoDTO> tiposEventoCriminalMovimento;
	
	@Create
	public void init() throws PJeRestException {
		tiposEventoCriminal = tipoEventoCriminalRestClient.getResources();
	}
	
	public void inicializarSelecionados(Evento evento) {
		this.evento = evento;
		reiniciarListas();
		this.tiposEventoCriminalMovimento = tipoEventoCriminalMovimentoRestClient.recuperarPorCodMovimento(evento.getCodEvento());
		verificarTiposSelecionados();
	}

	private void verificarTiposSelecionados() {
		if (tiposEventoCriminalMovimento != null && !tiposEventoCriminalMovimento.isEmpty()) {
			for (TipoEventoCriminalMovimentoDTO tipoMovimento : tiposEventoCriminalMovimento) {
				tiposEventoCriminalSelecionados.add(tipoMovimento.getTipoEventoCriminal());
			}
			for (Iterator<TipoEventoCriminalDTO> iterator = tiposEventoCriminalDisponiveis.iterator(); iterator.hasNext();) {
				TipoEventoCriminalDTO tipo = iterator.next();
				for (TipoEventoCriminalDTO tipoSelecionado : tiposEventoCriminalSelecionados) {
					if (tipo.getCodTipoIc().equals(tipoSelecionado.getCodTipoIc())) {
						iterator.remove();
					}
				}
			}
		}
		tiposEventoCriminalSelecao.addAll(tiposEventoCriminalDisponiveis);
	}

	private void reiniciarListas() {
		tiposEventoCriminalDisponiveis.clear();
		tiposEventoCriminalSelecionados.clear();
		tiposEventoCriminalSelecao.clear();
		tiposEventoCriminalDisponiveis.addAll(tiposEventoCriminal);
	}

	public void addTipoEventoCriminal(TipoEventoCriminalDTO tipoEventoCriminal) throws ClientErrorException, PJeException {
		newInstance();
		getInstance().setCodMovimento(evento.getCodEvento());
		getInstance().setTipoEventoCriminal(tipoEventoCriminal);
		persist();
		inicializarSelecionados(evento);
	}
	
	public void removeTipoEventoCriminal(TipoEventoCriminalDTO tipoEventoCriminal) {
		for (TipoEventoCriminalMovimentoDTO dto : tiposEventoCriminalMovimento) {
			if (dto.getTipoEventoCriminal().getCodTipoIc().equals(tipoEventoCriminal.getCodTipoIc())) {
				remove(dto.getId());
				break;
			}
		}
		inicializarSelecionados(evento);
	}
	
	public void pesquisar() {
		String descricao = Util.instance().eval("tipoEventoCriminalSearch.descricao");
		if (descricao != null && !descricao.trim().isEmpty()) {
			descricao = descricao.toLowerCase();
			tiposEventoCriminalSelecao.clear();
			for (TipoEventoCriminalDTO dto : tiposEventoCriminalDisponiveis) {
				if (dto.getDescricao().toLowerCase().contains(descricao)) {
					tiposEventoCriminalSelecao.add(dto);
				}
			}
		} else {
			limparPesquisa();
		}
	}
	
	public void limparPesquisa() {
		tiposEventoCriminalSelecao.clear();
		tiposEventoCriminalSelecao.addAll(tiposEventoCriminalDisponiveis);
	}
	
	@Override
	protected BaseRestClient<TipoEventoCriminalMovimentoDTO> getRestClient() {
		return tipoEventoCriminalMovimentoRestClient;
	}

	@Override
	public Integer getPageSize() {
		return pageSize;
	}

	@Override
	public Integer getCurrentPage() {
		return currentPage;
	}

	public List<TipoEventoCriminalDTO> getTiposEventoCriminal() {
		return tiposEventoCriminal;
	}

	public void setTiposEventoCriminal(List<TipoEventoCriminalDTO> tiposEventoCriminal) {
		this.tiposEventoCriminal = tiposEventoCriminal;
	}

	public List<TipoEventoCriminalDTO> getTiposEventoCriminalDisponiveis() {
		return tiposEventoCriminalDisponiveis;
	}

	public void setTiposEventoCriminalDisponiveis(List<TipoEventoCriminalDTO> tiposEventoCriminalDisponiveis) {
		this.tiposEventoCriminalDisponiveis = tiposEventoCriminalDisponiveis;
	}

	public List<TipoEventoCriminalDTO> getTiposEventoCriminalSelecionados() {
		return tiposEventoCriminalSelecionados;
	}

	public void setTiposEventoCriminalSelecionados(List<TipoEventoCriminalDTO> tiposEventoCriminalSelecionados) {
		this.tiposEventoCriminalSelecionados = tiposEventoCriminalSelecionados;
	}

	public TipoEventoCriminalDTO getDtoPesquisa() {
		return dtoPesquisa;
	}

	public void setDtoPesquisa(TipoEventoCriminalDTO dtoPesquisa) {
		this.dtoPesquisa = dtoPesquisa;
	}

	public List<TipoEventoCriminalDTO> getTiposEventoCriminalSelecao() {
		return tiposEventoCriminalSelecao;
	}

	public void setTiposEventoCriminalSelecao(List<TipoEventoCriminalDTO> tiposEventoCriminalSelecao) {
		this.tiposEventoCriminalSelecao = tiposEventoCriminalSelecao;
	}
	
}
