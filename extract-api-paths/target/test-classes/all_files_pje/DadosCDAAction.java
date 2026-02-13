/*
 * DadosCDAAction.java
 * 
 * Data: 16/09/2020
 */
package br.com.infox.pje.action;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.CdaManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoNaturezaDebitoManager;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.dto.CdaDTO;
import br.jus.pje.nucleo.dto.DebitoDTO;
import br.jus.pje.nucleo.dto.DevedorCdaDTO;
import br.jus.pje.nucleo.dto.DevedorDocIdentificacaoDTO;
import br.jus.pje.nucleo.dto.IPTUNaturezaDebitoDTO;
import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Debito;
import br.jus.pje.nucleo.entidades.DevedorCda;
import br.jus.pje.nucleo.entidades.DevedorDocIdentificacao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoNaturezaDebito;
import br.jus.pje.nucleo.enums.EnumTipoDevedor;
import br.jus.pje.nucleo.enums.EnumTipoValorCda;

/**
 * Classe action responsável pela manipulação da CDA de um ProcessoTrf.
 * 
 * @author Adriano Pamplona
 */
@Name(DadosCDAAction.NAME)
@Scope(ScopeType.PAGE)
public class DadosCDAAction extends BaseAction<Cda> implements Serializable {

	public static final String NAME = "dadosCDAAction";
	private List<CdaDTO> colecaoCdaDTO;

	/**
	 * @return Instância de DadosCDAAction.
	 */
	public static DadosCDAAction instance() {
		return ComponentUtil.getComponent(DadosCDAAction.class);
	}

	/**
	 * Evento invocado ao selectionar uma rich:tab.
	 * 
	 * @param processo
	 * @throws PJeBusinessException
	 */
	public void onOpenTab(ProcessoTrf processo) throws PJeBusinessException {
		
		if (getColecaoCdaDTO().isEmpty()) {
			Collection<CdaDTO> dtos = CdaManager.instance().getColecaoCdaDTO(processo.getColecaoCda());
			getColecaoCdaDTO().addAll(dtos);
		}
		
		CepSuggestBean.instance().setInstance(null);
	}
	
	/**
	 * Evento invocado ao alterar um TipoNaturezaDebito da combo.
	 * 
	 * @param debitoDTO
	 */
	public void onChangeTipoNaturezaDebito(DebitoDTO debitoDTO) {
		TipoNaturezaDebito tipoNaturezaDebito = debitoDTO.getTipoNaturezaDebito();
		if (tipoNaturezaDebito == null) {
			onSuggestRemove(debitoDTO);
			CepSuggestBean.instance().setInstance(null);
		}
	}
	
	public Boolean isClasseExecucaoFiscal(){
		Boolean resultado = Boolean.FALSE;
		
		ClasseJudicial classeJudicial = getProcessoTrf().getClasseJudicial();
		if (classeJudicial != null && classeJudicial.getIdClasseJudicial() != 0){
			resultado = ClasseJudicialManager.instance().isClasseExecucaoFiscal(classeJudicial);
		}
		return resultado;
	}
	
	/**
	 * @return True se o usuário tiver permissão para consultar ou editar CDA.
	 */
	public Boolean isTemPermissao() {
		return isPermissaoConsultarCDA() || isPermissaoEditarCDA();
	}
	
	/**
	 * @return True se o usuário tiver permissão para consultar CDA.
	 */
	public Boolean isPermissaoConsultarCDA() {
		return Identity.instance().hasRole(Papeis.CONSULTAR_CDA);
	}
	
	/**
	 * @return True se o usuário tiver permissão para editar CDA.
	 */
	public Boolean isPermissaoEditarCDA() {
		return Identity.instance().hasRole(Papeis.EDITAR_CDA);
	}
	
	/**
	 * Adiciona uma nova CDA.
	 * 
	 * @param processo
	 */
	public void adicionarCDA(ProcessoTrf processo) {
		CdaManager manager = CdaManager.instance();
		
		Cda cda = new Cda();
		cda.setAtivo(Boolean.TRUE);
		cda.setTipoValorCda(EnumTipoValorCda.O);
		cda.setDataApuracao(new Date());
		cda.setMoedaValor("Real");
		cda.setValor(BigDecimal.ZERO);
		cda.setProcessoTrf(processo);
		cda.setNumero(manager.gerarNumero());
		getColecaoCdaDTO().add(new CdaDTO(cda));
		salvarCDA(processo);
	}
	
