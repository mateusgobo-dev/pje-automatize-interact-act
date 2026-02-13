package br.com.itx.test.util;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import br.com.itx.util.FileUtil;

public class FileUtilTest {

	@Test
	public void deleteDirTest() {
		File dir = new File("\\File");
		dir.mkdir();

		boolean deletou = FileUtil.deleteDir(dir);

		Assert.assertEquals(true, deletou);
	}

}
