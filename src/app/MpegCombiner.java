package app;

import gui.entry.FileEntry;
import gui.props.UIEntryProps;
import gui.props.variable.StringVariable;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import process.io.ProcessStreamSiphon;
import statics.GUIUtils;
import ui.log.LogDialog;

/**
 * @author Daniel J. Rivers
 *         2013
 *
 * Created: Aug 24, 2013, 6:14:01 PM
 */
public class MpegCombiner extends JFrame {

	private static final long serialVersionUID = 1L;

	private UIEntryProps props = new UIEntryProps();

	public MpegCombiner() {
//		ImageIcon icon = new ImageIcon( getClass().getResource( "MKV.png" ) );
		this.setTitle( "Mpeg Combiner" );
//		this.setIconImage( icon.getImage() );
		this.setSize( new Dimension( 640, 235 ) );
		this.setDefaultCloseOperation( EXIT_ON_CLOSE );
		this.setLayout( new BorderLayout() );
		setupProps();
		this.add( programPanel(), BorderLayout.CENTER );
		JButton b = new JButton( "Run Mpeg Combine" );
		b.addActionListener( e -> {
			String pName = "MPEGCOMBINE";
			LogDialog ld = new LogDialog( MpegCombiner.this, pName, false );
			ld.getLogPanel().setSkimReplace( new ProcessStreamSiphon() {

				public void skimMessage( String name, String s ) {
					boolean sameLine = false;
					String out = s;
					if ( s.endsWith( ProcessWrapper.SAME_LINE ) ) {
						sameLine = true;
						out = s.substring( 0, s.length() - ProcessWrapper.SAME_LINE.length() );
					}

					if ( !sameLine ) {
						ld.getLogPanel().appendWithTime( out );
						ld.getLogPanel().addBlankLine();
					} else {
						ld.getLogPanel().append( out );
					}
				}
				
				public void notifyProcessEnded( String arg0 ) {}

				public void notifyProcessStarted( String arg0 ) {}
			});
			new ProcessWrapper( pName, props ).execute();
		} );
		this.add( b, BorderLayout.SOUTH );
		this.setVisible( true );
	}

	private void setupProps() {
		props.addVariable( "outputFile", new StringVariable( "combined.mp4" ) );
		props.addVariable( "inputDir", new StringVariable( "S:/SHADOWPLAY/Desktop" ) );
		props.addVariable( "outputDir", new StringVariable( "S:/SHADOWPLAY/Desktop" ) );
		props.addVariable( "ffmpeg", new StringVariable( "D:/Archived/Streaming/ffmpeg/bin/ffmpeg.exe" ) );
	}
	
	private JPanel programPanel() {
		JPanel p = new JPanel();
		p.setLayout( new BoxLayout( p, BoxLayout.Y_AXIS ) );
		p.add( new FileEntry( "FFMpeg:", props.getVariable( "ffmpeg" ) ) );
		GUIUtils.spacer( p );
		p.add( new FileEntry( "Input Dir:", props.getVariable( "inputDir" ) ) );
		GUIUtils.spacer( p );
		p.add( new FileEntry( "Output Dir:", props.getVariable( "outputDir" ) ) );
		GUIUtils.spacer( p );
		p.add( new FileEntry( "Output File:", props.getVariable( "outputFile" ) ) );
		return p;
	}

	public static void main( String args[] ) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e ) {
			System.err.println( "Critical JVM Failure!" );
			e.printStackTrace();
		}
		new MpegCombiner();
	}
}