	/**
	 * Valida os campos da CDA para que nao possuam caracteres invalidos.
	 *
	 * @param cda
	 */
	public void validarCda(Cda cda) {
		CharsetEncoder encoder = StandardCharsets.ISO_8859_1.newEncoder();
		
	    if (cda.getNumero() != null && !encoder.canEncode(cda.getNumero())) {
	        throw new NegocioException("O campo 'Numero da CDA' esta com caractere invalido.");
	    }
	
	    if (cda.getNumeroControle() != null && !encoder.canEncode(cda.getNumeroControle())) {
	        throw new NegocioException("O campo 'Numero de controle' esta com caractere invalido.");
	    }
	
	    if (cda.getNumeroProcessoAdm() != null && !encoder.canEncode(cda.getNumeroProcessoAdm())) {
	        throw new NegocioException("O campo 'Processo administrativo' esta com caractere invalido.");
	    }
	}

	
	/**
	 * Salva a lista de CDA's.
	 * 
	 * @param processo
	 * @throws PJeBusinessException
	 */
	public void salvarCDA(ProcessoTrf processo) {
		try {
			for(CdaDTO cdaDTO : getColecaoCdaDTO()) {
				validarCda(cdaDTO.getCda());
			}
			CdaManager.instance().salvarColecaoCdaDTO(getColecaoCdaDTO());			
			boolean isVazio = processo.getColecaoCda().isEmpty();
			
			if (!isVazio) {
				processo.getColecaoCda().clear();
			}

			
			for (CdaDTO cdaDTO : getColecaoCdaDTO()) {
				processo.getColecaoCda().add(cdaDTO.getCda());
			}
			
			verificarAtribuirValorProcessoComSomaCda(processo);
			
		} catch (PJeBusinessException e) {
			FacesUtil.adicionarMensagemError(true, e.getLocalizedMessage());
		} catch (NegocioException e) {
	        FacesUtil.adicionarMensagemError(true, e.getMensagem());
	    }

	}
	
	/**
	 * Exclui uma CDA.
	 * 
	 * @param processo
	 * @param cdaDTO
	 * @throws PJeBusinessException
	 */
	public void excluirCDA(ProcessoTrf processo, CdaDTO cdaDTO) throws PJeBusinessException {
		try {
			Cda cdaEntity = cdaDTO.getCda();
			getColecaoCdaDTO().remove(cdaDTO);
			processo.getColecaoCda().remove(cdaEntity);
			
			if (cdaEntity.getId() != null) {
				Cda c = getManager().findById(cdaEntity.getId());
				CdaManager.instance().remove(c);
				EntityUtil.flush();
			}
			verificarAtribuirValorProcessoComSomaCda(processo);
		} catch (PJeBusinessException e) {
			FacesUtil.adicionarMensagemError(true, e.getLocalizedMessage());
		}
		salvarCDA(processo);
	}
	
	/**
	 * Adiciona um novo DevedorCda.
	 * 
	 * @param cdaDTO
	 */
	public void adicionarDevedorCda(CdaDTO cdaDTO) {
		DevedorCdaDTO devedorDTO =  new DevedorCdaDTO(new DevedorCda(), cdaDTO);
		devedorDTO.setTipoDevedor(EnumTipoDevedor.P);
		devedorDTO.setCdaDTO(cdaDTO);
		cdaDTO.getColecaoDevedorCda().add(devedorDTO.getDevedorCda());
		cdaDTO.getColecaoDevedorCdaDTO().add(devedorDTO);
	}
	
	/**
	 * Exclui o DevedorDTO.
	 * 
	 * @param devedorDTO
	 */
	public void excluirDevedorCda(DevedorCdaDTO devedorDTO) {
		CdaDTO cdaDTO = devedorDTO.getCdaDTO();
		cdaDTO.getColecaoDevedorCdaDTO().remove(devedorDTO);
		cdaDTO.getColecaoDevedorCda().remove(devedorDTO.getDevedorCda());
	}
	
