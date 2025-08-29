package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.TransitoEmJulgado;

public abstract class IcrAssociarTransitoEmJulgadoAction<T extends InformacaoCriminalRelevante, J extends InformacaoCriminalRelevanteManager<T>>
		extends IcrBaseAction<T, J>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3137721006780515330L;

	public List<String> getPolos(){
		return getHome().getPolos();
	}

	public List<ProcessoParte> getPartesTodosOsPolos(){
		return getHome().getPartesTodosOsPolos();
	}

	public List<ProcessoParte> getPartesPoloAtivo(){
		return getHome().getPartesPoloAtivo();
	}

	public List<ProcessoParte> getPartesPoloPassivo(){
		return getHome().getPartesPoloPassivo();
	}

	public List<ProcessoParte> getPartesPoloTerceiros(){
		return getHome().getPartesPoloTerceiros();
	}

	/**
	 * default retorna os autores(polo ativo) e o réu da icr
	 * 
	 * @return
	 */
	public List<ProcessoParte> getPartesTransitoEmJulgado(){
		List<ProcessoParte> result = getPartesPoloAtivo();
		// adiciona o réu da icr
		if (getInstance().getProcessoParte() != null && !result.contains(getInstance().getProcessoParte())){
			result.add(getInstance().getProcessoParte());
		}
		return result;
	}

	public void removerTransitoEmJulgadoDaLista(TransitoEmJulgado icrTrans){
		getInstance().getTransitoEmJulgadoList().remove(icrTrans);
		// para o caso de já estar no banco só inativa:
		if (icrTrans.getId() != null){
			icrTrans.setAtivo(false);
			getInstance().getTransitoEmJulgadoList().add(icrTrans);
		}
	}

	public void colocarTransitoEmJulgadoNaLista(){
		getInstance().getTransitoEmJulgadoTemp().setAtivo(true);
		getInstance().getTransitoEmJulgadoTemp().setIcr(getInstance());
		try{
			getManager().validarTransitoEmJulgado(getInstance().getTransitoEmJulgadoTemp());
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), null);
			return;
		}
		if (getInstance().getTransitoEmJulgadoTemp().getId() == null){
			if (verificaDuplicidade(getInstance().getTransitoEmJulgadoTemp())){
				addMessage(Severity.ERROR, "transitoEmJulgado.registroDuplicado", null);
				return;
			}
		}
		adiciona(getInstance().getTransitoEmJulgadoTemp());
		getInstance().setTransitoEmJulgadoTemp(new TransitoEmJulgado());
	}

	private void adiciona(TransitoEmJulgado transito){
		if (getInstance().getTransitoEmJulgadoList().contains(transito))
			getInstance().getTransitoEmJulgadoList().remove(transito);
		getInstance().getTransitoEmJulgadoList().add(transito);
	}

	private boolean verificaDuplicidade(TransitoEmJulgado transito){
		for (TransitoEmJulgado icrTrans : getInstance().getTransitoEmJulgadoList()){
			if (icrTrans.getAtivo() && icrTrans.getIcr().equals(transito.getIcr())
				&& icrTrans.getProcessoParte().equals(transito.getProcessoParte())){
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private void getCopiaTransitoList(T instancia, List<TransitoEmJulgado> transitoList) throws Exception{
		if (transitoList != null && !transitoList.isEmpty()){
			List<TransitoEmJulgado> copiaList = new ArrayList<TransitoEmJulgado>();
			for (TransitoEmJulgado trans : transitoList){
				TransitoEmJulgado copia = new TransitoEmJulgado();
				PropertyUtils.copyProperties(copia, trans);
				copia.setIcr(instancia);
				copiaList.add(copia);
			}
			instancia.setTransitoEmJulgadoList(copiaList);
		}
	}
}
