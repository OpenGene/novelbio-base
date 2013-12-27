package com.novelbio.base;

import org.apache.log4j.Logger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** 内部有synchronized */
//TODO 何高人说有更好的，回头去问他
public class SerializeKryo {
	private static final Logger logger = Logger.getLogger(SerializeKryo.class);
	Kryo kryo = new Kryo();
	
	public synchronized byte[] write(Object object) {
//		byte[] data = null;
//		try {
//			Output output = new Output(100, -1);
//	        ObjectOutputStream oos = new ObjectOutputStream(output);
//	        oos.writeObject(object);
//
//	        oos.close();     
//	        data = output.getBuffer();
//		} catch (Exception e) {
//			logger.error("serialize error", e);
//		}

		        Output output = new Output(100, -1);
        kryo.writeClassAndObject(output, object);
        byte[] data = output.getBuffer();
        return data;
	}
	public synchronized Object read( byte[] data) {
//		try {
//	        Input input = new Input();
//	 		input.setBuffer(data);
//	        ObjectInputStream ois = new ObjectInputStream(input);
//		     Object obj = ois.readObject();
//		     ois.close();
//		     return obj;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			logger.error("serialize error", e);
//		}
//		return null;
        Input input = new Input();
 		input.setBuffer(data);
 		return kryo.readClassAndObject(input);
	}
	
}
