package at.splendit.simonykees.sample.postRule.allRules;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.LogRecord;

@SuppressWarnings({ "nls", "null" })
public class TestCornerCasesTryWithResourceRule {

	public StringReader lostStreamsWithoutTry() {
		StringReader a = new StringReader("lalelu");
		return a;
	}

	public Object lostStreamsWithTry(String input) {
		Object result;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutput out;
		try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
			out = new ObjectOutputStream(buffer);
			out.writeObject(input);
			result = in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	static void readFirstLineFromFile(String path) {

		try (BufferedReader br = new BufferedReader(new FileReader(path));
				BufferedReader br2 = new BufferedReader(new FileReader(path));
				Closeable cl = new BufferedReader(new FileReader(path))) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void morphiaCornerCase() {

		final LogRecord record = null;
		final StringBuilder sb = new StringBuilder();

		if (record.getThrown() != null) {
			try (final StringWriter sw = new StringWriter(); final PrintWriter pw = new PrintWriter(sw)) {
				// CHECKSTYLE:OFF
				record.getThrown().printStackTrace(pw);
				sb.append(sw.toString());
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
}
