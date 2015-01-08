package com.basic4gl.vm.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;

import com.basic4gl.util.Streaming;

////////////////////////////////////////////////////////////////////////////////
	// VmValTypeSet
	//
	// A small number of VM op-codes operate on VmValType advanced data types
	// (e.g. OP_COPY). Op-codes don't have storage space to specify an advanced
	// data
	// type, so instead they specify an index into this set array.
	public class ValTypeSet {
		
		Vector<ValType> m_types;

		public ValTypeSet(){
			m_types = new Vector<ValType>();
		}
		
		public void Clear() {
			m_types.clear();
		}

		public int GetIndex(ValType type) {

			// Get index of type "type" in our set.
			// If type is not present, create a new one and return an index to
			// that.

			// Look for type
			int i;
			for (i = 0; i < m_types.size(); i++)
				if (m_types.get(i).Equals(type))
					return i;

			// Otherwise create new one
			i = m_types.size();
			m_types.add(type);
			return i;
		}

		public ValType GetValType(int index) {
			assert (index >= 0);
			assert (index < m_types.size());
			return m_types.get(index);
		}

		// Streaming
		public void StreamOut(ByteBuffer buffer) {
			try {
				Streaming.WriteLong(buffer, m_types.size());
				for (int i = 0; i < m_types.size(); i++)
					m_types.get(i).StreamOut(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void StreamIn(ByteBuffer buffer) {
			int count;
			try {
				count = (int) Streaming.ReadLong(buffer);

				m_types.setSize(count);
				for (int i = 0; i < count; i++)
					m_types.get(i).StreamIn(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
