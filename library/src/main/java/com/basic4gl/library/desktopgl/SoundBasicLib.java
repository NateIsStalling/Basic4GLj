package com.basic4gl.library.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.library.desktopgl.soundengine.MusicStream;
import com.basic4gl.library.desktopgl.soundengine.Sound;
import com.basic4gl.library.desktopgl.soundengine.SoundEngine;
import com.basic4gl.lib.util.FileOpener;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.lib.util.IFileAccess;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.runtime.HasErrorState;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.ResourceStore;
import org.lwjgl.openal.AL10;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nate on 1/19/2016.
 */
public class SoundBasicLib implements FunctionLibrary, IFileAccess {

    // Error state
    static String error = "";
    static boolean triedToLoad = false;

    static FileOpener files            = null;
    static SoundEngine engine = null;
    static MusicStream music = null;
    @Override
    public void init(FileOpener files) {
        this.files = files;
    }

    ////////////////////////////////////////////////////////////////////////////////
//  Init function
    public final class InitLibFunction implements Function {
        public void run(TomVM vm) {
            SndReset();
        }
    }
    static boolean checkSoundEngine() {
        if (!triedToLoad) {

            // Try to load sound engine

            // Initialise sound library
            engine = new SoundEngine(10);
            triedToLoad = true;
        }

        return true;//dll != null;
    }

    @Override
    public Map<String, Constant> constants() {
        return null;
    }

