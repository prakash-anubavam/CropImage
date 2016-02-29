/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package org.selfkleptomaniac.ti.mod.cropimage;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.titanium.io.TiFileFactory;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;

import android.net.Uri;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import java.lang.OutOfMemoryError;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import android.graphics.Bitmap;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.database.Cursor;

import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;

@Kroll.module(name="Cropimage", id="org.selfkleptomaniac.ti.mod.cropimage")
public class CropimageModule extends KrollModule
{

	// Standard Debugging variables
	private static final String LCAT = "CropimageModule";
	private static final boolean DBG = TiConfig.LOGD;

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	private Uri mImageCaptureUri;
	@Kroll.constant public static final String MEDIA_TYPE_PHOTO = "public.image";
	@Kroll.constant public static final String CAPTURE_TITLE = "croppedimage";
	
	public CropimageModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(LCAT, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}

	// Methods
	@Kroll.method
	public void cropper(KrollDict options, int cropper_width, int cropper_height) {

		if(false == storageAvailable()){
			 throw new NullPointerException();
		}

		
		final KrollFunction successCallback = getCallback(options, "success");
		final KrollFunction cancelCallback = getCallback(options, "cancel");
		final KrollFunction errorCallback = getCallback(options, "error");
		

//		Activity activity = TiApplication.getAppRootOrCurrentActivity();
		Activity activity = TiApplication.getInstance().getCurrentActivity();
		TiActivitySupport activitySupport = (TiActivitySupport) activity;

		
		TiBlob data = getImageData(options, "image");

		
//		Integer cropper_width = getSize(options, "width");
//		Integer cropper_height = getSize(options, "height");
		
		boolean saveToPhotoGallery = false;
		if (options.containsKey("saveToPhotoGallery")) {
			saveToPhotoGallery = options.getBoolean("saveToPhotoGallery");
		}

		String file_path;
		String content_name;
		String native_path = data.getNativePath();
		if(native_path.startsWith("content://")){
			content_name = native_path;
			Cursor c = getActivity().getContentResolver().query(Uri.parse(native_path), null, null, null, null); 
			c.moveToFirst(); 
			file_path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA)); 
		}else{
			file_path = data.getNativePath().replace("file://", "");
			Cursor c = getActivity().getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					null,
					MediaStore.Images.ImageColumns.DATA + " = ?",
							new String[]{file_path},
							null);
			c.moveToFirst();
			content_name = "content://media/external/images/media/" + c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID));
		}

		mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				   "/org.selfkleptomaniac.ti.mod.cropimage/tmp_cropper_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

		
		Intent intent = new Intent("com.android.camera.action.CROP");
//		intent.setClassName("com.android.gallery", "com.android.camera.CropImage");

		intent.setType("image/*");
		intent.setData(Uri.parse(content_name));

		intent.putExtra("crop", "true");
		intent.putExtra("outputX", cropper_width);
		intent.putExtra("outputY", cropper_height);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", true);
		intent.putExtra("output", file_path);

		
		CropImageResultHander resultHandler = new CropImageResultHander();
		resultHandler.image_file = data;
		resultHandler.saveToPhotoGallery = saveToPhotoGallery;
		resultHandler.successCallback = successCallback;
		resultHandler.cancelCallback = cancelCallback;
		resultHandler.errorCallback = errorCallback;
		resultHandler.activitySupport = activitySupport;
		resultHandler.cropperIntent = intent;
		resultHandler.imageUri = mImageCaptureUri;
		

		try{
			activity.runOnUiThread(resultHandler);
		}catch(ActivityNotFoundException e){
			Log.e(LCAT,"Activity not found: " + e);
			OutOfMemoryError error = new OutOfMemoryError("activity not found.");
			throw(error);
		}
	}

	private boolean storageAvailable(){
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return true;
		}else{
			return false;
		}
	}

	private KrollFunction getCallback(final KrollDict options, final String name){
		if(options.containsKey(name)){
			return (KrollFunction) options.get(name);
		}else{
			Log.e(LCAT, "Callback not found:" + name);
			return null;
		}
	}

