//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.fife.ui.rtextarea;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.ActiveLineRangeEvent;
import org.fife.ui.rsyntaxtextarea.ActiveLineRangeListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class MultiHeaderGutter extends JPanel {
  public static final Color DEFAULT_ACTIVE_LINE_RANGE_COLOR = new Color(51, 153, 255);
  private RTextArea textArea;
  private JPanel headerArea;
  private LineNumberList lineNumberList;
  private Color lineNumberColor;
  private int lineNumberingStartIndex;
  private Font lineNumberFont;
  private List<IconRowHeader> iconAreas;
  private List<Boolean> autoHideIconArea = new ArrayList<Boolean>();
  private boolean iconRowHeaderInheritsGutterBackground;
  private FoldIndicator foldIndicator;
  private MultiHeaderGutter.TextAreaListener listener = new MultiHeaderGutter.TextAreaListener();

  public MultiHeaderGutter(RTextArea textArea) {
    this.lineNumberColor = Color.gray;
    this.lineNumberFont = RTextArea.getDefaultFont();
    this.lineNumberingStartIndex = 1;
    this.iconRowHeaderInheritsGutterBackground = false;
    this.setTextArea(textArea);
    this.setLayout(new BorderLayout());
    if (this.textArea != null) {
      this.setLineNumbersEnabled(true);
      if (this.textArea instanceof RSyntaxTextArea) {
        RSyntaxTextArea bg = (RSyntaxTextArea) this.textArea;
        this.setFoldIndicatorEnabled(bg.isCodeFoldingEnabled());
      }
    }

    this.setBorder(new MultiHeaderGutter.GutterBorder(0, 0, 0, 1));
    Color bg1 = null;
    if (textArea != null) {
      bg1 = textArea.getBackground();
    }

    this.setBackground(bg1 != null ? bg1 : Color.WHITE);

    this.headerArea = new JPanel();
    this.headerArea.setLayout(new BoxLayout(this.headerArea, BoxLayout.LINE_AXIS));
    this.add(this.headerArea, "Before");
  }

  public GutterIconInfo addLineTrackingIcon(int headerIndex, int line, Icon icon)
      throws BadLocationException {
    return this.addLineTrackingIcon(headerIndex, line, icon, (String) null);
  }

  public GutterIconInfo addLineTrackingIcon(int headerIndex, int line, Icon icon, String tip)
      throws BadLocationException {
    int offs = this.textArea.getLineStartOffset(line);
    return this.addOffsetTrackingIcon(headerIndex, offs, icon, tip);
  }

  public GutterIconInfo addOffsetTrackingIcon(int offs, Icon icon) throws BadLocationException {
    return this.addOffsetTrackingIcon(0, offs, icon, (String) null);
  }

  public GutterIconInfo addOffsetTrackingIcon(int headerIndex, int offs, Icon icon, String tip)
      throws BadLocationException {
    if (headerIndex > -1 && headerIndex < iconAreas.size()) {
      return this.iconAreas.get(headerIndex).addOffsetTrackingIcon(offs, icon, tip);
    } else {
      return null;
    }
  }

  private void clearActiveLineRange() {
    for (IconRowHeader iconArea : this.iconAreas) {
      iconArea.clearActiveLineRange();
    }
  }

  private void clearActiveLineRange(int headerIndex) {
    this.iconAreas.get(headerIndex).clearActiveLineRange();
  }

  public Color getActiveLineRangeColor(int headerIndex) {
    return this.iconAreas.get(headerIndex).getActiveLineRangeColor();
  }

  public Icon getBookmarkIcon(int headerIndex) {
    return this.iconAreas.get(headerIndex).getBookmarkIcon();
  }

  public GutterIconInfo[] getBookmarks(int headerIndex) {
    return this.iconAreas.get(headerIndex).getBookmarks();
  }

  public Color getBorderColor() {
    return ((MultiHeaderGutter.GutterBorder) this.getBorder()).getColor();
  }

  public Color getFoldBackground() {
    return this.foldIndicator.getFoldIconBackground();
  }

  public Color getFoldIndicatorForeground() {
    return this.foldIndicator.getForeground();
  }

  public boolean getIconRowHeaderInheritsGutterBackground() {
    return this.iconRowHeaderInheritsGutterBackground;
  }

  public Color getLineNumberColor() {
    return this.lineNumberColor;
  }

  public Font getLineNumberFont() {
    return this.lineNumberFont;
  }

  public int getLineNumberingStartIndex() {
    return this.lineNumberingStartIndex;
  }

  public boolean getLineNumbersEnabled() {
    for (int i = 0; i < this.getComponentCount(); ++i) {
      if (this.getComponent(i) == this.lineNumberList) {
        return true;
      }
    }

    return false;
  }

  public boolean getShowCollapsedRegionToolTips() {
    return this.foldIndicator.getShowCollapsedRegionToolTips();
  }

  public GutterIconInfo[] getTrackingIcons(int headerIndex, Point p) throws BadLocationException {
    int offs = this.textArea.viewToModel(new Point(0, p.y));
    int line = this.textArea.getLineOfOffset(offs);
    return this.iconAreas.get(headerIndex).getTrackingIcons(line);
  }

  public boolean isFoldIndicatorEnabled() {
    for (int i = 0; i < this.getComponentCount(); ++i) {
      if (this.getComponent(i) == this.foldIndicator) {
        return true;
      }
    }

    return false;
  }

  public boolean isBookmarkingEnabled(int headerIndex) {
    return this.iconAreas.get(headerIndex).isBookmarkingEnabled();
  }

  public boolean isIconRowHeaderEnabled(int headerIndex) {
    for (int i = 0; i < this.getComponentCount(); ++i) {
      if (this.getComponent(i) == this.iconAreas.get(headerIndex)) {
        return true;
      }
    }

    return false;
  }

  public void removeTrackingIcon(int headerIndex, GutterIconInfo tag) {
    this.iconAreas.get(headerIndex).removeTrackingIcon(tag);
  }

  public void removeAllTrackingIcons() {
    for (IconRowHeader iconArea : this.iconAreas) {
      iconArea.removeAllTrackingIcons();
    }
  }

  public void removeAllTrackingIcons(int headerIndex) {
    this.iconAreas.get(headerIndex).removeAllTrackingIcons();
  }

  public void setActiveLineRangeColor(int headerIndex, Color color) {
    this.iconAreas.get(headerIndex).setActiveLineRangeColor(color);
  }

  private void setActiveLineRange(int startLine, int endLine) {
    for (IconRowHeader iconArea : this.iconAreas) {
      iconArea.setActiveLineRange(startLine, endLine);
    }
  }

  public void setBookmarkIcon(int headerIndex, Icon icon) {
    this.iconAreas.get(headerIndex).setBookmarkIcon(icon);
  }

  public void setBookmarkingEnabled(int headerIndex, boolean enabled) {
    this.iconAreas.get(headerIndex).setBookmarkingEnabled(enabled);
    if (enabled && !this.isIconRowHeaderEnabled(headerIndex)) {
      this.setIconRowHeaderEnabled(headerIndex, true);
    }
  }

  public void setBorderColor(Color color) {
    ((MultiHeaderGutter.GutterBorder) this.getBorder()).setColor(color);
    this.repaint();
  }

  public void setComponentOrientation(ComponentOrientation o) {
    if (o.isLeftToRight()) {
      ((MultiHeaderGutter.GutterBorder) this.getBorder()).setEdges(0, 0, 0, 1);
    } else {
      ((MultiHeaderGutter.GutterBorder) this.getBorder()).setEdges(0, 1, 0, 0);
    }

    super.setComponentOrientation(o);
  }

  //    public void setFoldIcons(Icon collapsedIcon, Icon expandedIcon) {
  //        if(this.foldIndicator != null) {
  //            FoldIndicatorIcon collapsedFoldIndicatorIcon = new FoldIndicatorIcon();
  //            this.foldIndicator.setFoldIcons(collapsedIcon, expandedIcon);
  //        }
  //
  //    }

  public void setFoldIndicatorEnabled(boolean enabled) {
    if (this.foldIndicator != null) {
      if (enabled) {
        this.add(this.foldIndicator, "After");
      } else {
        this.remove(this.foldIndicator);
      }

      this.revalidate();
    }
  }

  public void setFoldBackground(Color bg) {
    if (bg == null) {
      bg = FoldIndicator.DEFAULT_FOLD_BACKGROUND;
    }

    this.foldIndicator.setFoldIconBackground(bg);
  }

  public void setFoldIndicatorForeground(Color fg) {
    if (fg == null) {
      fg = FoldIndicator.DEFAULT_FOREGROUND;
    }

    this.foldIndicator.setForeground(fg);
  }

  public int getIconRowHeaderCount() {
    return this.iconAreas != null ? this.iconAreas.size() : -1;
  }

  public void removeIconRowHeader(int headerIndex) {
    setIconRowHeaderEnabled(headerIndex, false);
    this.iconAreas.remove(headerIndex);
  }

  public void addIconRowHeader() {
    if (this.iconAreas == null) {
      this.iconAreas = new ArrayList<IconRowHeader>();
    }
    RTextAreaEditorKit kit = (RTextAreaEditorKit) textArea.getUI().getEditorKit(textArea);
    IconRowHeader header = kit.createIconRowHeader(textArea);
    header.setInheritsGutterBackground(this.getIconRowHeaderInheritsGutterBackground());
    this.iconAreas.add(header);
    this.autoHideIconArea.add(false);
    setIconRowHeaderEnabled(this.iconAreas.size() - 1, true);
  }

  void addIconRowHeader(RTextArea textArea) {
    if (this.iconAreas == null) {
      this.iconAreas = new ArrayList<IconRowHeader>();
    }
    RTextAreaEditorKit kit = (RTextAreaEditorKit) textArea.getUI().getEditorKit(textArea);
    IconRowHeader header = kit.createIconRowHeader(textArea);
    header.setInheritsGutterBackground(this.getIconRowHeaderInheritsGutterBackground());
    this.iconAreas.add(header);
    this.autoHideIconArea.add(false);
    setIconRowHeaderEnabled(this.iconAreas.size() - 1, true);
  }

  public IconRowHeader getIconRowHeader(int headerIndex) {
    return this.iconAreas.get(headerIndex);
  }

  void setIconRowHeaderEnabled(int headerIndex, boolean enabled) {
    if (this.iconAreas == null) {
      return;
    }
    if (headerIndex > -1
        && headerIndex < this.iconAreas.size()
        && this.iconAreas.get(headerIndex) != null) {
      if (enabled) {
        int position =
            (headerIndex < this.headerArea.getComponentCount())
                ? headerIndex
                : this.headerArea.getComponentCount();
        // TODO Remove magic number; readd elements in order
        this.headerArea.add(this.iconAreas.get(headerIndex), 0);
      } else {
        this.headerArea.remove(this.iconAreas.get(headerIndex));
      }

      this.revalidate();
    }
  }

  public void setIconRowHeaderInheritsGutterBackground(boolean inherits) {
    if (inherits != this.iconRowHeaderInheritsGutterBackground) {
      this.iconRowHeaderInheritsGutterBackground = inherits;
      for (IconRowHeader iconArea : this.iconAreas) {
        if (iconArea != null) {
          iconArea.setInheritsGutterBackground(inherits);
        }
      }
    }
  }

  public void setIconRowHeaderInheritsGutterBackground(int headerIndex, boolean inherits) {
    if (inherits != this.iconRowHeaderInheritsGutterBackground) {
      this.iconRowHeaderInheritsGutterBackground = inherits;
      if (headerIndex > -1 && headerIndex < this.iconAreas.size()) {
        if (iconAreas.get(headerIndex) != null) {
          this.iconAreas.get(headerIndex).setInheritsGutterBackground(inherits);
        }
      }
    }
  }

  public void setLineNumberColor(Color color) {
    if (color != null && !color.equals(this.lineNumberColor)) {
      this.lineNumberColor = color;
      if (this.lineNumberList != null) {
        this.lineNumberList.setForeground(color);
      }
    }
  }

  public void setLineNumberFont(Font font) {
    if (font == null) {
      throw new IllegalArgumentException("font cannot be null");
    } else {
      if (!font.equals(this.lineNumberFont)) {
        this.lineNumberFont = font;
        if (this.lineNumberList != null) {
          this.lineNumberList.setFont(font);
        }
      }
    }
  }

  public void setLineNumberingStartIndex(int index) {
    if (index != this.lineNumberingStartIndex) {
      this.lineNumberingStartIndex = index;
      this.lineNumberList.setLineNumberingStartIndex(index);
    }
  }

  void setLineNumbersEnabled(boolean enabled) {
    if (this.lineNumberList != null) {
      if (enabled) {
        this.add(this.lineNumberList);
      } else {
        this.remove(this.lineNumberList);
      }

      this.revalidate();
    }
  }

  public void setShowCollapsedRegionToolTips(boolean show) {
    if (this.foldIndicator != null) {
      this.foldIndicator.setShowCollapsedRegionToolTips(show);
    }
  }

  void setTextArea(RTextArea textArea) {
    if (this.textArea != null) {
      this.listener.uninstall();
    }

    if (textArea != null) {
      RTextAreaEditorKit kit = (RTextAreaEditorKit) textArea.getUI().getEditorKit(textArea);
      if (this.lineNumberList == null) {
        this.lineNumberList = kit.createLineNumberList(textArea);
        this.lineNumberList.setFont(this.getLineNumberFont());
        this.lineNumberList.setForeground(this.getLineNumberColor());
        this.lineNumberList.setLineNumberingStartIndex(this.getLineNumberingStartIndex());
      } else {
        this.lineNumberList.setTextArea(textArea);
      }
      if (this.iconAreas == null) {
        this.iconAreas = new ArrayList<IconRowHeader>();
      }
      if (this.iconAreas.size() != 0) {
        for (IconRowHeader iconArea : this.iconAreas) {
          iconArea.setTextArea(textArea);
        }
      }

      if (this.foldIndicator == null) {
        this.foldIndicator = new FoldIndicator(textArea);
      } else {
        this.foldIndicator.setTextArea(textArea);
      }

      this.listener.install(textArea);
    }

    this.textArea = textArea;
  }

  public boolean toggleBookmark(int headerIndex, int line) throws BadLocationException {
    int bookmarkCount = this.getBookmarks(headerIndex).length;
    boolean result = this.iconAreas.get(headerIndex).toggleBookmark(line);
    if (this.autoHideIconArea.get(headerIndex)) {
      if (this.getBookmarks(headerIndex).length == 0) {
        this.setIconRowHeaderEnabled(headerIndex, false);
      } else if (bookmarkCount == 0 && this.getBookmarks(headerIndex).length > 0) {
        this.setIconRowHeaderEnabled(headerIndex, true);
      }
    }
    return result;
  }

  public void setAutohideIconRowHeader(int headerIndex, boolean autohide) {
    if (headerIndex > -1 && headerIndex < this.autoHideIconArea.size()) {
      this.autoHideIconArea.set(headerIndex, autohide);
    }
    if (this.iconAreas.get(headerIndex).getBookmarks().length == 0) {
      setIconRowHeaderEnabled(headerIndex, false);
    }
  }

  public void setBorder(Border border) {
    if (border instanceof MultiHeaderGutter.GutterBorder) {
      super.setBorder(border);
    }
  }

  private static class GutterBorder extends EmptyBorder {
    private Color color = new Color(221, 221, 221);
    private Rectangle visibleRect = new Rectangle();

    public GutterBorder(int top, int left, int bottom, int right) {
      super(top, left, bottom, right);
    }

    public Color getColor() {
      return this.color;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      this.visibleRect = g.getClipBounds(this.visibleRect);
      if (this.visibleRect == null) {
        this.visibleRect = ((JComponent) c).getVisibleRect();
      }

      g.setColor(this.color);
      if (this.left == 1) {
        g.drawLine(0, this.visibleRect.y, 0, this.visibleRect.y + this.visibleRect.height);
      } else {
        g.drawLine(
            width - 1, this.visibleRect.y, width - 1, this.visibleRect.y + this.visibleRect.height);
      }
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public void setEdges(int top, int left, int bottom, int right) {
      this.top = top;
      this.left = left;
      this.bottom = bottom;
      this.right = right;
    }
  }

  private class TextAreaListener extends ComponentAdapter
      implements DocumentListener, PropertyChangeListener, ActiveLineRangeListener {
    private boolean installed;

    private TextAreaListener() {}

    public void activeLineRangeChanged(ActiveLineRangeEvent e) {
      if (e.getMin() == -1) {
        MultiHeaderGutter.this.clearActiveLineRange();
      } else {
        MultiHeaderGutter.this.setActiveLineRange(e.getMin(), e.getMax());
      }
    }

    public void changedUpdate(DocumentEvent e) {}

    public void componentResized(ComponentEvent e) {
      MultiHeaderGutter.this.revalidate();
    }

    protected void handleDocumentEvent(DocumentEvent e) {
      for (int i = 0; i < MultiHeaderGutter.this.getComponentCount(); ++i) {
        if (MultiHeaderGutter.this.getComponent(i) instanceof AbstractGutterComponent) {
          AbstractGutterComponent agc =
              (AbstractGutterComponent) MultiHeaderGutter.this.getComponent(i);
          agc.handleDocumentEvent(e);
        }
      }
      for (int i = 0; i < MultiHeaderGutter.this.headerArea.getComponentCount(); ++i) {
        if (MultiHeaderGutter.this.headerArea.getComponent(i) instanceof AbstractGutterComponent) {
          AbstractGutterComponent agc =
              (AbstractGutterComponent) MultiHeaderGutter.this.headerArea.getComponent(i);
          agc.handleDocumentEvent(e);
        }
      }
    }

    public void insertUpdate(DocumentEvent e) {
      this.handleDocumentEvent(e);
    }

    public void install(RTextArea textArea) {
      if (this.installed) {
        this.uninstall();
      }

      textArea.addComponentListener(this);
      textArea.getDocument().addDocumentListener(this);
      textArea.addPropertyChangeListener(this);
      if (textArea instanceof RSyntaxTextArea) {
        RSyntaxTextArea rsta = (RSyntaxTextArea) textArea;
        rsta.addActiveLineRangeListener(this);
        rsta.getFoldManager().addPropertyChangeListener(this);
      }

      this.installed = true;
    }

    public void propertyChange(PropertyChangeEvent e) {
      String name = e.getPropertyName();
      if (!"font".equals(name) && !"RSTA.syntaxScheme".equals(name)) {
        if ("RSTA.codeFolding".equals(name)) {
          boolean var5 = ((Boolean) e.getNewValue()).booleanValue();
          if (MultiHeaderGutter.this.lineNumberList != null) {
            MultiHeaderGutter.this.lineNumberList.updateCellWidths();
          }

          MultiHeaderGutter.this.setFoldIndicatorEnabled(var5);
        } else if ("FoldsUpdated".equals(name)) {
          MultiHeaderGutter.this.repaint();
        } else if ("document".equals(name)) {
          RDocument var6 = (RDocument) e.getOldValue();
          if (var6 != null) {
            var6.removeDocumentListener(this);
          }

          RDocument var7 = (RDocument) e.getNewValue();
          if (var7 != null) {
            var7.addDocumentListener(this);
          }
        }
      } else {
        for (int old = 0; old < MultiHeaderGutter.this.getComponentCount(); ++old) {
          AbstractGutterComponent newDoc =
              (AbstractGutterComponent) MultiHeaderGutter.this.getComponent(old);
          newDoc.lineHeightsChanged();
        }
      }
    }

    public void removeUpdate(DocumentEvent e) {
      this.handleDocumentEvent(e);
    }

    public void uninstall() {
      if (this.installed) {
        MultiHeaderGutter.this.textArea.removeComponentListener(this);
        MultiHeaderGutter.this.textArea.getDocument().removeDocumentListener(this);
        MultiHeaderGutter.this.textArea.removePropertyChangeListener(this);
        if (MultiHeaderGutter.this.textArea instanceof RSyntaxTextArea) {
          RSyntaxTextArea rsta = (RSyntaxTextArea) MultiHeaderGutter.this.textArea;
          rsta.removeActiveLineRangeListener(this);
          rsta.getFoldManager().removePropertyChangeListener(this);
        }

        this.installed = false;
      }
    }
  }
}
