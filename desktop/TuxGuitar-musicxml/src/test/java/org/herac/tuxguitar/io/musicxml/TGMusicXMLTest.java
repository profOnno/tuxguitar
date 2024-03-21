package org.herac.tuxguitar.io.musicxml;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.herac.tuxguitar.app.tools.custom.converter.TGConverterFormat;
import org.herac.tuxguitar.graphics.control.TGFactoryImpl;
import org.herac.tuxguitar.io.base.TGFileFormat;
import org.herac.tuxguitar.io.base.TGFileFormatException;
import org.herac.tuxguitar.io.base.TGFileFormatManager;
import org.herac.tuxguitar.io.base.TGFileFormatUtils;
import org.herac.tuxguitar.io.base.TGSongPersistenceHelper;
import org.herac.tuxguitar.io.base.TGSongReaderHandle;
import org.herac.tuxguitar.io.base.TGSongReaderHelper;
import org.herac.tuxguitar.io.base.TGSongStreamContext;
import org.herac.tuxguitar.io.base.TGSongWriter;
import org.herac.tuxguitar.io.base.TGSongWriterHandle;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.TGSong;
import org.herac.tuxguitar.util.TGContext;

//import org.herac.tuxguitar.editor.action.file.TGSongPersistenceActionBase;
//import org.herac.tuxguitar.editor.action.file.TGReadSongAction;

/**
 * 
 */

class TGMusicXMLTest {
	
	public static final int FILE_OK = 250;
	public static final int FILE_BAD = 403;
	public static final int FILE_COULDNT_WRITE = 401;
	public static final int FILE_NOT_FOUND = 404;
	public static final int OUT_OF_MEMORY = 500;
	public static final int EXPORTER_NOT_FOUND = 590;
	public static final int UNKNOWN_ERROR = 666;
	
	public static final String ATTRIBUTE_CONTEXT = TGSongStreamContext.class.getName();
	public static final String ATTRIBUTE_FORMAT = TGFileFormat.class.getName();
	public static final String ATTRIBUTE_FORMAT_CODE = TGSongPersistenceHelper.ATTRIBUTE_FORMAT_CODE;
	public static final String ATTRIBUTE_MIME_TYPE = TGSongPersistenceHelper.ATTRIBUTE_MIME_TYPE;
	
	// TODO implement batch? multiple files with specifics?
	// TODO use path builder?
	static final String FILE = "./src/test/resources/lyricsTest.tg";
	static final String FILE_CONVERTED = ".src/test/resources/output/lyricsTest.musicxml";
	
	private TGContext context;	
	private TGConverterFormat format;
		
	public static final TGFileFormat MUSICXML_FORMAT = new TGFileFormat("MusicXML", "application/vnd.recordare.musicxml+xml", new String[]{"musicxml"});
	public static final String TG_FORMAT_CODE = ("tg");
	public static final TGFileFormat TG_FORMAT = new TGFileFormat("TuxGuitar", "audio/x-tuxguitar", new String[]{ TG_FORMAT_CODE });
	
	private void convert(String fileName, String convertFileName) {
		try {
			TGSongManager manager = new TGSongManager();
			manager.setFactory(new TGFactoryImpl());
			TGSong song = null;
			
			try {
				TGSongReaderHandle tgSongLoaderHandle = new TGSongReaderHandle();
				tgSongLoaderHandle.setFactory(manager.getFactory());				
				tgSongLoaderHandle.setFormat(TG_FORMAT);
				tgSongLoaderHandle.setInputStream(new FileInputStream(fileName));
				tgSongLoaderHandle.setContext(new TGSongStreamContext());			
				tgSongLoaderHandle.getContext().setAttribute(TGSongReaderHelper.ATTRIBUTE_FORMAT_CODE, TGFileFormatUtils.getFileFormatCode(fileName));
				TGFileFormatManager.getInstance(this.context).read(tgSongLoaderHandle);
				
				song = tgSongLoaderHandle.getSong();
				
			} catch (TGFileFormatException e) {
				fail("fileName: " + fileName + " FILE_BAD");
			}
			
			if (song != null){
				try {
					TGSongWriter stream = new MusicXMLSongWriter();
					TGFileFormatManager.getInstance(this.context).addWriter(stream);
					
					manager.autoCompleteSilences(song);
					manager.orderBeats(song);

					// TODO: delete old file?
					new File(new File(convertFileName).getParent()).mkdirs();
					
					if( this.format != null ){
						TGSongWriterHandle tgSongWriterHandle = new TGSongWriterHandle();
						tgSongWriterHandle.setSong(song);
						tgSongWriterHandle.setFactory(manager.getFactory());
						tgSongWriterHandle.setFormat(this.format.getFileFormat());
						tgSongWriterHandle.setOutputStream(new BufferedOutputStream(new FileOutputStream(convertFileName)));
						tgSongWriterHandle.setContext(new TGSongStreamContext());
						TGFileFormatManager.getInstance(this.context).write(tgSongWriterHandle);
					}
					
					this.validateXML(convertFileName);
					
				} catch (TGFileFormatException e) {
					fail("fileName: " + fileName + " FILE_COULDNT_WRITE");					
				}
			} 
		} catch (FileNotFoundException ex) {
			fail("fileName: " + fileName + " FILE_NOT_FOUND");
		} catch (OutOfMemoryError e) {
			fail("convertFileName: " + convertFileName + " OUT_OF_MEMORY");
		} catch (Throwable throwable) {
			// most likely an error in validateXML
			// this will propagate this error
			fail(throwable.getMessage());
		}
	}
	
	private void validateXML(String fileName) {
		// for testing test
		//fileName = "./src/test/resources/output/lyricsTestBad.musicxml";
				
		String schema = "./src/test/resources/musicxml-4.0/schema/musicxml.xsd";
		
		try {
			// xmllint --schema schema/musicxml.xsd lyricsTest.musicxml --noout
			String[] cmd = {"xmllint",
					"--schema",
					schema,
					fileName,
					"--noout"
					};

			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			
			Process ps = pb.start();			

			BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			String line;
			StringBuilder lines = new StringBuilder();

			while ((line = in.readLine()) != null) {
				lines.append(line);				
			}			
						
			int exitCode = ps.waitFor();             
            
            if (exitCode == 0) {
            	assert(true); // or do nothing
            } else {
            	fail(lines.toString());            
            }
            
		} catch (Exception e) {			
			fail(e.toString());
		}
	}
	
	/*// used for testing the test validation only
	@Test
	void validationTest() {
		validateXML(FILE_CONVERTED);
	}
	*/
	
	@Test
	void test() {
		
		
		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" + currentDir);
		
		context = new TGContext();
		//this.format = FILE_FORMAT;
		//public TGConverterFormat(TGFileFormat fileFormat, String extension){
		
		this.format = new TGConverterFormat(MUSICXML_FORMAT, "musicxml");
		//this.listener = this;
		
		this.convert(FILE, FILE_CONVERTED);
	}		
}