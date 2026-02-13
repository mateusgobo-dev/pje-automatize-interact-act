/**
 * ProcessoTrfParaPoloProcessualConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadePoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual;
import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO.CriterioPesquisa;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ServicosPJeMNIEnum;
import br.jus.pje.nucleo.enums.TipoParteEnum;


/**
 * Conversor de ProcessoTrf para PoloProcessual.
 * 
 * @author Adriano Pamplona
 */
public class ProcessoTrfParaPoloProcessualConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoTrf, List<PoloProcessual>> {

	@In (br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService.NAME)
	br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService intercomunicacaoService;
	
	Boolean deveCarregarPartesDeTodasSituacoes = Boolean.TRUE;
	
	@Logger
	private Log log;
	
	@SuppressWarnings("static-access")
	public ProcessoTrfParaPoloProcessualConverter() {
		intercomunicacaoService = intercomunicacaoService.getInstance();
	}
	
	@SuppressWarnings("static-access")
	public ProcessoTrfParaPoloProcessualConverter(Boolean deveCarregarPartesDeTodasSituacoes) {
		intercomunicacaoService = intercomunicacaoService.getInstance();
		this.deveCarregarPartesDeTodasSituacoes = deveCarregarPartesDeTodasSituacoes;
	}
	
	
	@Override
	public List<PoloProcessual> converter(ProcessoTrf processo) {
		List<PoloProcessual> resultado = new ArrayList<PoloProcessual>();
		if(intercomunicacaoService.getServicoAtual() == null ||
				intercomunicacaoService.getServicoAtual() == ServicosPJeMNIEnum.ConsultarProcesso ){
			if (isNotNull(processo)) {
				PoloProcessual poloAtivo = obterPoloAtivo(processo);
			  	if(!isPoloVazio(poloAtivo)){
			  		resultado.add(poloAtivo);
			  	}
			  	PoloProcessual poloPassivo = obterPoloPassivo(processo);
			  	if(!isPoloVazio(poloPassivo)){
			  		resultado.add(poloPassivo);
			  	}
			  	List<PoloProcessual> polosTerceiro = obterPoloTerceiro(processo);
			  	if(polosTerceiro.size() > 0){
			  		resultado.addAll(polosTerceiro);
			  	}
			}		
		}
		return resultado;
	}
	
	protected boolean isPoloVazio(PoloProcessual poloProcessual){
	  	return isNull(poloProcessual)
	  			|| isNull(poloProcessual.getPolo())
	  			|| isNull(poloProcessual.getParte())
	  			|| isVazio(poloProcessual.getParte());
	}

	/**
	 * @param processo
	 * @return polo ativo.
	 */
	protected PoloProcessual obterPoloAtivo(ProcessoTrf processo) {
		return obterPolo(processo, ModalidadePoloProcessual.AT, recuperarPartes(processo, ProcessoParteParticipacaoEnum.A));
	}

	/**
	 * @param processo
	 * @return polo passivo.
	 */
	protected PoloProcessual obterPoloPassivo(ProcessoTrf processo) {
		return obterPolo(processo, ModalidadePoloProcessual.PA, recuperarPartes(processo, ProcessoParteParticipacaoEnum.P));
	}

	/**
	 * @param processo
	 * @return polo de terceiros.
	 */
	protected List<PoloProcessual> obterPoloTerceiro(ProcessoTrf processo) {
		List<PoloProcessual> polos = new ArrayList<PoloProcessual>();
		
		for (ProcessoParte processoParte : recuperarPartes(processo, ProcessoParteParticipacaoEnum.T)) {
			PoloProcessual polo = new PoloProcessual();
			
			processoParte =  refreshProcessoParte(processoParte);
			Parte parte = obterParte(processoParte);
			parte.getAdvogado().addAll(consultarColecaoAdvogado(processoParte));
			
			if(processoParte.getProcuradoria() != null){
				RepresentanteProcessual orgaoRepresentacao = converterOrgaoRepresentacao(processoParte);
				parte.getAdvogado().add(orgaoRepresentacao);
			}
			
			//VERIFICAR A REGRA ABAIXO.
			TipoParte tipoParte = processoParte.getTipoParte();
			TipoParteEnum tipoPoloMNI = tipoParte.getTipoPoloMNI();
			
			if (tipoPoloMNI == null) {
				polo.setPolo(ModalidadePoloProcessual.TC);
			} else if (tipoPoloMNI.toString().equalsIgnoreCase("TJ")) {
				polo.setPolo(ModalidadePoloProcessual.TJ);
			} else if (tipoPoloMNI.toString().equalsIgnoreCase("AD")) {
				polo.setPolo(ModalidadePoloProcessual.AD);
			} else if (tipoPoloMNI.toString().equalsIgnoreCase("VI")) {
				polo.setPolo(ModalidadePoloProcessual.VI);
			} else if(tipoPoloMNI.toString().equalsIgnoreCase("FL")) {
				polo.setPolo(ModalidadePoloProcessual.FL);
			} else {
				polo.setPolo(ModalidadePoloProcessual.TC);
			}
			// Fiscal da Lei é definido ao protocolar o processo conforme instalação do PJe na instância superior
			
			if(polo.getPolo() == ModalidadePoloProcessual.FL && !IntercomunicacaoService.getInstance().isServicoConsultarProcesso()) {
				continue;
			}
			
			polo.getParte().add(parte);
			polos.add(polo);
			
			incrementaParametrosDeSituacaoDasPartes(polo.getPolo(), polo, processoParte, parte);

		}
		
		return polos;
	}

