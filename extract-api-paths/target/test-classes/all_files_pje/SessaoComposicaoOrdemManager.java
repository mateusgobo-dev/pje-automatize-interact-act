/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.dao.SessaoComposicaoOrdemDAO;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.enums.SexoEnum;

@Name(SessaoComposicaoOrdemManager.NAME)
public class SessaoComposicaoOrdemManager extends BaseManager<SessaoComposicaoOrdem> {

	public static final String NAME = "sessaoComposicaoOrdemManager";

	@In
	private SessaoComposicaoOrdemDAO sessaoComposicaoOrdemDAO; 
	
	@In
	private SessaoPautaProcessoComposicaoManager sessaoPautaProcessoComposicaoManager;
		
	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	
	@In
	UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;	
	
	@In
	PessoaMagistradoManager pessoaMagistradoManager;
	
	@In
	SubstituicaoMagistradoManager substituicaoMagistradoManager;	
	
		
	@Override
	protected SessaoComposicaoOrdemDAO getDAO() {
		return sessaoComposicaoOrdemDAO;
	}

	/**
	 * Altera o magistrado presente da composicao da sessao e 
	 *  em cascata altera os magistrados das composicoes dos processos da sessao que não possui uma 
	 *  definicao mais especifica.
	 * 
	 * @param sessaoComposicaoOrdem
	 * @throws PJeBusinessException 
	 */
	public void alterarMagistradoPresente(SessaoComposicaoOrdem sessaoComposicaoOrdem) throws PJeBusinessException {
		SessaoComposicaoOrdem objManaged = this.merge(sessaoComposicaoOrdem);
		sessaoPautaProcessoComposicaoManager.atualizarMagistradoPresenteNasComposicoesDosProcessosDaSessao(objManaged);
		this.flush();
	}
	
	/**
	 * Altera o magistrado substituto da composicao da sessao e 
	 * 
	 * @param sessaoComposicaoOrdem
	 * @throws PJeBusinessException 
	 */
	public void alterarMagistradoSubstituto(SessaoComposicaoOrdem sessaoComposicaoOrdem) throws PJeBusinessException {
		mergeAndFlush(sessaoComposicaoOrdem);
	}
	
	
	/**
	 * Metodo que retorna a composicao da sessao exceto a composicao passada por parametro.
	 * 
	 * @param idSessao
	 * @param idSessaoComposicaoExcecao
	 * @return Lista de composição.
	 */
	public List<SessaoComposicaoOrdem> obterComposicaoSessao(Integer idSessao, Integer idSessaoComposicaoExcecao){
		return getDAO().obterComposicaoSessao(idSessao, idSessaoComposicaoExcecao);
	}

	
	/**
	 * Obtem a composicao do Orgao Julgador Presidente da sessao
	 * 
	 * @param sessao
	 * @return SCO do presidente.
	 */
	public SessaoComposicaoOrdem obterOrgaoJulgadorPresidente(Sessao sessao){
		return sessaoComposicaoOrdemDAO.obterOrgaoJulgadorPresidente(sessao);
	}
	
	/**
	 * Lista os Orgãos Julgadores na composição da <code>sessao</code>
	 * informada.
	 * 
	 * @param sessao
	 *            que se deseja obter os Orgaos Julgadores
	 * @return lista de Orgãos Julgadores da sessao informada.
	 */
	public List<OrgaoJulgador> listOrgaoJulgadorComposicaoSessao(Sessao sessao) {
		return sessaoComposicaoOrdemDAO.listOrgaoJulgadorComposicaoSessao(sessao);
	}
	
	public void criarComposicaoSessao(Sessao sessao, Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapTitularesComposicaoColegiado,  
			Map<OrgaoJulgadorColegiadoOrgaoJulgador, PessoaMagistrado> mapMagistradosSubstitutosComposicaoColegiado) throws PJeBusinessException {

		for (OrgaoJulgadorColegiadoOrgaoJulgador componenteColegiado : mapTitularesComposicaoColegiado.keySet()) {
			this.criarComponenteComposicaoSessao(sessao, componenteColegiado, mapTitularesComposicaoColegiado.get(componenteColegiado), 
					mapMagistradosSubstitutosComposicaoColegiado.get(componenteColegiado));
		}

		this.flush();
	}
	
