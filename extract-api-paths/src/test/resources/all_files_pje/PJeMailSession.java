package org.jboss.seam.mail;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.DEPLOYMENT;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.naming.NamingException;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Naming;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.util.StringUtil;

@Name("org.jboss.seam.mail.mailSession")
@Install(precedence = DEPLOYMENT, classDependencies = "javax.mail.Session")
@Scope(APPLICATION)
@BypassInterceptors
public class PJeMailSession extends MailSession {

    private static final long serialVersionUID = -1486371176204439669L;
    private static final LogProvider log = Logging.getLogProvider(PJeMailSession.class);
    private Session session;
    private Date mailParamUpdate;

    @Override
    @Unwrap
    public Session getSession() throws NamingException {
        Parametro mailParam = getMailParam();
        if (mailParam != null && mailParamUpdate != null && mailParam.getDataAtualizacao().after(mailParamUpdate)) {
            create();
        }

        if (session == null) {
            return (Session) Naming.getInitialContext().lookup(getSessionJndiName());
        } else {
            return session;
        }
    }

    @Override
    @Create
    public MailSession create() {
        if (getSessionJndiName() == null) {
            Parametro mailParam = getMailParam();
            if (mailParam != null) {
                try {
					setMailAttributes(mailParam);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
            createSession();
        }
        return this;
    }

    private Parametro getMailParam() {
        ParametroService parametroService = ComponentUtil.getComponent(ParametroService.NAME);
        return parametroService.findByName(Parametros.CONFIGURACAO_SMTP);
    }

    private void setMailAttributes(Parametro mailParam) throws JSONException {
        JSONObject jsonObject = new JSONObject(new JSONTokener(mailParam.getValorVariavel()));
        setPort(jsonObject.optInt("port"));
        setDebug(jsonObject.optBoolean("debug"));
        setUsername(jsonObject.optString("username"));
        setPassword(jsonObject.optString("password"));
        setHost(jsonObject.optString("host"));
        setSsl(jsonObject.optBoolean("ssl"));

        mailParamUpdate = mailParam.getDataAtualizacao();
    }

    private void createSession() {
        if (getPort() != null) {
            log.debug("Creating JavaMail Session (" + getHost() + ':' + getPort() + ")");
        } else {
            log.debug("Creating JavaMail Session (" + getHost() + ")");
        }

        Properties properties = new Properties();

        // Enable debugging if set
        properties.put("mail.debug", isDebug());

        if (getUsername() != null && getPassword() == null) {
            log.warn("username supplied without a password (if an empty password is required supply an empty string)");
        }
        if (getUsername() == null && getPassword() != null) {
            log.warn("password supplied without a username (if no authentication required supply neither)");
        }

        if (getHost() != null) {
            if (isSsl()) {
                properties.put("mail.smtps.host", getHost());
            } else {
                properties.put("mail.smtp.host", getHost());
            }

        }
        if (getPort() != null) {
            if (isSsl()) {
                properties.put("mail.smtps.port", getPort().toString());
            } else {
                properties.put("mail.smtp.port", getPort().toString());
            }
        } else {
            if (isSsl()) {
                properties.put("mail.smtps.port", "465");
            } else {
                properties.put("mail.smtp.port", "25");
            }
        }

        properties.put("mail.transport.protocol", getTransport());

        // Authentication if required
        Authenticator authenticator = null;        
        if (StringUtil.isNotEmpty(getUsername()) && StringUtil.isNotEmpty(getPassword())) {
            if (isSsl()) {
                properties.put("mail.smtps.auth", "true");
            } else {

                properties.put("mail.smtp.auth", "true");
            }
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getUsername(), getPassword());
                }
            };
        }

        // Use TLS (if supported)
        if (isTls()) {
            properties.put("mail.smtp.starttls.enable", "true");
        }

        session = javax.mail.Session.getInstance(properties, authenticator);
        session.setDebug(isDebug());

        log.debug("connected to mail server");
    }
}
