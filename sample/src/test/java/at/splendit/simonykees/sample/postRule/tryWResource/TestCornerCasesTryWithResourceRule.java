package at.splendit.simonykees.sample.postRule.tryWResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringReader;

@SuppressWarnings("nls")
public class TestCornerCasesTryWithResourceRule {

	public StringReader lostStreamsWithoutTry(){
		StringReader a = new StringReader("lalelu");
		return a;
	}

	public Object lostStreamsWithTry(String input) {
		Object result;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutput out;
		try(ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))){
			out = new ObjectOutputStream(buffer);
			out.writeObject(input);
			result = in.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
}
