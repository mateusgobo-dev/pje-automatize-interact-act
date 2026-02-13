package br.jus.cnj.pje.template;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.component.Util;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

@Name(FreeMakerTemplate.NAME)
@Scope(ScopeType.APPLICATION)
@AutoCreate
public class FreeMakerTemplate {

	public static final String NAME = "freeMakerTemplate";
	
	private Configuration cfg = null;

    @Create
	public void init() throws IOException {
        
        cfg = new Configuration(Configuration.VERSION_2_3_28);
        cfg.setDirectoryForTemplateLoading(new File(new Util().getContextRealPath() + "/WEB-INF/xhtml/templates/freemaker"));
        cfg.setDefaultEncoding("UTF-8"); 
        cfg.setLocale(new Locale("pt","BR"));
        cfg.setDateTimeFormat("dd/MM/yyyy HH:mm");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
    }
    
    public Configuration getConfig() {
        return this.cfg;
    }
}