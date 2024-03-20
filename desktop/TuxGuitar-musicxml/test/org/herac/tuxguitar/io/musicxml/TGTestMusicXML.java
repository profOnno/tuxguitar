package org.herac.tuxguitar.io.musicxml;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.tools.custom.converter.*;
import org.herac.tuxguitar.event.TGEvent;
import org.herac.tuxguitar.event.TGEventException;
import org.herac.tuxguitar.event.TGEventListener;
import org.herac.tuxguitar.io.base.TGFileFormat;
import org.herac.tuxguitar.io.base.TGFileFormatException;
import org.herac.tuxguitar.io.base.TGFileFormatManager;
import org.herac.tuxguitar.io.base.TGFileFormatUtils;
import org.herac.tuxguitar.io.base.TGSongReaderHandle;
import org.herac.tuxguitar.io.base.TGSongReaderHelper;
import org.herac.tuxguitar.io.base.TGSongStreamContext;
import org.herac.tuxguitar.io.base.TGSongWriter;
import org.herac.tuxguitar.io.base.TGSongWriterHandle;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.TGSong;
import org.herac.tuxguitar.util.TGContext;


/**
 * 
 */
class TGTestMusicXML implements TGConverterListener, TGEventListener/*, TGSongWriter */ {
	
	public static final int FILE_OK = 250;
	public static final int FILE_BAD = 403;
	public static final int FILE_COULDNT_WRITE = 401;
	public static final int FILE_NOT_FOUND = 404;
	public static final int OUT_OF_MEMORY = 500;
	public static final int EXPORTER_NOT_FOUND = 590;
	public static final int UNKNOWN_ERROR = 666;
	
	
	
	static final String FILE = "./resources/lyricsTest.tg";
	static final String FILE_CONVERTED = "./resources/lyricsTest.musicxml";
	private TGContext context;	
	private TGConverterFormat format;
	private TGConverterListener listener; // is an interface
	
	private boolean cancelled;
	//public static TGFileFormat ALL_FORMATS = new TGFileFormat(TuxGuitar.getProperty("file.all-files"), "*/*", new String[]{"*"});
	//public static final TGFileFormat FILE_FORMAT = new TGFileFormat("MusicXML", "application/vnd.recordare.musicxml+xml", new String[]{"musicxml"});
	public static final TGFileFormat FILE_FORMAT = new TGFileFormat("MusicXML", "application/vnd.recordare.musicxml+xml", new String[]{"musicxml"});
	
	//private TGConverterListener listener; // is an interface
	
	private void convert(String fileName, String convertFileName) {
		try {
			this.getListener().notifyFileProcess(convertFileName);
			
			TGSongManager manager = new TGSongManager();
			TGSong song = null;
			try {
				
				TGSongWriter stream = new MusicXMLSongWriter(); 
				TGFileFormatManager.getInstance(this.context).addWriter(stream);
				
				TGSongReaderHandle tgSongLoaderHandle = new TGSongReaderHandle();
				tgSongLoaderHandle.setFactory(manager.getFactory());
				tgSongLoaderHandle.setInputStream(new FileInputStream(fileName));
				tgSongLoaderHandle.setContext(new TGSongStreamContext());
				tgSongLoaderHandle.getContext().setAttribute(TGSongReaderHelper.ATTRIBUTE_FORMAT_CODE, TGFileFormatUtils.getFileFormatCode(fileName));
				TGFileFormatManager.getInstance(this.context).read(tgSongLoaderHandle);
				
				song = tgSongLoaderHandle.getSong();
			} catch (TGFileFormatException e) {
				this.getListener().notifyFileResult(fileName,FILE_BAD);
			}
			
			if (song != null){
				try {
					manager.autoCompleteSilences(song);
					manager.orderBeats(song);
					
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
					
					this.getListener().notifyFileResult(convertFileName,FILE_OK);
				} catch (TGFileFormatException e) {
					this.getListener().notifyFileResult(fileName,FILE_COULDNT_WRITE);
				}
			} 
		} catch (FileNotFoundException ex) {
			this.getListener().notifyFileResult(fileName,FILE_NOT_FOUND);
		} catch (OutOfMemoryError e) {
			this.getListener().notifyFileResult(convertFileName,OUT_OF_MEMORY);
		} catch (Throwable throwable) {
			this.getListener().notifyFileResult(convertFileName,UNKNOWN_ERROR);
		}
	}
	
