package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.FiltroDinamicoConsultaDAO;
import br.jus.cnj.pje.business.dao.FiltroDinamicoEntidadeDAO;
import br.jus.cnj.pje.business.dao.FiltroDinamicoParametroDAO;
import br.jus.pje.nucleo.entidades.FiltroDinamicoConsulta;
import br.jus.pje.nucleo.entidades.FiltroDinamicoEntidade;
import br.jus.pje.nucleo.entidades.FiltroDinamicoParametro;

/**
 * @author Éverton Nogueira Pereira
 *
 */
@Name(FiltroDinamicoManager.NAME)
public class FiltroDinamicoManager implements Serializable{
	public static final String NAME = "filtroDinamicoManager";
	private static final long serialVersionUID = 1L;
	
	@In
	private FiltroDinamicoParametroDAO filtroDinamicoParametroDAO;
	
	@In
	private FiltroDinamicoConsultaDAO filtroDinamicoConsultaDAO;
	
	@In
	private FiltroDinamicoEntidadeDAO filtroDinamicoEntidadeDAO;
	
	public List<FiltroDinamicoParametro> obtemParametrosDaConsulta(FiltroDinamicoConsulta consulta) {
		List<FiltroDinamicoParametro> parametros = new ArrayList<FiltroDinamicoParametro>();
		Pattern pattern = Pattern.compile("\\B\\W{1}\\w+");
		Matcher matcher = pattern.matcher(consulta.getHql());
		while(matcher.find()){
			parametros.add(new FiltroDinamicoParametro(matcher.group(), consulta));
		}
		return parametros;
	}
	
	public List<FiltroDinamicoConsulta> obtemConsultasByFuncionalidade(String funcionalidade) {
		return filtroDinamicoConsultaDAO.obtemConsultasByFuncionalidade(funcionalidade);
	}

	public List<FiltroDinamicoParametro> obtemTodosParametros() {
		return filtroDinamicoParametroDAO.obtemTodosParametros();
	}

	public FiltroDinamicoParametro obtemEntidadeByParametro(String parametro, List<FiltroDinamicoParametro> list) {
		for (FiltroDinamicoParametro parametroConsulta : list) {
			if(parametroConsulta.getParametro().equals(parametro)){
				return parametroConsulta;
			}
		}
		return null;
	}

	public void cadastraConsulta(FiltroDinamicoConsulta cadastroConsulta) {
		filtroDinamicoConsultaDAO.persist(cadastroConsulta);
		filtroDinamicoConsultaDAO.flush();
	}

	public List<FiltroDinamicoEntidade> obtemTodasEntidades() {
		return filtroDinamicoEntidadeDAO.obtemTodasEntidades();
	}

	public void cadastraEntidade(FiltroDinamicoEntidade cadastroEntidade) {
		filtroDinamicoEntidadeDAO.persist(cadastroEntidade);
		filtroDinamicoEntidadeDAO.flush();
	}

	public List<FiltroDinamicoConsulta> obtemTodasConsultas() {
		return filtroDinamicoConsultaDAO.obtemTodasConsultas();
	}
}