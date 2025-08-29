package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.cnj.pje.business.dao.MandadoAlvaraDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.MandadoAlvaraManager;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.MandadoAlvara;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoProcedimentoOrigem;
import br.jus.pje.nucleo.entidades.TipificacaoDelito;

public abstract class PrepararMandadoAlvaraAction<E extends MandadoAlvara, M extends MandadoAlvaraManager<E, ? extends MandadoAlvaraDAO<E>>>
extends PreparaExpedienteCriminalAction<E, M> {

	private static final long serialVersionUID = -4156733818252968136L;
	private List<AssuntoTrf> assuntos = new ArrayList<AssuntoTrf>(0);

	@Override
	public M getManager(){
		return null;
	}

	protected void prepararDataUltimoDelito(){
		TipificacaoDelito tip = getManager().recuperarTipificacaoMaiorPena(getProcessoExpedienteCriminalEdit().getPessoa(),
				getProcessoExpedienteCriminalEdit().getProcessoTrf());
		if (tip != null && tip.getDataDelito() != null){
			getProcessoExpedienteCriminalEdit().setDataDelito(tip.getDataDelito());
			getProcessoExpedienteCriminalEdit().setInDataDelitoDesconhecida(false);
		}
	}

	public void informarDataUltimodelito(){
		if (!getProcessoExpedienteCriminalEdit().getInDataDelitoDesconhecida()){
			prepararDataUltimoDelito();
		}
		else{
			getProcessoExpedienteCriminalEdit().setDataDelito(null);
		}
	}

	protected void associarProcedimentosDeOrigem(){
		// vincula apenas se estiver inserindo
		if (getProcessoExpedienteCriminalEdit().getId() == null){
			List<ProcessoProcedimentoOrigem> procedimentosDeOrigem = getProcessoExpedienteCriminalEdit()
					.getProcessoTrf().getProcedimentoOrigemList();
			getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList().clear();
			getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList().addAll(procedimentosDeOrigem);
			//prepararDataUltimoDelito();
		}
	}

	protected void prepararAssuntos(){
		
		try{
			assuntos = getManager().recuperarAssuntosUltimaTipificacao((PessoaFisica)getProcessoExpedienteCriminalEdit().getPessoa(), getProcessoExpedienteCriminalEdit().getProcessoTrf());
			if(assuntos != null && !assuntos.isEmpty()){
				DispositivoNorma delitoMaior = getManager().recuperarDelitoMaiorPena(getProcessoExpedienteCriminalEdit().getPessoa(), getProcessoExpedienteCriminalEdit().getProcessoTrf());
				getProcessoExpedienteCriminalEdit().setAssuntoPrincipal(delitoMaior.getAssuntoTrf());
			}else{
				assuntos = getProcessoExpedienteCriminalEdit().getProcessoTrf().getAssuntoTrfList();
				getProcessoExpedienteCriminalEdit().setAssuntoPrincipal(getProcessoJudicial().getProcessoAssuntoPrincipal().getAssuntoTrf());
			}
			
			/*
			 * Necessário, pois o magistrado pode demorar muito tempo para assinar
			 * e nesse tempo uma nova icr com tipificações possuindo outros assuntos
			 * pode ser cadastrada.
			 * Caso ocorra, carrega os assuntos da última ICR com tipif. e adiciona
			 * o assunto selecionado durante a preparação do expediente
			 */
			if(isAssinando() || isRetificando()){
				if(assuntos != null && !assuntos.contains(getProcessoExpedienteCriminalEdit().getAssuntoPrincipal())){
					assuntos.add(getProcessoExpedienteCriminalEdit().getAssuntoPrincipal());
				}
			}
		} catch (PJeBusinessException e){
			e.printStackTrace();
		}
	}

	public void selecionarAssunto(AssuntoTrf assunto){
		getProcessoExpedienteCriminalEdit().setAssuntoPrincipal(assunto);
	}

	@Override
	public void editarProcessoExpedienteCriminal(PessoaFisica pessoa){
		super.editarProcessoExpedienteCriminal(pessoa);
		associarProcedimentosDeOrigem();
		prepararAssuntos();
		prepararDataUltimoDelito();
	}

	@Override
	public void editarProcessoExpedienteCriminal(E expediente){
		super.editarProcessoExpedienteCriminal(expediente);
		prepararAssuntos();
	}

	public void removerProcedimentoOrigem(ProcessoProcedimentoOrigem processoProcedimentoOrigem){
		if (getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList() != null
			&& getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList().size() > 1){
			getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList().remove(processoProcedimentoOrigem);
		}
		else{
			getFacesMessages().addFromResourceBundle(Severity.ERROR, "pje.prepararMandadoAlvaraAction.error.excluirProcedimentoOrigemNaoPermitido");
		}
	}

	public List<AssuntoTrf> getAssuntos(){
		return assuntos;
	}

	public void setAssuntos(List<AssuntoTrf> assuntos){
		this.assuntos = assuntos;
	}
	
	public String getTextoProcedimentoOrigem(){
		StringBuilder texto = new StringBuilder();
		if (getProcessoExpedienteCriminalEdit() != null && getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList() != null){
			for (ProcessoProcedimentoOrigem aux : getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList()){
				texto.append(aux.getTipoOrigem());
				texto.append(" - ");
				texto.append(aux.getTipoProcedimentoOrigem());
				texto.append(" - ");
				texto.append(aux.getOrgaoProcedimentoOriginario());
				texto.append(" - ");
				texto.append(aux.getNumeroAno());
				texto.append("\n");
			}
			
			if(texto != null && texto.length() != 0){
				texto = new StringBuilder(texto.substring(0, texto.lastIndexOf("\n")-1));
			}
		}
		return texto.toString();
	}
	
	@Override
	public void proximoPasso() {
		if(!validarDadosProcesso()){
			setPasso(0);
		}else{
			super.proximoPasso();
		}
	}
	
	protected Boolean validarDadosProcesso(){
		if(getProcessoExpedienteCriminalEdit().getDataDelito() == null && (getProcessoExpedienteCriminalEdit().getInDataDelitoDesconhecida() == null || getProcessoExpedienteCriminalEdit().getInDataDelitoDesconhecida() == false)){
			getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.error.prepararMandadoAlvaraAction.dataDelitoNaoInformada");
			return false;
		}
		
		if(getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList().isEmpty()){
			getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.error.prepararMandadoAlvaraAction.procedimentosOrigemNaoInformados");
			return false;
		}
		
		if(getProcessoExpedienteCriminalEdit().getAssuntoPrincipal() == null){
			getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.error.prepararMandadoAlvaraAction.assuntoPrincipalNaoInformado");
			return false;
		}
		
		return true;
	}
	
	public void associarDemaisProcedimentosOrigem(){
		for(ProcessoProcedimentoOrigem aux : getProcessoExpedienteCriminalEdit().getProcessoTrf().getProcedimentoOrigemList()){
			if(!getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList().contains(aux)){
				getProcessoExpedienteCriminalEdit().getProcessoProcedimentoOrigemList().add(aux);
			}
		}
	}
}
