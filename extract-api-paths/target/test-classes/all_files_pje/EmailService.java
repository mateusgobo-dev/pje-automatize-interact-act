package br.com.infox.ibpm.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.mail.Message.RecipientType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.log.Log;

import br.com.infox.command.EMailData;
import br.com.infox.command.SendmailCommand;
import br.com.infox.exceptions.SendMailException;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

@Name(EmailService.NAME)
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.EVENT)
@AutoCreate
public class EmailService implements Serializable {

    private static final long serialVersionUID = 1L;
    public final static String NAME = "emailService";
    private String template = "/WEB-INF/email/emailTemplate.xhtml";

    @Logger
    private transient Log log;

    public void enviarEmail(List<UsuarioLogin> usuariosLogin, String subject, String body,
            RecipientType recipientType) {
        EMailData data = null;
        for (UsuarioLogin usuarioLogin : usuariosLogin) {
            data = ComponentUtil.getComponent(EMailData.NAME);
            data.setUseHtmlBody(true);
            data.getRecipientList().clear();
            data.getRecipientList().add(usuarioLogin);
            data.setRecipientType(recipientType);
            data.setSubject(subject);
            data.setBody((String) Expressions.instance().createValueExpression(body).getValue());
            try {
                new SendmailCommand().execute(template);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public void enviarEmail(List<UsuarioLogin> usuariosLogin, String subject, String body) {
        enviarEmail(usuariosLogin, subject, body, RecipientType.TO);
    }

    public void enviarEmail(UsuarioLogin usuarioLogin, String subject, String body) {
        enviarEmail(Arrays.asList(usuarioLogin), subject, body);
    }

    public void enviarEmail(String nome, List<String> emails, String subject, String body,
            RecipientType recipientType) {
        EMailData data = null;
        for (String email : emails) {
            data = ComponentUtil.getComponent(EMailData.NAME);
            data.setUseHtmlBody(true);
            data.setRecipientName(nome);
            data.setRecipientAdress(email);
            data.setRecipientType(recipientType);
            data.setSubject(subject);
            data.setBody((String) Expressions.instance().createValueExpression(body).getValue());
            try {
                new SendmailCommand().execute(template);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public void enviarEmail(String nome, List<String> emails, String subject, String body) {
        enviarEmail(nome, emails, subject, body, RecipientType.TO);
    }

    public void enviarEmail(String nome, String emails, String subject, String body) {
        this.enviarEmail(nome, Arrays.asList(emails), subject, body);
    }

    public void enviarEmailBcc(List<String> emails, String subject, String body) {
        enviarEmail("", emails, subject, body, RecipientType.BCC);
    }

    public void enviarEmailAgrupado(List<String> emails, String subject, String body, RecipientType recipientType)
            throws SendMailException {

        EMailData data = ComponentUtil.getComponent(EMailData.NAME);

        data.setUseHtmlBody(true);
        // Não há necessidade de especificar nome a nome
        data.setRecipientName("");
        data.setRecipientAdressList(emails);
        data.setRecipientType(recipientType);
        data.setSubject(subject);
        data.setBody((String) Expressions.instance().createValueExpression(body).getValue());
        new SendmailCommand().execute(template);
    }
}
