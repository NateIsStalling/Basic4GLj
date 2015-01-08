package com.basic4gl.vm.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType.BasicValType;
import com.basic4gl.vm.util.Constants;

////////////////////////////////////////////////////////////////////////////////
	// VmTypeLibrary
	//
	// Used to store structure definitions, and operate on data types

	public class TypeLibrary {
		Vector<StructureField> m_fields;
		Vector<Structure> m_structures;

		public TypeLibrary() {
			m_fields = new Vector<StructureField>();
			m_structures = new Vector<Structure>();
		}
		public Vector<StructureField> Fields() {
			return m_fields;
		}

		public Vector<Structure> Structures() {
			return m_structures;
		}

		public void Clear() {
			m_fields.clear();
			m_structures.clear();
		}

		// Finding structures and fields
		public boolean Empty() {
			return m_structures.isEmpty();
		}

		public boolean StrucStored(String name) {
			return GetStruc(name) >= 0;
		}

		public boolean FieldStored(Structure struc, String fieldName) {
			return GetField(struc, fieldName) >= 0;
		}

		public int GetStruc(String name)
		{
		    name = name.toLowerCase();
		    for (int i = 0; i < m_structures.size (); i++) {
		        if (m_structures.get(i).m_name.equals(name))
		            return i;
		    }
		    return -1;
		}

		public int GetField(Structure struc, String fieldName)
		{
		    fieldName = fieldName.toLowerCase();
		    for (   int i = struc.m_firstField;
		            i < struc.m_firstField + struc.m_fieldCount;
		            i++) {
		        assert (i >= 0);
		        assert (i < m_fields.size ());
		        if (m_fields .get(i).m_name.equals(fieldName))
		            return i;
		    }
		    return -1;
		}

		// Data type operations
		public int DataSize(ValType type){

		    // How big would a variable of type "type" be?
		    if (type.PhysicalPointerLevel () > 0)       // Pointers are always one element long
		        return 1;

		    if (type.m_arrayLevel > 0)                  // Calculate array size
		        return type.ArraySize (DataSize (new ValType (type.m_basicType)));

		    if (type.m_basicType >= 0) {

		        // Structured type. Lookup and return size of structure.
		        assert (type.m_basicType < m_structures.size ());
		        return m_structures.get(type.m_basicType).m_dataSize;
		    }

		    // Otherwise is basic type
		    return 1;
		}

		public boolean DataSizeBiggerThan(ValType type, int size)
		{

		    // Return true if data size > size.
		    // This is logically equivalent to: DataSize (type) > size,
		    // except it correctly handles integer overflow for really big types.
		    assert (TypeValid (type));
		    if (type.PhysicalPointerLevel () == 0 && type.m_arrayLevel > 0) {
		        ValType element = type;
		        element.m_arrayLevel = 0;
		        return type.ArraySizeBiggerThan (size, DataSize (element));
		    }
		    else
		        return DataSize (type) > size;
		}

		public boolean TypeValid(ValType type)
		{
		    return      type.m_basicType >= BasicValType.VTP_INT.getType()
		            &&  BasicValType.getType(type.m_basicType) != BasicValType.VTP_UNDEFINED
		            &&  (type.m_basicType < 0 || type.m_basicType < m_structures.size ())
		            &&  type.m_arrayLevel < Constants.VM_MAXDIMENSIONS;
		}

		public boolean ContainsString(ValType type) {
			assert (TypeValid(type));

			// Pointers to objects don't *contain* anything.
			// (Pointing to something that contains a string doesn't count.)
			if (type.VirtualPointerLevel() > 0)
				return false;

			// Examine data type
			if (type.m_basicType < 0)
				return BasicValType.getType(type.m_basicType) == BasicValType.VTP_STRING;
			else
				return m_structures.get(type.m_basicType).m_containsString;
		}

		public boolean ContainsArray(ValType type) {
			assert (TypeValid(type));

			return type.VirtualPointerLevel() == 0 // Must not be a pointer
					&& (type.m_arrayLevel > 0 // Can be an array
					|| (type.m_basicType >= 0 && m_structures
							.get(type.m_basicType).m_containsArray));// or
																				// a
																				// structure
																				// containing
																				// an
																				// array
		}

		public boolean ContainsPointer(ValType type) {
			assert (TypeValid(type));

			// Type is a pointer?
			if (type.m_pointerLevel > 0)
				return true;

			// Is a structure (or array of structures) containing a pointer?
			if (type.m_basicType >= 0)
				return m_structures.get(type.m_basicType).m_containsPointer;

			return false;
		}

		// Building structures
		public Structure CurrentStruc() {
			assert (!Empty()); // Must have at least one structure
			return m_structures.lastElement();
		}

		public StructureField CurrentField() {
			assert (m_fields.size() > CurrentStruc().m_firstField); // Current
																	// structure
																	// must have
																	// at least
																	// 1 field
			return m_fields.lastElement();
		}

		public Structure NewStruc(String name) { // Create a new structure and
													// make it current

			// Name must be valid and not already used
			assert (!name.equals(""));
			assert (!StrucStored(name));

			// Create new structure
			m_structures.add(new Structure(name, m_fields.size()));
			return CurrentStruc();
		}

		public StructureField NewField(String name, ValType type) { // Create
																		// a new
																		// field
																		// and
																		// assign
																		// it to
																		// the
																		// current
																		// structure

			// Name must be valid and not already used within current structure
			assert (!name.equals(""));
			assert (!FieldStored(CurrentStruc(), name));

			// Type must be valid, and not an instance of the current structure
			// type
			// (or an array. Can be a pointer though.)
			assert (TypeValid(type));
			assert (!type.m_byRef);
			assert (type.m_pointerLevel > 0 || type.m_basicType < 0 || type.m_basicType + 1 < m_structures.size());

			// Create new field
			m_fields.add(new StructureField(name, type,
					CurrentStruc().m_dataSize));
			CurrentStruc().m_fieldCount++;
			CurrentStruc().m_dataSize += DataSize(CurrentField().m_type);

			// Update current structure statistics
			CurrentStruc().m_containsString = CurrentStruc().m_containsString
					|| ContainsString(type);
			CurrentStruc().m_containsArray = CurrentStruc().m_containsArray
					|| ContainsArray(type);
			CurrentStruc().m_containsPointer = CurrentStruc().m_containsPointer
					|| ContainsPointer(type);
			return CurrentField();
		}

		// Debugging/output
		public String DescribeVariable(String name, ValType type) {

			if (!TypeValid(type))
				return "INVALID TYPE " + name;

			// Return a string describing what the variable stores
			String result;

			// Variable type
			if (type.m_basicType >= 0)
				result = m_structures.get(type.m_basicType).m_name
						+ " "; // Structure type
			else
				result = ValType.BasicValTypeName(type.m_basicType) + " "; // Basic
																		// type

			// Append pointer prefix
			int i;
			for (i = 0; i < type.VirtualPointerLevel(); i++)
				result += "&";

			// Variable name
			result += name;

			// Append array indices
			for (i = type.m_arrayLevel - 1; i >= 0; i--) {
				result += "(";
				if (type.VirtualPointerLevel() == 0)
					result += String.valueOf(type.m_arrayDims[i] - 1);
				result += ")";
			}

			return result;
		}

		// Streaming
		public void StreamOut(ByteBuffer buffer) {
			int i;
			try {
				// Write out fields
				Streaming.WriteLong(buffer, m_fields.size());
				for (i = 0; i < m_fields.size(); i++)
					m_fields.get(i).StreamOut(buffer);

				// Write out structures
				Streaming.WriteLong(buffer, m_structures.size());
				for (i = 0; i < m_structures.size(); i++)
					m_structures.get(i).StreamOut(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void StreamIn(ByteBuffer buffer) {
			int i, count;
			try {
				// Clear existing data
				m_fields.clear();
				m_structures.clear();

				// Read fields
				count = (int) Streaming.ReadLong(buffer);
				m_fields.setSize(count);
				for (i = 0; i < count; i++)
					m_fields.get(i).StreamIn(buffer);

				// Read structures
				count = (int) Streaming.ReadLong(buffer);
				m_structures.setSize(count);
				for (i = 0; i < count; i++)
					m_structures.get(i).StreamIn(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}