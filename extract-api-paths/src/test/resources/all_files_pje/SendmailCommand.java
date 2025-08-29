/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.command;

import java.io.Serializable;
import java.security.Security;

import javax.net.ssl.SSLSocketFactory;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;

import br.com.infox.exceptions.SendMailException;

public class SendmailCommand implements Serializable{

	private static final long serialVersionUID = 1L;

	public void execute(String templateFile) throws SendMailException {
		Renderer renderer = Renderer.instance();
		FacesMessages fm = FacesMessages.instance();
		String msg;
		try{
			String name = SSLSocketFactory.class.getName();
			Security.setProperty("ssl.SocketFactory.provider", name);
			renderer.render(templateFile);
			msg = "Email enviado com sucesso. ";
			fm.clear();
			fm.add(msg);
		} catch (Exception e){
		    msg = "Erro ao enviar email: " + e.getLocalizedMessage();
			fm.add(msg, e);
			throw new SendMailException(e);
		} finally{
			Security.setProperty("ssl.SocketFactory.provider", "");
		}
	}
	
}