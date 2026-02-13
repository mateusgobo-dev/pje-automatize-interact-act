package org.jbpm.context.exe.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmException;
import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.ContextConverter;
import org.jbpm.db.hibernate.Converters;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.Token;
import org.jbpm.util.CustomLoaderObjectInputStream;

public class SerializableToByteArrayConverter implements ContextConverter<Serializable, ByteArray> {

	private static final long serialVersionUID = 1L;

	public SerializableToByteArrayConverter() {
		Converters.registerConverter("R", this);
	}

	public boolean supports(Object value) {
		return value instanceof Serializable;
	}

	public ByteArray convert(Serializable o) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream);
			objectStream.writeObject(o);
			objectStream.flush();
			bytes = memoryStream.toByteArray();
		} catch (IOException e) {
			throw new JbpmException("could not serialize: " + o, e);
		}
		return new ByteArray(bytes);
	}

	public Serializable revert(ByteArray o) {
		return revert(o, null);
	}

	public Serializable revert(ByteArray o, Token token) {
		InputStream memoryStream = new ByteArrayInputStream(o.getBytes());
		try {
			ObjectInputStream objectStream;
			if (token != null) {
				ProcessDefinition processDefinition = token.getProcessInstance().getProcessDefinition();
				ClassLoader classLoader = JbpmConfiguration.getProcessClassLoader(processDefinition);
				objectStream = new CustomLoaderObjectInputStream(memoryStream, classLoader);
			} else {
				objectStream = new ObjectInputStream(memoryStream);
			}
			return (Serializable) objectStream.readObject();
		} catch (IOException e) {
			throw new JbpmException("failed to read object", e);
		} catch (ClassNotFoundException e) {
			throw new JbpmException("serialized object class not found", e);
		}
	}
}