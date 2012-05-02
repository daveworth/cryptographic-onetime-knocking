/*
 *  Copyright (c) 2004, David Worth <cesium@hexi-dump.org>
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *  
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 * 
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  
 *  Neither the name of the Hexi-Dump.org nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cokd;

import java.util.prefs.*;
import java.io.*;
import java.util.zip.DataFormatException;

/**
 * A Class to assist in serializing any object that implements
 * Serializable into a preferences object for easy storage!
 */
public class PrefChunker{

	/** Max length from prefs API*/
	static public final int chunkLen = ((3 * Preferences.MAX_VALUE_LENGTH)/4);

	/**
	 * Given an object, a preferences node, and a string, insert an
	 * object into the preferences node in a serialized form , so that
	 * it may be reconstructed.
	 *
	 * @param obj      Object to store
	 * @param prefnode Preferences node in which to store the serialize object
	 * @param namebase A sting containing the "base" of the name of
	 * the stored object
	 *
	 * @throws IOException
	 * @throws BackingStoreException
	 */
	public static void putObjectInPreferences(Object obj,
											  Preferences prefnode, 
											  String namebase) 
		throws IOException, BackingStoreException {

		byte[][] chunks = chunkObject(obj);
		
		prefnode.putInt(namebase+"_chunks", chunks.length);
		for (int x = 0; x < chunks.length; x++)
			prefnode.putByteArray(namebase+"_"+x, chunks[x]);
	}

	/**
	 * Invert putObjectInPreferences given the preferences node and
	 * the base name from putObjectInPreferences
	 *
	 * @see #putObjectInPreferences
	 *
	 * @param prefnode Preference node in which serialized object is stored
	 * @param namebase String containing the "base" name of the stored object
	 * 
	 * @return returns a reconstructed object from preferences
	 *
	 * @throws IOException
	 * @throws BackingStoreException
	 */
	public static Object getObjectFromPreferences(Preferences prefnode,
												  String namebase) 
		throws IOException, ClassNotFoundException {

		int chunkcount = prefnode.getInt(namebase+"_chunks", 0);
		byte[][] chunks = new byte[chunkcount][];
		for (int x = 0; x < chunkcount; x++)
			chunks[x] = prefnode.getByteArray(namebase+"_"+x, new byte[0]);

		return unchunkObject(chunks);
	}

	/**
	 * Given an object, serialize the object, break it into "chunks"
	 * in the form of byte[], and return the array of those chunks.
	 *
	 * @param obj Object to chunk
	 *
	 * @return a byte[][] containing all chunks
	 *
	 * @throws IllegalArgumentException if the object is not serializable
	 * @throws IOException
	 */
	private static byte[][] chunkObject(Object obj) 
		throws IllegalArgumentException, IOException {
		
		if (!(obj instanceof Serializable))
			throw new IllegalArgumentException("Object is not serializable!");

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bs);
		
		os.writeObject(obj);
		
		byte[] longbytes = bs.toByteArray();

		int totalchunks = (int)Math.ceil(((double)longbytes.length/chunkLen));
		byte[][] output = new byte[totalchunks][];

		int pos = 0;
		for (int x = 0; x < totalchunks; x++) {
			int copylen = (longbytes.length-pos < chunkLen) ? 
				(longbytes.length-pos) : chunkLen;

			output[x] = new byte[copylen];
			System.arraycopy(longbytes, pos, output[x], 0, copylen);
			pos+=copylen;
		}

		return output;
	}

	/**
	 * Invert chunkObject, given a byte[][] rebuild the object
	 *
	 * @see chunkObject
	 *
	 * @param serialobj a byte[][] produced by chunkObject
	 * 
	 * @return a reconstructed object
	 *
	 * @throws IllegalArgumentException if the chunk array is length 0
	 * @throws IOException
	 * @throws ClassNotFoundException if the chunks passed in do not
	 * form an object
	 */
	private static Object unchunkObject(byte[][] serialobj) 
		throws IllegalArgumentException,IOException,ClassNotFoundException{

		if (serialobj.length == 0)
			throw new IllegalArgumentException("No chunks passed!");

		int len = 0;
		for (int x = 0; x < serialobj.length; x++)
			len+=serialobj[x].length;

		byte[] longbytes = new byte[len];

		int pos = 0;
		for(int x = 0; x < serialobj.length; x++) {
			System.arraycopy(serialobj[x], 0, longbytes, pos, serialobj[x].length);
			pos+=serialobj[x].length;
		}

		ByteArrayInputStream bs = new ByteArrayInputStream(longbytes);
		ObjectInputStream is = new ObjectInputStream(bs);

		return is.readObject();
	}

	/**
	 * A quick and dirty testing routine
	 */
	public static void main(String[] args) {
		try { 
			String str = new String("This is a really gigantic string right?");
			byte[][] chunks = chunkObject(str);
			String newstr = (String)unchunkObject(chunks);

			if (str.equals(newstr))
				System.out.println("Everything looks good");
			else
				System.out.println("Everything sucks!");
			
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/** declare away */
	private PrefChunker(){}
}
