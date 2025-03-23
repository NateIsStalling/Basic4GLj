package com.basic4gl.library.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.lib.util.*;
import com.basic4gl.library.desktopgl.soundengine.Basic4GLSoundLibrary;
import com.basic4gl.library.desktopgl.soundengine.Sound;
import com.basic4gl.library.desktopgl.soundengine.SoundLibrary;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.ResourceStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nate on 1/19/2016.
 */
public class SoundBasicLib implements FunctionLibrary, IFileAccess {

	private static final String DEFAULT_SOUND_ENGINE_ERROR =
			"Sound playback is not available; the sound engine failed to initialize.";

	private static boolean triedToLoad = false;
	private static SoundLibrary library = null;

	private static FileOpener files = null;

	@Override
	public void init(FileOpener files) {
		SoundBasicLib.files = files;
	}

	////////////////////////////////////////////////////////////////////////////////
	//  Init function
	public final class InitLibFunction implements Function {
		public void run(TomVM vm) {
			if (library != null) {
				library.reset();
			}
		}
	}

	static boolean checkSoundEngine() {
		if (!triedToLoad) {

			// Try to load sound engine
			triedToLoad = true;
			try {
				// Initialise sound library
				library = new Basic4GLSoundLibrary();
				library.init(10);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		return library != null;
	}

	@Override
	public Map<String, Constant> constants() {
		return null;
	}

	@Override
	public Map<String, FunctionSpecification[]> specs() {
		Map<String, FunctionSpecification[]> s = new HashMap<>();
		s.put("loadsound", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapLoadSound.class,
					new ParamTypeList(BasicValType.VTP_STRING),
					true,
					true,
					BasicValType.VTP_INT,
					true,
					false,
					null)
		});
		s.put("deletesound", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapDeleteSound.class,
					new ParamTypeList(BasicValType.VTP_INT),
					true,
					false,
					BasicValType.VTP_INT,
					false,
					false,
					null)
		});
		s.put("playsound", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapPlaySound.class,
					new ParamTypeList(BasicValType.VTP_INT),
					true,
					true,
					BasicValType.VTP_INT,
					false,
					false,
					null),
			new FunctionSpecification(
					WrapPlaySound2.class,
					new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_REAL, BasicValType.VTP_INT),
					true,
					true,
					BasicValType.VTP_INT,
					false,
					false,
					null)
		});
		s.put("stopsoundvoice", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapStopSoundVoice.class,
					new ParamTypeList(BasicValType.VTP_INT),
					true,
					false,
					BasicValType.VTP_INT,
					false,
					false,
					null)
		});
		s.put("stopsounds", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapStopSounds.class, new ParamTypeList(), true, false, BasicValType.VTP_INT, false, false, null)
		});
		s.put("playmusic", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapPlayMusic.class,
					new ParamTypeList(BasicValType.VTP_STRING),
					true,
					false,
					BasicValType.VTP_INT,
					true,
					false,
					null),
			new FunctionSpecification(
					WrapPlayMusic2.class,
					new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_REAL, BasicValType.VTP_INT),
					true,
					false,
					BasicValType.VTP_INT,
					true,
					false,
					null)
		});
		s.put("stopmusic", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapStopMusic.class, new ParamTypeList(), true, false, BasicValType.VTP_INT, false, false, null)
		});
		s.put("musicplaying", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapMusicPlaying.class, new ParamTypeList(), true, true, BasicValType.VTP_INT, false, false, null)
		});
		s.put("setmusicvolume", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapSetMusicVolume.class,
					new ParamTypeList(BasicValType.VTP_REAL),
					true,
					false,
					BasicValType.VTP_INT,
					false,
					false,
					null)
		});
		s.put("sounderror", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapSoundError.class, new ParamTypeList(), true, true, BasicValType.VTP_STRING, false, false, null)
		});
		return s;
	}

	@Override
	public HashMap<String, String> getTokenTips() {
		return null;
	}

	@Override
	public String name() {
		return SoundBasicLib.class.getSimpleName();
	}

	@Override
	public String description() {
		return null;
	}

	@Override
	public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {}

	@Override
	public void init(TomBasicCompiler comp, IServiceCollection services) {

		// Register sound resources
		comp.getVM().addResources(sounds);

		// Register initialisation function
		comp.getVM().addInitFunction(new InitLibFunction());
	}

	@Override
	public void cleanup() {
		if (library != null) {
			library.dispose();
		}
		if (sounds != null) {
			sounds.clear();
		}
	}

	@Override
	public List<String> getDependencies() {
		return null;
	}

	@Override
	public List<String> getClassPathObjects() {
		return null;
	}

	/**
	 *  Stores sound objects as returned from the sound engine.
	 */
	public class SoundStore extends ResourceStore<Sound> {
		protected void deleteElement(int index) {
			if (library != null) {
				library.deleteSound(getValueAt(index));
			}
		}

		public SoundStore() {
			super(null);
		}
	}

	private final SoundStore sounds = new SoundStore();

	// region Runtime function wrappers
	public class WrapLoadSound implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {

				// Load sound file
				String filename = files.getFileAbsolutePath(vm.getStringParam(1));

				Sound sound = library.loadSound(filename);
				if (sound != null) {
					vm.getReg().setIntVal(sounds.alloc(sound));
				} else {
					vm.getReg().setIntVal(0);
				}
			} else {
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
					vm.getReg().setIntVal(library.playSound(sounds.getValueAt(handle), 1, false));
				} else {
					vm.getReg().setIntVal(-1);
				}
			} else {
				vm.getReg().setIntVal(-1);
			}
		}
	}

	public class WrapPlaySound2 implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				int handle = vm.getIntParam(3);
				if (handle > 0 && sounds.isIndexStored(handle)) {
					vm.getReg()
							.setIntVal(library.playSound(
									sounds.getValueAt(handle), vm.getRealParam(2), vm.getIntParam(1) != 0));
				} else {
					vm.getReg().setIntVal(-1);
				}
			} else {
				vm.getReg().setIntVal(-1);
			}
		}
	}

	public class WrapStopSounds implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				library.stopSounds();
			}
		}
	}

	public class WrapPlayMusic implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				String filename = files.getFileAbsolutePath(vm.getStringParam(1));
				library.playMusic(filename, 1, false);
			}
		}
	}

	public class WrapPlayMusic2 implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				String filename = files.getFileAbsolutePath(vm.getStringParam(3));
				library.playMusic(filename, vm.getRealParam(2), vm.getIntParam(1) != 0);
			}
		}
	}

	public class WrapStopMusic implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				library.stopMusic();
			}
		}
	}

	public class WrapMusicPlaying implements Function {
		public void run(TomVM vm) {
			vm.getReg().setIntVal(checkSoundEngine() && library.isMusicPlaying() ? -1 : 0);
		}
	}

	public class WrapSetMusicVolume implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				library.setMusicVolume(vm.getRealParam(1));
			}
		}
	}

	public class WrapSoundError implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				StringBuilder buffer = new StringBuilder();
				library.getError(buffer);
				vm.setRegString(buffer.toString());
			} else {
				vm.setRegString(DEFAULT_SOUND_ENGINE_ERROR);
			}
		}
	}

	public class WrapStopSoundVoice implements Function {
		public void run(TomVM vm) {
			if (checkSoundEngine()) {
				library.stopSoundVoice(vm.getIntParam(1));
			}
		}
	}

	// endregion
}
