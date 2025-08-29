/**
 * CdaManager
 * 
 * Data: 20/08/2020
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jboss.seam.annotations.Name;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.CdaDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.dto.CdaDTO;
import br.jus.pje.nucleo.dto.DebitoDTO;
import br.jus.pje.nucleo.dto.DevedorCdaDTO;
import br.jus.pje.nucleo.dto.DevedorDocIdentificacaoDTO;
import br.jus.pje.nucleo.entidades.Cda;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.TipoNaturezaDebito;

/**
 * Classe responsável pelos métodos negociais de uma CDA.
 * 
 * @author Adriano Pamplona
 */
@Name(CdaManager.NAME)
public class CdaManager extends BaseManager<Cda>{

	public static final String NAME = "cdaManager";

	/**
	 * @return Instância de CdaManager.
	 */
	public static CdaManager instance() {
		return ComponentUtil.getComponent(CdaManager.class);
	}
	
	@Override
	protected CdaDAO getDAO() {
		return ComponentUtil.getComponent(CdaDAO.class);
	}

	@Override
	public Cda persist(Cda cda) throws PJeBusinessException {
		Cda resultado = null;
		
		//validar(cda);
		
		resultado = getDAO().obter(cda.getProcessoTrf(), cda.getNumero());
		if (resultado != null) {
			if (Authenticator.hasRole(Papeis.EDITAR_CDA_VIA_MNI) ) {
				//resultado.setValor(cda.getValor());
				cda.setId(resultado.getId());
				resultado = getDAO().merge(cda);
			}
		} else {
			resultado = getDAO().persist(cda);
		}
		return resultado;
	};
	
	/**
	 * Inverte o status de uma Cda. Se estiver ativa ficará inativa e vice-versa.
	 * 
	 * @param cda Cda.
	 * @throws PJeBusinessException
	 */
	public void inverterStatus(Cda cda) throws PJeBusinessException {
		if (cda != null) {
			Boolean status = BooleanUtils.negate(cda.getAtivo());
			cda.setAtivo(status);
			if (status) {
				//validar(cda);
			}
			mergeAndFlush(cda);
			
		}
	}
	
	/**
	 * Converte uma lista de Cda em lista de CdaDTO.
	 * 
	 * @param cdas
	 * @return Lista de CdaDTO.
	 * @throws PJeBusinessException
	 */
	public Collection<CdaDTO> getColecaoCdaDTO(List<Cda> cdas) throws PJeBusinessException {
		Collection<CdaDTO> resultado = new ArrayList<>();
		
		if (ProjetoUtil.isNotVazio(cdas)) {
			TipoDocumentoIdentificacaoManager tipoDocumentoIdentificacaoManager = TipoDocumentoIdentificacaoManager.instance();
			TipoNaturezaDebitoManager tipoNaturezaDebitoManager = TipoNaturezaDebitoManager.instance();
			
			for (Cda cda : cdas) {
				CdaDTO dto = new CdaDTO(cda);
				for (DevedorCdaDTO devedor : dto.getColecaoDevedorCdaDTO()) {
					for (DevedorDocIdentificacaoDTO documento : devedor.getColecaoDevedorDocIdentificacaoDTO()) {
						TipoDocumentoIdentificacao tipo = tipoDocumentoIdentificacaoManager.carregarTipoDocumentoIdentificacao(documento.getCodigoTipo());
						documento.setTipoDocumentoIdentificacao(tipo);
					}
				}
				for (DebitoDTO debito : dto.getColecaoDebitoDTO()) {
					TipoNaturezaDebito tipo = tipoNaturezaDebitoManager.findByCodigo(debito.getCodigoNatureza());
					debito.setTipoNaturezaDebito(tipo);
				}
				resultado.add(dto);
			}
		}
		
		return resultado;
	}

	/**
	 * Salva as Cda's passadas por parâmetro.
	 * 
	 * @param dtos List<CdaDTO>
	 * @throws PJeBusinessException
	 */
	public void salvarColecaoCdaDTO(List<CdaDTO> dtos) throws PJeBusinessException {
		if (ProjetoUtil.isNotVazio(dtos)) {
			for (CdaDTO dto : dtos) {
				for(DebitoDTO debitoDTO : dto.getColecaoDebitoDTO()) {
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.setSerializationInclusion(Include.NON_NULL);
			        try {
			            String json = objectMapper.writeValueAsString(debitoDTO.getIptuNaturezaDebitoDTO());
			            debitoDTO.setDados(json);
			        } catch (JsonProcessingException e) {
			        	new RuntimeException(e);
			        }
					
				}
				Cda cda = dto.getCda();
				persistAndFlush(cda);
			}
		}
	}

	/**
	 * @return Número aleatório de CDA.
	 */
	public String gerarNumero() {
		String resultado = null;
		do {
			logger.info("Gerando número de CDA.");
			resultado = String.valueOf(new Random().nextInt(10000000));
		} while (getDAO().isNumeroExiste(resultado));
		
		return resultado;
	}
	/**
	 * Valida se a cda já está cadastrada em outro processo.
	 * 
	 * @param cda Cda
	 * @throws PJeBusinessException 
	 */
	protected void validar(Cda cda) throws PJeBusinessException {
		StringBuilder mensagem = new StringBuilder();
		
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		
		List<Cda> cdasCadastradas = getDAO().consultar(cda.getNumero());
		for (Cda cdaCadastrada : cdasCadastradas) {
			ProcessoTrf cdaCadastradaProcessoTrf = cdaCadastrada.getProcessoTrf();
			Jurisdicao cdaCadastradaJurisdicao = cdaCadastradaProcessoTrf.getJurisdicao(); 
			
			Boolean isNovaCda = (cda.getId() == null || cda.getId() == 0);
			Boolean isCdaIdsIguais = (cdaCadastrada.getId() == cda.getId());
			Boolean isCdaCadastradaMesmoProcesso = (cdaCadastradaProcessoTrf.getIdProcessoTrf() == cda.getProcessoTrf().getIdProcessoTrf());
			Boolean isCdaCadastradaProcessoArquivado = processoTrfHome.isArquivado(cdaCadastradaProcessoTrf);
			Boolean isCdaCadastradaMesmaJurisdicao = cdaCadastradaJurisdicao.getIdJurisdicao() == cda.getProcessoTrf().getJurisdicao().getIdJurisdicao();
			
			if (!isCdaIdsIguais && (isCdaCadastradaMesmoProcesso || !isCdaCadastradaProcessoArquivado || isCdaCadastradaMesmaJurisdicao)) {
				String processo = ObjectUtils.firstNonNull(cdaCadastradaProcessoTrf.getNumeroProcesso(), "Mesmo");
				mensagem.append(String.format("CDA [%s] - processo [%s] - [%s].", 
						cdaCadastrada.getNumero(), 
						processo, 
						cdaCadastradaJurisdicao.getJurisdicao()));
				mensagem.append("\n");
			}
		}
		
		if (mensagem.length() > 0) {
			mensagem.insert(0, String.format("A CDA [%s] já existe no processo: ", cda.getNumero()));
			throw new PJeBusinessException(mensagem.toString());
		}
	}
}
