package eu.jsparrow.sample.preRule;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.LogRecord;

@SuppressWarnings({"nls", "null"})
public class TestCornerCasesTryWithResourceRule {

	public StringReader lostStreamsWithoutTry() {
		StringReader a = new StringReader("lalelu");
		return a;
	}

	public Object lostStreamsWithTry(String input) {
		Object result;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(buffer);
			out.writeObject(input);
			ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
			result = in.readObject();
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	static void readFirstLineFromFile(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			Closeable cl = new BufferedReader(new FileReader(path));
			br2.close();
			cl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void readFirstLineFromFile_withFinalBlock(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			BufferedReader br2 = new BufferedReader(new FileReader(path));
			Closeable cl = new BufferedReader(new FileReader(path));
			br2.close();
			cl.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done");
		}
	}
	
	public void morphiaCornerCase() {
		
		final LogRecord record = null;
		final StringBuilder sb = new StringBuilder();
		
		if (record.getThrown() != null) {
			try {
	            final StringWriter sw = new StringWriter();
	            final PrintWriter pw = new PrintWriter(sw);
	            //CHECKSTYLE:OFF
	            record.getThrown().printStackTrace(pw);
	            //CHECKSTYLE:ON
	            pw.close();
	            sb.append(sw.toString());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public void morphiaCornerCaseWithClosedResources() {
		
		final LogRecord record = null;
		final StringBuilder sb = new StringBuilder();
		
		if (record.getThrown() != null) {
			try {
				// comment before resource
	            final StringWriter sw = // I don't want to break anything
	            		new StringWriter();
	            final PrintWriter pw = new PrintWriter(sw);
	            
	            
	            
	            //CHECKSTYLE:OFF
	            record.getThrown().printStackTrace(pw);
	            
	            
				/*
				 * Unconnected comment
				 */
	            
	            //CHECKSTYLE:ON
	            pw.close();
	            sb.append(sw.toString()); // trailing comment after append
	            
	            // comment before close 
	            sw.close();
	            
	            // comment before println
	            System.out.println(sb);
	            
	            // comment in the end of the body
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public void morphiaCornerCaseWithClosedResources_emptyTryStatement() {
		
		final LogRecord record = null;
		final StringBuilder sb = new StringBuilder();
		
		if (record.getThrown() != null) {
			try {
				
				// comment before resource
	            final StringWriter sw = new StringWriter();
	            final PrintWriter pw = new PrintWriter(sw);
	            
	            
	            
				/*
				 * Unconnected comment
				 */
	            
	            //CHECKSTYLE:ON
	            pw.close();
	            
	            // comment before close 
	            sw.close();
	            
	            // comment in the end of the body
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public void wekaNotInitialized() {
		try (ByteArrayInputStream istream = new ByteArrayInputStream(null)) {
			ObjectInputStream p;
		} catch (Exception e) {
		}
	}
	
	public void closedResourcesUsingEachOther() {
		
		final LogRecord record = null;
		final StringBuilder sb = new StringBuilder();
		
		if (record.getThrown() != null) {
			try {
	            final StringWriter sw = new StringWriter();
	            final PrintWriter pw = new PrintWriter(sw);
	            //CHECKSTYLE:OFF
	            record.getThrown().printStackTrace(pw);
	            //CHECKSTYLE:ON
	            pw.close();
	            sb.append(sw.toString());
	            sw.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public void catchResourceCloseExceptions(String path) {
		try {
			OutputStream ostream = new FileOutputStream(path);
            // Flush and close the stream.
			try {
	        	ostream.flush();
	            ostream.close();
	        } catch (IOException fileNotFoundException) {
	        	fileNotFoundException.getMessage();
			}

        } catch (IOException fileNotFoundException) {
        	
        }
	}
	
	public void resourceUsingManipulatedLocalDeclarations() {
		String path = "some/Funny/Path";
		try {
			
			path = path + "getting/sad";
			OutputStream uniqueName001 = new FileOutputStream(path);
            // Flush and close the stream.
        	uniqueName001.flush();
            uniqueName001.close();

        } catch (IOException fileNotFoundException) {
        	
        }
	}
	
	public void resourceUsingLocalDeclarations() {
		String path = "some/Funny/Path";
		try {
			
			OutputStream uniqueName001 = new FileOutputStream(path);
            // Flush and close the stream.
        	uniqueName001.flush();
            uniqueName001.close();

        } catch (IOException fileNotFoundException) {
        	
        }
	}
	
	public void savingComments() {
		String path = "some/Funny/Path";
		try {
			/*
			 * Block comment on resource
			 */
			OutputStream uniqueName001 =
					// inline comment inside the resource
					new FileOutputStream(path); // training line comment
            // Flush and close the stream.
        	uniqueName001.flush();
        	// comment on close
            uniqueName001.close();

        } catch (IOException fileNotFoundException) {
        	
        }
	}
}