	/**
	 * Adiciona um novo DevedorDocIdentificacaoDTO.
	 * 
	 * @param devedorDTO
	 */
	public void adicionarDocumento(DevedorCdaDTO devedorDTO) {
		TipoDocumentoIdentificacao cpf = TipoDocumentoIdentificacaoManager.instance().carregarTipoDocumentoIdentificacao("CPF");
		DevedorDocIdentificacaoDTO documentoDTO = new DevedorDocIdentificacaoDTO(new DevedorDocIdentificacao(), devedorDTO);
		documentoDTO.setDevedorCdaDTO(devedorDTO);
		documentoDTO.setNumero("0");
		documentoDTO.setTipoDocumentoIdentificacao(cpf);
		devedorDTO.getColecaoDevedorDocIdentificacao().add(documentoDTO.getDevedorDocIdentificacao());
		devedorDTO.getColecaoDevedorDocIdentificacaoDTO().add(documentoDTO);
	}
	
	/**
	 * Exclui o DevedorDocIdentificacaoDTO informado.
	 * 
	 * @param documentoDTO
	 */
	public void excluirDocumento(DevedorDocIdentificacaoDTO documentoDTO) {
		DevedorCdaDTO devedorDTO = documentoDTO.getDevedorCdaDTO();
		devedorDTO.getColecaoDevedorDocIdentificacao().remove(documentoDTO.getDevedorDocIdentificacao());
		devedorDTO.getColecaoDevedorDocIdentificacaoDTO().remove(documentoDTO);
	}
	
	/**
	 * Adiciona um novo DebitoDTO.
	 * 
	 * @param cdaDTO
	 */
	public void adicionarDebito(CdaDTO cdaDTO) {
		DebitoDTO debitoDTO = new DebitoDTO(new Debito(), cdaDTO);
		debitoDTO.setCdaDTO(cdaDTO);
		debitoDTO.setDataExercicio(new Date());
		
		cdaDTO.getColecaoDebito().add(debitoDTO.getDebito());
		cdaDTO.getColecaoDebitoDTO().add(debitoDTO);
	}

	/**
	 * Exclui o DebitoDTO informado.
	 * 
	 * @param debitoDTO
	 */
	public void excluirDebito(DebitoDTO debitoDTO) {
		CdaDTO cdaDTO = debitoDTO.getCdaDTO();
		cdaDTO.getColecaoDebito().remove(debitoDTO.getDebito());
		cdaDTO.getColecaoDebitoDTO().remove(debitoDTO);
	}
	
	/**
	 * Evento invocado ao selecionar um item no suggestion.
	 * 
	 * @param debitoDTO
	 */
	public void onSuggestSelect(DebitoDTO debitoDTO) {
		CepSuggestBean cepSuggestBean = CepSuggestBean.instance();
		
		if(cepSuggestBean.getInstance() != null) {
			setEnderecoNoIPTUNaturezaDebito(cepSuggestBean.getInstance(), debitoDTO);
		}else {
			List<Cep> ceps = CepService.instance().findByNumero(cepSuggestBean.getDefaultValue());
			if(ceps != null && !ceps.isEmpty()) {
				setEnderecoNoIPTUNaturezaDebito(ceps.get(0), debitoDTO);
				cepSuggestBean.setInstance(ceps.get(0));
			} else {
				cepSuggestBean.setDefaultValue("");
			}
		}
		cepSuggestBean.setInstance(null);
	}
	
	/**
	 * Evento invocado ao clicar no botão limpar do suggestion.
	 * 
	 * @param debitoDTO
	 */
	public void onSuggestRemove(DebitoDTO debitoDTO) {
		CepSuggestBean cepSuggestBean = CepSuggestBean.instance();
		cepSuggestBean.setInstance(null);
		
		debitoDTO.getIptuNaturezaDebitoDTO().setBairro(null);
		debitoDTO.getIptuNaturezaDebitoDTO().setCep(null);
		debitoDTO.getIptuNaturezaDebitoDTO().setComplemento(null);
		debitoDTO.getIptuNaturezaDebitoDTO().setEstado(null);
		debitoDTO.getIptuNaturezaDebitoDTO().setLogradouro(null);
		debitoDTO.getIptuNaturezaDebitoDTO().setMunicipio(null);
		debitoDTO.getIptuNaturezaDebitoDTO().setNumero(null);
	}
	
