## Mac Signing Notes

- Basic4GLj relies on embedded helper tools to support displaying output in a separate window and debugging functionality
- jpackage is utilized to bundle release builds with OpenJDK and include required embedded helper tools
- OpenJDK 21 is being evaluated for Basic4GLj distributions for its jpackage options to support `--app-content embedded.provisionprofile`, which is needed to properly sign the `provisionprofile` when strictly relying on jpackage mac-sign functionality
- Signing Basic4GLj MacOS jpackage distributions with the current version of OpenJDK 21 fails at runtime when attempting to launch the embedded tools with plist errors
- See Apple's documentation on Embedding a Helper Tool In a Sandboxed App
  - https://developer.apple.com/documentation/xcode/embedding-a-helper-tool-in-a-sandboxed-app
  - `Adding other entitlements to the tool can cause problems. If the tool immediately crashes with a code signing error when your app runs the tool, check that the tool is signed with just these two entitlements: com.apple.security.app-sandbox and com.apple.security.inherit.`
- OpenJDK 21+35 jpackage implementation uses the same entitlements when signing the app and embedded executables
- A temporary workaround may be to modify OpenJDK's jpackage tool to support separate entitlements for signing bundle executables and signing the app itself in `jdk.jpackage.internal.MacAppImageBuilder.signAppBundle(...)`
- Manually handling signing process would allow supporting older OpenJDK versions (JDK 17 minimum is still preference for jpackage support and license)

### TODO
- **Need to test signed build after completing Mac notarization process (built using customized JDK jpackage workaround)**
  - Pre-notarization build still fails at runtime with an unknown developer error when attempting to load the LWJGL dylib
  - Technically a different error from the PLIST error (the PLIST error only showed up in the MacOS system console and otherwise failed silently)
  - :crossedfingers:
- **Evaluate replacing jpackage mac-sign usage with sh script for MacOS signing build steps**

### Application Entitlements PLIST
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.security.cs.allow-jit</key>
    <true/>
    <key>com.apple.security.cs.allow-unsigned-executable-memory</key>
    <true/>
    <key>com.apple.security.cs.disable-library-validation</key>
    <true/>
	<key>com.apple.security.cs.allow-dyld-environment-variables</key>
	<true/>
	<key>com.apple.security.network.server</key>
	<true/>
	<key>com.apple.security.network.client</key>
	<true/>
	<key>com.apple.security.app-sandbox</key>
	<true/>
</dict>
</plist>
```
_sandbox.plist_
### Embedded Helper Tool Entitlements PLIST
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
  <dict>
    <key>com.apple.security.app-sandbox</key>
    <true/>
    <key>com.apple.security.inherit</key>
    <true/>
    <key>com.apple.security.cs.allow-unsigned-executable-memory</key>
    <true/>
    <key>com.apple.security.cs.disable-library-validation</key>
    <true/>
  </dict>
</plist>
```
_embedded-tool.plist_ 