package br.com.infox.pje.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.bean.VotoAcompanhadoBean;
import br.com.infox.pje.dao.SessaoComposicaoOrdemDAO;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.TipoVotoEnum;

/**
 * Servicos referente a Sessao, Processo, Pauta, entre outras entidades
 * envolvidas.
 * 
 * @author daniel
 * 
 */
@Name(SessaoJulgamentoService.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SessaoJulgamentoService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3688590868845683174L;

	public static final String NAME = "sessaoJulgamentoService";
	
	@In
	private transient SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;

	@In
	private TipoVotoManager tipoVotoManager;
	
	@In
	private SessaoProcessoDocumentoDAO sessaoProcessoDocumentoDAO;
	
	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;
	
	@In
	private SessaoComposicaoOrdemDAO sessaoComposicaoOrdemDAO;

	/**
	 * Recupera o número total de processos incluídos na sessão dada, independentemente
	 * do tipo de inclusão.
	 *  
	 * @param sessao a sessão de referência.
	 * @return o número total de processos incluídos e não excluídos na sessão dada.
	 */
	public long totalIncluidos(Sessao sessao) {
		return sessaoPautaProcessoTrfManager.totalIncluidos(sessao);
	}

	/**
	 * Recupera o número total de processos julgados na sessão dada.
	 * 
	 * @param sessao a sessão de referência
	 * @return o número total de processos julgados na sessão dada.
	 */
	public long totalJulgados(Sessao sessao) {
		return sessaoPautaProcessoTrfManager.totalJulgados(sessao);
	}

	/**
	 * Recupera o número total de processos que receberam pedido de vista na sessão dada.
	 * 
	 * @param sessao a sessão de referência
	 * @return o número total de processos que tiveram pedido de vista na sessão dada.
	 */
	public long totalComVista(Sessao sessao) {
		return sessaoPautaProcessoTrfManager.totalComVista(sessao);
	}

	/**
	 * Recupera o número total de processos que receberam pedido de adiamento de julgamento
	 * na sessõa dada.
	 * 
	 * @param sessao a sessão de referência
	 * @return o número total de processos que tiveram pedido de adiamento
	 */
	public long totalAdiados(Sessao sessao) {
		return sessaoPautaProcessoTrfManager.totalAdiados(sessao);
	}

	/**
	 * Recupera o número total de processos que foram retirados de pauta na sessão dada.
	 * 
	 * @param sessao a sessão de referência.
	 * @return o número total de processos que foram retirados de pauta.
	 */
	public long totalRetirados(Sessao sessao) {
		return sessaoPautaProcessoTrfManager.totalRetirados(sessao);
	}

	public List<OrgaoJulgador> listOrgaoJulgadorComposicaoSessao(Sessao sessao) {
		return sessaoComposicaoOrdemDAO.listOrgaoJulgadorComposicaoSessao(sessao);
	}

	public List<TipoVoto> listTipoVotoAtivoSemRelator() {
		return tipoVotoManager.tiposVotosVogais();
	}

	public TipoVoto listTipoVotoDocumentoSessaoRelator(Sessao s, int idProcessoTrf, TipoProcessoDocumento tpdv,
			OrgaoJulgador oj) {
		return tipoVotoManager.recuperaTipoVoto(s, idProcessoTrf, tpdv, oj);
	}

	public Long qtdeVotoSessao(Sessao sessao, TipoVotoEnum tipoVoto, OrgaoJulgador oj) {
		return sessaoProcessoDocumentoVotoManager.contagemVotos(sessao, tipoVoto, oj);
	}

	public Long qtdeVotoSessaoProcesso(Sessao sessao, TipoVotoEnum tipoVoto, OrgaoJulgador oj, ProcessoTrf processo) {
		return sessaoProcessoDocumentoVotoManager.contagemVotos(sessao, processo, tipoVoto, processo.getOrgaoJulgador());
	}

	public List<SessaoProcessoDocumento> listSessaoProcessoDocumentoAtivo(Sessao s, TipoProcessoDocumento tpdv,
			OrgaoJulgador oj, List<Processo> processos) {
		return sessaoProcessoDocumentoDAO
				.listSessaoProcessoDocumentoAtivoByTipoAndOrgaoJulgador(s, tpdv, oj, processos);
	}

	public void excluirSessaoProcessoDocumentoList(List<SessaoProcessoDocumento> sessaoProcessoDocumentoList,
			Usuario usuarioExclusao) {
		Date dataExclusao = new Date();
		EntityManager em = EntityUtil.getEntityManager();
		for (SessaoProcessoDocumento sessaoProcessoDocumento : sessaoProcessoDocumentoList) {
			ProcessoDocumento pd = sessaoProcessoDocumento.getProcessoDocumento();
			pd.setAtivo(false);
			pd.setDataExclusao(dataExclusao);
			pd.setUsuarioExclusao(usuarioExclusao);
			em.merge(pd);
		}
		em.flush();
	}

	/**
	 * Define qual é o orgao julgador que será setado como ojAcompanhado, de
	 * acordo com o que for selecionado na tabela exibida na página.
	 * 
	 * @param procTrf
	 *            processoTrf
	 * @param spdv
	 *            SessaoProcessoDocumentoVoto a ser definido o ojAcompanhado.
	 */
	public void setOjAcompanhado(ProcessoTrf procTrf, SessaoProcessoDocumentoVoto spdv,
			List<VotoAcompanhadoBean> votoAcompanhadoBeanList, OrgaoJulgador usuarioOj) {
		/*
		 * [PJEII-2552] Rodrigo S. Menezes: Verifica casos em que o Magistrado se declara impedido ou suspeito
		 * sem que necessite proferir um voto. Esta é uma solução de contorno pois a funcionalidade
		 * não levou em conta a possibilidade de o Magistrado se declarar suspeito ou impedido e não necessitar
		 * proferir um voto
		 */
		if(spdv.getTipoVoto() != null && ParametroUtil.instance().getIdTipoVotoSuspeicao().equals(spdv.getTipoVoto().getIdTipoVoto())){
			spdv.setOjAcompanhado(spdv.getOrgaoJulgador());
			spdv.setImpedimentoSuspeicao(true);
		}else if(spdv.getImpedimentoSuspeicao() == true) {
			try {
				spdv.setOjAcompanhado(spdv.getOrgaoJulgador());
				spdv.setTipoVoto(tipoVotoManager.findById(ParametroUtil.instance().getIdTipoVotoSuspeicao()));
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		} else if (spdv.getTipoVoto().getContexto().equals(TipoVotoEnum.C.toString())) {
			spdv.setOjAcompanhado(procTrf.getOrgaoJulgador());
		} else {
			int count = 0;
			SessaoProcessoDocumentoVoto temp = null;
			for (VotoAcompanhadoBean vab : votoAcompanhadoBeanList) {
				if (vab.getCheck()) {
					count++;
					temp = vab.getSessaoProcessoDocumentoVoto();
				}
			}
			if (count == 1) {
				spdv.setOjAcompanhado(temp.getOrgaoJulgador());
			} else {
				spdv.setOjAcompanhado(usuarioOj);
			}
		}
	}

    /**
     * [PJEII-4330]
     * @param processoTrf O Processo
     * @return Sessao do processo que foi julgado
     */
    public SessaoPautaProcessoTrf getSessaoPautaProcessoTrfJulgado(ProcessoTrf processoTrf) {
        return sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrfJulgado(processoTrf);
    }
    
}