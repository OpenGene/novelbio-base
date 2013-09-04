package com.novelbio.base;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.novelbio.base.fileOperate.FileOperate;

public class SerializeKryo {
	Kryo kryo = new Kryo();
	
	public byte[] write(Object object) {
        Output output = new Output(100, -1);
        kryo.writeClassAndObject(output, object);
        byte[] data = output.getBuffer();
        return data;
	}
	public Object read( byte[] data) {
        Input input = new Input();
 		input.setBuffer(data);
 		return kryo.readClassAndObject(input);
	}
	
}
