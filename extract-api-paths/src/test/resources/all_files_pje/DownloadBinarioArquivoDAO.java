/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.mni.entidades.DownloadBinarioArquivo;

/**
 * Componente de controle de acesso à entidade {@link DownloadBinarioArquivo}.
 * 
 * @author cristof
 *
 */
@Name("downloadBinarioArquivoDAO")
public class DownloadBinarioArquivoDAO extends BaseDAO<DownloadBinarioArquivo> {

	@Override
	public Integer getId(DownloadBinarioArquivo d) {
		return d.getId();
	}
	
}
