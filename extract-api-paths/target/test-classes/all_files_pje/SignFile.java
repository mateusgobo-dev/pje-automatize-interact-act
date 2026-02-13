package br.com.infox.cliente.component.signfile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.JbpmContext;
import org.jbpm.persistence.db.DbPersistenceService;

import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.acesso.HashSession;

@BypassInterceptors
@Name("signFile")
@Scope(ScopeType.EVENT)
public class SignFile implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(SignFile.class);

	private String id;
	private String codIni;
	private String md5;
	private String sign;
	private String certChain;
	private String hashSession;
	private String data;

	public static String dateToString(Date date) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sf.format(date);
	}

	public void sign() throws Exception {
		
		HashSession session = HashSessionControler.instance().getHashSessionByHash(hashSession);

		StringBuilder sbLog = new StringBuilder();

		if (session == null) {
			String msg = "Sessão inválida: " + hashSession;
			log.error(msg);
			sbLog.append(msg).append('\n');
		} 
		else {
			
			if (!session.isIpValido(LogUtil.getIpRequest())) {
				log.warn("Ip diferente do que iniciou a sessão: " + session.getIp() + " x " + LogUtil.getIpRequest());
			}
			
			// Iniciar uma transação, se não houver transação ativa.
			Util.beginTransaction();
			
			// Obtém o contexto JBPM gerenciado pelo Seam: ManagedJbpmContext
			JbpmContext currentJbpmContext = ManagedJbpmContext.instance();
			DbPersistenceService dbPersistenceService = (DbPersistenceService) currentJbpmContext.getServices().getPersistenceService();
			
			try {
				Pessoa pessoa = EntityUtil.getEntityManager().find(Pessoa.class, session.getPessoa().getIdUsuario());

				// Se não colocar na sessão, não é registrado o usuário no log.
				if (Contexts.getSessionContext().get(Authenticator.USUARIO_LOGADO) == null) {
					Contexts.getSessionContext().set(Authenticator.USUARIO_LOGADO, pessoa);
				}
				
				try {
					DocumentoJudicialService documentoJudicialService = ComponentUtil.getComponent(DocumentoJudicialService.NAME);
					documentoJudicialService.gravarAssinatura(id, codIni, md5, sign, certChain, pessoa);
				}
				catch (Exception e) {
					e.printStackTrace();
					sbLog.append(e.getLocalizedMessage());
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
				sbLog.append("erro: " + e.getLocalizedMessage()).append('\n');
			}

			if (sbLog.length() == 0) {
				// Obtém a sessão Hibernate do JBPM e envia as modificações ao banco. Dessa forma, se na próxima iteração
				// ocorrer um erro, poderá ser feito o session.clear() para limpar as modificação da "transação" JBPM atual.
				Session s = dbPersistenceService.getSession();
				s.flush();
				
				// Commita a transação do Seam e remove a associação entra a thread corrente e a transação.
				// Por conta disso, antes de qualquer outra consulta a banco, é necessário chamar o Util.beginTransaction();
				Util.commitTransction();
			} else {
				// Mesma solução desenvolvida para não "sumir" os processos na movimentação.
				JbpmUtil.clearAndClose(currentJbpmContext);
				
				try{
					// Libera a associação entre a transação (do Seam) e a thread corrente. Dessa forma o 
					// Util.beginTransaction pode iniciar outra transação e associar à thread corrente.
					//
					// Não foi usado o Util.rollbackTransction, pois ele checa se a transação está ativa, porém, dependendo da exceção (método Work.isRollbackRequired()), 
					// o interceptor do Seam pode "marcar" a transação para rollback, o que deixa a transação como inativa,
					// mas deixa a thread associada à transação inativa (o que prejudica a próxima iteração, quando ocorrer o beginTransaction - exceção:
					// "thread is already associated with a transaction!").
					Transaction.instance().rollback();
				} catch (Exception e1){}
				
				// Remove dos contextos do Seam o contexto do JBPM (ManagedJbpmContext.instance()), dessa forma, na próxima 
				// iteração, o Seam irá criar outro contexto JBPM (ManagedJbpmContext.create()), associado a uma sessão nova do Hibernate, 
				// pois ao dar "rollback" no contexto JBPM a sessão tem que ser fechada (JbpmUtil.clearAndClose()). 
				// Isso é necessário, pois a implementação do JBPM do Seam não usa JTA.
				Contexts.removeFromAllContexts("org.jboss.seam.bpm.jbpmContext");
				
				// Iniciar uma transação, se não houver transação ativa.
				Util.beginTransaction();
			}
		}

		gravarResponseOperacao(sbLog);
	}

	private void gravarResponseOperacao(StringBuilder sbLog) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		response.setContentType("text/plain;charset=ISO-8859-1");
		response.setContentLength(sbLog.length());
		response.setHeader("Content-disposition", "inline; filename=\"" + "info.txt" + "\"");
		byte[] data = sbLog.toString().getBytes(Charset.forName("ISO-8859-1"));
		try {
			OutputStream out = response.getOutputStream();
			out.write(data);
			out.flush();
			facesContext.responseComplete();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCodIni() {
		return codIni;
	}

	public void setCodIni(String codIni) {
		this.codIni = codIni;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getHashSession() {
		return hashSession;
	}

	public void setHashSession(String hashSession) {
		this.hashSession = hashSession;
	}

	public String getData() {
		return data;
	}

	public void setData(String data){
		this.data = data;
	}
}
