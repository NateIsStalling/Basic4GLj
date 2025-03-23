//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.fife.ui.rtextarea;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.Arrays;
import java.util.Stack;
import javax.swing.JScrollPane;

public class RMultiHeaderScrollPane extends JScrollPane {
  private MultiHeaderGutter gutter;

  public RMultiHeaderScrollPane() {
    this((Component) null, true);
  }

  public RMultiHeaderScrollPane(Component comp) {
    this(comp, true);
  }

  public RMultiHeaderScrollPane(Component comp, boolean lineNumbers) {
    this(comp, lineNumbers, Color.GRAY);
  }

  public RMultiHeaderScrollPane(Component comp, boolean lineNumbers, Color lineNumberColor) {
    super(comp);
    RTextArea textArea = getFirstRTextAreaDescendant(comp);
    Font defaultFont = new Font("Monospaced", 0, 12);
    this.gutter = new MultiHeaderGutter(textArea);
    this.gutter.setLineNumberFont(defaultFont);
    this.gutter.setLineNumberColor(lineNumberColor);
    this.setLineNumbersEnabled(lineNumbers);
    this.setVerticalScrollBarPolicy(22);
    this.setHorizontalScrollBarPolicy(30);
  }

  private void checkGutterVisibility() {
    int count = this.gutter.getComponentCount();
    if (count == 0) {
      if (this.getRowHeader() != null && this.getRowHeader().getView() == this.gutter) {
        this.setRowHeaderView((Component) null);
      }
    } else if (this.getRowHeader() == null || this.getRowHeader().getView() == null) {
      this.setRowHeaderView(this.gutter);
    }
  }

  public MultiHeaderGutter getGutter() {
    return this.gutter;
  }

  public boolean getLineNumbersEnabled() {
    return this.gutter.getLineNumbersEnabled();
  }

  public RTextArea getTextArea() {
    return (RTextArea) this.getViewport().getView();
  }

  public boolean isFoldIndicatorEnabled() {
    return this.gutter.isFoldIndicatorEnabled();
  }

  public boolean isIconRowHeaderEnabled(int headerIndex) {
    return this.gutter.isIconRowHeaderEnabled(headerIndex);
  }

  public void setFoldIndicatorEnabled(boolean enabled) {
    this.gutter.setFoldIndicatorEnabled(enabled);
    this.checkGutterVisibility();
  }

  public void setIconRowHeaderEnabled(int headerIndex, boolean enabled) {
    this.gutter.setIconRowHeaderEnabled(headerIndex, enabled);
    this.checkGutterVisibility();
  }

  public void setLineNumbersEnabled(boolean enabled) {
    this.gutter.setLineNumbersEnabled(enabled);
    this.checkGutterVisibility();
  }

  public void setViewportView(Component view) {
    RTextArea rtaCandidate = null;
    if (!(view instanceof RTextArea)) {
      rtaCandidate = getFirstRTextAreaDescendant(view);
      if (rtaCandidate == null) {
        throw new IllegalArgumentException(
            "view must be either an RTextArea or a JLayer wrapping one");
      }
    } else {
      rtaCandidate = (RTextArea) view;
    }

    super.setViewportView(view);
    if (this.gutter != null) {
      this.gutter.setTextArea(rtaCandidate);
    }
  }

  private static final RTextArea getFirstRTextAreaDescendant(Component comp) {
    Stack stack = new Stack();
    stack.add(comp);

    while (!stack.isEmpty()) {
      Component current = (Component) stack.pop();
      if (current instanceof RTextArea) {
        return (RTextArea) current;
      }

      if (current instanceof Container) {
        Container container = (Container) current;
        stack.addAll(Arrays.asList(container.getComponents()));
      }
    }

    return null;
  }
}
