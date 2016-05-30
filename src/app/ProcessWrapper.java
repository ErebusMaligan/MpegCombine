package app;

import gui.props.UIEntryProps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import process.NonStandardProcess;
import ui.log.LogFileSiphon;

import comparator.WindowsExplorerFileComparator;

/**
 * @author Daniel J. Rivers
 *         2013
 *
 * Created: Aug 26, 2013, 12:53:49 AM 
 */
public class ProcessWrapper extends NonStandardProcess {
	
	private UIEntryProps props;
	
	private int part = 0;
	
	public static String SAME_LINE = "$SL";
	
	public ProcessWrapper( String name, UIEntryProps props ) {
		super( name );
		this.props = props;
		new File( props.getString( "outputDir" ) ).mkdir();
		new LogFileSiphon( name, props.getString( "outputDir" ) + ".log" ) {
			public void skimMessage( String name, String s ) {
				try {
					boolean sameLine = false;
					String out = s;
					if ( s.endsWith( SAME_LINE ) ) {
						sameLine = true;
						out = s.substring( 0, s.length() - SAME_LINE.length() );
					}
					if ( !sameLine ) {
						fstream.write( "[" + sdf.format( new Date( System.currentTimeMillis() ) ) + "]:  " + out );
						fstream.newLine();
					} else {
						fstream.write( out );
					}
					fstream.flush();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		};
	}
	//execute ffmpeg
	
	
	public void execute() {
		new Thread( () -> {
			File in = new File( props.getString( "inputDir" ) );
			if ( in.exists() && in.isDirectory() ) {
				List<File> output = new ArrayList<>();
				List<File> files = Arrays.asList( in.listFiles( ( d, n ) -> { return n.toLowerCase().endsWith( ".mp4" ); } ) );
				files.sort( new WindowsExplorerFileComparator() );
				files.forEach( f -> renameFile( f, output ) );
				writeInputFile( output );
				ffmpeg();
				sendMessage( "PROCESS COMPLETE!" );
			} else {
				sendMessage( "INVALID INPUT DIRECTORY!" );
			}
		} ).start();
	}
	
	private void renameFile( File f, List<File> output ) {
		File n =  new File( f.getParentFile().getPath() + "/part" + ( part++ ) + ".mp4" );
		sendMessage( "Renaming: " + f.getName() + " -> " + n.getName() );
		f.renameTo( n );
		output.add( n );
	}
	
	private void writeInputFile( List<File> files ) {
		try ( PrintWriter p = new PrintWriter( props.getString( "inputDir" ) + "/input.txt" ) ) {
			files.forEach( f -> p.write( "file '" + f.getName() + "'\n" ) );
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
	}
	
	private void ffmpeg() {
		try {
			String args = "-f concat -safe 0 -i \"" + props.getString( "inputDir" ) + "/input.txt" + "\" -vcodec copy -acodec copy \"" + props.getString( "outputDir" ) + "/" + props.getString( "outputFile" ) + "\"";
			sendMessage( "Starting ffmpeg: " + props.getString( "ffmpeg" ) + " " + args );
			Process p = new ProcessBuilder( props.getString( "ffmpeg" ), "-f", "concat", "-safe", "0", "-i", "\"" + props.getString( "inputDir" ) + "/input.txt\"", "-vcodec", "copy", "-acodec", "copy", "\"" + props.getString( "outputDir" ) + "/" + props.getString( "outputFile" ) + "\"" ).redirectErrorStream( true ).start();
			BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			String line;
			while ( ( line = br.readLine() ) != null ) {
				sendMessage( line + "\n" );
			}
			p.waitFor();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}
}