	protected ProcessoParte refreshProcessoParte(ProcessoParte processoParte) {
		try {
			if (EntityUtil.getEntityManager().contains(processoParte)) {
				EntityUtil.getEntityManager().refresh(processoParte);
			} else if (isNotNull(processoParte) && isNotNull(processoParte.getIdProcessoParte())) {
				processoParte = EntityUtil.getEntityManager().find(processoParte.getClass(), processoParte.getIdProcessoParte());
			}
		} catch (Exception e) {
			log.error("Erro ao realizar refresh nos dados das partes: " + e.getLocalizedMessage() + ".", e);
		}
		return processoParte;
	}
	
	/**
	 * Retorna o polo de acordo com a modalidade (ativo, passivo).
	 * 
	 * @param modalidade
	 * @param processo
	 * @param partes
	 * @return polo de acordo com a modalidade e a lista de partes.
	 */
	protected PoloProcessual obterPolo(ProcessoTrf processo, ModalidadePoloProcessual modalidade, List<ProcessoParte> partes) {
		PoloProcessual polo = new PoloProcessual();
		polo.setPolo(modalidade);
		for (ProcessoParte processoParte : partes) {
			processoParte =  refreshProcessoParte(processoParte);
			if (processoParte.getPartePrincipal()) {
				if(processoParte.getParteSigilosa() && Authenticator.getUsuarioLogado() == null){
					continue;
				}
				Parte parte = obterParte(processoParte);
				try {
					AtoComunicacaoService service = (AtoComunicacaoService) Component.getInstance("atoComunicacaoService");
					long nint = service.contagemAtos(processoParte.getPessoa(), processoParte.getProcessoTrf(), CriterioPesquisa.INTIMACAO_PENDENTE);
					parte.setIntimacaoPendente((new Long(nint)).intValue());
				} catch (PJeBusinessException e) {
					e.printStackTrace();
				} 
				parte.getAdvogado().addAll(consultarColecaoAdvogado(processoParte));

				if(processoParte.getProcuradoria() != null){
					RepresentanteProcessual orgaoRepresentacao = converterOrgaoRepresentacao(processoParte);
					parte.getAdvogado().add(orgaoRepresentacao);
				}

				polo.getParte().add(parte);
				
				incrementaParametrosDeSituacaoDasPartes(modalidade, polo, processoParte, parte);
				incrementaParametrosDeSigiloDasPartes(modalidade, polo, processoParte, parte);
				incrementaParametrosDePartePrincipalDasPartes(modalidade, polo, processoParte, parte);
			}
		}
		
		return polo;
	}

	/**
	 * Função que preenche o parâmetro de situações das partes
	 * 
	 * @param modalidade
	 * @param polo
	 * @param processoParte
	 * @param parte
	 */
	private void incrementaParametrosDeSituacaoDasPartes(ModalidadePoloProcessual modalidade, PoloProcessual polo, ProcessoParte processoParte, Parte parte){
		MNIParametro.getListIndiceParteSituacao().add(""+ modalidade +":"+ polo.getParte().indexOf(parte) +":"+ processoParte.getInSituacao());
		int index = 0;
		for(ProcessoParteRepresentante processoParteRepresentante : processoParte.getProcessoParteRepresentanteList()){
			MNIParametro.getListIndiceParteRepresentateSituacaoValor().add(""+ modalidade +":"+ polo.getParte().indexOf(parte)+":"+(index++)+":"+ processoParteRepresentante.getParteRepresentante().getInSituacao());
		}
	}
	
	/**
	 * Função que preenche o parâmetro de sigilo das partes
	 * 
	 * @param modalidade
	 * @param polo
	 * @param processoParte
	 * @param parte
	 */
	private void incrementaParametrosDeSigiloDasPartes(ModalidadePoloProcessual modalidade, PoloProcessual polo, ProcessoParte processoParte, Parte parte){
		MNIParametro.getListIndiceParteSigiloValor().add(""+ modalidade +":"+ polo.getParte().indexOf(parte) +":"+ processoParte.getParteSigilosa());
		int index = 0;
		for(ProcessoParteRepresentante processoParteRepresentante : processoParte.getProcessoParteRepresentanteList()){
			MNIParametro.getListIndiceParteRepresentateSigiloValor().add(""+ modalidade +":"+ polo.getParte().indexOf(parte)+":"+(index++)+":"+ processoParteRepresentante.getParteRepresentante().getParteSigilosa());
		}
	}
	
