package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.pje.nucleo.entidades.AcompanhamentoCondicaoSuspensao;
import br.jus.pje.nucleo.entidades.CondicaoSuspensao;
import br.jus.pje.nucleo.entidades.CondicaoSuspensaoAssociada;
import br.jus.pje.nucleo.entidades.IcrSuspensao;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.TipoSuspensao;

@Name("icrSuspensaoAction")
@Scope(ScopeType.CONVERSATION)
public class IcrSuspensaoAction extends
		InformacaoCriminalRelevanteAction<IcrSuspensao, IcrSuspensaoManager>{

	private static final long serialVersionUID = 8109808285537887790L;
	List<CondicaoSuspensao> condicaoSuspensaoList = new ArrayList<CondicaoSuspensao>(
			0);
	private String tab = null;
	private CondicaoSuspensaoAssociada condicaoSuspensaoAssociadaTemp = new CondicaoSuspensaoAssociada();

	private AcompanhamentoCondicaoSuspensao acompanhamentoCondicaoSuspensaoTemp = new AcompanhamentoCondicaoSuspensao();

	public TipoSuspensao getTipoSuspensao(){
		return getInstance().getTipoSuspensao();
	}

	public void setTipoSuspensao(TipoSuspensao tipo){
		boolean changed = tipo != null
			&& !tipo.equals(getInstance().getTipoSuspensao());
		if (changed){
			for (CondicaoSuspensaoAssociada cond : getInstance().getCondicaoSuspensaoAssociadaList()){
				cond.setAtivo(false);
			}
			// getInstance().getCondicaoSuspensaoAssociadaList().clear();
			setCondicaoSuspensaoDominio(getManager().getCondicaoSuspensaoList(getInstance()));
		}
		getInstance().setTipoSuspensao(tipo);
	}

	public List<CondicaoSuspensao> getCondicaoSuspensaoDominio(){
		if (getInstance() == null)
			return condicaoSuspensaoList;
		if ((condicaoSuspensaoList == null || condicaoSuspensaoList.isEmpty()) && getInstance().getTipoSuspensao() != null){
			condicaoSuspensaoList = getManager().getCondicaoSuspensaoList(getInstance());
		}
		return condicaoSuspensaoList;
	}

	public void setCondicaoSuspensaoDominio(List<CondicaoSuspensao> list){
		this.condicaoSuspensaoList = list;
	}

	/*
	 * ****** PERSISTÊNCIA *********************************************************
	 */
	public void adicionarCondicao(){
		if (getInstance() == null)
			return;
		FacesMessages.instance().clear();
		try{
			getCondicaoSuspensaoAssociadaTemp().setIcrSuspensao(getInstance());
			getCondicaoSuspensaoAssociadaTemp().setAtivo(true);
			getManager().validar(getCondicaoSuspensaoAssociadaTemp());// validação no banco
			if (getCondicaoSuspensaoAssociadaTemp().getId() == null){
				// verifica duplicidade na memória:
				for (CondicaoSuspensaoAssociada condMem : getCondicaoAssociadaListAtivos()){
					if (condMem.equals(getCondicaoSuspensaoAssociadaTemp())){
						throw new IcrValidationException("Condição já adicionada.");
					}
				}
				getInstance().getCondicaoSuspensaoAssociadaList().add(getCondicaoSuspensaoAssociadaTemp());
				newCondicaoSuspensaoAssociadaTemp();
			}
		} catch (IcrValidationException e){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getMessage());
		}
	}

	public void adicionarAcompanhamentoCondicao(){
		if (getInstance() == null)
			return;
		FacesMessages.instance().clear();
		try{
			getAcompanhamentoCondicaoSuspensaoTemp().setCondicaoSuspensaoAssociada(getCondicaoSuspensaoAssociadaTemp());
			// validação no banco antes de adicionar na lista
			getManager().validar(getAcompanhamentoCondicaoSuspensaoTemp());
			if (getAcompanhamentoCondicaoSuspensaoTemp().getId() == null){
				getAcompanhamentoCondicaoSuspensaoTemp().setAtivo(true);
				getAcompanhamentoCondicaoSuspensaoTemp().setNumeroTarefa(getNumeroNovaTarefa());
				for (CondicaoSuspensaoAssociada condicao : getInstance().getCondicaoSuspensaoAssociadaList()){
					if (condicao.equals(getAcompanhamentoCondicaoSuspensaoTemp().getCondicaoSuspensaoAssociada())){
						condicao.getAcompanhamentoCondicaoSuspensaoList().add(getAcompanhamentoCondicaoSuspensaoTemp());
					}
				}

			}
			getManager().persist(getInstance());
		} catch (IcrValidationException e){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getMessage());
		}
		newAcompanhamentoCondicaoSuspensaoTemp();
		organizaNumeroTarefa();
	}

	public void removerCondicao(CondicaoSuspensaoAssociada condicao){
		if (getInstance() == null)
			return;
		if (condicao.getId() != null){
			condicao.setAtivo(false);
		}
		else{
			getInstance().getCondicaoSuspensaoAssociadaList().remove(condicao);
		}
	}

	public void removerAcompanhamento(AcompanhamentoCondicaoSuspensao acompanhamento){
		if (getInstance() == null)
			return;
		if (acompanhamento.getId() != null){
			acompanhamento.setAtivo(false);
		}
		else{
			for (CondicaoSuspensaoAssociada condicao : getInstance().getCondicaoSuspensaoAssociadaList()){
				if (condicao.equals(acompanhamento.getCondicaoSuspensaoAssociada())){
					condicao.getAcompanhamentoCondicaoSuspensaoList().remove(acompanhamento);
				}
			}
		}
		try{
			getManager().persist(getInstance());
		} catch (IcrValidationException e){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getMessage());
		}
		organizaNumeroTarefa();
	}

	public List<CondicaoSuspensaoAssociada> getFiltraCondicaoAssociadaList(Boolean ativos, Boolean gravados){
		List<CondicaoSuspensaoAssociada> result = new ArrayList<CondicaoSuspensaoAssociada>();
		for (CondicaoSuspensaoAssociada item : getInstance()
				.getCondicaoSuspensaoAssociadaList()){
			if (ativos && !gravados && item.getAtivo()){
				result.add(item);
			}
			else if (!ativos && gravados && item.getId() != null){
				result.add(item);
			}
			else if (ativos && gravados && item.getAtivo() && item.getId() != null){
				result.add(item);
			}
			else if (!ativos && !gravados && !item.getAtivo() && item.getId() == null){
				result.add(item);
			}

		}
		return result;
	}

	public List<CondicaoSuspensaoAssociada> getCondicaoAssociadaListAtivos(){
		return getFiltraCondicaoAssociadaList(true, false);
	}

	public List<CondicaoSuspensaoAssociada> getCondicaoAssociadaListAtivoESalvo(){
		return getFiltraCondicaoAssociadaList(true, true);
	}

	public List<AcompanhamentoCondicaoSuspensao> getAcompanhamentosAtivos(){
		List<AcompanhamentoCondicaoSuspensao> result = new ArrayList<AcompanhamentoCondicaoSuspensao>(0);
		if (getCondicaoSuspensaoAssociadaTemp() == null)
			return result;
		for (CondicaoSuspensaoAssociada condicao : getInstance().getCondicaoSuspensaoAssociadaList()){
			if (condicao.getAtivo()){
				for (AcompanhamentoCondicaoSuspensao acompanhamento : condicao.getAcompanhamentoCondicaoSuspensaoList()){
					if (acompanhamento.getAtivo() && acompanhamento.getCondicaoSuspensaoAssociada().equals(getCondicaoSuspensaoAssociadaTemp()))
						result.add(acompanhamento);
				}
			}
		}
		return result;
	}

	public Integer getNumeroNovaTarefa(){
		List<AcompanhamentoCondicaoSuspensao> acompanhamentosAtivos = getAcompanhamentosAtivos();
		if (acompanhamentosAtivos.size() == 0)
			return 1;
		// return acompanhamentosAtivos.get(acompanhamentosAtivos.size() - 1)
		// .getNumeroTarefa() + 1;
		return acompanhamentosAtivos.size() + 1;
	}

	private void organizaNumeroTarefa(){
		int index = 1;
		for (AcompanhamentoCondicaoSuspensao acomp : getAcompanhamentosAtivos()){
			acomp.setNumeroTarefa(index);
			index++;
		}
		getAcompanhamentoCondicaoSuspensaoTemp().setNumeroTarefa(getNumeroNovaTarefa());
	}

	public boolean mostraPrazoSuspensao(){
		return mostraPrazoSuspensaoDia() || mostraPrazoSuspensaoMes()
			|| mostraPrazoSuspensaoAno();
	}

	public boolean mostraPrazoSuspensaoDia(){
		return getInstance().getTipoSuspensao() != null
			&& getInstance().getTipoSuspensao().getPrazoSuspencaoDia();
	}

	public boolean mostraPrazoSuspensaoMes(){
		return getInstance().getTipoSuspensao() != null
			&& getInstance().getTipoSuspensao().getPrazoSuspencaoMes();
	}

	public boolean mostraPrazoSuspensaoAno(){
		return getInstance().getTipoSuspensao() != null
			&& getInstance().getTipoSuspensao().getPrazoSuspencaoAno();
	}

	public boolean mostraDataPrevistaTermino(){
		return getInstance().getTipoSuspensao() != null
			&& getInstance().getTipoSuspensao().getDataPrevistaTermino();
	}

	public boolean mostraListaCondicoes(){
		return getInstance().getTipoSuspensao() != null
			&& !getInstance().getTipoSuspensao()
					.getCondicoesParaSuspensao().isEmpty();
	}

	public List<ProcessoParte> getListaReus(){
		return getManager().getListaReus(getHome().getProcessoTrf());
	}

	public List<TipoSuspensao> getTipoSuspensaoList(){
		return getManager().getTipoSuspensaoList(getInstance());
	}

	public boolean isPrazoSuspensaoObrigatorio(){
		if (getInstance().getTipoSuspensao() != null)
			return getInstance().getTipoSuspensao()
					.getPrazoSuspencaoObrigatorio();

		return false;
	}

	public CondicaoSuspensaoAssociada getCondicaoSuspensaoAssociadaTemp(){
		if (condicaoSuspensaoAssociadaTemp == null)
			newCondicaoSuspensaoAssociadaTemp();
		return condicaoSuspensaoAssociadaTemp;
	}

	public void newCondicaoSuspensaoAssociadaTemp(){
		setCondicaoSuspensaoAssociadaTemp(new CondicaoSuspensaoAssociada());
	}

	public void setCondicaoSuspensaoAssociadaTemp(
			CondicaoSuspensaoAssociada condicaoSuspensaoAssociada){
		this.condicaoSuspensaoAssociadaTemp = condicaoSuspensaoAssociada;
	}

	public AcompanhamentoCondicaoSuspensao getAcompanhamentoCondicaoSuspensaoTemp(){
		if (acompanhamentoCondicaoSuspensaoTemp.getNumeroTarefa() == null){
			acompanhamentoCondicaoSuspensaoTemp.setNumeroTarefa(getNumeroNovaTarefa());
		}
		return acompanhamentoCondicaoSuspensaoTemp;
	}

	public void setAcompanhamentoCondicaoSuspensaoTemp(
			AcompanhamentoCondicaoSuspensao acompanhamentoCondicaoSuspensao){
		this.acompanhamentoCondicaoSuspensaoTemp = acompanhamentoCondicaoSuspensao;
	}

	public void newAcompanhamentoCondicaoSuspensaoTemp(){
		acompanhamentoCondicaoSuspensaoTemp = new AcompanhamentoCondicaoSuspensao();
	}

	public String getTab(){
		return tab;
	}

	public void setTab(String tab){
		this.tab = tab;
	}

	public boolean exibirTabAcompanhamentoCondicaoSuspensao(){
		if (getInstance() == null)
			return false;
		boolean retorno = false;
		if (isManaged()){
			retorno = getInstance() instanceof IcrSuspensao
				&& (getInstance()).getTipoSuspensao() != null
				&& (getInstance()).getTipoSuspensao().getAcompanhamentoCondicao();
		}
		return retorno;
	}
}