//	private Integer getSize(final KrollDict options, final String name){
//		Log.e(LCAT, "Received: " + name);
//		if(options.containsKey(name)){
//			Log.e(LCAT, "Found: " + name);
//			Log.e(LCAT, "Found: " + options.getString(name));
//			return options.getInt(name);
//		}else{
//			Log.e(LCAT, "Size not found: " + name);
//			return 300;
//		}
//	}

	private TiBlob getImageData(final KrollDict options, final String name){
		if(options.containsKey(name)){
			return (TiBlob) options.get(name);
		}else{
			Log.e(LCAT, "Image not found: " + name);
			return null;
		}
	}

	protected class CropImageResultHander implements TiActivityResultHandler,Runnable{
		protected TiBlob image_file;
		protected Intent cropperIntent;
		protected TiActivitySupport activitySupport;
		protected int code;
		protected KrollFunction successCallback, cancelCallback, errorCallback;
		protected boolean saveToPhotoGallery;
		protected Uri imageUri;
		protected Intent cameraIntent;

		public void run(){
			Log.e(LCAT, "run called");
			code = activitySupport.getUniqueResultCode();
			activitySupport.launchActivityForResult(cropperIntent, code, this);
		}

		public void onError(Activity activity, int requestCode, Exception e){
			Log.e(LCAT, "onError called");
			String errorMessage = "Something wrong; " + e.getMessage();
			Log.e(LCAT, errorMessage);
			KrollDict d = new KrollDict();
			d.put("message", errorMessage);
			errorCallback.callAsync(getKrollObject(), d);
		}
		public void onResult(Activity activity, int requestCode, int resultCode, Intent data){
			Log.e(LCAT, "onResult called");
			if (resultCode == Activity.RESULT_CANCELED) {
				KrollDict d = new KrollDict();
				d.put("message", "canceled");
				cancelCallback.callAsync(getKrollObject(), d);
			}else{
				Bitmap bitmap = data.getExtras().getParcelable("data");
				File f = new File(imageUri.getPath());
				try{
					KrollDict result = createDictForImage(bitmap, f, "image/jpeg");
					successCallback.callAsync(getKrollObject(), result);
				}catch(NullPointerException e){
					Log.e(LCAT, "faild to call successCallback");
				}
			}
		}
	}

	//KrollDict createDictForImage(Uri path, String mimeType) {
	KrollDict createDictForImage(Bitmap bitmap, File path, String mimeType) {

	    KrollDict d = new KrollDict();

	    int width = -1;
	    int height = -1;

	    OutputStream outputStream = null;
	    try {
			File dir = new File(path.getParent());
			if(!dir.exists()){
				dir.mkdirs();
			}
			if(path.exists()){
				path.delete();
			}
			if(path.createNewFile()){
				outputStream = new FileOutputStream(path);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
				width = bitmap.getWidth();
				height = bitmap.getHeight();
			}
		} catch (IOException e) {
			Log.e(LCAT, "failed to save bitmap:" + e);
		}finally{
			if(outputStream != null){
				try {
					outputStream.close();
				} catch (IOException e) {
					Log.e(LCAT, "failed to save bitmap:" + e);
				}
			}
		}

	    d.put("x", 0);
	    d.put("y", 0);
	    d.put("width", width);
	    d.put("height", height);

	    KrollDict cropRect = new KrollDict();
	    cropRect.put("x", 0);
	    cropRect.put("y", 0);
	    cropRect.put("width", width);
	    cropRect.put("height", height);
	    d.put("cropRect", cropRect);

	    String[] parts = { path.toString() };
	    d.put("mediaType", MEDIA_TYPE_PHOTO);
	    d.put("media", TiBlob.blobFromFile(TiFileFactory.createTitaniumFile(parts, false), mimeType));
	    
	    return d;
	  }

}