	/**
	 * @param cda
	 * @throws PJeBusinessException
	 */
	public void inverterStatusCDA(ProcessoTrf processo, CdaDTO cdaDTO) throws PJeBusinessException {
		Cda cdaEntity = cdaDTO.getCda();
		
		if (cdaEntity.getId() != null) {
			Cda c = getManager().findById(cdaEntity.getId());
			getManager().inverterStatus(c);
		}
	}

	/**
	 * Inverte o status de uma Cda. Se estiver ativa ficará inativa e vice-versa.
	 * 
	 * @param cda Cda.
	 * @throws PJeBusinessException
	 */
	public Boolean isTipoValorOriginario(Cda cda) {
		return (cda != null && cda.getTipoValorCda() != null && cda.getTipoValorCda().equals(EnumTipoValorCda.O));
	}

	/**
	 * @return True se existir CDA para o ProcessoTrf.
	 */
	public Boolean isExisteColecaoCda() {
		return ProjetoUtil.isNotVazio(getProcessoTrf().getColecaoCda());
	}

	/**
	 * @return ProcessoTrf
	 */
	public ProcessoTrf getProcessoTrf() {
		return ProcessoTrfHome.instance().getInstance();
	}

	/**
	 * @return colecaoCda.
	 */
	public List<CdaDTO> getColecaoCdaDTO() {
		if (colecaoCdaDTO == null) {
			colecaoCdaDTO = new ArrayList<>();
		}
		return colecaoCdaDTO;
	}

	/**
	 * @return Array de EnumTipoDevedor
	 */
	public EnumTipoDevedor[] getTipoDevedorValues() {
		return EnumTipoDevedor.values();
	}

	/**
	 * @return Coleção de TipoNaturezaDebito.
	 * 
	 * @throws PJeBusinessException
	 */
	public Collection<TipoNaturezaDebito> getColecaoTipoNaturezaDebito() throws PJeBusinessException {
		return TipoNaturezaDebitoManager.instance().findAll();
	}
	
	/**
	 * @return Coleção de TipoDocumentoIdentificacao.
	 * 
	 * @throws PJeBusinessException
	 */
	public Collection<TipoDocumentoIdentificacao> getColecaoTipoDocumentoIdentificacao() throws PJeBusinessException {
		TipoDocumentoIdentificacaoManager manager = ComponentUtil.getComponent(TipoDocumentoIdentificacaoManager.class);
		return manager.findAll();
	}
	
	@Override
	public EntityDataModel<Cda> getModel() {
		return null;
	}

	@Override
	protected CdaManager getManager() {
		return CdaManager.instance();
	}

	/**
	 * metodo responsavel por atualizar as informacoes do endereco com as informacoes do CEP 
	 * passado em parametro.
	 * @param cep
	 */
	protected void setEnderecoNoIPTUNaturezaDebito(Cep cep, DebitoDTO debitoDTO) {
		
		IPTUNaturezaDebitoDTO iptu = debitoDTO.getIptuNaturezaDebitoDTO();
		iptu.setCep(cep.getNumeroCep());
		iptu.setEstado(cep.getMunicipio().getEstado().getEstado());
		iptu.setMunicipio(cep.getMunicipio().getMunicipio());
		iptu.setBairro(cep.getNomeBairro());		
		iptu.setLogradouro(cep.getNomeLogradouro());
		debitoDTO.setIptuNaturezaDebitoDTO(iptu);
	}
	
	/**
	 * Metodo responsavel por verificar o parametro do cda para
	 * realizar a soma do valor da causa ao salvar ou excluir uma cda
	 * @param processo
	 */
	private void verificarAtribuirValorProcessoComSomaCda(ProcessoTrf processo) {
		ParametroService parametroService = ComponentUtil.getParametroService();
		
		if(BooleanUtils.toBoolean(parametroService.valueOf(Parametros.PJE_EF_AJUSTAR_VALOR_CAUSA_CDA))){
			ProcessoTrfManager.instance().atribuirValorProcessoComSomaCda(processo);
		}
	}

}