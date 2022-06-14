package eu.jsparrow.jdt.ls.core.internal;

import java.util.HashMap;

import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class JSONUtility {

	/**
	 * Converts given JSON objects to given Model objects.
	 *
	 * @throws IllegalArgumentException if clazz is null
	 */
	public static <T> T toModel(Object object, Class<T> clazz){
		return toModel(new Gson(), object, clazz);
	}

	/**
	 * Converts given JSON objects to given Model objects with the customized
	 * TypeAdapters from lsp4j.
	 *
	 * @throws IllegalArgumentException
	 *             if clazz is null
	 */
	public static <T> T toLsp4jModel(Object object, Class<T> clazz) {
		return toModel(new MessageJsonHandler(new HashMap<>()).getGson(), object, clazz);
	}

	private static <T> T toModel(Gson gson, Object object, Class<T> clazz) {
		if(object == null){
			return null;
		}
		if(clazz == null ){
			throw new IllegalArgumentException("Class can not be null");
		}
		if(object instanceof JsonElement){
			return gson.fromJson((JsonElement) object, clazz);
		}
		if (clazz.isInstance(object)) {
			return clazz.cast(object);
		}
		if (object instanceof String) {
			return gson.fromJson((String) object, clazz);
		}
		return null;
	}
}
