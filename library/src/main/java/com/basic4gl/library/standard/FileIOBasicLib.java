package com.basic4gl.library.standard;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.lib.util.*;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Function;
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
public class FileIOBasicLib implements FunctionLibrary, IFileAccess {

    // State variables
    private static IAppSettings appSettings;
    private static FileOpener files;

    // FileAccessorAdapter pluginAdapter;
    private FileStreamResourceStore fileStreams;
    private String lastError = "";
    private FileStream stream;

    private List<File> findFileCollection = new ArrayList<>();
    private int findFileHandle = -1;

    @Override
    public String name() {
        return "FileIOBasicLib";
    }

    @Override
    public String description() {
        return "File IO functions; reads and writes files with little-endian byte order.";
    }

    @Override
    public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {
        appSettings = settings;

        if (fileStreams == null) {
            fileStreams = new FileStreamResourceStore();
            services.registerService(FileStreamResourceStore.class, fileStreams);
        }

        fileStreams.clear();

        // Clear error state
        lastError = "";
        closeFind();
    }

    @Override
    public void init(TomBasicCompiler comp, IServiceCollection services) {
        if (fileStreams == null) {
            fileStreams = new FileStreamResourceStore();
            services.registerService(FileStreamResourceStore.class, fileStreams);
        }

        // Register resources
        comp.getVM().addResources(fileStreams);

        // Register initialisation functions
        comp.getVM().addInitFunction(new Init());
    }

    @Override
    public void cleanup() {
        if (fileStreams != null) {
            fileStreams.clear();
        }
        if (stream != null) {
            stream.close();
        }
    }

    @Override
    public void init(FileOpener files) {
        FileIOBasicLib.files = files;
    }

    @Override
    public Map<String, Constant> constants() {

        return null;
    }

