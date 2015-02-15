package com.tablecloth.bookshelf.db;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ExifInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.tablecloth.bookshelf.util.ImageUtil;
import com.tablecloth.bookshelf.util.ToastUtil;
import com.tablecloth.bookshelf.util.Util;

/**
 * Created by shnomura on 2014/08/16.
 */
public class SeriesData {
    public int mSeriesId = -1;
    public String mTitle;
    public String mTitlePronunciation;
    public String mAuthor;
    public String mAuthorPronunciation;
    public String mCompany;
    public String mCompanyPronunciation;
    public String mMagazine;
    public String mMagazinePronunciation;
    private Bitmap mImageCache;
    public String mImagePath;
    public String mMemo;

    public ArrayList<String> mTagsList;
    public boolean mIsSeriesEnd = false;
    public ArrayList<Integer> mVolumeList;

    public long mInitUpdateUnix;
    public long mLastUpdateUnix;

    public SeriesData(String seriesName) {
        mTitle = seriesName;
        mVolumeList = new ArrayList<Integer>();
        mTagsList = new ArrayList<String>();
    }
    
    public Bitmap getImage(Activity activity) {
    	if(mImageCache != null) return mImageCache;
    	if(Util.isEmpty(mImagePath)) return null;
    	try {
    		mImageCache = ImageUtil.getBitmapFromPath(activity, mImagePath);
    		ExifInterface exifInterface = new ExifInterface(mImagePath);
    		int exifR = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
    		int orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
    		switch (exifR) {
    		case ExifInterface.ORIENTATION_ROTATE_90:
                mImageCache = ImageUtil.rotateBitmap(mImageCache, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                mImageCache = ImageUtil.rotateBitmap(mImageCache, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                mImageCache = ImageUtil.rotateBitmap(mImageCache, 270);
                break;
            default:
                break;
    		}
    		return mImageCache;
    	} catch(OutOfMemoryError e) {
    		ToastUtil.show(activity, "メモリー不足のため、画像の取得に失敗しました");
    		e.printStackTrace();
    	} catch (IOException e) {
    		ToastUtil.show(activity, "予期せぬエラーのため、画像の取得に失敗しました");
    		e.printStackTrace();
		}
    	return null;
    }
    public void setImage(String filePath, Bitmap bitmap) {
    	if(bitmap == null) {
    		if(mImageCache != null) {
    			mImageCache.recycle();
    			mImageCache = null;
    		}
    	} else {
    		mImageCache = bitmap;
    	}
    	mImagePath = filePath;
    }
    public void setImage(String filePath) {
    	setImage(filePath, null);
    }

    public SeriesData() {
        mVolumeList = new ArrayList<Integer>();
        mTagsList = new ArrayList<String>();
    }


    /**
     * 小さい巻数->最大巻数の順番入っている前提
     * @param volume
     */
    public void addVolume(int volume) {
        // 新しく所持巻情報を追加
        mVolumeList.add(volume);
        Collections.sort(mVolumeList);
    }

    public void removeVolume(int volume) {
        // 新しく所持巻情報を追加
    	int index = mVolumeList.indexOf(volume);
    	if(index >= 0) mVolumeList.remove(index);
    	Collections.sort(mVolumeList);
    }

    public String getVolumeString() {
        String ret = "";
        int prevVolume = -1; // 最後に所持情報がある巻数
        int firstContinueVolume = -1; // 連番が始まった巻数
        int firstVolume = 0; // はじめに記録された巻数
        //ソート処理
        Collections.sort(mVolumeList);

        // 所持している巻数を小さいものから順番に見ていく
        for(int i = 0 ; i <= mVolumeList.size() ; i ++ ) {
            int value = -1;
        	if(i < mVolumeList.size()) {
        		value = mVolumeList.get(i);
        	}

            // はじめの巻専用
            if(i == 0) {
            	firstVolume = value;
                firstContinueVolume = value;
                prevVolume = value;
            }
            // 連番中
            else if(prevVolume + 1 == value) {
            	prevVolume = value;
            // 連番終了・または単発の巻数がある
            } else {
            	if(firstVolume != firstContinueVolume) {
            		ret += "、";
            	}
            	// 連番
            	if(firstContinueVolume < prevVolume) {
            		ret += firstContinueVolume + "巻 〜 " + prevVolume + "巻";
            	}
            	// 単発
            	else {
            		ret += prevVolume + "巻";
            	}
            	
            	firstContinueVolume = value;
            	prevVolume = value;
            }
//            // 最終巻専用
//            else if(i == mVolumeList.size() - 1) {
//                if(value != firstVolume) {
//                    ret += value;
//                }
//                ret += "巻";
//            }
//            // １つ前の巻が「数値的に１つ前の巻」出なかった場合、表記を変更
//            // （１冊目以外）
//            else if(value - 1 != prevVolume && prevVolume != 0) {
//                // 「連番が始まった巻数」と「連番が終わった巻数」が同じの場合
//                // 例）１巻、３巻、５巻〜１０巻を所持してい場合の１巻と３巻
//                if(firstContinueVolume == prevVolume) {
//                    ret += "、" + value;
//                } else {
//                    ret += "〜" + prevVolume + "、" + value;
//                }
//            }
//            ret += String.valueOf(value);
        }
    	
        if(mVolumeList.size() > 0) {
//            ret += "巻";
        } else {
            ret += "所持巻なし";
        }
        return ret;
    }

}