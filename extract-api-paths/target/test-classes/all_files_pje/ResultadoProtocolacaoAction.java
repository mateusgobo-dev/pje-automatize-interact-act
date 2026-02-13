package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;

@Name("resultadoProtocolacaoAction")
@Scope(ScopeType.EVENT)
public class ResultadoProtocolacaoAction implements Serializable{

	private static final long serialVersionUID = 8329340875804349313L;

	@In(create = true)
	private transient ProcessoJudicialManager processoJudicialManager;
	
	private String mensagemProtocolacao;
	
	private Integer idProcessoTrf;
	
	private Boolean houveErroProtocolacao;
	
	private ProcessoTrf processoJudicial;
	
	@Logger
	private Log log;

	@Create
	public void init() throws UnsupportedEncodingException {
		String decoded = SecurityTokenControler.instance().verificaChaveAcessoProtocolo();
		if(decoded == null){
			return;
		}
		String[] valores = decoded.split("\\|\\|");
		idProcessoTrf = Integer.parseInt(valores[0]);
		mensagemProtocolacao = valores[1];
		houveErroProtocolacao = Boolean.parseBoolean(valores[2]);
		if(idProcessoTrf == null || idProcessoTrf == 0) {
			return;
		}
		try {
			this.processoJudicial = processoJudicialManager.findById(idProcessoTrf);
		} catch (PJeBusinessException e) {
			log.error("Erro ao tentar capturar o processo judicial.", e);
		}
	}

	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public String getMensagemProtocolacao() {
		return mensagemProtocolacao;
	}

	public Boolean getHouveErroProtocolacao() {
		return houveErroProtocolacao;
	}
	
	public Usuario getRelator() {
		return processoJudicialManager.getRelator(processoJudicial);
	}

	public boolean getExibeRevisor() {
		return Boolean.TRUE == processoJudicial.getExigeRevisor();
	}
}
