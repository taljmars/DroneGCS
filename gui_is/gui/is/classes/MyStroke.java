package gui.is.classes;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import tools.logger.Logger;

public class MyStroke implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8582610651786768502L;
	
	private transient BasicStroke stroke;
	
	public MyStroke(int i) {
		stroke = new BasicStroke(i);
	}

	public MyStroke(float width, int cap, int join, float miterLimit,
			float[] dash, float dashPhase) {
		stroke = new BasicStroke(width, cap, join, miterLimit, dash, dashPhase);
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		if (stream == null) {
			throw new IllegalArgumentException("Null 'stream' argument.");
		}
		
		stream.writeObject(MyStroke.class);
		if (stroke == null) {
			Logger.LogErrorMessege("Failed to write MyStroke Object, value is null");
			return;
		}
		stream.writeFloat(stroke.getLineWidth());
		stream.writeInt(stroke.getEndCap());
		stream.writeInt(stroke.getLineJoin());
		stream.writeFloat(stroke.getMiterLimit());
		stream.writeObject(stroke.getDashArray());
		stream.writeFloat(stroke.getDashPhase());
	}

	@SuppressWarnings({ "rawtypes" })
	private void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
		if (stream == null) {
			throw new IllegalArgumentException("Null 'stream' argument.");
		}
		BasicStroke result = null;
	
		final Class c = (Class) stream.readObject();
		if (c.equals(MyStroke.class)) {
			final float width = stream.readFloat();
			final int cap = stream.readInt();
			final int join = stream.readInt();
			final float miterLimit = stream.readFloat();
			final float[] dash = (float[]) stream.readObject();
			final float dashPhase = stream.readFloat();
			result = new BasicStroke(width, cap, join, miterLimit, dash, dashPhase);
		}
		else {
			Logger.LogErrorMessege("Failed to parse MyStroke Object");
		}
		stroke = result;
	}

	public Stroke getStroke() {
		return stroke;
	}

}
