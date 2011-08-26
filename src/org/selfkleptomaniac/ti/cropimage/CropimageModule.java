/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package org.selfkleptomaniac.ti.cropimage;

import java.io.File;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;

import android.net.Uri;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.provider.MediaStore;
import android.database.Cursor;

import org.appcelerator.titanium.TiBlob;

@Kroll.module(name="Cropimage", id="org.selfkleptomaniac.ti.cropimage")
public class CropimageModule extends KrollModule
{

	// Standard Debugging variables
	private static final String LCAT = "CropimageModule";
	private static final boolean DBG = TiConfig.LOGD;

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	
	public CropimageModule(TiContext tiContext) {
		super(tiContext);
	}

	// Methods
	@Kroll.method
	public String example() {
		Log.d(LCAT, "example called");
		return "hello world";
	}
	
	// Properties
	@Kroll.getProperty
	public String getExampleProp() {
		Log.d(LCAT, "get example property");
		return "hello world";
	}
	
	
	@Kroll.setProperty
	public void setExampleProp(String value) {
		Log.d(LCAT, "set example property: " + value);
	}

  @Kroll.method
  public void cropper(TiBlob data, int width, int height) {
    String file_path;
    String content_name;
    String native_path = data.getNativePath();
    if(native_path.startsWith("content://")){
    	content_name = native_path;
    }else{
    	file_path = data.getNativePath().replace("file://", "");
    	Cursor c = getTiContext().getActivity().getContentResolver().query( 
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                null, 
                MediaStore.Images.ImageColumns.DATA + " = ?", 
                new String[]{file_path}, 
                null); 
    	c.moveToFirst(); 
    	content_name = "content://media/external/images/media/" + c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID)); 
    }
    
    Intent intent = new Intent("com.android.camera.action.CROP");

    intent.setType("image/*");
    intent.setData(Uri.parse(content_name));

    intent.putExtra("crop", "true");
    intent.putExtra("outputX", width);
    intent.putExtra("outputY", height);
    intent.putExtra("aspectX", 1);
    intent.putExtra("aspectY", 1);
    intent.putExtra("scale", true);
    intent.putExtra("noFaceDetection", true);
    intent.putExtra("return-data", true);
    intent.putExtra("output", Uri.parse(data.getNativePath()));
    try{
      getTiContext().getActivity().startActivity(intent);
    }catch(ActivityNotFoundException e){
      Log.e(LCAT,"Activity not found: " + e);
    }
  }
}