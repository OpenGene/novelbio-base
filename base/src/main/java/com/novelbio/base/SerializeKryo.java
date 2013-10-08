package com.novelbio.base;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** 内部有synchronized */
//TODO 何高人说有更好的，回头去问他
public class SerializeKryo {
	Kryo kryo = new Kryo();
	
	public synchronized byte[] write(Object object) {
        Output output = new Output(100, -1);
        kryo.writeClassAndObject(output, object);
        byte[] data = output.getBuffer();
        return data;
	}
	public synchronized Object read( byte[] data) {
        Input input = new Input();
 		input.setBuffer(data);
 		return kryo.readClassAndObject(input);
	}
	
}
