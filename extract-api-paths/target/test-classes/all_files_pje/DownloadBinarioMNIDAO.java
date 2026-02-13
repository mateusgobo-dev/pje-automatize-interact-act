/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.mni.entidades.DownloadBinario;

/**
 * Componente de acesso a dados da entidade {@link DownloadBinario}
 * @author cristof
 *
 */
@Name("downloadBinarioMNIDAO")
public class DownloadBinarioMNIDAO extends BaseDAO<DownloadBinario> {

	@Override
	public Integer getId(DownloadBinario d) {
		return d.getId();
	}

}
