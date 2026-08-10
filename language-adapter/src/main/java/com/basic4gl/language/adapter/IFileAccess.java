package com.basic4gl.language.adapter;

import com.basic4gl.language.core.runtime.IFileOpener;

/**
 * Created by Nate on 11/18/2015.
 */
public interface IFileAccess {
    void init(IFileOpener files);
}
