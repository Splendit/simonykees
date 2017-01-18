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
import java.io.StringReader;

@SuppressWarnings("nls")
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
}