	/**
	 * Função que preenche o parâmetro de parte princpal das partes
	 * 
	 * @param modalidade
	 * @param polo
	 * @param processoParte
	 * @param parte
	 */
	private void incrementaParametrosDePartePrincipalDasPartes(ModalidadePoloProcessual modalidade, PoloProcessual polo, ProcessoParte processoParte, Parte parte){
		MNIParametro.getListIndicePartePrincipalValor().add(""+ modalidade +":"+ polo.getParte().indexOf(parte) +":"+ processoParte.getPartePrincipal());
		int index = 0;
		for(ProcessoParteRepresentante processoParteRepresentante : processoParte.getProcessoParteRepresentanteList()){
			MNIParametro.getListIndiceParteRepresentatePrincipalValor().add(""+ modalidade +":"+ polo.getParte().indexOf(parte)+":"+(index++)+":"+ processoParteRepresentante.getParteRepresentante().getPartePrincipal());
		}
	}
	
	/**
	 * @param processoParte
	 * @return coleção dos representantes processuais (advogados)
	 */
	protected Collection<RepresentanteProcessual> consultarColecaoAdvogado(ProcessoParte processoParte) {
		ProcessoParteRepresentanteParaRepresentanteProcessualConverter converter = new ProcessoParteRepresentanteParaRepresentanteProcessualConverter();
		return converter.converterColecao(processoParte.getProcessoParteRepresentanteListAtivos());
	}

	/**
	 * Converte a pessoa em uma parte.
	 * @param pessoa
	 * @return parte
	 */
	protected Parte obterParte(ProcessoParte processoParte) {
		ProcessoParteParaParteConverter converter = novoProcessoParteParaParteConverter();
		return converter.converter(processoParte);
	}

	/**
	 * @return ProcessoParteParaParteConverter
	 */
	protected ProcessoParteParaParteConverter novoProcessoParteParaParteConverter() {
		return ComponentUtil.getComponent(ProcessoParteParaParteConverter.class);
	}
	
	/**
	 * Método que retorna uma lista somente com as partes ativas do processo conforme o polo (ativo, passivo) passado por parâmetro.
	 * Este método substitui o getListaParteAtivo/Passivo pois estes retornam também as partes baixadas;
	 * 
	 * @param tipoPolo (Passivo, Ativo)
	 * @param processo
	 * @return
	 */
	protected List<ProcessoParte> getListaPartesAtivas(ProcessoParteParticipacaoEnum tipoPolo, ProcessoTrf processo){
		List<ProcessoParte> partesAtivas = new ArrayList<ProcessoParte>();
		for(ProcessoParte parte : processo.getProcessoParteList()){
			if(tipoPolo.equals(parte.getInParticipacao()) && parte.getIsAtivo()){
				partesAtivas.add(parte);
			}
		}
		return partesAtivas;
	}

	/**
	 * Método respoonsável por transformar Procuradoria em RepresentanteProcessual
	 * 
	 * @param orgaoRepresentacao
	 * @return
	 */
	protected RepresentanteProcessual converterOrgaoRepresentacao(ProcessoParte processoParte){
		ProcessoParteProcuradoriaParaRepresentanteProcessualConverter converter = 
				novoProcessoParteProcuradoriaParaRepresentanteProcessualConverter();
		return converter.converter(processoParte);
	}
	
	/**
	 * @return Instância de ProcessoParteProcuradoriaParaRepresentanteProcessualConverter.
	 */
	protected ProcessoParteProcuradoriaParaRepresentanteProcessualConverter 
			novoProcessoParteProcuradoriaParaRepresentanteProcessualConverter() {
		return ComponentUtil.getComponent(ProcessoParteProcuradoriaParaRepresentanteProcessualConverter.class);
	}
	
	
	public List<ProcessoParte> recuperarPartes(ProcessoTrf processo,
			ProcessoParteParticipacaoEnum participacaoEnum) {
		if (participacaoEnum == null) {
			throw new IllegalArgumentException("A participação da parte é requerida");
		}
		List<ProcessoParte> list = new ArrayList<ProcessoParte>(0);
		for (ProcessoParte processoParte : processo.getProcessoParteList()) {
			if (participacaoEnum.equals(processoParte.getInParticipacao())) {
				if (this.deveCarregarPartesDeTodasSituacoes) {
					list.add(processoParte);
				} else {
					if (processoParte.getIsAtivo()) {
						list.add(processoParte);
					}
				}
			}
		}
		return list;
	}
}
