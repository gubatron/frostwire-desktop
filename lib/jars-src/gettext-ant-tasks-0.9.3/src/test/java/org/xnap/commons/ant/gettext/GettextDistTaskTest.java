package org.xnap.commons.ant.gettext;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;

public class GettextDistTaskTest extends TestCase {

	public void testFileMatchesPercentage() throws Exception {
		// 99.1 percent file
		File file = new File(getClass().getResource("de.po").toURI());
		assertTrue(file.isFile());
		
		GettextDistTask task = new GettextDistTask();
		task.setPercentage(99);
		
		assertTrue(task.fileMatchesPercentage(file));
		
		task.setMoreOrLess("<");
		assertFalse(task.fileMatchesPercentage(file));
		

		// 0 percent file 
		file = new File(getClass().getResource("ja.po").toURI());
		assertTrue(file.isFile());
		
		task = new GettextDistTask();
		task.setPercentage(92);
		
		assertFalse(task.fileMatchesPercentage(file));
		
		task.setMoreOrLess("<");
		assertTrue(task.fileMatchesPercentage(file));
		
		// file with fuzzy translations
		file = new File(getClass().getResource("fr.po").toURI());
		assertTrue(file.isFile());
		
		task = new GettextDistTask();
		task.setPercentage(92);
		
		assertFalse(task.fileMatchesPercentage(file));
		
		task.setMoreOrLess("<");
		assertTrue(task.fileMatchesPercentage(file));
	}
	
	public void testFileWithZeroTranslations() throws Exception {
		
		File file = new File(getClass().getResource("en.po").toURI());
		assertTrue(file.isFile());
		
		GettextDistTask task = new GettextDistTask();
		task.setPercentage(1);
		
		assertFalse(task.fileMatchesPercentage(file));
		
		task.setMoreOrLess("less");
		assertTrue(task.fileMatchesPercentage(file));
		
		task.setPercentage(0);
		assertFalse(task.fileMatchesPercentage(file));
		
		task.setMoreOrLess("greaterOrEqual");
		assertTrue(task.fileMatchesPercentage(file));
	}
	
	public void testFileWithCompleteTranslations() throws Exception {
		File file = new File(getClass().getResource("fullytranslated.po").toURI());
		assertTrue(file.isFile());
		
		GettextDistTask task = new GettextDistTask();
		task.setPercentage(100);
		
		assertTrue(task.fileMatchesPercentage(file));
		
		task.setMoreOrLess("less");
		assertFalse(task.fileMatchesPercentage(file));
	}
	
	private GettextDistTask getTask() throws Exception {
		
		File file = new File(getClass().getResource("en.po").toURI());
		assertTrue(file.isFile());
		
		Project project = new Project();
		project.init();
		
		Location location = new Location(file.getParentFile().getAbsolutePath());
		
		GettextDistTask task = new GettextDistTask();
		task.setProject(project);
		task.setLocation(location);
		
		return task;
	}
	
	public void testGetPoFiles() throws Exception {
		
		File file = new File(getClass().getResource("en.po").toURI());
		assertTrue(file.isFile());
		
		GettextDistTask task = getTask();
		
		FileSet fileSet = new FileSet();
		fileSet.setDir(file.getParentFile());
		FilenameSelector selector = new FilenameSelector();
		selector.setName("**/*.po");
		fileSet.add(selector);
		task.addFileSet(fileSet);

		String[] files = task.getPoFiles();
		assertEquals(5, files.length);
	}
	
	public void testGetSinglePoFile() throws Exception {
		File file = new File(getClass().getResource("en.po").toURI());
		assertTrue(file.isFile());
		
		GettextDistTask task = getTask();
		
		FileSet fileSet = new FileSet();
		fileSet.setDir(file.getParentFile());
		FilenameSelector selector = new FilenameSelector();
		selector.setName("en.po");
		fileSet.add(selector);
		task.addFileSet(fileSet);
		
		String[] files = task.getPoFiles();
		assertEquals(1, files.length);
	}
}
