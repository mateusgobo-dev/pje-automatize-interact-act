/**
 * 
 */
package br.jus.cnj.pje.servicos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.jboss.seam.log.Logging;
import org.junit.Before;
import org.junit.Test;

/**
 * @author cristof
 *
 */
public class MimeUtilCheckTest {
	
	private MimeUtilChecker mimeUtilChecker;
	
	@Before
	public void beforeClass() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		mimeUtilChecker = new MimeUtilChecker();
		Field f = MimeUtilChecker.class.getDeclaredField("logger");
		f.setAccessible(true);
		f.set(mimeUtilChecker, Logging.getLog(MimeUtilCheckTest.class));
		f.setAccessible(false);
	}
	
	@Test
	public void checkMimeExists(){
		assertNotNull(mimeUtilChecker);
	}
	
	@Test
	public void checkFailedPDF(){
		try{
			File f = new File(getClass().getResource("/arquivos/mimeutiltest_failed_file.pdf").getPath());
			assertThat(mimeUtilChecker.getMimeType(f), is("application/pdf"));
		}catch (Exception e){
			assertTrue("Exceção ao tentar acessar o arquivo de teste", false);
		}
	}

	@Test
	public void checkBinaryFile(){
		try{
			File f = new File(getClass().getResource("/arquivos/mimeutiltest_binary_file.bin").getPath());
			assertThat(mimeUtilChecker.getMimeType(f), is("application/octet-stream"));
		}catch(Exception e){
			assertTrue("Exceção ao tentar acessar o arquivo de teste", false);
		}
	}

}
