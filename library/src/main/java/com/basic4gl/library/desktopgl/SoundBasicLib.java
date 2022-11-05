//package com.basic4gl.library.desktopgl;
//
//import com.basic4gl.compiler.Constant;
//import com.basic4gl.compiler.ParamTypeList;
//import com.basic4gl.compiler.TomBasicCompiler;
//import com.basic4gl.library.desktopgl.soundengine.MusicStream;
//import com.basic4gl.library.desktopgl.soundengine.Sound;
//import com.basic4gl.library.desktopgl.soundengine.SoundEngine;
//import com.basic4gl.lib.util.FileOpener;
//import com.basic4gl.lib.util.FunctionLibrary;
//import com.basic4gl.lib.util.IFileAccess;
//import com.basic4gl.compiler.util.FuncSpec;
//import com.basic4gl.runtime.HasErrorState;
//import com.basic4gl.runtime.TomVM;
//import com.basic4gl.runtime.types.ValType;
//import com.basic4gl.runtime.util.Function;
//import com.basic4gl.runtime.util.ResourceStore;
//import org.lwjgl.openal.AL10;
//import org.lwjgl.openal.ALC10;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by Nate on 1/19/2016.
// */
//public class SoundBasicLib implements FunctionLibrary, IFileAccess {
//
//    // Error state
//    static String error = "";
//    static boolean triedToLoad = false;
//
//    static FileOpener files            = null;
//    static SoundEngine engine = null;
//    static MusicStream music = null;
//    @Override
//    public void init(FileOpener files) {
//        this.files = files;
//    }
//
//    ////////////////////////////////////////////////////////////////////////////////
////  Init function
//    public final class InitLibFunction implements Function {
//        public void run(TomVM vm) {
//            SndReset();
//        }
//    }
//    static boolean CheckSoundEngine() {
//        if (!triedToLoad) {
//
//            // Try to load sound engine
//
//            // Initialise sound library
//            engine = new SoundEngine(10);
//            triedToLoad = true;
//        }
//
//        return true;//dll != null;
//    }
//
//    @Override
//    public Map<String, Constant> constants() {
//        return null;
//    }
//
//    @Override
//    public Map<String, FuncSpec[]> specs() {
//        Map<String, FuncSpec[]> s = new HashMap<>();
//        s.put("loadsound", new FuncSpec[]{ new FuncSpec( WrapLoadSound.class, new ParamTypeList( ValType.VTP_STRING), true, true, ValType.VTP_INT, true, false, null)});
//        s.put("deletesound", new FuncSpec[]{ new FuncSpec( WrapDeleteSound.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
//        s.put("playsound", new FuncSpec[]{ new FuncSpec( WrapPlaySound.class, new ParamTypeList(ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null),
//                new FuncSpec( WrapPlaySound2.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_REAL, ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
//        s.put("stopsoundvoice", new FuncSpec[]{ new FuncSpec( WrapStopSoundVoice.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
//        s.put("stopsounds", new FuncSpec[]{ new FuncSpec( WrapStopSounds.class, new ParamTypeList(), true, false, ValType.VTP_INT, false, false, null)});
//        s.put("playmusic", new FuncSpec[]{ new FuncSpec( WrapPlayMusic.class, new ParamTypeList(ValType.VTP_STRING), true, false, ValType.VTP_INT, true, false, null),
//                new FuncSpec( WrapPlayMusic2.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_REAL, ValType.VTP_INT), true, false, ValType.VTP_INT, true, false, null)});
//        s.put("stopmusic", new FuncSpec[]{ new FuncSpec( WrapStopMusic.class, new ParamTypeList(), true, false, ValType.VTP_INT, false, false, null)});
//        s.put("musicplaying", new FuncSpec[]{ new FuncSpec( WrapMusicPlaying.class, new ParamTypeList(), true, true, ValType.VTP_INT, false, false, null)});
//        s.put("setmusicvolume", new FuncSpec[]{ new FuncSpec( WrapSetMusicVolume.class, new ParamTypeList(ValType.VTP_REAL), true, false, ValType.VTP_INT, false, false, null)});
//        s.put("sounderror", new FuncSpec[]{ new FuncSpec( WrapSoundError.class, new ParamTypeList(), true, true, ValType.VTP_STRING, false, false, null)});
//        return s;
//    }
//
//    @Override
//    public HashMap<String, String> getTokenTips() {
//        return null;
//    }
//
//    @Override
//    public String name() {
//        return null;
//    }
//
//    @Override
//    public String description() {
//        return null;
//    }
//
//    @Override
//    public void init(TomVM vm) {
//
//    }
//
//    @Override
//    public void init(TomBasicCompiler comp) {
//
//        // Register sound resources
//        comp.VM().AddResources(sounds);
//
//        // Register initialisation function
//        comp.VM().AddInitFunc (new InitLibFunction());
//    }
//
//    @Override
//    public void cleanup() {
//        engine.dispose();
//        sounds.Clear();
//    }
//
//    @Override
//    public List<String> getDependencies() {
//        return null;
//    }
//
//    @Override
//    public List<String> getClassPathObjects() {
//        return null;
//    }
//
//    ////////////////////////////////////////////////////////////////////////////////
////  SoundStore
////
///// Stores sound objects as returned from the DLL.
//    public class SoundStore extends ResourceStore<Sound> {
//        protected void DeleteElement(int index) {
//            //if (dll != null)
//                SndDeleteSound(Value(index));
//        }
//        public SoundStore(){ super(null) ;}
//    }
//
//
//    SoundStore sounds = new SoundStore();
//
//    ////////////////////////////////////////////////////////////////////////////////
////  Runtime function wrappers
//    public class WrapLoadSound implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine()) {
//
//                // Load sound file
//                String filename = files.FilenameForRead(vm.GetStringParam(1), false);
//                Sound sound = SndLoadSound(filename);
//                if (sound != null)
//                    vm.Reg().setIntVal( sounds.Alloc(sound));
//                else
//                    vm.Reg().setIntVal(0);
//            }
//            else
//                vm.Reg().setIntVal(0);
//        }
//    }
//    public class WrapDeleteSound implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine()) {
//                int handle = vm.GetIntParam(1);
//                if (handle > 0 && sounds.IndexStored(handle))
//                    sounds.Free(handle);
//            }
//        }
//    }
//    public class WrapPlaySound implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine()) {
//                int handle = vm.GetIntParam(1);
//                if (handle > 0 && sounds.IndexStored(handle))
//                    vm.Reg().setIntVal( SndDoPlaySound(sounds.Value(handle), 1, false));
//                else
//                    vm.Reg().setIntVal(-1);
//            }
//            else
//                vm.Reg().setIntVal(-1);
//        }
//    }
//    public class WrapPlaySound2 implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine()) {
//                int handle = vm.GetIntParam(3);
//                if (handle > 0 && sounds.IndexStored(handle))
//                    vm.Reg().setIntVal( SndDoPlaySound(sounds.Value(handle), vm.GetRealParam(2), vm.GetIntParam(1) != 0));
//                else
//                    vm.Reg().setIntVal(-1);
//            }
//            else
//                vm.Reg().setIntVal(-1);
//        }
//    }
//    public class WrapStopSounds implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine())
//                SndStopSounds();
//        }
//    }
//    public class WrapPlayMusic implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine()) {
//                String filename = files.FilenameForRead(vm.GetStringParam(1), false);
//                SndPlayMusic(filename, 1, false);
//            }
//        }
//    }
//    public class WrapPlayMusic2 implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine()) {
//                String filename = files.FilenameForRead(vm.GetStringParam(3), false);
//                SndPlayMusic(filename, vm.GetRealParam(2), vm.GetIntParam(1) != 0);
//            }
//        }
//    }
//    public class WrapStopMusic implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine())
//                SndStopMusic();
//        }
//    }
//    public class WrapMusicPlaying implements Function {
//        public void run(TomVM vm) {
//            vm.Reg().setIntVal( CheckSoundEngine() && SndMusicPlaying() ? -1 : 0);
//        }
//    }
//    public class WrapSetMusicVolume implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine())
//                SndSetMusicVolume(vm.GetRealParam(1));
//        }
//    }
//    public class WrapSoundError implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine()) {
//                StringBuilder buffer = new StringBuilder();
//                SndGetError(buffer);
//                vm.setRegString( buffer.toString());
//            }
//            else
//                //TODO update error message; Basic4GLj does not use the mentioned dll's
//                vm.setRegString( "Sound playback requires Audiere.dll and B4GLSound.dll to be placed in the same folder");
//        }
//    }
//    public class WrapStopSoundVoice implements Function {
//        public void run(TomVM vm) {
//            if (CheckSoundEngine())
//                SndStopSoundVoice(vm.GetIntParam(1));
//        }
//    }
//
//
//
//    //DLL functions; may be moved to different class
//    private void SndReset(){
//        if (engine != null)
//            engine.StopAll();
//        if (music != null)
//            music.CloseFile();
//    }
//    private Sound SndLoadSound(String filename){
//        //Todo load sound file
//        Sound s = new Sound(filename);
//        if (CheckError(s)){
//            return s;
//        }
//        else {
//            s.dispose();
//            return null;
//        }
//    }
//    private void SndDeleteSound(Sound sound){
//        if (sound != null)
//            sound.dispose();
//
//    }
//    private int SndDoPlaySound(Sound sound, float volume, boolean looped){
//        if (sound != null) {
//            int voice = engine.PlaySound( sound, volume, looped);
//            CheckError(sound);
//            return voice;
//        }
//        else
//            return -1;
//    }
//    private void SndStopSounds(){
//        engine.StopAll();
//    }
//    private void SndPlayMusic(String filename, float volume, boolean looped){
//        music.OpenFile(filename, volume, looped);
//        music.UpdateErrorState();
//        CheckError(music);
//    }
//    private void SndStopMusic(){
//        music.CloseFile();
//    }
//    private boolean SndMusicPlaying(){
//        return music.Playing();
//    }
//    private void SndSetMusicVolume(float volume){
//        music.SetGain(volume);
//    }
//    private void SndStopSoundVoice(int voice){
//        engine.StopVoice(voice);
//    }
//    private void SndGetError(StringBuilder message){
//        message.append(AL10.alGetError());
//    }
//
//    static boolean CheckError(HasErrorState obj) {
//        if (obj.hasError()) {
//            error = obj.getError();
//            return false;
//        }
//        else {
//            error = "";
//            return true;
//        }
//    }
//}
