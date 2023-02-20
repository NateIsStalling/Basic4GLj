package com.basic4gl.library.standard;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.compiler.util.FuncSpec;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.PointerResourceStore;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nate on 11/17/2015.
 */
public class FileIOBasicLib implements FunctionLibrary, IFileAccess{


    // State variables
    static FileOpener files;
    //FileAccessorAdapter pluginAdapter;
    PointerResourceStore<FileStream> fileStreams;
    String lastError = "";
    FileStream stream;

    List<File> findFileCollection = new ArrayList<>();
    int findFileHandle = -1;

    @Override
    public String name() { return "FileIOBasicLib";}
    @Override
    public String description() { return "File IO functions; reads and writes files with little-endian byte order.";}

    @Override
    public void init(TomVM vm) {
        if (fileStreams == null) {
            fileStreams = new PointerResourceStore<FileStream>();
        }

        fileStreams.clear();

        // Clear error state
        lastError = "";
        CloseFind();
    }

    @Override
    public void init(TomBasicCompiler comp) {
        if (fileStreams == null) {
            fileStreams = new PointerResourceStore<FileStream>();
        }

        // Register resources
        comp.VM().addResources(fileStreams);

        // Register initialisation functions
        comp.VM().addInitFunction(new Init());


    }

    @Override
    public void cleanup() {
        //Do nothing
    }

    @Override
    public void init(FileOpener files){
        FileIOBasicLib.files = files;
    }

