package br.com.infox.editor.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.editor.exception.AnotacaoException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.editor.Anotacao;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoItemConsideracoes;
import br.jus.pje.nucleo.enums.editor.NivelVisibilidadeAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAcolhidoAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusAnotacao;
import br.jus.pje.nucleo.enums.editor.StatusCienciaAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;

@Name(AnotacaoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AnotacaoManager extends GenericManager {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "anotacaoManager";
	
	private OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
	
	private UsuarioLocalizacao usuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
	
	@In
	private ParametroUtil parametroUtil;
	
	private List<Anotacao> anotacoes = new ArrayList<Anotacao>(0);
	
	public List<Anotacao> getAnotacoesDoDocumento(ProcessoDocumentoEstruturado documento) {
		if ((anotacoes == null || anotacoes.isEmpty()) || 
				(!anotacoes.get(0).getDocumento().getIdProcessoDocumentoEstruturado().equals(documento.getIdProcessoDocumentoEstruturado()))) {
			anotacoes = documento.getAnotacaoList();
			Iterator<Anotacao> it = anotacoes.iterator();
			while (it.hasNext()) {
				Anotacao anotacao = it.next();
				if (anotacao.getStatusAnotacao() == StatusAnotacao.E ||
					(anotacao.getNivelVisibilidadeAnotacao() == NivelVisibilidadeAnotacao.PRIVADA && !anotacao.getPessoaCriacao().equals(usuarioLocalizacaoAtual.getUsuario())) || 
					(anotacao.getNivelVisibilidadeAnotacao() == NivelVisibilidadeAnotacao.INTERNA && !anotacao.getOrgaoJulgador().equals(orgaoJulgadorAtual))) {
					it.remove();
				}
			}
		}
		return anotacoes;
	}
	
	public void setAnotacoesDoDocumento(List<Anotacao> anotacoes) {
		this.anotacoes = anotacoes;
	}
	
	public void salvarAnotacoes() {
		for (Anotacao anotacao : anotacoes) {
			if (isManaged(anotacao)) {
				update(anotacao);
			} else {
				persist(anotacao);
			}
		}
	}
	
	public Anotacao criarAnotacao(ProcessoDocumentoEstruturadoTopico topico) {
		if (topico.getTopico().getTipoTopico() == TipoTopicoEnum.ITEM_CONSIDERACOES) {
			throw new AnotacaoException("Tópico de Item de Considerações não permite anotações");
		}
		
		Anotacao anotacao = new Anotacao();
		anotacao.setConteudo("");
		anotacao.setDataCriacao(new Date());
		anotacao.setDataAlteracao(anotacao.getDataCriacao());
		anotacao.setDataAlteracaoStatus(anotacao.getDataCriacao());
		anotacao.setDestaque(false);
		anotacao.setNivelVisibilidadeAnotacao(NivelVisibilidadeAnotacao.PRIVADA);
		anotacao.setPessoaCriacao(usuarioLocalizacaoAtual.getUsuario());
		anotacao.setOrgaoJulgador(orgaoJulgadorAtual);
		anotacao.setTipoAnotacao(TipoAnotacao.ANOTACAO);
		anotacao.setStatusAnotacao(StatusAnotacao.N);
		anotacao.setDocumento(topico.getProcessoDocumentoEstruturado());
		anotacao.setTopico(topico);
		
		if (!parametroUtil.isPrimeiroGrau()) {
			if (topico.getTopico().getTipoTopico() == TipoTopicoEnum.IT_DISP_VOTO) {
				if (orgaoJulgadorPossuiAnotacoesDoTipo(TipoAnotacao.SUGESTAO_DISPOSITIVO, topico.getProcessoDocumentoEstruturado())) {
					throw new AnotacaoException("O Órgão Julgador já possui uma anotação do tipo Sugestão Dispositivo no documento");
				}
				if (isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf())) {
					throw new AnotacaoException("O relator do processo não pode criar Sugestões de Dispositivo");
				}
				anotacao.setTipoAnotacao(TipoAnotacao.SUGESTAO_DISPOSITIVO);
				anotacao.setConteudo(topico.getConteudo());
			} else if (topico.getTopico().getTipoTopico() == TipoTopicoEnum.CONSIDERACOES) {
				if (orgaoJulgadorPossuiAnotacoesDoTipo(TipoAnotacao.VOTO, topico.getProcessoDocumentoEstruturado())) {
					throw new AnotacaoException("O órgão julgador já possui voto liberado para este documento");
				}
				if (isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf())) {
					throw new AnotacaoException("O relator não pode criar um voto");
				}
				anotacao.setTipoAnotacao(TipoAnotacao.VOTO);
			}
		}
		
		getAnotacoesDoDocumento(topico.getProcessoDocumentoEstruturado()).add(anotacao);
		return anotacao;
	}
	
	public Anotacao atualizarStatusAnotacao(Anotacao anotacao) {
		if (anotacao.getStatusAnotacao() == StatusAnotacao.C) {
			anotacao.setNivelVisibilidadeAnotacao(NivelVisibilidadeAnotacao.INTERNA);
		} else if (anotacao.getStatusAnotacao() == StatusAnotacao.L) {
			anotacao.setNivelVisibilidadeAnotacao(NivelVisibilidadeAnotacao.PUBLICA);
		} else if (anotacao.getStatusAnotacao() == StatusAnotacao.R) {
			if (anotacao.getTipoAnotacao() == TipoAnotacao.DIVERGENCIA && anotacao.getStatusAcolhidoAnotacao() != null && !isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf())) {
				anotacao.setStatusCienciaAnotacao(StatusCienciaAnotacao.R);
			}
		} else if (anotacao.getStatusAnotacao() == StatusAnotacao.E) {
			anotacoes.remove(anotacao);
		}
		anotacao.setDataAlteracaoStatus(new Date());
		return anotacao;
	}
	
	public boolean podeReabrirAnotacao(Anotacao anotacao) {
		return !parametroUtil.isPrimeiroGrau() && anotacao.getTipoAnotacao() == TipoAnotacao.SUGESTAO_DISPOSITIVO && orgaoJulgadorAtual.equals(anotacao.getOrgaoJulgador());
	}
	
	public boolean podeExcluirAnotacao(Anotacao anotacao) {
		return anotacao.getStatusAnotacao() != StatusAnotacao.L && anotacao.getStatusAnotacao() != StatusAnotacao.R;
	}
	
	public boolean podeLiberarAnotacao(Anotacao anotacao) {
		return anotacao.getStatusAnotacao() == StatusAnotacao.C || anotacao.getStatusAnotacao() == StatusAnotacao.N;
	}

	public boolean podeConcluirAnotacao(Anotacao anotacao) {
		return anotacao.getStatusAnotacao() == StatusAnotacao.N;
	}
	
	public boolean podeRetirarAnotacao(Anotacao anotacao) {
		boolean podeRetirarAnotacao = anotacao.getStatusAnotacao() == StatusAnotacao.L && 
				anotacao.getTipoAnotacao() != TipoAnotacao.VOTO && 
				anotacao.getOrgaoJulgador().equals(orgaoJulgadorAtual);
		if (anotacao.getTipoAnotacao() == TipoAnotacao.DIVERGENCIA) {
			podeRetirarAnotacao = podeRetirarAnotacao && 
					!isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf()) && 
					(anotacao.getStatusAcolhidoAnotacao() == null || anotacao.getStatusAcolhidoAnotacao() != StatusAcolhidoAnotacao.A); 
		}
		return podeRetirarAnotacao;
	}
	
	public boolean podeDestacarAnotacao(Anotacao anotacao) {
		return !parametroUtil.isPrimeiroGrau() && anotacao.getStatusAnotacao() != StatusAnotacao.L && 
			   anotacao.getTipoAnotacao() != TipoAnotacao.SUGESTAO_DISPOSITIVO && 
			   anotacao.getTipoAnotacao() != TipoAnotacao.VOTO;
	}
	
	public boolean podeCriarDivergencia(Anotacao anotacao) {
		return !parametroUtil.isPrimeiroGrau() && anotacao.getStatusAnotacao() != StatusAnotacao.L && 
			   anotacao.getTipoAnotacao() != TipoAnotacao.SUGESTAO_DISPOSITIVO &&
			   anotacao.getTipoAnotacao() != TipoAnotacao.VOTO && 
			   !isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf());
	}
	
	public Integer temDivergencia(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if(TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) && a.getStatusAnotacao() != StatusAnotacao.R &&
					(a.getStatusAnotacao() == StatusAnotacao.L || a.getOrgaoJulgador().equals(orgaoJulgadorAtual))) {
				if (!isRelator(orgaoJulgadorAtual, a.getDocumento().getProcessoTrf()) || a.getStatusAcolhidoAnotacao() != null) {
					return a.getCodigoIdentificador();
				}
			}
		}
		return null;
	}
	
	public Integer temDivergenciaNaoConcluidaNaoLiberada(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if(TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
					(a.getStatusAnotacao() == StatusAnotacao.N || a.getStatusAnotacao() == StatusAnotacao.C) && 
					podeVisualizarAnotacao(a)){
				return a.getCodigoIdentificador();
			}
		}
		return null;
	}
	
	public Integer temDivergenciaAcaoPendente(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if(TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
					a.getStatusAcolhidoAnotacao() == null &&
					isRelator(orgaoJulgadorAtual, a.getDocumento().getProcessoTrf()) && 
					a.getStatusAnotacao() != StatusAnotacao.R){
				return a.getCodigoIdentificador();
			}
			if(TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
					a.getStatusAcolhidoAnotacao() != null &&
					a.getStatusCienciaAnotacao() == null &&
					a.getOrgaoJulgador().equals(orgaoJulgadorAtual)){
				return a.getCodigoIdentificador();
			}			
		}
		return null;
	}
	
	public Integer temAnotacao(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if((TipoAnotacao.ANOTACAO.equals(a.getTipoAnotacao()) || a.getTipoAnotacao() == TipoAnotacao.SUGESTAO_DISPOSITIVO) &&
					((a.getOrgaoJulgador().equals(orgaoJulgadorAtual) && StatusAnotacao.C.equals(a.getStatusAnotacao())) || StatusAnotacao.L.equals(a.getStatusAnotacao()))){
				return a.getCodigoIdentificador();
			}
		}
		return null;
	}
	
	public Integer temAnotacaoNaoConcluida(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if ((a.getTipoAnotacao() == TipoAnotacao.ANOTACAO || a.getTipoAnotacao() == TipoAnotacao.VOTO || a.getTipoAnotacao() == TipoAnotacao.SUGESTAO_DISPOSITIVO) && 
					a.getStatusAnotacao() == StatusAnotacao.N && a.getPessoaCriacao().equals(usuarioLocalizacaoAtual.getUsuario()) || 
				(a.getTipoAnotacao() == TipoAnotacao.VOTO && a.getStatusAnotacao() == StatusAnotacao.C && a.getOrgaoJulgador().equals(orgaoJulgadorAtual))) {
				return a.getCodigoIdentificador();
			}
		}
		return null;
	}
	
	public Integer temDestaqueDivergencia(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if(a.getDestaque() && 
					TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
					StatusAnotacao.L.equals(a.getStatusAnotacao())){
				return a.getCodigoIdentificador();
			}
		}
		return null;
	}
	
	public Integer temDestaqueDivergenciaNaoConcluidoNaoLiberado(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if(a.getDestaque() && TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) && 
					(a.getStatusAnotacao() == StatusAnotacao.N || a.getStatusAnotacao() == StatusAnotacao.C) && 
					podeVisualizarAnotacao(a)) {
				return a.getCodigoIdentificador();
			}
		}
		return null;
	}
	
	public Integer temDestaqueAnotacao(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if(a.getDestaque() && 
					!TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
					StatusAnotacao.L.equals(a.getStatusAnotacao())){
				return a.getCodigoIdentificador();
			}
		}
		return null;
	}
	
	public Integer temDestaqueAnotacaoNaoConcluidoNaoLiberado(ProcessoDocumentoEstruturadoTopico topico){
		for(Anotacao a : getAnotacoesDoTopico(topico)){
			if(a.getDestaque() && !TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
					(a.getStatusAnotacao() == StatusAnotacao.N || a.getStatusAnotacao() == StatusAnotacao.C) && 
					podeVisualizarAnotacao(a)){
				return a.getCodigoIdentificador();
			}
		}
		return null;
	}
	
	public List<Anotacao> getAnotacoesDoTopico(ProcessoDocumentoEstruturadoTopico topico) {
		List<Anotacao> anotacoesDoTopico = new ArrayList<Anotacao>();
		for (Anotacao anotacao : getAnotacoesDoDocumento(topico.getProcessoDocumentoEstruturado())) {
			if (anotacao.getTopico().equals(topico)) {
				anotacoesDoTopico.add(anotacao);
			}
		}
		return anotacoesDoTopico;
	}
	
	private boolean orgaoJulgadorPossuiAnotacoesDoTipo(TipoAnotacao tipoAnotacao, ProcessoDocumentoEstruturado documento) {
		for (Anotacao anotacao : getAnotacoesDoDocumento(documento)) {
			if (anotacao.getStatusAnotacao() != StatusAnotacao.E && anotacao.getTipoAnotacao() == tipoAnotacao && anotacao.getOrgaoJulgador().equals(orgaoJulgadorAtual)) {
				return true;
			}
		}
		
		if (tipoAnotacao == TipoAnotacao.VOTO) {
			for (ProcessoDocumentoEstruturadoTopico processoTopico : documento.getProcessoDocumentoEstruturadoTopicoList()) {
				if (processoTopico.getTopico().getTipoTopico() == TipoTopicoEnum.ITEM_CONSIDERACOES) {
					TopicoItemConsideracoes topicoItemConsideracoes = (TopicoItemConsideracoes) EntityUtil.removeProxy(processoTopico.getTopico());
					if (topicoItemConsideracoes.getOrgaoJulgador().equals(orgaoJulgadorAtual)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String getNomePessoaCriacao(Anotacao anotacao) {
		if (anotacao.getOrgaoJulgador().equals(orgaoJulgadorAtual)) {
			return anotacao.getPessoaCriacao().getNome();
		} else {
			return anotacao.getOrgaoJulgador().getOrgaoJulgador();
		}
	}
	/**
	 * Remove uma alteração juntamente com o seu histórico;
	 * Também remove da lista em memória do ProcessoDocumentoEstruturado associado
	 * @param anotacao que vai ser removida
	 * PJE-JT Antonio Lucas 12/03/2013 
	 * Método refatorado para remover do banco e a referência de memória, além de remover o histórico
	 */
	public void removerAnotacao(Anotacao anotacao) {
		ProcessoDocumentoEstruturado pde = anotacao.getDocumento();
        EntityManager entityManager = EntityUtil.getEntityManager();
		this.removerHistoricoDaAnotacao(anotacao);
        entityManager.remove(anotacao);//remove no banco de dados.
		pde.getAnotacaoList().remove(anotacao);//remove da lista em memória
	}

	public Anotacao reabrirAnotacao(Anotacao anotacao) {
		if (anotacao.getTipoAnotacao() != TipoAnotacao.SUGESTAO_DISPOSITIVO) {
			throw new AnotacaoException("Só sugestões de dispositivo podem ser reabertas");
		}
		
		anotacao.setDataAlteracao(new Date());
		anotacao.setStatusAnotacao(StatusAnotacao.N);
		anotacao.setNivelVisibilidadeAnotacao(NivelVisibilidadeAnotacao.PRIVADA);
		return anotacao;
	}
	
	public boolean podeEditarAnotacao(Anotacao anotacao) {
		return anotacao.getStatusAnotacao() == StatusAnotacao.N || anotacao.getStatusAnotacao() == StatusAnotacao.C;
	}
	
	public boolean podeMostrarAcoesDivergenciaRelator(Anotacao anotacao) {
		return anotacao.getTipoAnotacao() == TipoAnotacao.DIVERGENCIA && anotacao.getStatusAnotacao() != StatusAnotacao.R && 
			   isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf()) && 
			   anotacao.getStatusAcolhidoAnotacao() == null;
	}

	public boolean podeMarcarDivergenciaComoCiente(Anotacao anotacao) {
		return anotacao.getTipoAnotacao() == TipoAnotacao.DIVERGENCIA && anotacao.getStatusAnotacao() != StatusAnotacao.R && 
			   !isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf()) && 
			   anotacao.getStatusAcolhidoAnotacao() == StatusAcolhidoAnotacao.A && 
			   anotacao.getStatusCienciaAnotacao() == null && 
			   anotacao.getOrgaoJulgador().equals(orgaoJulgadorAtual);
	}
	
	public boolean podeManterDivergencia(Anotacao anotacao) {
		return anotacao.getTipoAnotacao() == TipoAnotacao.DIVERGENCIA && anotacao.getStatusAnotacao() != StatusAnotacao.R && 
				   !isRelator(orgaoJulgadorAtual, anotacao.getDocumento().getProcessoTrf()) &&
				   anotacao.getStatusAcolhidoAnotacao() != null &&
				   anotacao.getStatusAcolhidoAnotacao() != StatusAcolhidoAnotacao.A &&
				   anotacao.getStatusCienciaAnotacao() == null && 
				   anotacao.getOrgaoJulgador().equals(orgaoJulgadorAtual);
	}
	
	public boolean isRelator(OrgaoJulgador orgaoJulgador, ProcessoTrf processoTrf) {
		return orgaoJulgador != null && processoTrf != null && orgaoJulgador.equals(processoTrf.getOrgaoJulgador());
	}
	
	public String buildTituloAnotacao(Anotacao anotacao) {
		StringBuilder sb = new StringBuilder();
		if (anotacao.getTipoAnotacao() == TipoAnotacao.ANOTACAO) {
			if (anotacao.getNivelVisibilidadeAnotacao() == NivelVisibilidadeAnotacao.PUBLICA) {
				sb.append("Anotação Pública");
			} else {
				sb.append("Anotação");
			}
		} else if (anotacao.getTipoAnotacao() == TipoAnotacao.DIVERGENCIA) {
			sb.append("Divergência");
		} else if (anotacao.getTipoAnotacao() == TipoAnotacao.VOTO || anotacao.getTipoAnotacao() == TipoAnotacao.SUGESTAO_DISPOSITIVO) {
			sb.append(anotacao.getOrgaoJulgador().getOrgaoJulgador());
		}
		
		if (anotacao.getDestaque()) {
			if (sb.length() == 0) {
				sb.append("Destaque");
			} else {
				sb.append(" - Destaque");
			}
		}
		return sb.toString();
	}
	
	public String buildObservacaoAnotacao(Anotacao anotacao) {
		StringBuilder sb = new StringBuilder();
		if (anotacao.getStatusAnotacao() == StatusAnotacao.N) {
			sb.append("(não concluída)");
		} else if (anotacao.getStatusAnotacao() == StatusAnotacao.C) {
			sb.append("(aguardando liberação)");
		} else if (anotacao.getStatusAnotacao() == StatusAnotacao.R) {
			sb.append("(retirada)");
		} else if (anotacao.getStatusAnotacao() == StatusAnotacao.L) {
			if (anotacao.getTipoAnotacao() == TipoAnotacao.DIVERGENCIA) {
				if (anotacao.getStatusAcolhidoAnotacao() == null) {
					sb.append("(aguardando análise do relator)");
				} else if (anotacao.getStatusAcolhidoAnotacao() == StatusAcolhidoAnotacao.A) {
					sb.append("(acolhida - ");
					sb.append(getObservacaoRevisor(anotacao));
					sb.append(")");
				} else if (anotacao.getStatusAcolhidoAnotacao() == StatusAcolhidoAnotacao.P) {
					sb.append("(acolhida parcialmente - ");
					sb.append(getObservacaoRevisor(anotacao));
					sb.append(")");
				} else if (anotacao.getStatusAcolhidoAnotacao() == StatusAcolhidoAnotacao.R) {
					sb.append("(recusada - ");
					sb.append(getObservacaoRevisor(anotacao));
					sb.append(")");
				}
			}
		}
		
		if (!anotacao.getTopico().isAtivo() || !anotacao.getTopico().isHabilitado()) {
			String status = !anotacao.getTopico().isAtivo() ? "excluído" : "desabilitado";
			if (sb.length() == 0) {
				sb.append("(O tópico ");
				sb.append(Jsoup.clean(anotacao.getTopico().getTitulo(), new Whitelist()));
				sb.append(" foi ");
				sb.append(status);
				sb.append(")");
			} else {
				sb.append(" - O tópico ");
				sb.append(Jsoup.clean(anotacao.getTopico().getTitulo(), new Whitelist()));
				sb.append(" foi ");
				sb.append(status);
			}
		}
		return sb.toString();
	}
	
	private String getObservacaoRevisor(Anotacao anotacao) {
		if (anotacao.getStatusCienciaAnotacao() == null) {
			return "aguardando análise pelo autor da divergência";
		} else if (anotacao.getStatusCienciaAnotacao() == StatusCienciaAnotacao.C) {
			return "ciente pelo autor da divergência";
		} else if (anotacao.getStatusCienciaAnotacao() == StatusCienciaAnotacao.M) {
			return "mantida pelo autor da divergência";
		}
		return null;
	}
	
	private boolean podeVisualizarAnotacao(Anotacao anotacao) {
		if (anotacao.getStatusAnotacao() == StatusAnotacao.N) {
			return anotacao.getPessoaCriacao().equals(usuarioLocalizacaoAtual.getUsuario());
		}
		return anotacao.getOrgaoJulgador().equals(orgaoJulgadorAtual);
	}

	
	/**
	 * [PJEII-5293] Método que identifica a existência de divergências dentre as anotações do documento, bem como seus respectivos status:
	 * 1 - Divergência com análise pendente
	 * 2 - Divergência não concluída/liberada
	 * 3 - Divergência
	 * 
	 * Obs: Método utilizado na grid de processos apenas, não utilizado pelo editor.
	 * 
	 * @author fernando.junior (05/02/2013)
	 */
	public int temDivergencias(ProcessoDocumentoEstruturado documento) {
		int retorno = 0;
		
		for(Anotacao a : getAnotacoesDoDocumento(documento)){
			 
			/*  StatusAnotacao (ENUM)
					C("Concluída"), L("Liberada"), R("Retirada"), E("Excluída"), N("Não Concluída");
				
				StatusAcolhidoAnotacao (ENUM)
					A("Acolhido"), P("Acolhido parcialmente"), R("Retirada");
				
				StatusCienciaAnotacao
					M("Mantenho"), R("Retiro"), C("Ciente");
			 */
			
			
			//Trecho modificado devido a ISSUE PJEII-5560
			/*
			 * Retorno = 0 - NAO HA DIVERGENCIA
			 * Retorno = 1 - DIVERGENCIA PENDENTE
			 * Retorno = 2 - DIVERGENCIA NAO CONCLUIDA OU LIBERADA
			 * Retorno = 3 - HA DIVERGENCIA
			 * */
			
			//Se o status for RETIRADA, retirar o ícone de divergencia para todos
			if (StatusAcolhidoAnotacao.R.equals(a.getStatusAcolhidoAnotacao())){
				retorno = 0; //NÃO HÁ ÍCONE DE DIVERGENCIA
			}
			else{
				//Se o status for ACOLHIDA (pelo relator) e o revisor dá CIENCIA ("C"), retirar o ícone de divergencia para todos				
				if ((StatusAcolhidoAnotacao.A.equals(a.getStatusAcolhidoAnotacao())) && (StatusCienciaAnotacao.C.equals(a.getStatusCienciaAnotacao())))  {
					retorno = 0; //NÃO HÁ ÍCONE DE DIVERGENCIA
				}
				else{
					//DIVERGENCIA PENDENTE (Retorno = 1)
					//Se for RELATOR e alguem criou uma divergencia (Divergencia pendente)
					if(TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
							a.getStatusAcolhidoAnotacao() == null &&
							isRelator(orgaoJulgadorAtual, a.getDocumento().getProcessoTrf()) && 
							a.getStatusAnotacao() != StatusAnotacao.R){
						return 1; //divergencia pendente
					}
					else{
						//DIVERGENCIA PENDENTE (RETORNO = 1)
						//Se for o revisor que criou, e o relator acolhe, acolhe parcialmente ou retira, SE O REVISOR NÃO DEU CIENCIA
						Usuario usuarioLogado = Authenticator.getUsuarioLogado();
						if(TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
								(StatusAcolhidoAnotacao.A.equals(a.getStatusAcolhidoAnotacao()) || (StatusAcolhidoAnotacao.P.equals(a.getStatusAcolhidoAnotacao()) || (StatusAcolhidoAnotacao.R.equals(a.getStatusAcolhidoAnotacao())) &&								
								a.getStatusCienciaAnotacao() == null &&
								a.getOrgaoJulgador().equals(orgaoJulgadorAtual) &&
								a.getPessoaCriacao().equals(usuarioLogado)))){
							return 1; //divergencia pendente							
						}												
					}	
					
					// Divergência não concluída/liberada
					if(TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) &&
							(a.getStatusAnotacao() == StatusAnotacao.N || a.getStatusAnotacao() == StatusAnotacao.C) && 
							podeVisualizarAnotacao(a)){
						retorno = 2;
					}
					
					// Divergência
					if((retorno != 2) && (TipoAnotacao.DIVERGENCIA.equals(a.getTipoAnotacao()) && a.getStatusAnotacao() != StatusAnotacao.R &&
							(a.getStatusAnotacao() == StatusAnotacao.L || a.getOrgaoJulgador().equals(orgaoJulgadorAtual)))) {
						
						if (!isRelator(orgaoJulgadorAtual, a.getDocumento().getProcessoTrf()) || a.getStatusAcolhidoAnotacao() != null) {
							retorno = 3;
						}
					}
				}
			}			
		}
		
		return retorno;
	}

	/**
	 * [PJEII-5293] Método que identifica a existência de destaques dentre as anotações do documento, bem como seus respectivos status:
	 * 1 - Destaque não concluído/liberado
	 * 2 - Destaque
	 * 
	 * Obs: Método utilizado na grid de processos apenas, não utilizado pelo editor.
	 * 
	 * @author fernando.junior (05/02/2013)
	 */
	public int temDestaques(ProcessoDocumentoEstruturado documento) {
		int retorno = 0;
		
		for (Anotacao a : getAnotacoesDoDocumento(documento)) {
			// Destaque não concluído/liberado
			if(a.getDestaque() && (a.getStatusAnotacao() == StatusAnotacao.N || a.getStatusAnotacao() == StatusAnotacao.C) && 
					podeVisualizarAnotacao(a)) {
				return 1;
			}
			
			// Destaque
			if(a.getDestaque() && StatusAnotacao.L.equals(a.getStatusAnotacao())){
				retorno = 2;
			}
		}
		
		return retorno;
	}

	/**
	 * [PJEII-5293] Método que identifica a existência de anotações comuns dentre as anotações do documento, bem como seus respectivos status:
	 * 1 - Anotação não concluída
	 * 2 - Anotação
	 * 
	 * Obs: Método utilizado na grid de processos apenas, não utilizado pelo editor.
	 * 
	 * @author fernando.junior (05/02/2013)
	 */
	public int temAnotacoes(ProcessoDocumentoEstruturado documento) {
		int retorno = 0;
		
		for (Anotacao a : getAnotacoesDoDocumento(documento)) {
			// Anotação não concluída
			if (((a.getTipoAnotacao() == TipoAnotacao.ANOTACAO && !a.getDestaque()) || a.getTipoAnotacao() == TipoAnotacao.VOTO || 
					a.getTipoAnotacao() == TipoAnotacao.SUGESTAO_DISPOSITIVO) && a.getStatusAnotacao() == StatusAnotacao.N && 
					a.getPessoaCriacao().equals(usuarioLocalizacaoAtual.getUsuario()) || 
				(a.getTipoAnotacao() == TipoAnotacao.VOTO && a.getStatusAnotacao() == StatusAnotacao.C && a.getOrgaoJulgador().equals(orgaoJulgadorAtual))) {
				return 1;
			}
			
			// Anotação
			if(((TipoAnotacao.ANOTACAO.equals(a.getTipoAnotacao()) && !a.getDestaque()) || a.getTipoAnotacao() == TipoAnotacao.SUGESTAO_DISPOSITIVO) &&
					((a.getOrgaoJulgador().equals(orgaoJulgadorAtual) && StatusAnotacao.C.equals(a.getStatusAnotacao())) || 
							StatusAnotacao.L.equals(a.getStatusAnotacao()))){
				retorno = 2;
			}
		}
		
		return retorno;
	}

	@SuppressWarnings("unchecked")
	public void removerAnotacoes(ProcessoDocumentoEstruturado pd) {
		if (pd != null){
	        EntityManager entityManager = EntityUtil.getEntityManager();
	        //recupera as anotacoes
	        Query q = entityManager.createQuery("select a.idAnotacao from Anotacao a where a.documento = :pd");
	        q.setParameter("pd", pd);
	        List<Integer> anotacoes = q.getResultList();
	        //Se não tem anotações, não tem o que apagar
	        if (anotacoes != null && !anotacoes.isEmpty()){
				//remove todos os historicos das anotacoes
		        q = entityManager.createQuery("delete from HistoricoAnotacao where anotacao.idAnotacao in (:lista)");
		        q.setParameter("lista", anotacoes);
		        q.executeUpdate();
		        entityManager.flush();
				//remove todas as anotacoes
		        q = entityManager.createQuery("delete from Anotacao a where a.idAnotacao in (:lista)");
		        q.setParameter("lista", anotacoes);
		        q.executeUpdate();
		        entityManager.flush();
	        }
		}
	}
	
	private void removerHistoricoDaAnotacao(Anotacao anotacao) {
        EntityManager entityManager = EntityUtil.getEntityManager();
        Query q = entityManager.createQuery("delete from HistoricoAnotacao where anotacao = :a");
        q.setParameter("a", anotacao);
        q.executeUpdate();
	}
}