    @Override
    public Map<String, FunctionSpecification[]> specs() {
        Map<String, FunctionSpecification[]> s = new HashMap<>();
        s.put("loadsound", new FunctionSpecification[]{ new FunctionSpecification( WrapLoadSound.class, new ParamTypeList( BasicValType.VTP_STRING), true, true, BasicValType.VTP_INT, true, false, null)});
        s.put("deletesound", new FunctionSpecification[]{ new FunctionSpecification( WrapDeleteSound.class, new ParamTypeList (BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, false, false, null)});
        s.put("playsound", new FunctionSpecification[]{ new FunctionSpecification( WrapPlaySound.class, new ParamTypeList (BasicValType.VTP_INT), true, true, BasicValType.VTP_INT, false, false, null),
                new FunctionSpecification( WrapPlaySound2.class, new ParamTypeList (BasicValType.VTP_INT, BasicValType.VTP_REAL, BasicValType.VTP_INT), true, true, BasicValType.VTP_INT, false, false, null)});
        s.put("stopsoundvoice", new FunctionSpecification[]{ new FunctionSpecification( WrapStopSoundVoice.class, new ParamTypeList (BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, false, false, null)});
        s.put("stopsounds", new FunctionSpecification[]{ new FunctionSpecification( WrapStopSounds.class, new ParamTypeList(), true, false, BasicValType.VTP_INT, false, false, null)});
        s.put("playmusic", new FunctionSpecification[]{ new FunctionSpecification( WrapPlayMusic.class, new ParamTypeList (BasicValType.VTP_STRING), true, false, BasicValType.VTP_INT, true, false, null),
                new FunctionSpecification( WrapPlayMusic2.class, new ParamTypeList (BasicValType.VTP_STRING, BasicValType.VTP_REAL, BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put("stopmusic", new FunctionSpecification[]{ new FunctionSpecification( WrapStopMusic.class, new ParamTypeList(), true, false, BasicValType.VTP_INT, false, false, null)});
        s.put("musicplaying", new FunctionSpecification[]{ new FunctionSpecification( WrapMusicPlaying.class, new ParamTypeList(), true, true, BasicValType.VTP_INT, false, false, null)});
        s.put("setmusicvolume", new FunctionSpecification[]{ new FunctionSpecification( WrapSetMusicVolume.class, new ParamTypeList (BasicValType.VTP_REAL), true, false, BasicValType.VTP_INT, false, false, null)});
        s.put("sounderror", new FunctionSpecification[]{ new FunctionSpecification( WrapSoundError.class, new ParamTypeList(), true, true, BasicValType.VTP_STRING, false, false, null)});
        return s;
    }

    @Override
    public HashMap<String, String> getTokenTips() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public void init(TomVM vm) {

    }

    @Override
    public void init(TomBasicCompiler comp) {

        // Register sound resources
        comp.VM().addResources(sounds);

        // Register initialisation function
        comp.VM().addInitFunction(new InitLibFunction());
    }

    @Override
    public void cleanup() {
        engine.dispose();
        sounds.clear();
    }

    @Override
    public List<String> getDependencies() {
        return null;
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }

    static boolean checkError(HasErrorState obj) {
        if (obj.hasError()) {
            error = obj.getError();
            return false;
        }
        else {
            error = "";
            return true;
        }
    }

    /**
     *  Stores sound objects as returned from the sound engine.
     */
    public class SoundStore extends ResourceStore<Sound> {
        protected void deleteElement(int index) {
            //if (dll != null)
                SndDeleteSound(getValueAt(index));
        }
        public SoundStore(){ super(null) ;}
    }


    SoundStore sounds = new SoundStore();

    //region Runtime function wrappers
    public class WrapLoadSound implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {

                // Load sound file
                String filename = files.FilenameForRead(vm.getStringParam(1), false);
                Sound sound = SndLoadSound(filename);
                if (sound != null) {
                    vm.getReg().setIntVal( sounds.alloc(sound));
                } else {
                    vm.getReg().setIntVal(0);
                }
            }
            else {
                vm.getReg().setIntVal(0);
            }
        }
    }
    public class WrapDeleteSound implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                int handle = vm.getIntParam(1);
                if (handle > 0 && sounds.isIndexStored(handle)) {
                    sounds.free(handle);
                }
            }
        }
    }
    public class WrapPlaySound implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                int handle = vm.getIntParam(1);
                if (handle > 0 && sounds.isIndexStored(handle)) {
                    vm.getReg().setIntVal( SndDoPlaySound(sounds.getValueAt(handle), 1, false));
                } else {
                    vm.getReg().setIntVal(-1);
                }
            }
            else {
                vm.getReg().setIntVal(-1);
            }
        }
    }
    public class WrapPlaySound2 implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                int handle = vm.getIntParam(3);
                if (handle > 0 && sounds.isIndexStored(handle)) {
                    vm.getReg().setIntVal( SndDoPlaySound(sounds.getValueAt(handle), vm.getRealParam(2), vm.getIntParam(1) != 0));
                } else {
                    vm.getReg().setIntVal(-1);
                }
            }
            else {
                vm.getReg().setIntVal(-1);
            }
        }
    }
    public class WrapStopSounds implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                SndStopSounds();
            }
        }
    }
    public class WrapPlayMusic implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                String filename = files.FilenameForRead(vm.getStringParam(1), false);
                SndPlayMusic(filename, 1, false);
            }
        }
    }
    public class WrapPlayMusic2 implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                String filename = files.FilenameForRead(vm.getStringParam(3), false);
                SndPlayMusic(filename, vm.getRealParam(2), vm.getIntParam(1) != 0);
            }
        }
    }
    public class WrapStopMusic implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                SndStopMusic();
            }
        }
    }
    public class WrapMusicPlaying implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal( checkSoundEngine() && SndMusicPlaying() ? -1 : 0);
        }
    }
    public class WrapSetMusicVolume implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                SndSetMusicVolume(vm.getRealParam(1));
            }
        }
    }
    public class WrapSoundError implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                StringBuilder buffer = new StringBuilder();
                SndGetError(buffer);
                vm.setRegString( buffer.toString());
            } else {
                //TODO update error message; Basic4GLj does not use the mentioned dll's
                vm.setRegString( "Sound playback requires Audiere.dll and B4GLSound.dll to be placed in the same folder");
            }
        }
    }
    public class WrapStopSoundVoice implements Function {
        public void run(TomVM vm) {
            if (checkSoundEngine()) {
                SndStopSoundVoice(vm.getIntParam(1));
            }
        }
    }

    //endregion



    //region Sound Engine functions; may be moved to different class
    private void SndReset(){
        if (engine != null) {
            engine.stopAll();
        }
        if (music != null) {
            music.closeFile();
        }
    }
    private Sound SndLoadSound(String filename){
        //Todo load sound file
        Sound s = new Sound(filename);
        if (checkError(s)){
            return s;
        }
        else {
            s.dispose();
            return null;
        }
    }
    private void SndDeleteSound(Sound sound){
        if (sound != null) {
            sound.dispose();
        }

    }
    private int SndDoPlaySound(Sound sound, float volume, boolean looped){
        if (sound != null) {
            int voice = engine.playSound(sound, volume, looped);
            checkError(sound);
            return voice;
        }
        else {
            return -1;
        }
    }
    private void SndStopSounds(){
        engine.stopAll();
    }
    private void SndPlayMusic(String filename, float volume, boolean looped){
        music.openFile(filename, volume, looped);
        music.updateErrorState();
        checkError(music);
    }
    private void SndStopMusic(){
        music.closeFile();
    }
    private boolean SndMusicPlaying(){
        return music.isPlaying();
    }
    private void SndSetMusicVolume(float volume){
        music.setGain(volume);
    }
    private void SndStopSoundVoice(int voice){
        engine.stopVoice(voice);
    }
    private void SndGetError(StringBuilder message){
        message.append(AL10.alGetError());
    }

    //endregion
}