	@Test
	void test2() {
		context = new TGContext();
		//this.format = FILE_FORMAT;
		//public TGConverterFormat(TGFileFormat fileFormat, String extension){

		this.setFormat(new TGConverterFormat(FILE_FORMAT, "musicxml"));
		this.setListener(this);
		this.convert(FILE, FILE_CONVERTED);
	}

	
	//@Test
	void test() {
		//fail("Not yet implemented");
		//TGConverter tgc = new TGConverter();

		TGSongManager manager = new TGSongManager();
		TGSong song = null;
		context = new TGContext();
	    //System.out.println("Working Directory = " + System.getProperty("user.dir"));
		
		//System.out.println(ALL_FORMATS.getSupportedFormats().toString());
		//String[] s = ALL_FORMATS.getSupportedFormats(); 		
		
	    try {
			TGSongReaderHandle tgSongLoaderHandle = new TGSongReaderHandle();
			tgSongLoaderHandle.setFactory(manager.getFactory());
			tgSongLoaderHandle.setInputStream(new FileInputStream(FILE));
			tgSongLoaderHandle.setContext(new TGSongStreamContext());			
			tgSongLoaderHandle.getContext().setAttribute(TGSongReaderHelper.ATTRIBUTE_FORMAT_CODE, TGFileFormatUtils.getFileFormatCode(FILE));
			//
			//tgSongLoaderHandle.getContext().setAttribute(TGSongReaderHelper.ATTRIBUTE_FORMAT_CODE, FILE_FORMAT);
			TGFileFormatManager.getInstance(context).read(tgSongLoaderHandle);
			
			song = tgSongLoaderHandle.getSong();
			
			if (song != null){
				try {
					manager.autoCompleteSilences(song);
					manager.orderBeats(song);
					
					new File(new File(FILE_CONVERTED).getParent()).mkdirs();
					
					if( this.format != null ){
						TGSongWriterHandle tgSongWriterHandle = new TGSongWriterHandle();
						tgSongWriterHandle.setSong(song);
						tgSongWriterHandle.setFactory(manager.getFactory());
						tgSongWriterHandle.setFormat(this.format.getFileFormat());
						tgSongWriterHandle.setOutputStream(new BufferedOutputStream(new FileOutputStream(FILE_CONVERTED)));
						tgSongWriterHandle.setContext(new TGSongStreamContext());
						TGFileFormatManager.getInstance(this.context).write(tgSongWriterHandle);
					}
					
					//this.getListener().notifyFileResult(convertFileName,FILE_OK);
				} catch (TGFileFormatException e) {
					//this.getListener().notifyFileResult(fileName,FILE_COULDNT_WRITE);
					// ignore
				}
			} 
			
		} catch (TGFileFormatException e) {
			//this.getListener().notifyFileResult(fileName,FILE_BAD);
			//assertTrue(false);
			fail("Bad format");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//assertTrue(false);
			fail("Filename not found");
		}
		assert(true);
	}
	
	
	public void setFormat( TGConverterFormat format ) {
		this.format = format;
	}
	
	public TGConverterListener getListener() {
		return this.listener;
	}
	
	public void setListener(TGConverterListener listener) {
		this.listener = listener;
	}
	
	public boolean isCancelled() {
		return this.cancelled;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public void processEvent(TGEvent event) throws TGEventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyFileProcess(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyFileResult(String filename, int errorCode) {
		// TODO Auto-generated method stub
		
	}
/*
	@Override
	public TGFileFormat getFileFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(TGSongWriterHandle handle) throws TGFileFormatException {
		// TODO Auto-generated method stub
		
	}
	*/
/*
	public static void main(String[] args) {
		MusicXMLSongWriter
	}
	*/

}