	private void criarComponenteComposicaoSessao(Sessao sessao, OrgaoJulgadorColegiadoOrgaoJulgador componenteColegiado,
			PessoaMagistrado magistradoTitular, PessoaMagistrado magistradoSubstituto) throws PJeBusinessException {

		OrgaoJulgador orgaoJulgador = componenteColegiado.getOrgaoJulgador();
		OrgaoJulgador orgaoJulgadorRevisor = (componenteColegiado.getOrgaoJulgadorRevisor() != null) ? 
				componenteColegiado.getOrgaoJulgadorRevisor().getOrgaoJulgador() : null;

		SessaoComposicaoOrdem componenteSessao = new SessaoComposicaoOrdem();
		componenteSessao.setOrgaoJulgador(orgaoJulgador);
		componenteSessao.setOrgaoJulgadorRevisor(orgaoJulgadorRevisor);
		componenteSessao.setPresidente(orgaoJulgador.equals(sessao.getOrgaoJulgadorColegiado().getOrgaoJulgadorPresidente()));
		componenteSessao.setSessao(sessao);
		componenteSessao.setMagistradoTitularPresenteSessao(Boolean.TRUE);
		componenteSessao.setMagistradoPresenteSessao(magistradoTitular);
		componenteSessao.setMagistradoSubstitutoSessao(magistradoSubstituto);

		this.persist(componenteSessao);
	}
	
	
	public void removerComposicaoSessao(Sessao sessao) {
		getDAO().removerComposicao(sessao); 
	}

	public String obterPresidenteSessao(Sessao sessao) {
		String retorno = "";
		sessao = ComponentUtil.getSessaoManager().recuperarSessao(sessao);
		if(sessao != null) {
			SessaoComposicaoOrdem presidente = obterOrgaoJulgadorPresidente(sessao);
			if(presidente != null) {
				if(ParametroUtil.instance().isTerceiroGrau() && ParametroJtUtil.instance().justicaEleitoral()) {
					if(presidente.getMagistradoPresenteSessao().getSexo() != null && SexoEnum.F.equals(presidente.getMagistradoPresenteSessao().getSexo())) {
						retorno = "da Senhora Ministra ";
					} else {
						retorno = "do Senhor Ministro ";
					}
				}
				retorno = retorno + presidente.getMagistradoPresenteSessao().getNome();
			}
		}
		return retorno;
	}

	private boolean verificarPresidente(boolean comPresidente, SessaoComposicaoOrdem componente) {
        return comPresidente || !componente.getPresidente();
    }

	public String obterComposicaoSessao(Sessao sessao, boolean comPresidente) {
		String retorno = "";
		sessao = ComponentUtil.getSessaoManager().recuperarSessao(sessao);
		if(sessao != null) {
			List<SessaoComposicaoOrdem> componentes = obterComposicaoSessao(sessao.getIdSessao(), 0); 
			if(!componentes.isEmpty()) {
				if(ParametroUtil.instance().isTerceiroGrau() && ParametroJtUtil.instance().justicaEleitoral()) {
					List<String> nomesMagistradosFemininos = new ArrayList<String>(componentes.size());
					List<String> nomesMagistradosMasculinos = new ArrayList<String>(componentes.size());
					int quantidadeFeminino = 0;
					int quantidadeMasculino = 0;
					String tituloFeminino = "";
					String tituloMasculino = "";
					for(SessaoComposicaoOrdem componente: componentes) {
						if(componente.getMagistradoTitularPresenteSessao() && verificarPresidente(comPresidente, componente)) {
							if(componente.getMagistradoPresenteSessao().getSexo() != null && SexoEnum.F.equals(componente.getMagistradoPresenteSessao().getSexo())) {
								quantidadeFeminino = quantidadeFeminino + 1;
								if(quantidadeFeminino == 1 ) {
									tituloFeminino = "a Senhora Ministra ";
								} else {
									tituloFeminino = "as Senhoras Ministras ";
								}
								nomesMagistradosFemininos.add(componente.getMagistradoPresenteSessao().toString());
							}
							if(componente.getMagistradoPresenteSessao().getSexo() == null || SexoEnum.M.equals(componente.getMagistradoPresenteSessao().getSexo())) {
								quantidadeMasculino = quantidadeMasculino + 1;
								if(quantidadeMasculino == 1 ) {
									tituloMasculino = "o Senhor Ministro ";
								} else {
									tituloMasculino = "os Senhores Ministros ";
								}
								nomesMagistradosMasculinos.add(componente.getMagistradoPresenteSessao().toString());
							}
						}
					}
					if(quantidadeFeminino > 0) {
						Collections.sort(nomesMagistradosFemininos);
						retorno = tituloFeminino + StringUtils.join(nomesMagistradosFemininos, ", ");  
					}
					if(quantidadeMasculino > 0) {
						Collections.sort(nomesMagistradosMasculinos);
						if(retorno.length() > 0) {
							retorno = retorno + " e ";
						}
						retorno = retorno + tituloMasculino + StringUtils.join(nomesMagistradosMasculinos, ", ");  
					}
				} else {
					List<String> nomesMagistrados = new ArrayList<String>(componentes.size());
					for(SessaoComposicaoOrdem componente: componentes) {
						if(componente.getMagistradoTitularPresenteSessao() && verificarPresidente(comPresidente, componente)) {
							nomesMagistrados.add(componente.getMagistradoPresenteSessao().toString());
						}
					}
					Collections.sort(nomesMagistrados);
					retorno = StringUtils.join(nomesMagistrados,", ");
				}
			}
		}
		return retorno;
	}

}