package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.FileEntity;

public class ProgressFileEntity  extends FileEntity {
	
	private OnWriteListener _listener;

	public ProgressFileEntity(File file) {
		super(file, "binary/octet-stream");
		setChunked(true);
	}

	public OnWriteListener getOnWriteListener() {
		return _listener;
	}
	
	public void setOnWriteListener(OnWriteListener listener) {
		_listener = listener;
	}
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
		
        InputStream instream = new FileInputStream(this.file);
        try {
            byte[] tmp = new byte[4096];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                outstream.write(tmp, 0, l);
                fireOnWrite(l);
            }
            outstream.flush();
        } finally {
            instream.close();
        }
	}
	
	protected void fireOnWrite(int written) {
		if (_listener != null) {
			_listener.onWrite(this, written);
		}
	}
	
	public interface OnWriteListener {
		public void onWrite(ProgressFileEntity progressFileEntity, int written);
	}
}