    @Override
    public Map<String, FunctionSpecification[]> specs() {
        Map<String, FunctionSpecification[]> s = new HashMap<>();
        // Register function wrappers

        s.put("OpenFileRead", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapOpenFileRead.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("OpenFileWrite", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapOpenFileWrite.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("CloseFile", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapCloseFile.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("FileError", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapFileError.class, new ParamTypeList(), true, true, BasicValType.VTP_STRING, true, false, null)
        });
        s.put("EndOfFile", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapEndOfFile.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteChar", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteChar.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_STRING),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteString", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteString.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_STRING),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteLine", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteLine.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_STRING),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteByte", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteByte.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteWord", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteWord.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteInt", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteInt.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteFloat", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteFloat.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("WriteReal", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteFloat.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        }); // (WriteReal is a synonym for WriteFloat)
        s.put("WriteDouble", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWriteDouble.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("ReadLine", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadLine.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_STRING,
                    true,
                    false,
                    null)
        });
        s.put("ReadChar", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadChar.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_STRING,
                    true,
                    false,
                    null)
        });
        s.put("ReadByte", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadByte.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("ReadWord", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadWord.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("ReadInt", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadInt.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("ReadFloat", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadFloat.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_REAL,
                    true,
                    false,
                    null)
        });
        s.put("ReadReal", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadFloat.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_REAL,
                    true,
                    false,
                    null)
        });
        s.put("ReadDouble", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadDouble.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_REAL,
                    true,
                    false,
                    null)
        });
        s.put("Seek", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapSeek.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_REAL,
                    true,
                    false,
                    null)
        });
        s.put("ReadText", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapReadText.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_STRING,
                    true,
                    false,
                    null)
        });
        s.put("FindFirstFile", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapFindFirstFile.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_STRING,
                    true,
                    false,
                    null)
        });
        s.put("FindNextFile", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapFindNextFile.class, new ParamTypeList(), true, true, BasicValType.VTP_STRING, true, false, null)
        });
        s.put("FindClose", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapFindClose.class, new ParamTypeList(), true, false, BasicValType.VTP_INT, true, false, null)
        });
        s.put("DeleteFile", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapDeleteFile.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });

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

    static boolean isSandboxMode() {
        return appSettings.isSandboxModeEnabled();
    }

    void closeFind() {
        if (findFileCollection != null && findFileHandle != -1) {
            findFileCollection.clear();
            findFileHandle = -1;
        }
    }

    boolean getStream(int index) {

        // Get file stream and store in stream variable
        if (index > 0 && fileStreams.isIndexStored(index)) {
            stream = fileStreams.getValueAt(index);
            lastError = "";
            return true;
        } else {
            stream = null;
            lastError = "Invalid file handle";
            return false;
        }
    }

    boolean getInputStream(int index) {
        if (!getStream(index)) {
            return false;
        }
        if (stream.in == null) {
            lastError = "File not in INPUT mode";
            return false;
        }
        return true;
    }

    boolean getOutputStream(int index) {
        if (!getStream(index)) {
            return false;
        }
        if (stream.out == null) {
            lastError = "File not in OUTPUT mode";
            return false;
        }
        return true;
    }

    boolean updateError(String operation, Exception exception) {
        if (exception != null && stream != null && (stream.in != null || stream.out != null)) {
            lastError = operation + " failed";
            return false;
        } else {
            lastError = "";
            return true;
        }
    }

    int internalOpenFileRead(String filename) {

        // Attempt to open file
        System.out.println("sandbox mode: " + isSandboxMode());
        FileInputStream file = files.openRead(filename, isSandboxMode());
        if (file == null) {
            lastError = files.getError();
            return 0;
        } else {
            lastError = "";
            return fileStreams.alloc(new FileStream(file));
        }
    }

    int internalOpenFileWrite(String filename) {

        // Attempt to open file
        FileOutputStream file = files.openWrite(filename, isSandboxMode());
        if (file == null) {
            lastError = files.getError();
            return 0;
        } else {
            lastError = "";
            return fileStreams.alloc(new FileStream(file));
        }
    }

    // Pre-run initialisation
    public final class Init implements Function {
        public void run(TomVM vm) {

            // Clear error state
            lastError = "";
            closeFind();
        }
    }

    public final class WrapOpenFileRead implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(internalOpenFileRead(vm.getStringParam(1)));
        }
    }

    public final class WrapOpenFileWrite implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(internalOpenFileWrite(vm.getStringParam(1)));
        }
    }

    public final class WrapCloseFile implements Function {
        public void run(TomVM vm) {
            int handle = vm.getIntParam(1);
            if (handle > 0 && fileStreams.isIndexStored(handle)) {
                fileStreams.free(handle);
                lastError = "";
            } else {
                lastError = "Invalid file handle";
            }
        }
    }

    public final class WrapWriteChar implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
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
            updateError("Write", exception);
        }
    }

    public final class WrapWriteString implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
                return;
            }

            // Write string. (Excludes 0 terminator)
            String str = vm.getStringParam(1);
            Exception exception = null;
            if (!str.isEmpty()) {
                try {
                    stream.out.write(str.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                    exception = e;
                }
                updateError("Write", exception);
            }
        }
    }

    public final class WrapWriteLine implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
                return;
            }

            // Write string. (Excludes 0 terminator)
            String str = vm.getStringParam(1) + "\r\n";
            Exception exception = null;
            if (!str.isEmpty()) {
                try {
                    stream.out.write(str.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                    exception = e;
                }
                updateError("Write", exception);
            }
        }
    }

    public final class WrapWriteByte implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
                return;
            }

            byte element = vm.getIntParam(1).byteValue();
            Exception exception = null;
            try {
                stream.out.write(element);
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
            updateError("Write", exception);
        }
    }

    public final class WrapWriteWord implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
                return;
            }

            short element = vm.getIntParam(1).shortValue();
            Exception exception = null;
            try {
                stream.out.write(ByteBuffer.allocate(2)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putShort(element)
                        .array());
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
            updateError("Write", exception);
        }
    }

    public final class WrapWriteInt implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
                return;
            }

            int element = vm.getIntParam(1);
            Exception exception = null;
            try {
                stream.out.write(ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(element)
                        .array());
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
            updateError("Write", exception);
        }
    }

    public final class WrapWriteFloat implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
                return;
            }

            float element = vm.getRealParam(1);
            Exception exception = null;
            try {
                stream.out.write(ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putFloat(element)
                        .array());
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
            updateError("Write", exception);
        }
    }

    public final class WrapWriteDouble implements Function {
        public void run(TomVM vm) {
            if (!getOutputStream(vm.getIntParam(2))) {
                return;
            }

            double element = vm.getRealParam(1);
            Exception exception = null;
            try {
                stream.out.write(ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putDouble(element)
                        .array());
            } catch (IOException e) {
                e.printStackTrace();
                exception = e;
            }
            updateError("Write", exception);
        }
    }

    public final class WrapReadLine implements Function {
        public void run(TomVM vm) {
            vm.setRegString("");
            StringBuilder regString = new StringBuilder();
            if (!getInputStream(vm.getIntParam(1))) {
                return;
            }
            if (!updateError("Read", null)) {
                return;
            }

            // Skip returns and linefeeds
            int c = 0;
            Exception exception = null;
            try {
                c = (char) stream.in.read();
                while (c == 10 || c == 13) {
                    c = stream.in.read();
                }

                // Read printable characters
                while (c != -1 && c != 10 && c != 13) {
                    regString.append((char) c);
                    c = stream.in.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            } finally {
                // porting note: original source read characters into the vm RegString until reading failed
                // or completed
                vm.setRegString(regString.toString());
            }
            // Don't treat eof as an error
            if (exception != null) {
                updateError("Read", exception);
            } else {
                lastError = "";
            }
        }
    }

    public final class WrapReadChar implements Function {
        public void run(TomVM vm) {
            vm.setRegString("");
            if (!getInputStream(vm.getIntParam(1))) {
                return;
            }

            // Read char
            char c = 0;
            Exception exception = null;
            try {
                c = (char) stream.in.read();
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (updateError("Read", exception)) {
                vm.setRegString(String.valueOf(c));
            }
        }
    }

    public final class WrapReadByte implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(0);
            if (!getInputStream(vm.getIntParam(1))) {
                return;
            }

            // Read byte
            int element = 0; // byte values are unsigned
            Exception exception = null;
            try {
                element = stream.in.read();
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (updateError("Read", exception)) {
                vm.getReg().setIntVal((int) element);
            }
        }
    }

    public final class WrapReadWord implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(0);
            if (!getInputStream(vm.getIntParam(1))) {
                return;
            }

            // Read byte
            int element = 0; // use int since word values are unsigned
            Exception exception = null;
            try {
                byte[] buffer = new byte[Short.SIZE / Byte.SIZE];
                stream.in.read(buffer);
                element = (int) ByteBuffer.wrap(buffer)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asShortBuffer()
                        .get(0);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (updateError("Read", exception)) {
                vm.getReg().setIntVal((int) element);
            }
        }
    }

    public final class WrapReadInt implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(0);
            if (!getInputStream(vm.getIntParam(1))) {
                return;
            }

            // Read byte
            int element = 0;
            Exception exception = null;
            try {
                byte[] buffer = new byte[Integer.SIZE / Byte.SIZE];
                stream.in.read(buffer);
                element = ByteBuffer.wrap(buffer)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asIntBuffer()
                        .get(0);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (updateError("Read", exception)) {
                vm.getReg().setIntVal(element);
            }
        }
    }

    public final class WrapReadFloat implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(0);
            if (!getInputStream(vm.getIntParam(1))) {
                return;
            }

            // Read byte
            float element = 0;
            Exception exception = null;
            try {
                byte[] buffer = new byte[Float.SIZE / Byte.SIZE];
                stream.in.read(buffer);
                element = ByteBuffer.wrap(buffer)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asFloatBuffer()
                        .get(0);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (updateError("Read", exception)) {
                vm.getReg().setRealVal(element);
            }
        }
    }

    public final class WrapReadDouble implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(0);
            if (!getInputStream(vm.getIntParam(1))) {
                return;
            }

            // Read byte
            double element = 0;
            Exception exception = null;
            try {
                byte[] buffer = new byte[Double.SIZE / Byte.SIZE];
                stream.in.read(buffer);
                element = ByteBuffer.wrap(buffer)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asDoubleBuffer()
                        .get(0);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (updateError("Read", exception)) {
                vm.getReg().setRealVal((float) element);
            }
        }
    }

    public final class WrapSeek implements Function {
        public void run(TomVM vm) {
            if (!getStream(vm.getIntParam(2))) {
                return;
            }
            Exception exception = null;
            try {
                if (stream.in != null) {
                    FileChannel ch = ((FileInputStream) stream.in).getChannel();
                    ch.position(vm.getIntParam(1));
                }
                if (stream.out != null) {
                    FileChannel ch = ((FileOutputStream) stream.out).getChannel();
                    ch.position(vm.getIntParam(1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            updateError("Seek", exception);
        }
    }

    public final class WrapReadText implements Function {
        public void run(TomVM vm) {

            // Read a string of non whitespace tokens
            if (!getInputStream(vm.getIntParam(2))) {
                return;
            }
            if (!updateError("Read", null)) {
                return;
            }
            boolean skipNewLines = vm.getIntParam(1) != 0;

            Exception exception = null;

            // Skip leading whitespace
            char c = ' ';
            vm.setRegString("");
            try {
                while ((c != '\n' || skipNewLines) && c <= ' ') {
                    c = (char) stream.in.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (!updateError("Read", exception)) {
                return;
            }

            // Read non whitespace
            try {
                while (c > ' ') {
                    vm.setRegString(vm.getRegString() + c);
                    c = (char) stream.in.read();
                }

                // Backup one character, so that we don't skip the following whitespace
                FileChannel ch = ((FileInputStream) stream.in).getChannel();
                ch.position(ch.position() - 1);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }

            if (!updateError("Read", exception)) {
                return;
            }
        }
    }

    public final class WrapFindFirstFile implements Function {
        public void run(TomVM vm) {

            // Close any previous find
            closeFind();

            // Get filename
            String filename = vm.getStringParam(1);

            // Check path is in files folder
            if (isSandboxMode() && !files.checkFilesFolder(filename)) {
                lastError = files.getError();
                vm.setRegString("");
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
                StringBuffer b = new StringBuffer();
                while (m.find()) {
                    if (m.group(1) != null) {
                        m.appendReplacement(b, ".*");
                    } else {
                        m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
                    }
                }
                m.appendTail(b);
                String pattern = b.toString();

                for (Path entry : stream) {
                    if (entry.toString().matches(pattern)) {
                        findFileCollection.add(new File(entry.toString()));
                    }
                }
                // Alphabetize file list
                Collections.sort(findFileCollection);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            findFileHandle = !findFileCollection.isEmpty() ? 0 : -1;

            // Return filename
            if (findFileHandle != -1 && exception == null) {
                vm.setRegString(findFileCollection.get(0).getName());
            } else {
                vm.setRegString("");
            }
        }
    }

    public final class WrapFindNextFile implements Function {
        public void run(TomVM vm) {

            // Return data
            if (findFileHandle != -1 && findFileCollection != null && findFileHandle < findFileCollection.size()) {
                vm.setRegString(findFileCollection.get(findFileHandle).getName());
                findFileHandle++;
            } else {
                vm.setRegString("");
                findFileHandle = -1;
            }
        }
    }

    public final class WrapFindClose implements Function {
        public void run(TomVM vm) {
            closeFind();
        }
    }

    public final class WrapDeleteFile implements Function {
        public void run(TomVM vm) {
            String filename = vm.getStringParam(1);
            if (files.delete(filename, isSandboxMode())) {
                lastError = "";
                vm.getReg().setIntVal(-1);
            } else {
                lastError = files.getError();
                vm.getReg().setIntVal(0);
            }
        }
    }

    public final class WrapFileError implements Function {
        public void run(TomVM vm) {
            vm.setRegString(lastError);
        }
    }

    public final class WrapEndOfFile implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(-1);
            if (!getStream(vm.getIntParam(1))) {
                return;
            }
            try {
                if ((stream.in != null && (stream.in.available() > 0))
                        || (stream.out != null)) // Todo check if output stream is eof
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