    @Override
    public Map<String, Constant> constants() {

        return null;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {
        Map<String, FuncSpec[]> s = new HashMap<>();
        // Register function wrappers

        s.put ("OpenFileRead", new FuncSpec[]{ new FuncSpec( WrapOpenFileRead.class, new ParamTypeList ( BasicValType.VTP_STRING), true, true, BasicValType.VTP_INT, true, false, null)});
        s.put ("OpenFileWrite", new FuncSpec[]{ new FuncSpec( WrapOpenFileWrite.class, new ParamTypeList ( BasicValType.VTP_STRING), true, true, BasicValType.VTP_INT, true, false, null)});
        s.put ("CloseFile", new FuncSpec[]{ new FuncSpec( WrapCloseFile.class, new ParamTypeList ( BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("FileError", new FuncSpec[]{ new FuncSpec( WrapFileError.class, new ParamTypeList (), true, true, BasicValType.VTP_STRING, true, false, null)});
        s.put ("EndOfFile", new FuncSpec[]{ new FuncSpec( WrapEndOfFile.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteChar", new FuncSpec[]{ new FuncSpec( WrapWriteChar.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_STRING), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteString", new FuncSpec[]{ new FuncSpec( WrapWriteString.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_STRING), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteLine", new FuncSpec[]{ new FuncSpec( WrapWriteLine.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_STRING), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteByte", new FuncSpec[]{ new FuncSpec( WrapWriteByte.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteWord", new FuncSpec[]{ new FuncSpec( WrapWriteWord.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteInt", new FuncSpec[]{ new FuncSpec( WrapWriteInt.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteFloat", new FuncSpec[]{ new FuncSpec( WrapWriteFloat.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_REAL), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("WriteReal", new FuncSpec[]{ new FuncSpec( WrapWriteFloat.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_REAL), true, false, BasicValType.VTP_INT, true, false, null)}); // (WriteReal is a synonym for WriteFloat)
        s.put ("WriteDouble", new FuncSpec[]{ new FuncSpec( WrapWriteDouble.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_REAL), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("ReadLine", new FuncSpec[]{ new FuncSpec( WrapReadLine.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_STRING, true, false, null)});
        s.put ("ReadChar", new FuncSpec[]{ new FuncSpec( WrapReadChar.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_STRING, true, false, null)});
        s.put ("ReadByte", new FuncSpec[]{ new FuncSpec( WrapReadByte.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_INT, true, false, null)});
        s.put ("ReadWord", new FuncSpec[]{ new FuncSpec( WrapReadWord.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_INT, true, false, null)});
        s.put ("ReadInt", new FuncSpec[]{ new FuncSpec( WrapReadInt.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_INT, true, false, null)});
        s.put ("ReadFloat", new FuncSpec[]{ new FuncSpec( WrapReadFloat.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_REAL, true, false, null)});
        s.put ("ReadReal", new FuncSpec[]{ new FuncSpec( WrapReadFloat.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_REAL, true, false, null)});
        s.put ("ReadDouble", new FuncSpec[]{ new FuncSpec( WrapReadDouble.class, new ParamTypeList ( BasicValType.VTP_INT), true, true, BasicValType.VTP_REAL, true, false, null)});
        s.put ("Seek", new FuncSpec[]{ new FuncSpec( WrapSeek.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_INT), true, false, BasicValType.VTP_REAL, true, false, null)});
        s.put ("ReadText", new FuncSpec[]{ new FuncSpec( WrapReadText.class, new ParamTypeList ( BasicValType.VTP_INT, BasicValType.VTP_INT), true, true, BasicValType.VTP_STRING, true, false, null)});
        s.put ("FindFirstFile", new FuncSpec[]{ new FuncSpec( WrapFindFirstFile.class, new ParamTypeList ( BasicValType.VTP_STRING), true, true, BasicValType.VTP_STRING, true, false, null)});
        s.put ("FindNextFile", new FuncSpec[]{ new FuncSpec( WrapFindNextFile.class, new ParamTypeList (), true, true, BasicValType.VTP_STRING, true, false, null)});
        s.put ("FindClose", new FuncSpec[]{ new FuncSpec( WrapFindClose.class, new ParamTypeList (), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put ("DeleteFile", new FuncSpec[]{ new FuncSpec( WrapDeleteFile.class, new ParamTypeList( BasicValType.VTP_STRING), true, true, BasicValType.VTP_INT, true, false, null)});

        return s;
    }

    @Override
    public HashMap<String, String> getTokenTips() {
        return null;
    }

    @Override
    public List<String> getDependencies() {
        return null;
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }

    static boolean IsSandboxMode() {
        //TODO implement sandbox mode option
        return true;//GetAppSettings()->IsSandboxMode();
    }

    void CloseFind() {
        if (findFileCollection != null && findFileHandle != -1) {
            findFileCollection.clear();
            findFileHandle = -1;
        }
    }
    boolean GetStream (int index) {

        // Get file stream and store in stream variable
        if (index > 0 && fileStreams.isIndexStored(index)) {
            stream = fileStreams.getValueAt(index);
            lastError = "";
            return true;
        }
        else {
            stream = null ;
            lastError = "Invalid file handle";
            return false;
        }
    }

    boolean GetIStream (int index) {
        if (!GetStream (index)) {
            return false;
        }
        if (stream.in == null ) {
            lastError = "File not in INPUT mode";
            return false;
        }
        return true;
    }

    boolean GetOStream (int index) {
        if (!GetStream (index)) {
            return false;
        }
        if (stream.out == null ) {
            lastError = "File not in OUTPUT mode";
            return false;
        }
        return true;
    }

    boolean UpdateError (String operation, Exception exception) {
        if (  exception != null &&  stream != null   && ( stream.in	!= null ||	stream.out != null )) {
            lastError = operation + " failed";
            return false;
        }
        else {
            lastError = "";
            return true;
        }
    }

    int InternalOpenFileRead (String filename) {

        // Attempt to open file
        System.out.println("sandbox mode: " + IsSandboxMode());
        FileInputStream file = files.OpenRead (filename, IsSandboxMode());
        if (file == null ) {
            lastError = files.getError();
            return 0;
        }
        else {
            lastError = "";
            return fileStreams.alloc(new FileStream (file));
        }
    }

    int InternalOpenFileWrite (String filename) {

        // Attempt to open file
        FileOutputStream file = files.OpenWrite (filename, IsSandboxMode());
        if (file == null ) {
            lastError = files.getError();
            return 0;
        }
        else {
            lastError = "";
            return fileStreams.alloc(new FileStream (file));
        }
    }

    // Pre-run initialisation
    public final class Init  implements Function {
        public void run(TomVM vm) {

            // Clear error state
            lastError = "";
            CloseFind();
        }
    }

    public final class WrapOpenFileRead  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal (InternalOpenFileRead (vm.getStringParam(1)));
    }
    }
    public final class WrapOpenFileWrite  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(InternalOpenFileWrite(vm.getStringParam(1)));
    }
    }
    public final class WrapCloseFile  implements Function { public void run(TomVM vm) {
        int handle = vm.getIntParam(1);
        if (handle > 0 && fileStreams.isIndexStored(handle)) {
            fileStreams.free(handle);
            lastError = "";
        }
        else {
            lastError = "Invalid file handle";
        }
    }
    }
    public final class WrapWriteChar  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        // Write a single character
        char c = 0;
        c = vm.getStringParam(1).charAt(0);
        Exception exception = null;
        try {
            stream.out.write((byte) c);
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        UpdateError ("Write", exception);
    }
    }
    public final class WrapWriteString  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        // Write string. (Excludes 0 terminator)
        String str = vm.getStringParam(1);
        Exception exception = null;
        if (!str.equals("")) {
            try {
                stream.out.write (str.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
            UpdateError ("Write", exception);
        }
    }
    }
    public final class WrapWriteLine  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        // Write string. (Excludes 0 terminator)
        String str = vm.getStringParam(1) + "\r\n";
        Exception exception = null;
        if (!str.equals("")) {
            try {
                stream.out.write(str.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
            UpdateError ("Write", exception);
        }
    }
    }
    public final class WrapWriteByte  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        byte element = vm.getIntParam(1).byteValue();
        Exception exception = null;
        try {
            stream.out.write (element);
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        }
        UpdateError ("Write", exception);
    }
    }
    public final class WrapWriteWord  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        short element = vm.getIntParam(1).shortValue();
        Exception exception = null;
        try {
            stream.out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(element).array());
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        }
        UpdateError ("Write", exception);
    }
    }
    public final class WrapWriteInt  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        int element = vm.getIntParam(1);
        Exception exception = null;
        try {
            stream.out.write (ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(element).array());
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        }
        UpdateError ("Write", exception);
    }
    }
    public final class WrapWriteFloat  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        float element = vm.getRealParam(1);
        Exception exception = null;
        try {
            stream.out.write (ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(element).array());
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        }
        UpdateError ("Write", exception);
    }
    }
    public final class WrapWriteDouble  implements Function { public void run(TomVM vm) {
        if (!GetOStream (vm.getIntParam(2))) {
            return;
        }

        double element = vm.getRealParam(1);
        Exception exception = null;
        try {
            stream.out.write (ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putDouble(element).array());
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        }
        UpdateError ("Write", exception);
    }
    }
    public final class WrapReadLine  implements Function { public void run(TomVM vm) {
        vm.setRegString ( "");
        if (!GetIStream (vm.getIntParam(1))) {
            return;
        }
        if (!UpdateError ("Read", null)) {
            return;
        }

        // Skip returns and linefeeds
        char c = 0;
        Exception exception = null;
        try {
            c = (char)stream.in.read();
            while ((stream.in.available() > 0) && (c == 10 || c == 13)) {
                c = (char)stream.in.read();
            }

            // Read printable characters
            while ((stream.in.available() > 0) && c != 10 && c != 13) {
                vm.setRegString(vm.getRegString() + c);
                c = (char)stream.in.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        // Don't treat eof as an error
        if (exception != null) {
            UpdateError ("Read", exception);
        } else {
            lastError = "";
        }
    }
    }
    public final class WrapReadChar  implements Function { public void run(TomVM vm) {
        vm.setRegString ("");
        if (!GetIStream (vm.getIntParam(1))) {
            return;
        }

        // Read char
        char c = 0;
        Exception exception = null;
        try {
            c = (char)stream.in.read();
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (UpdateError ("Read", exception)) {
            vm.setRegString(String.valueOf(c));
        }
    }
    }
    public final class WrapReadByte  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal ( 0);
        if (!GetIStream (vm.getIntParam(1))) {
            return;
        }

        // Read byte
        int element = 0;  //byte values are unsigned
        Exception exception = null;
        try {
            element = stream.in.read();
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (UpdateError ("Read", exception)) {
            vm.getReg().setIntVal ( (int) element);
        }
    }
    }
    public final class WrapReadWord  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal ( 0);
        if (!GetIStream (vm.getIntParam(1))) {
            return;
        }

        // Read byte
        int element = 0;    //use int since word values are unsigned
        Exception exception = null;
        try {
            byte [] buffer = new byte[Short.SIZE / Byte.SIZE];
            stream.in.read(buffer);
            element = (int) ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(0);
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (UpdateError ("Read", exception)) {
            vm.getReg().setIntVal ( (int)element);
        }
    }
    }
    public final class WrapReadInt  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal ( 0);
        if (!GetIStream (vm.getIntParam(1))) {
            return;
        }

        // Read byte
        int element = 0;
        Exception exception = null;
        try {
            byte [] buffer = new byte[Integer.SIZE / Byte.SIZE];
            stream.in.read(buffer);
            element = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(0);
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (UpdateError ("Read", exception)) {
            vm.getReg().setIntVal ( element);
        }
    }
    }
    public final class WrapReadFloat  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal ( 0);
        if (!GetIStream (vm.getIntParam(1))) {
            return;
        }

        // Read byte
        float element = 0;
        Exception exception = null;
        try {
            byte [] buffer = new byte[Float.SIZE / Byte.SIZE];
            stream.in.read(buffer);
            element = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(0);
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (UpdateError ("Read", exception)) {
            vm.getReg().setRealVal(element);
        }
    }
    }
    public final class WrapReadDouble  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal ( 0);
        if (!GetIStream (vm.getIntParam(1))) {
            return;
        }

        // Read byte
        double element = 0;
        Exception exception = null;
        try {
            byte [] buffer = new byte[Double.SIZE / Byte.SIZE];
            stream.in.read(buffer);
            element = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer().get(0);
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (UpdateError ("Read", exception)) {
            vm.getReg().setRealVal ((float) element);
        }
    }
    }
    public final class WrapSeek  implements Function { public void run(TomVM vm) {
        if (!GetStream (vm.getIntParam(2))) {
            return;
        }
        Exception exception = null;
        try {
            if (stream.in != null) {
                FileChannel ch = stream.in.getChannel();
                ch.position(vm.getIntParam(1));
            }
            if (stream.out != null) {
                FileChannel ch = stream.out.getChannel();
                ch.position(vm.getIntParam(1));
            }
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        UpdateError ("Seek", exception);
    }
    }
    public final class WrapReadText  implements Function { public void run(TomVM vm) {

        // Read a string of non whitespace tokens
        if (!GetIStream (vm.getIntParam(2))) {
            return;
        }
        if (!UpdateError ("Read", null)) {
            return;
        }
        boolean skipNewLines = vm.getIntParam(1) != 0;

        Exception exception = null;

        // Skip leading whitespace
        char c = ' ';
        vm.setRegString ( "");
        try {
            while ((c != '\n' || skipNewLines) && c <= ' ') {
                c = (char) stream.in.read();
            }
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (!UpdateError ("Read", exception)) {
            return;
        }

        // Read non whitespace
        try{
            while (c > ' ') {
                vm.setRegString ( vm.getRegString() + c);
                c = (char) stream.in.read();
            }

            // Backup one character, so that we don't skip the following whitespace
            FileChannel ch = stream.in.getChannel ();
            ch.position(ch.position() -1);
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }

        if (!UpdateError ("Read", exception)) {
            return;
        }
    }
    }
    public final class WrapFindFirstFile implements Function { public void run(TomVM vm) {

        // Close any previous find
        CloseFind();

        // Get filename
        String filename = vm.getStringParam(1);

        // Check path is in files folder
        if (IsSandboxMode() && !files.CheckFilesFolder(filename)) {
            lastError = files.getError();
            vm.setRegString( "");
            return;
        }

        // Find file
        lastError = "";
        Path dir = Paths.get("");
        if (findFileCollection != null) {
            findFileCollection.clear();
        } else {
            findFileCollection = new ArrayList<>();
        }

        Exception exception = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            Pattern regex = Pattern.compile("[^*]+|(\\*)");
            Matcher m = regex.matcher(filename);
            StringBuffer b= new StringBuffer();
            while (m.find()) {
                if(m.group(1) != null) {
                    m.appendReplacement(b, ".*");
                } else {
                    m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
                }
            }
            m.appendTail(b);
            String pattern = b.toString();

            for (Path entry: stream) {
                if (entry.toString().matches(pattern)) {
                    findFileCollection.add(new File(entry.toString()));
                }
            }
            //Alphabetize file list
            Collections.sort(findFileCollection);
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        findFileHandle = findFileCollection.size() > 0 ? 0 : -1;

        // Return filename
        if (findFileHandle != -1 && exception == null) {
            vm.setRegString(findFileCollection.get(0).getName());
        } else {
            vm.setRegString( "");
        }
    }
    }
    public final class WrapFindNextFile implements Function { public void run(TomVM vm) {


        // Return data
        if (findFileHandle != -1 && findFileCollection != null && findFileHandle < findFileCollection.size()) {
            vm.setRegString(findFileCollection.get(findFileHandle).getName());
            findFileHandle++;
        }
        else {
            vm.setRegString("");
            findFileHandle = -1;
        }
    }
    }

    public final class WrapFindClose implements Function { public void run(TomVM vm) {
        CloseFind();
    }
    }
    public final class WrapDeleteFile implements Function { public void run(TomVM vm) {
        String filename = vm.getStringParam(1);
        if (files.Delete(filename, IsSandboxMode())) {
            lastError = "";
            vm.getReg().setIntVal( -1);
        }
        else {
            lastError = files.getError();
            vm.getReg().setIntVal( 0);
        }
    }
    }
    public final class WrapFileError  implements Function { public void run(TomVM vm) {
        vm.setRegString ( lastError);
    }
    }
    public final class WrapEndOfFile  implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal ( -1);
        if (!GetStream (vm.getIntParam(1))) {
            return;
        }
        try {
            if ((stream.in != null && (stream.in.available() > 0))
                    || (stream.out != null))//Todo check if output stream is eof
                // && (stream.out.available() > 0)))
            {
                vm.getReg().setIntVal(0);
            }
        } catch (Exception e) {
            vm.getReg().setIntVal(-1);
        }
    }
    }
}
